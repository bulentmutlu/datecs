package com.blk.sdk.olib;

import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

class XmlWriter extends ObjectSerialize {

    static class x
    {
        XmlSerializer xmlSerializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        String tag;
        String innerXml;
        public x(String tag)
        {
            this.tag = tag;
            try {
                xmlSerializer.setOutput(writer);
                //xmlSerializer.startDocument(null, null);
                xmlSerializer.startTag("", tag);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        void addInnerXml(String innerXml)
        {
            this.innerXml = innerXml;
        }
        public String ToString()
        {
            try {


                if (innerXml != null) {
                    xmlSerializer.flush();
                    writer.write(innerXml);
                }

                xmlSerializer.endTag("", tag);
                //xmlSerializer.endDocument();

                xmlSerializer.flush();


                return writer.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }
    }


    @Override
    void addArrayMember(Object encoded, Class cArrayMember, Object oMember) throws Exception
    {
        throw new NoSuchMethodException();
    }

    @Override
    Object newObject(Class c) throws Exception {
        if (olib.isClassCollection(c))
            throw new NoSuchMethodException();
        return new x(c.getSimpleName());
    }

    @Override
    void setObject(Object owner, Object newObject, Field f) throws Exception {
        x xowner = (x) owner;
        x xnewObject = (x) newObject;
        xowner.addInnerXml(xnewObject.ToString());
        //xowner.xmlSerializer.text(xnewObject.ToString());
    }

    @Override
    boolean processField(Object userObject, Object owner, Field f) throws Exception {
        x jo = (x) userObject;
        Class<?> c = f.getType();
        Object value = f.get(owner);
        String name = f.getName();

        if (c == String.class) {
            if (value != null) {
                try {
                    jo.xmlSerializer.attribute("", name, "" + value);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return true;
        }
        if (c == int.class) {
            if (value != null) jo.xmlSerializer.attribute("", name, "" + value);
            return true;
        }
        if (c == long.class) {
            if (value != null) jo.xmlSerializer.attribute("", name, "" + value);
            return true;
        }
        if (c == double.class) {
            if (value != null) jo.xmlSerializer.attribute("", name, "" + value);
            return true;
        }
        if (c == boolean.class) {
            if (value != null) jo.xmlSerializer.attribute("", name, "" + value);
            return true;
        }
        if (c == Date.class) {
            if (value != null) {
                String sDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date) f.get(owner));
                jo.xmlSerializer.attribute("", name, "" + sDate);
            }

            return true;
        }
        return false;
    }

}