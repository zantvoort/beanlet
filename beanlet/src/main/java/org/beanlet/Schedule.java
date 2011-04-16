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
package org.beanlet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Methods declared with this annotation are scheduled to be executed by a
 * background thread.</p>
 *
 * <p><h3>Method Constraints</h3>
 * The method on which the {@code Schedule} annotation is applied MUST fulfill all 
 * of the following criteria:
 * <ul>
 * <li>The method MUST NOT have any parameters.
 * <li>The method MAY return any type.
 * <li>The method MAY throw a checked exception.
 * <li>The method on which Schedule is applied MAY be {@code public}, 
 * {@code protected}, package private or {@code private}. 
 * <li>The method MUST NOT be {@code static}.
 * <li>The method MAY be {@code final}. 
 * </ul>
 * </p>
 * 
 * {@beanlet.annotation}
 *
 * @author Leon van Zantvoort
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Schedule {
    
    /**
     * If {@code true}, the method is scheduled only once.
     */
    boolean once() default false;

    /**
     * If {@code true}, the running threads are interrupted on destroy.
     */
    boolean interrupt() default false;

    /**
     * If {@code true}, the container waits until this component has finished
     * the runnable method on destroy.
     */
    boolean join() default false;

    /**
     * Set to a positive {@code long} value to let the container wait for an
     * initial delay before executing the runnable method for the first time.
     */
    long initialDelay() default 0L;
    
    /**
     * Set to a positive {@code long} value to let the container execute the
     * runnable method with a fixed delay interval.
     */
    long delay() default 0L;
    
    /**
     * Set to a positive {@code long} value to let the container execute the
     * runnable method with a fixed rate interval.
     */
    long rate() default 0L;

    /**
     * <p>Provides a parser and evaluator for unix-like cron expressions. Cron
     * expressions provide the ability to specify complex time combinations such as
     * &quot;At 8:00am every Monday through Friday&quot; or &quot;At 1:30am every
     * last Friday of the month&quot;.</p>
     * <p>Cron expressions are comprised of 6 required fields and two optional fields
     * separated by white space. The fields respectively are described as follows:
     * <table cellspacing="8">
     * <tr>
     * <th align="left">Field Name</th>
     * <th align="left">&nbsp;</th>
     * <th align="left">Allowed Values</th>
     * <th align="left">&nbsp;</th>
     * <th align="left">Allowed Special Characters</th>
     * </tr>
     * <tr>
     * <td align="left"><code>Seconds</code></td>
     * <td align="left">&nbsp;</th>
     * <td align="left"><code>0-59</code></td>
     * <td align="left">&nbsp;</th>
     * <td align="left"><code>, - * /</code></td>
     * </tr>
     * <tr>
     * <td align="left"><code>Minutes</code></td>
     * <td align="left">&nbsp;</th>
     * <td align="left"><code>0-59</code></td>
     * <td align="left">&nbsp;</th>
     * <td align="left"><code>, - * /</code></td>
     * </tr>
     * <tr>
     * <td align="left"><code>Hours</code></td>
     * <td align="left">&nbsp;</th>
     * <td align="left"><code>0-23</code></td>
     * <td align="left">&nbsp;</th>
     * <td align="left"><code>, - * /</code></td>
     * </tr>
     * <tr>
     * <td align="left"><code>Day-of-month</code></td>
     * <td align="left">&nbsp;</th>
     * <td align="left"><code>1-31</code></td>
     * <td align="left">&nbsp;</th>
     * <td align="left"><code>, - * ? / L W</code></td>
     * </tr>
     * <td align="left"><code>Month</code></td>
     * <td align="left">&nbsp;</th>
     * <td align="left"><code>1-12 or JAN-DEC</code></td>
     * <td align="left">&nbsp;</th>
     * <td align="left"><code>, - * /</code></td>
     * </tr>
     * <tr>
     * <td align="left"><code>Day-of-Week</code></td>
     * <td align="left">&nbsp;</th>
     * <td align="left"><code>1-7 or SUN-SAT</code></td>
     * <td align="left">&nbsp;</th>
     * <td align="left"><code>, - * ? / L #</code></td>
     * </tr>
     * <tr>
     * <td align="left"><code>Year (Optional)</code></td>
     * <td align="left">&nbsp;</th>
     * <td align="left"><code>empty, 1970-2099</code></td>
     * <td align="left">&nbsp;</th>
     * <td align="left"><code>, - * /</code></td>
     * </tr>
     * <tr>
     * <td align="left"><code>TimeZone (Optional)</code></td>
     * <td align="left">&nbsp;</th>
     * <td align="left"><code>empty, Europe/Amsterdam</code></td>
     * <td align="left">&nbsp;</th>
     * <td align="left"><code>*</code></td>
     * </tr>
     * </table>
     * </p>
     *
     * <p>The '*' character is used to specify all values. For example, &quot;*&quot;
     * in the minute field means &quot;every minute&quot;.</p>
     *
     * <p>The '?' character is allowed for the day-of-month and day-of-week fields. It
     * is used to specify 'no specific value'. This is useful when you need to
     * specify something in one of the two fileds, but not the other.</p>
     *
     * <p>The '-' character is used to specify ranges For example &quot;10-12&quot; in
     * the hour field means &quot;the hours 10, 11 and 12&quot;.</p>
     *
     * <p>The ',' character is used to specify additional values. For example
     * &quot;MON,WED,FRI&quot; in the day-of-week field means &quot;the days Monday,
     * Wednesday, and Friday&quot;.</p>
     *
     * <p>The '/' character is used to specify increments. For example &quot;0/15&quot;
     * in the seconds field means &quot;the seconds 0, 15, 30, and 45&quot;. And
     * &quot;5/15&quot; in the seconds field means &quot;the seconds 5, 20, 35, and
     * 50&quot;.  Specifying '*' before the  '/' is equivalent to specifying 0 is
     * the value to start with. Essentially, for each field in the expression, there
     * is a set of numbers that can be turned on or off. For seconds and minutes,
     * the numbers range from 0 to 59. For hours 0 to 23, for days of the month 0 to
     * 31, and for months 1 to 12. The &quot;/&quot; character simply helps you turn
     * on every &quot;nth&quot; value in the given set. Thus &quot;7/6&quot; in the
     * month field only turns on month &quot;7&quot;, it does NOT mean every 6th
     * month, please note that subtlety.<p>
     *
     * <p>The 'L' character is allowed for the day-of-month and day-of-week fields.
     * This character is short-hand for &quot;last&quot;, but it has different
     * meaning in each of the two fields. For example, the value &quot;L&quot; in
     * the day-of-month field means &quot;the last day of the month&quot; - day 31
     * for January, day 28 for February on non-leap years. If used in the
     * day-of-week field by itself, it simply means &quot;7&quot; or
     * &quot;SAT&quot;. But if used in the day-of-week field after another value, it
     * means &quot;the last xxx day of the month&quot; - for example &quot;6L&quot;
     * means &quot;the last friday of the month&quot;. When using the 'L' option, it
     * is important not to specify lists, or ranges of values, as you'll get
     * confusing results.</p>
     *
     * <p>The 'W' character is allowed for the day-of-month field.  This character
     * is used to specify the weekday (Monday-Friday) nearest the given day.  As an
     * example, if you were to specify &quot;15W&quot; as the value for the
     * day-of-month field, the meaning is: &quot;the nearest weekday to the 15th of
     * the month&quot;. So if the 15th is a Saturday, the trigger will fire on
     * Friday the 14th. If the 15th is a Sunday, the trigger will fire on Monday the
     * 16th. If the 15th is a Tuesday, then it will fire on Tuesday the 15th.
     * However if you specify &quot;1W&quot; as the value for day-of-month, and the
     * 1st is a Saturday, the trigger will fire on Monday the 3rd, as it will not
     * 'jump' over the boundary of a month's days.  The 'W' character can only be
     * specified when the day-of-month is a single day, not a range or list of days.</p>
     *
     * <p>The 'L' and 'W' characters can also be combined for the day-of-month
     * expression to yield 'LW', which translates to &quot;last weekday of the
     * month&quot;.</p>
     *
     * <p>The '#' character is allowed for the day-of-week field. This character is
     * used to specify &quot;the nth&quot; XXX day of the month. For example, the
     * value of &quot;6#3&quot; in the day-of-week field means the third Friday of
     * the month (day 6 = Friday and &quot;#3&quot; = the 3rd one in the month).
     * Other examples: &quot;2#1&quot; = the first Monday of the month and
     * &quot;4#5&quot; = the fifth Wednesday of the month. Note that if you specify
     * &quot;#5&quot; and there is not 5 of the given day-of-week in the month, then
     * no firing will occur that month.</p>
     *
     * <p>The legal characters and the names of months and days of the week are not
     * case sensitive.<p>
     *
     * <p>
     * <b>NOTES:</b>
     * <ul>
     * <li>Support for specifying both a day-of-week and a day-of-month value is
     * not complete (you'll need to use the '?' character in on of these fields).
     * </li>
     * </ul>
     * </p>
     */
    String cron() default "";
    
    /**
     * Set to {@code true} if schedule must be performed even in case of misfire. 
     */
    boolean fireAll() default false;
    
    /**
     * Description of the underlying schedulable method.
     */
    String description() default "";
}