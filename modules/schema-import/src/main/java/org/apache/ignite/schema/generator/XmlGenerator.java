/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.schema.generator;

import org.apache.ignite.schema.model.*;
import org.apache.ignite.schema.ui.*;
import org.w3c.dom.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import java.io.*;
import java.nio.file.*;
import java.text.*;
import java.util.*;

import static org.apache.ignite.schema.ui.MessageBox.Result.*;

/**
 * Generator of XML files for type metadata.
 */
public class XmlGenerator {
    /**
     * Add comment with license and generation date.
     *
     * @param doc XML document.
     */
    private static void addComment(Document doc) {
        doc.appendChild(doc.createComment("\n" +
            "  Licensed to the Apache Software Foundation (ASF) under one or more\n" +
            "  contributor license agreements.  See the NOTICE file distributed with\n" +
            "  this work for additional information regarding copyright ownership.\n" +
            "  The ASF licenses this file to You under the Apache License, Version 2.0\n" +
            "  (the \"License\"); you may not use this file except in compliance with\n" +
            "  the License.  You may obtain a copy of the License at\n\n" +
            "       http://www.apache.org/licenses/LICENSE-2.0\n\n" +
            "  Unless required by applicable law or agreed to in writing, software\n" +
            "  distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
            "  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
            "  See the License for the specific language governing permissions and\n" +
            "  limitations under the License.\n"));

        doc.appendChild(doc.createComment("\n    XML generated by Apache Ignite Schema Import utility: " +
            new SimpleDateFormat("MM/dd/yyyy").format(new Date()) + "\n"));
    }

    /**
     * Add bean to XML document.
     *
     * @param doc XML document.
     * @param parent Parent XML node.
     * @param cls Bean class name.
     */
    private static Element addBean(Document doc, Node parent, String cls) {
        Element elem = doc.createElement("bean");

        elem.setAttribute("class", cls);

        parent.appendChild(elem);

        return elem;
    }

    /**
     * Add element to XML document.
     *
     * @param doc XML document.
     * @param parent Parent XML node.
     * @param tagName XML tag name.
     * @param attr1 Name for first attr.
     * @param val1 Value for first attribute.
     * @param attr2 Name for second attr.
     * @param val2 Value for second attribute.
     */
    private static Element addElement(Document doc, Node parent, String tagName,
        String attr1, String val1, String attr2, String val2) {
        Element elem = doc.createElement(tagName);

        if (attr1 != null)
            elem.setAttribute(attr1, val1);

        if (attr2 != null)
            elem.setAttribute(attr2, val2);

        parent.appendChild(elem);

        return elem;
    }

    /**
     * Add element to XML document.
     *
     * @param doc XML document.
     * @param parent Parent XML node.
     * @param tagName XML tag name.
     */
    private static Element addElement(Document doc, Node parent, String tagName) {
        return addElement(doc, parent, tagName, null, null, null, null);
    }

    /**
     * Add element to XML document.
     *
     * @param doc XML document.
     * @param parent Parent XML node.
     * @param tagName XML tag name.
     */
    private static Element addElement(Document doc, Node parent, String tagName, String attrName, String attrVal) {
        return addElement(doc, parent, tagName, attrName, attrVal, null, null);
    }

    /**
     * Add &quot;property&quot; element to XML document.
     *
     * @param doc XML document.
     * @param parent Parent XML node.
     * @param name Value for &quot;name&quot; attribute
     * @param val Value for &quot;value&quot; attribute
     */
    private static Element addProperty(Document doc, Node parent, String name, String val) {
        String valAttr = val != null ? "value" : null;

        return addElement(doc, parent, "property", "name", name, valAttr, val);
    }

    /**
     * Add type descriptors to XML document.
     *
     * @param doc XML document.
     * @param parent Parent XML node.
     * @param name Property name.
     * @param fields Collection of POJO fields.
     */
    private static void addFields(Document doc, Node parent, String name, Collection<PojoField> fields) {
        if (!fields.isEmpty()) {
            Element prop = addProperty(doc, parent, name, null);

            Element list = addElement(doc, prop, "list");

            for (PojoField field : fields) {
                Element item = addBean(doc, list, "org.apache.ignite.cache.CacheTypeFieldMetadata");

                addProperty(doc, item, "databaseName", field.dbName());
                Element dbType = addProperty(doc, item, "databaseType", null);
                addElement(doc, dbType, "util:constant", "static-field", "java.sql.Types." + field.dbTypeName());
                addProperty(doc, item, "javaName", field.javaName());
                addProperty(doc, item, "javaType", field.javaTypeName());
            }
        }
    }

    /**
     * Add query fields to xml document.
     *
     * @param doc XML document.
     * @param parent Parent XML node.
     * @param name Property name.
     * @param fields Map with fields.
     */
    private static void addQueryFields(Document doc, Node parent, String name, Collection<PojoField> fields) {
        if (!fields.isEmpty()) {
            Element prop = addProperty(doc, parent, name, null);

            Element map = addElement(doc, prop, "map");

            for (PojoField field : fields)
                addElement(doc, map, "entry", "key", field.javaName(), "value", field.javaTypeName());
        }
    }

