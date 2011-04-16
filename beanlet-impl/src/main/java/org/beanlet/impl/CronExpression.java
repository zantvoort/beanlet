/*
 * ============================================================================
 * GNU Lesser General Public License
 * ============================================================================
 *
 * Beanlet - JSE Application Container.
 * Copyright (C) 2006  Leon van Zantvoort
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
 *
 * Leon van Zantvoort
 * 243 Acalanes Drive #11
 * Sunnyvale, CA 94086
 * USA
 *
 * zantvoort@users.sourceforge.net
 * http://beanlet.org
 */
package org.beanlet.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.TimeZone;
import static org.beanlet.impl.CronExpression.CronCharacter.*;
import static org.beanlet.impl.CronExpression.CronField.*;

/**
 * @author Leon van Zantvoort
 */
public final class CronExpression {
    
    private final String expression;
    private final String[] fields;
    private final List<CronField> fieldList;
    private final TimeZone tz;
    
    public CronExpression(String expression) throws IllegalArgumentException {
        this.expression = expression;
        this.fields = expression.split(" ");
        if (fields.length < 6 || fields.length > 8) {
            throw new IllegalArgumentException("Cron expression MUST specify " +
                    "at least 6, but no more than 8 options.");
        }
        this.fieldList = new ArrayList<CronField>(Arrays.asList(CronField.values()));
        if (fields.length < 7) {
            fieldList.remove(0);
        }
        if (fields.length == 8) {
            if (fields[7].equalsIgnoreCase(ALL.character)) {
                this.tz = TimeZone.getDefault();
            } else {
                this.tz = TimeZone.getTimeZone(fields[7]);
            }
        } else {
            this.tz = TimeZone.getDefault();
        }
        // Call to check expression.
        nextFireTime();
    }
    
    /**
     * Returns next fire time computed from now.
     */
    public Date nextFireTime() {
        return nextFireTime(new Date());
    }
    
    /**
     * Returns next fire time computed from given date.
     */
    public Date nextFireTime(Date fromDate) {
        Calendar calendar = Calendar.getInstance(tz);
        calendar.setTime(fromDate);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.SECOND, 1);
        
