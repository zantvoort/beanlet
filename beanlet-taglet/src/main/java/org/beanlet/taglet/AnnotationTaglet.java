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
package org.beanlet.taglet;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationDesc.ElementValuePair;
import com.sun.javadoc.AnnotationTypeDoc;
import com.sun.javadoc.AnnotationTypeElementDoc;
import com.sun.javadoc.AnnotationValue;
import com.sun.javadoc.Doc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.Tag;
import com.sun.tools.doclets.Taglet;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.Map;

/**
 *
 * @author Leon van Zantvoort
 */
public final class AnnotationTaglet implements Taglet {
    
    private static final String NAME = "beanlet.annotation";
    
    private static final String BEANLET_HEADER =
            "&lt;beanlets xmlns=\"http://beanlet.org/schema/beanlet\"\n" +
            "          xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "          xsi:schemaLocation=\"http://beanlet.org/schema/beanlet http://beanlet.org/schema/beanlet/beanlet_1_0.xsd\"&gt;\n";
    
    private static final String MANAGEMENT_HEADER =
            "&lt;beanlets xmlns=\"http://beanlet.org/schema/beanlet\"\n" +
            "          xmlns:mx=\"http://beanlet.org/schema/management\"\n" +
            "          xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "          xsi:schemaLocation=\"http://beanlet.org/schema/beanlet http://beanlet.org/schema/beanlet/beanlet_1_0.xsd\n" +
            "                               http://beanlet.org/schema/management http://beanlet.org/schema/management/beanlet_management_1_0.xsd\"&gt;\n";
    
    private static final String NAMING_HEADER =
            "&lt;beanlets xmlns=\"http://beanlet.org/schema/beanlet\"\n" +
            "          xmlns:jndi=\"http://beanlet.org/schema/naming\"\n" +
            "          xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "          xsi:schemaLocation=\"http://beanlet.org/schema/beanlet http://beanlet.org/schema/beanlet/beanlet_1_0.xsd\n" +
            "                               http://beanlet.org/schema/management http://beanlet.org/schema/naming/beanlet_naming_1_0.xsd\"&gt;\n";
    
    private static final String TRANSACTION_HEADER =
            "&lt;beanlets xmlns=\"http://beanlet.org/schema/beanlet\"\n" +
            "          xmlns:tx=\"http://beanlet.org/schema/transaction\"\n" +
            "          xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "          xsi:schemaLocation=\"http://beanlet.org/schema/beanlet http://beanlet.org/schema/beanlet/beanlet_1_0.xsd\n" +
            "                              http://beanlet.org/schema/transaction http://beanlet.org/schema/transaction/beanlet_transaction_1_0.xsd\"&gt;\n";

    private static final String WEB_HEADER =
            "&lt;beanlets xmlns=\"http://beanlet.org/schema/beanlet\"\n" +
            "          xmlns:web=\"http://beanlet.org/schema/web\"\n" +
            "          xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "          xsi:schemaLocation=\"http://beanlet.org/schema/beanlet http://beanlet.org/schema/beanlet/beanlet_1_0.xsd\n" +
            "                              http://beanlet.org/schema/web http://beanlet.org/schema/web/beanlet_web_1_0.xsd\"&gt;\n";
    
    private static final String FOOTER = "&lt;/beanlets&gt;";
    
    private static final String BEANLET_ELEMENT_HEADER = 
            "  &lt;beanlet name=\"foo\" type=\"com.acme.Foo\"&gt;\n";
    private static final String BEANLET_ELEMENT_FOOTER = 
            "  &lt;/beanlet&gt;\n";
    
    public boolean isInlineTag() {
        return true;
    }
    
    public boolean inType() {
        return true;
    }
    
    public boolean inPackage() {
        return false;
    }
    
    public boolean inOverview() {
        return false;
    }
    
    public boolean inMethod() {
        return false;
    }
    
    public boolean inField() {
        return false;
    }
    
    public boolean inConstructor() {
        return false;
    }
    
    public String getName() {
        return NAME;
    }
    
    public String toString(Tag[] tags) {
        if (tags.length == 0) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (Tag tag : tags) {
            builder.append(toString(tag));
        }
        return builder.toString();
    }
    
    public String toString(Tag tag) {
        Doc tmp = tag.holder();
        if (!(tmp instanceof AnnotationTypeDoc)) {
            return null;
        }
        AnnotationTypeDoc doc = (AnnotationTypeDoc) tmp;
        String text = tag.text();
        if (text == null || text.length() == 0) {
            text = doc.simpleTypeName();
            StringBuilder t = new StringBuilder();
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (Character.isUpperCase(c)) {
                    if (i > 0) {
                        t.append("-");
                    }
                    t.append(Character.toLowerCase(c));
                } else {
                    t.append(c);
                }
            }
            text = t.toString();
        }
        