    /**
     * Add indexes to xml document.
     *
     * @param doc XML document.
     * @param parent Parent XML node.
     * @param groups Map with indexes.
     */
    private static void addQueryGroups(Document doc, Node parent,
        Map<String, Map<String, IndexItem>> groups) {
        if (!groups.isEmpty()) {
            Element prop = addProperty(doc, parent, "groups", null);

            Element map = addElement(doc, prop, "map");

            for (Map.Entry<String, Map<String, IndexItem>> group : groups.entrySet()) {
                Element entry1 = addElement(doc, map, "entry", "key", group.getKey());

                Element val1 = addElement(doc, entry1, "map");

                Map<String, IndexItem> fields = group.getValue();

                for (Map.Entry<String, IndexItem> field : fields.entrySet()) {
                    Element entry2 = addElement(doc, val1, "entry", "key", field.getKey());

                    Element val2 = addBean(doc, entry2, "org.apache.ignite.lang.IgniteBiTuple");

                    IndexItem idx = field.getValue();

                    addElement(doc, val2, "constructor-arg", null, null, "value", idx.name());
                    addElement(doc, val2, "constructor-arg", null, null, "value", String.valueOf(idx.descending()));
                }
            }
        }
    }

    /**
     * Add element with type metadata to XML document.
     *
     * @param doc XML document.
     * @param parent Parent XML node.
     * @param pkg Package fo types.
     * @param pojo POJO descriptor.
     */
    private static void addTypeMetadata(Document doc, Node parent, String pkg, PojoDescriptor pojo,
        boolean includeKeys) {
        Element bean = addBean(doc, parent, "org.apache.ignite.cache.CacheTypeMetadata");

        addProperty(doc, bean, "databaseSchema", pojo.schema());

        addProperty(doc, bean, "databaseTable", pojo.table());

        addProperty(doc, bean, "keyType", pkg + "." + pojo.keyClassName());

        addProperty(doc, bean, "valueType", pkg + "." + pojo.valueClassName());

        addFields(doc, bean, "keyFields", pojo.keyFields());

        addFields(doc, bean, "valueFields", pojo.valueFields(includeKeys));

        addQueryFields(doc, bean, "queryFields", pojo.fields());

        addQueryFields(doc, bean, "ascendingFields", pojo.ascendingFields());

        addQueryFields(doc, bean, "descendingFields", pojo.descendingFields());

        addQueryGroups(doc, bean, pojo.groups());
    }

    /**
     * Transform metadata into xml.
     *
     * @param pkg Package fo types.
     * @param pojo POJO descriptor.
     * @param out File to output result.
     * @param askOverwrite Callback to ask user to confirm file overwrite.
     */
    public static void generate(String pkg, PojoDescriptor pojo, boolean includeKeys, File out,
        ConfirmCallable askOverwrite) {
        generate(pkg, Collections.singleton(pojo), includeKeys, out, askOverwrite);
    }

    /**
     * Transform metadata into xml.
     *
     * @param pkg Package fo types.
     * @param pojos POJO descriptors.
     * @param out File to output result.
     * @param askOverwrite Callback to ask user to confirm file overwrite.
     */
    public static void generate(String pkg, Collection<PojoDescriptor> pojos, boolean includeKeys, File out,
        ConfirmCallable askOverwrite) {

        File outFolder = out.getParentFile();

        if (outFolder == null)
            throw new IllegalStateException("Invalid output file: " + out);

        if (!outFolder.exists() && !outFolder.mkdirs())
            throw new IllegalStateException("Failed to create output folder for XML file: " + outFolder);

        try {
            if (out.exists()) {
                MessageBox.Result choice = askOverwrite.confirm(out.getName());

                if (CANCEL == choice)
                    throw new IllegalStateException("XML generation was canceled!");

                if (NO == choice || NO_TO_ALL == choice)
                    return;
            }

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();

            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Document doc = docBuilder.newDocument();
            doc.setXmlStandalone(true);

            addComment(doc);

            Element beans = addElement(doc, doc, "beans");
            beans.setAttribute("xmlns", "http://www.springframework.org/schema/beans");
            beans.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            beans.setAttribute("xmlns:util", "http://www.springframework.org/schema/util");
            beans.setAttribute("xsi:schemaLocation",
                "http://www.springframework.org/schema/beans " +
                "http://www.springframework.org/schema/beans/spring-beans.xsd " +
                "http://www.springframework.org/schema/util " +
                "http://www.springframework.org/schema/util/spring-util.xsd");

            for (PojoDescriptor pojo : pojos)
                addTypeMetadata(doc, beans, pkg, pojo, includeKeys);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();

            Transformer transformer = transformerFactory.newTransformer();

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            ByteArrayOutputStream baos = new ByteArrayOutputStream(65536);

            transformer.transform(new DOMSource(doc), new StreamResult(baos));

            // Custom pretty-print of generated XML.
            Files.write(out.toPath(), baos.toString()
                .replaceAll("><", ">\n<")
                .replaceFirst("<!--", "\n<!--")
                .replaceFirst("-->", "-->\n")
                .replaceAll("\" xmlns", "\"\n       xmlns")
                .replaceAll("\" xsi", "\"\n       xsi")
                .replaceAll(" http://www.springframework", "\n                           http://www.springframework")
                .getBytes());
        }
        catch (ParserConfigurationException | TransformerException | IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