        CronField prev = null;
        for (ListIterator<CronField> i = fieldList.listIterator();
                i.hasNext();) {
            CronField field = i.next();
            Integer prevValue = null;
            if (prev != null) {
                prevValue = calendar.get(prev.calendar);
            }
            int added = setField(calendar, field);
            Integer unitSize = field.getUnitSize(calendar);
            if (unitSize == null && added < 0) {
                return null;
            }
            if (added > 0) {
                clear(calendar, field);
            }
            if (prevValue != null) {
                if (!prevValue.equals(calendar.get(prev.calendar))) {
                    i = fieldList.listIterator();
                    prev = null;
                    continue;
                }
            }
            prev = field;
        }
        return calendar.getTime();
    }
    
    private int setField(Calendar calendar, CronField field) {
        String fieldValue = fields[field.index];
        CronCharacter character = CronCharacter.parse(fieldValue);
        if (character == null) {
            return setNoneField(calendar, field);
        } else {
            if (!field.allowedCharacters.contains(character)) {
                throw new IllegalArgumentException("Option '" + character.character + "' " +
                        "not supported for field " + field + ".");
            }
            switch (character) {
            case ALL:
                return setAllField(calendar, field);
            case ANY:
                return setAnyField(calendar, field);
            case RANGE:
                return setRangeField(calendar, field);
            case ADDITIONAL:
                return setAdditionalField(calendar, field);
            case INCREMENT:
                return setIncrementField(calendar, field);
            case LAST:
                return setLastField(calendar, field);
            case WEEKDAY:
                return setWeekdayField(calendar, field);
            case NTH_DAY_OF_MONTH:
                return setNthDayOfMonthField(calendar, field);
            }
        }
        return 0;
    }
    
    private int setNoneField(Calendar calendar, CronField field) {
        int current = calendar.get(field.calendar);
        int value = field.getValue(fields[field.index]);
        final int add;
        Integer unitSize = field.getUnitSize(calendar);
        if (unitSize == null) {
            add = value - current;
        } else {
            add = (unitSize - current + value) % unitSize;
        }
        calendar.add(field.calendar, add);
        return add;
    }
    
    private int setAllField(Calendar calendar, CronField field) {
        // Do nothing.
        return 0;
    }
    
    private int setAnyField(Calendar calendar, CronField field) {
        // Do nothing.
        return 0;
    }
    
    private int setRangeField(Calendar calendar, CronField field) {
        String[] range = fields[field.index].split(RANGE.character);
        if (range.length != 2) {
            throw new IllegalArgumentException("Invalid " + field + " value: '" + 
                    fields[field.index] + "'.");
        }
        int lowerBound = field.getValue(range[0]);
        int upperBound = field.getValue(range[1]);
        int current = calendar.get(field.calendar);
        final int value;
        if (current < lowerBound || current > upperBound) {
            value = lowerBound;
        } else {
            value = current;
        }
        final int add;
        Integer unitSize = field.getUnitSize(calendar);
        if (unitSize == null) {
            add = value - current;
        } else {
            add = (unitSize - current + value) % unitSize;
        }
        calendar.add(field.calendar, add);
        return add;
    }
    
    private int setAdditionalField(Calendar calendar, CronField field) {
        String[] tmp = fields[field.index].split(ADDITIONAL.character);
        if (tmp.length == 0) {
            throw new IllegalArgumentException("Invalid " + field + " value: '" + 
                    fields[field.index] + "'.");
        }
        int[] values = new int[tmp.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = field.getValue(tmp[i]);
        }
        Arrays.sort(values);
        int current = calendar.get(field.calendar);
        int value = values[0];
        for (int i = 0; i < values.length; i++) {
            if (values[i] >= current) {
                value = values[i];
                break;
            }
        }
        final int add;
        Integer unitSize = field.getUnitSize(calendar);
        if (unitSize == null) {
            add = value - current;
        } else {
            add = (unitSize - current + value) % unitSize;
        }
        calendar.add(field.calendar, add);
        return add;
    }
    
    private int setIncrementField(Calendar calendar, CronField field) {
        String[] tmp = fields[field.index].split(INCREMENT.character);
        if (tmp.length != 2) {
            throw new IllegalArgumentException("Invalid " + field + " value: '" + 
                    fields[field.index] + "'.");
        }
        int initial = tmp[0].equals("*") ? 0 : field.getValue(tmp[0]);
        int increment = field.getValue(tmp[1]);
        int current = calendar.get(field.calendar);
        final int add;
        Integer unitSize = field.getUnitSize(calendar);
        if (unitSize == null) {
            int start = current - ((current % increment) - initial);
            int value = start;
            while (value < current) {
                value += increment;
            }
            add = value - current;
        } else {
            int value = initial;
            for (int i = initial; i <= unitSize; i+=increment) {
                if (i >= current) {
                    value = i;
                    break;
                }
            }
            add = (unitSize - current + value) % unitSize;
        }
        calendar.add(field.calendar, add);
        return add;
    }
    
    private int setLastField(Calendar calendar, CronField field) {
        String last = fields[field.index].toUpperCase();
        int current = calendar.get(field.calendar);
        int add = 0;
        if (last.equalsIgnoreCase("LW")) {
            if (field != DAY_OF_MONTH) {
                throw new IllegalArgumentException("'LW' option only supported " +
                        "for Day-of-Month.");
            }
            int now = calendar.get(DAY_OF_MONTH.calendar);
            int max = calendar.getActualMaximum(DAY_OF_MONTH.calendar);
            for (int i = max ; i > 0; i++) {
                calendar.set(DAY_OF_MONTH.calendar, i);
                int day = calendar.get(DAY_OF_WEEK.calendar);
                if (day >= Calendar.MONDAY || day <= Calendar.FRIDAY) {
                    break;
                }
            }
            add = calendar.get(DAY_OF_MONTH.calendar) - now;
            assert add >= 0;
        } else if (!last.startsWith("L")) {
            if (field != DAY_OF_WEEK) {
                throw new IllegalArgumentException(
                        "'L' option only supported for Day-of-Week.");
            }
            int day = field.getValue(last.substring(0, last.length() - 1));
            int now = calendar.get(DAY_OF_MONTH.calendar);
            int value = ((7 + day - current) % 7);
            add += value;
            calendar.add(DAY_OF_MONTH.calendar, value);
            while (calendar.getActualMaximum(DAY_OF_MONTH.calendar) - 7 >=
                    calendar.get(DAY_OF_MONTH.calendar)) {
                add += 7;
                calendar.add(DAY_OF_MONTH.calendar, 7);
            }
            assert add >= 0;
        } else {
            int value = calendar.getActualMaximum(field.calendar);
            calendar.set(field.calendar, value);
            add = value - current;
            assert add >= 0;
        }
        return add;
    }
    
    private int setWeekdayField(Calendar calendar, CronField field) {
        if (field != DAY_OF_MONTH) {
            throw new IllegalArgumentException(
                    "'W' option only supported for Day-of-Month.");
        }
        String tmp = fields[field.index];
        int current = calendar.get(field.calendar);
        int max = calendar.getActualMaximum(DAY_OF_MONTH.calendar);
        int weekday = Math.min(max,
                field.getValue(tmp.substring(0, tmp.length() - 1)));
        calendar.set(DAY_OF_MONTH.calendar, weekday);
        int day = calendar.get(DAY_OF_WEEK.calendar);
        
        final int value;
        if (day >= Calendar.MONDAY || day <= Calendar.FRIDAY) {
            value = weekday;
        } else {
            if (day == Calendar.SATURDAY) {
                if (weekday == 1) {
                    value = 3;
                } else {
                    value = weekday - 1;
                }
            } else if (day == Calendar.SUNDAY) {
                if (weekday == max) {
                    value = weekday - 2;
                } else {
                    value = weekday + 1;
                }
            } else {
                assert false;
                value = weekday;
            }
            calendar.set(DAY_OF_MONTH.calendar, value);
        }
        int add = value - current;
        assert add >= 0;
        return add;
    }
    
    private int setNthDayOfMonthField(Calendar calendar, CronField field) {
        String[] tmp = fields[field.index].split(NTH_DAY_OF_MONTH.character);
        if (tmp.length != 2) {
            throw new IllegalArgumentException("Invalid " + field + " value: '" + 
                    fields[field.index] + "'.");
        }
        int day = field.getValue(tmp[0]);
        int nth = 0;
        try {
            nth = Integer.parseInt(tmp[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Value after the '#' option must be an integer: '" + 
                    tmp[1] + "''.");
        }
        if (nth < 1 || nth > 5) {
            throw new IllegalArgumentException(
                    "Value after the '#' option must be between 1 and 5.");
        }
        int current = calendar.get(DAY_OF_MONTH.calendar);
        int add = -current;
        while (true) {
            calendar.set(DAY_OF_MONTH.calendar, 1);
            int max = calendar.getActualMaximum(DAY_OF_MONTH.calendar);
            int firstDay = calendar.get(DAY_OF_WEEK.calendar);
            int v = 1 + (7 + day - firstDay) % 7;
            int i = 1;
            while (i < nth && (v + 7) <= max) {
                i++;
                v += 7;
            }
            if (i == nth) {
                add += v;
                calendar.set(DAY_OF_MONTH.calendar, v);
                if (add >= 0) {
                    break;
                }
            }
            calendar.add(MONTH.calendar, 1);
            add += max;
        }
        return add;
    }
    
    private void clear(Calendar calendar, CronField field) {
        if (field == YEAR) {
            calendar.set(MONTH.calendar, calendar.getActualMinimum(MONTH.calendar));
            field = MONTH;
        }
        if (field == MONTH) {
            calendar.set(DAY_OF_MONTH.calendar, calendar.getActualMinimum(DAY_OF_MONTH.calendar));
            field = DAY_OF_MONTH;
        }
        if (field == DAY_OF_MONTH) {
            field = DAY_OF_WEEK;
        }
        if (field == DAY_OF_WEEK) {
            calendar.set(HOUR.calendar, calendar.getActualMinimum(HOUR.calendar));
            field = HOUR;
        }
        if (field == HOUR) {
            calendar.set(MINUTE.calendar, calendar.getActualMinimum(MINUTE.calendar));
            field = MINUTE;
        }
        if (field == MINUTE) {
            calendar.set(SECOND.calendar, calendar.getActualMinimum(SECOND.calendar));
        }
    }
    
    enum CronField {
        YEAR(6, Calendar.YEAR, EnumSet.<CronCharacter>of(
                ALL, RANGE, ADDITIONAL, INCREMENT)),
        MONTH(4, Calendar.MONTH, EnumSet.<CronCharacter>of(
                ALL, RANGE, ADDITIONAL, INCREMENT)),
        DAY_OF_WEEK(5, Calendar.DAY_OF_WEEK, EnumSet.<CronCharacter>of(
                ALL, RANGE, ADDITIONAL, INCREMENT, ANY, LAST, NTH_DAY_OF_MONTH)),
        DAY_OF_MONTH(3, Calendar.DAY_OF_MONTH, EnumSet.<CronCharacter>of(
                ALL, RANGE, ADDITIONAL, INCREMENT, ANY, LAST, WEEKDAY)),
        HOUR(2, Calendar.HOUR_OF_DAY, EnumSet.<CronCharacter>of(
                ALL, RANGE, ADDITIONAL, INCREMENT)),
        MINUTE(1, Calendar.MINUTE, EnumSet.<CronCharacter>of(
                ALL, RANGE, ADDITIONAL, INCREMENT)),
        SECOND(0, Calendar.SECOND, EnumSet.<CronCharacter>of(
                ALL, RANGE, ADDITIONAL, INCREMENT));
        
        private final int index;
        private final int calendar;
        private final Set<CronCharacter> allowedCharacters;
        
        CronField(int index, int calendar,
                Set<CronCharacter> allowedCharacters) {
            this.index = index;
            this.calendar = calendar;
            this.allowedCharacters = allowedCharacters;
        }
        
        public int getValue(String str) {
            final int value;
            try {
                switch (this) {
                    case YEAR:
                        value = Integer.parseInt(str);
                        if (value < 1970) {
                            throw new IllegalArgumentException(this +
                                    " value < 1970.");
                        }
                        if (value > 2099) {
                            throw new IllegalArgumentException(this + 
                                    " value > 2099.");
                        }
                        break;
                    case MONTH:
                        if (Character.isDigit(str.charAt(0))) {
                            int tmp = Integer.parseInt(str);
                            if (tmp < 1) {
                                throw new IllegalArgumentException(this + 
                                        " value < 1.");
                            }
                            if (tmp > 12) {
                                throw new IllegalArgumentException(this + 
                                        " value > 12.");
                            }
                            value = tmp - 1;
                        } else {
                            value = CronMonth.valueOf(str.toUpperCase()).month;
                        }
                        break;
                    case DAY_OF_WEEK:
                        if (Character.isDigit(str.charAt(0))) {
                            value = Integer.parseInt(str);
                        } else {
                            value = CronDay.valueOf(str.toUpperCase()).day;
                        }
                        if (value < 1) {
                            throw new IllegalArgumentException(this + 
                                    " value < 1.");
                        }
                        if (value > 7) {
                            throw new IllegalArgumentException(this +
                                    " value > 7.");
                        }
                        break;
                    case DAY_OF_MONTH:
                        value = Integer.parseInt(str);
                        if (value < 1) {
                            throw new IllegalArgumentException(this + 
                                    " value < 1.");
                        }
                        if (value > 31) {
                            throw new IllegalArgumentException(this + 
                                    " value > 31.");
                        }
                        break;
                    case HOUR:
                        value = Integer.parseInt(str);
                        if (value < 0) {
                            throw new IllegalArgumentException(this +
                                    " value < 0.");
                        }
                        if (value > 23) {
                            throw new IllegalArgumentException(this +
                                    " > 23.");
                        }
                        break;
                    case MINUTE:
                        value = Integer.parseInt(str);
                        if (value < 0) {
                            throw new IllegalArgumentException(this +
                                    " value < 0.");
                        }
                        if (value > 59) {
                            throw new IllegalArgumentException(this +
                                    " value > 59.");
                        }
                        break;
                    case SECOND:
                        value = Integer.parseInt(str);
                        if (value < 0) {
                            throw new IllegalArgumentException(this +
                                    " value < 0.");
                        }
                        if (value > 59) {
                            throw new IllegalArgumentException(this +
                                    " value > 59.");
                        }
                        break;
                    default:
                        value = Integer.parseInt(str);
                }
                return value;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(this + 
                        " value not an integer: '" + str + "'.");
            }
        }
        
        public Integer getUnitSize(Calendar c) {
            switch (this) {
                case YEAR:
                    return null;
                case MONTH:
                    return 12;
                case DAY_OF_WEEK:
                    return 7;
                case DAY_OF_MONTH:
                    return c.getActualMaximum(calendar);
                case HOUR:
                    return 24;
                case MINUTE:
                    return 60;
                case SECOND:
                    return 60;
                default:
                    return null;
            }
        }
    }
    
    enum CronCharacter {
        RANGE("-"),
        ADDITIONAL(","),
        INCREMENT("/"),
        NTH_DAY_OF_MONTH("#"),
        LAST("L"),
        WEEKDAY("W"),
        ALL("*"),
        ANY("?");
        
        private String character;
        CronCharacter(String character) {
            this.character = character;
        }
        
        public static CronCharacter parse(String value) throws
                IllegalArgumentException {
            CronCharacter c = null;
            Set<CronCharacter> all = EnumSet.allOf(CronCharacter.class);
            for (CronCharacter cc : all) {
                if (value.toUpperCase().indexOf(cc.character) != -1) {
                    if (value.startsWith("WED")) {
                        continue;
                    }
                    if (c != null) {
                        // Special cases:
                        if (value.equalsIgnoreCase("LW")) {
                            break;
                        } else if (value.startsWith("*/")) {
                            break;
                        } else if (value.indexOf("#") != -1) {
                            break;
                        }
                        throw new IllegalArgumentException(value);
                    }
                    c = cc;
                }
            }
            return c;
        }
    }
    
    enum CronMonth {
        JAN(0),
        FEB(1),
        MAR(2),
        APR(3),
        MAY(4),
        JUN(5),
        JUL(6),
        AUG(7),
        SEP(8),
        OCT(9),
        NOV(10),
        DEC(11),
        JANUARY(0),
        FEBRUARY(1),
        MARCH(2),
        APRIL(3),
        JUNE(5),
        JULY(6),
        AUGUST(7),
        SEPTEMBER(8),
        OCTOBER(9),
        NOVEMBER(10),
        DECEMBER(11);
        
        private final int month;
        CronMonth(int month) {
            this.month = month;
        }
    }
    
    enum CronDay{
        SUN(1),
        MON(2),
        TUE(3),
        WED(4),
        THU(5),
        FRI(6),
        SAT(7),
        SUNDAY(1),
        MONDAY(2),
        TUESDAY(3),
        WEDNESDAY(4),
        THURSDAY(5),
        FRIDAY(6),
        SATURDAY(7);
        
        private final int day;
        CronDay(int day) {
            this.day = day;
        }
    }
}