        String target = getTarget(doc);
        AnnotationTypeElementDoc[] elements = doc.elements();
        StringBuilder builder = new StringBuilder();
        builder.append("<p><h3>XML Representation</h3>");
        builder.append("The following xml-fragment shows how to express this " +
                "annotation in xml.");
        if (target.length() > 0) {
            builder.append(" The italic attribute of the '" + text +
                    "' tag is used to identify the element to which this " +
                    "annotation is applied.");
            if (elements.length == 1) {
                builder.append(" The other attribute can be specified " +
                        "optionally if the annotation specifies a " +
                        "default value for the particular annotation method.");
            } else if (elements.length > 1) {
                builder.append(" The other attributes can be specified " +
                        "optionally if the annotation specifies a " +
                        "default value for the particular annotation methods.");
            }
        } else {
            builder.append(" The '" + text + "' tag does not specify any " +
                    "element attribute, which means that this tag is applied " +
                    "to the beanlet's class.");
            if (elements.length == 1) {
                builder.append(" The attribute can be specified " +
                        "optionally if the annotation specifies a " +
                        "default value for the particular annotation method.");
            } else if (elements.length > 1) {
                builder.append(" The attributes can be specified " +
                        "optionally if the annotation specifies a " +
                        "default value for the particular annotation methods.");
            }
        }
        builder.append("<br><pre><tt>");
        String prefix = "";
        String pkg = doc.containingPackage().name();
        if (pkg.equals("org.beanlet")) {
            builder.append(BEANLET_HEADER);
        } else if (pkg.equals("org.beanlet.management")) {
            builder.append(MANAGEMENT_HEADER);
            prefix = "mx:";
        } else if (pkg.equals("org.beanlet.naming")) {
            builder.append(NAMING_HEADER);
            prefix = "jndi:";
        } else if (pkg.equals("org.beanlet.transaction")) {
            builder.append(TRANSACTION_HEADER);
            prefix = "tx:";
        } else if (pkg.equals("org.beanlet.web")) {
            builder.append(WEB_HEADER);
            prefix = "web:";
        } else {
            builder.append(BEANLET_HEADER);
        }
        builder.append(BEANLET_ELEMENT_HEADER);
        builder.append("    <b>&lt;");
        builder.append(prefix);
        builder.append(text);
        int e = 0;
        if (target.length() > 0) {
            e++;
            builder.append(" <i>");
            builder.append(target);
            builder.append("</i>");
        }
        for (AnnotationTypeElementDoc element : elements) {
            if (e != 0 && e % 3 == 0) {
                builder.append("\n     ");
                for (int i = 0; i < text.length(); i++) {
                    builder.append(" ");
                }
            }
            builder.append(" ");
            String name = element.qualifiedName();
            name = name.substring(doc.qualifiedTypeName().length() + 1);
            for (int i = 0; i < name.length(); i++) {
                char c = name.charAt(i);
                if (Character.isUpperCase(c)) {
                    builder.append("-");
                    builder.append(Character.toLowerCase(c));
                } else {
                    builder.append(c);
                }
            }
            builder.append("=");
            builder.append("\"");
            Object value = element.defaultValue() == null ? null : 
                    element.defaultValue().value();
            if (value != null) {
                if (value.getClass().isArray()) {
                    AnnotationValue[] array = (AnnotationValue[]) value;
                    for (int i = 0; i < array.length; i++) {
                        Object v = array[i].value();
                        if (v instanceof FieldDoc) {
                            builder.append(((FieldDoc) v).name());
                        } else {
                            builder.append(v);
                        }
                        if (i < array.length - 1) {
                            builder.append(",");
                        }
                    }
                } else if (value instanceof FieldDoc) {
                    builder.append(((FieldDoc) value).name());
                } else {
                    builder.append(value);
                }
            }
            builder.append("\"");
            e++;
        }
        builder.append("/&gt;</b>\n");
        builder.append(BEANLET_ELEMENT_FOOTER);
        builder.append(FOOTER);
        builder.append("</tt></pre></p>");
        return builder.toString();
    }

    private String getTarget(AnnotationTypeDoc doc) {
        boolean type = false;
        boolean method = false;
        boolean field = false;
        boolean constructor = false;
        for (AnnotationDesc desc : doc.annotations()) {
            if (desc.annotationType().qualifiedName().equals(Target.class.getName())) {
                ElementValuePair pair = desc.elementValues()[0];
                AnnotationValue[] array = (AnnotationValue[]) pair.value().value();
                for (AnnotationValue value : array) {
                    FieldDoc fieldDoc = (FieldDoc) value.value();
                    String name = fieldDoc.name();
                    if (name.equals(ElementType.TYPE.name())) {
                        type = true;
                    } else if (name.equals(ElementType.METHOD.name())) {
                        method = true;
                    } else if (name.equals(ElementType.PARAMETER.name())) {
                        method = true;
                    } else if (name.equals(ElementType.FIELD.name())) {
                        field = true;
                    } else if (name.equals(ElementType.CONSTRUCTOR.name())) {
                        field = true;
                    }
                }
            }
        }
        if (type) {
            return "";
        } else if (field) {
            return "field=\"bar\"";
        } else if (method) {
            return "method=\"bar\"";
        } else if (constructor) {
            return "constructor=\"true\"";
        } else {
            return "";
        }
    }
    
    /**
     * Register this Taglet.
     * @param tagletMap  the map to register this tag to.
     */
    public static void register(Map<String, Taglet> tagletMap) {
        AnnotationTaglet taglet = new AnnotationTaglet();
        Taglet t = tagletMap.get(taglet.getName());
        if (t != null) {
            tagletMap.remove(taglet.getName());
        }
        tagletMap.put(taglet.getName(), taglet);
    }
}
