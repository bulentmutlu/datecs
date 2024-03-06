package com.blk.sdk.olib;

import com.blk.sdk.com.xmlswitchserver.Body;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilderFactory;

class XmlReader extends ObjectDeserialize {

    static class r {
        Document doc = null;
        NodeList nl;
        Node n;
        NamedNodeMap m;
        public r(String xml, String tag)
        {
            try {
                InputStream in = new ByteArrayInputStream(xml.getBytes("utf-8"));
                doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
                nl = doc.getElementsByTagName(tag);
                n = nl.item(0);
                m = n.getAttributes();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        public r(Node n)
        {
            this.n = n;
            m = n.getAttributes();
        }
    }
    @Override
    public int getArrayLength(Object encoded, Field f) {
        return 0;
    }

    @Override
    public Object getArrayMember(Object encoded, Field f, int i) throws JSONException {
        return null;
    }

    @Override
    public Object getCollection(Object encoded, Field f) throws JSONException {
        return null;
    }

    @Override
    Object getObject(Object encoded, Field f) throws Exception {
        r jo = (r) encoded;


        String tagName = f.getType().getSimpleName();
        String xml = null;
        r innexR = null;

        NodeList nodeList = jo.n.getChildNodes();
        for (int count = 0; count < nodeList.getLength(); count++) {
            Node node = nodeList.item(count);
            if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals(tagName)) {
                innexR = new r(node);
                break;
            }
        }

        return innexR;
    }

    @Override
    boolean processField(Object userObject, Object owner, Field f) throws Exception {
        r jo = (r) userObject;

        String name = f.getName();
        Class<?> c = f.getType();
        if (c == String.class) {
            if (jo.m.getNamedItem(name) != null) {
                String value = jo.m.getNamedItem(name).getNodeValue();
                f.set(owner, value);
            }
            return true;
        }
        if (c == int.class) {
            if (jo.m.getNamedItem(name) != null) {
                String value = jo.m.getNamedItem(name).getNodeValue();
                f.set(owner, Integer.parseInt(value));
            }
            return true;
        }
        if (c == long.class) {
            if (jo.m.getNamedItem(name) != null) {
                String value = jo.m.getNamedItem(name).getNodeValue();
                f.set(owner, Long.parseLong(value));
            }
            return true;
        }
        if (c == double.class) {
            if (jo.m.getNamedItem(name) != null) {
                String value = jo.m.getNamedItem(name).getNodeValue();
                f.set(owner, Double.parseDouble(value));
            }
            return true;
        }
        if (c == boolean.class) {
            if (jo.m.getNamedItem(name) != null) {
                String value = jo.m.getNamedItem(name).getNodeValue();
                f.set(owner, Boolean.parseBoolean(value));
            }
            return true;
        }

        if (c == Date.class) {
            String sDate = jo.m.getNamedItem(name).getNodeValue();
            sDate = sDate.replace('T', '_').substring(0, 19);
            Date date = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").parse(sDate);
            f.set(owner, date);
            return true;
        }
        return false;
    }
}
