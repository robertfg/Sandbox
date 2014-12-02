package com.anz.rer.etl.utils;

import java.io.File;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.QName;
import org.dom4j.io.SAXReader;

public class XMLUtils {

	public static void removeAllNamespaces(Document doc) {
        Element root = doc.getRootElement();
        if (root.getNamespace() !=
                Namespace.NO_NAMESPACE) {            
                removeNamespaces(root.content());
        }
    }

    public static void unfixNamespaces(Document doc, Namespace original) {
        Element root = doc.getRootElement();
        if (original != null) {
            setNamespaces(root.content(), original);
        }
    }

    public static void setNamespace(Element elem, Namespace ns) {

        elem.setQName(QName.get(elem.getName(), ns,
                elem.getQualifiedName()));
    }


    public static void removeNamespaces(Element elem) {
        setNamespaces(elem, Namespace.NO_NAMESPACE);
    }

    public static void removeNamespaces(List l) {
        setNamespaces(l, Namespace.NO_NAMESPACE);
    }

    public static void setNamespaces(Element elem, Namespace ns) {
        setNamespace(elem, ns);
        setNamespaces(elem.content(), ns);
    }

    public static void setNamespaces(List l, Namespace ns) {
        Node n = null;
        for (int i = 0; i < l.size(); i++) {
            n = (Node) l.get(i);

            if (n.getNodeType() == Node.ATTRIBUTE_NODE) {
                ((Attribute) n).setNamespace(ns);
            }
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                setNamespaces((Element) n, ns);
            }            
        }
    }
	
    public static Document parse(File file) throws DocumentException {
	        SAXReader reader = new SAXReader();
	        Document document =   reader.read(file);
	        return document;
	  }
	
}
