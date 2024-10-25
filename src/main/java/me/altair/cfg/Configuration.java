package me.altair.cfg;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileNotFoundException;

public class Configuration {

    private boolean removeSourceFile;
    private boolean removeLocalVariables;
    private boolean removeLineNumbers;

    public Configuration() {
        loadConfiguration();
    }

    private void loadConfiguration() {
        try {
            File file = new File("configuration.xml");
            if (!file.exists()) {
                throw new FileNotFoundException("Configuration file not found: " + file.getAbsolutePath());
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);

            document.getDocumentElement().normalize();

            Element root = document.getDocumentElement();
            NodeList nodeList = root.getChildNodes();

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String tagName = element.getTagName();
                    String textContent = element.getTextContent().trim();

                    boolean value = parseBooleanValue(textContent, tagName);

                    switch (tagName) {
                        case "RemoveSourceFile":
                            this.removeSourceFile = value;
                            break;
                        case "RemoveLocalVariables":
                            this.removeLocalVariables = value;
                            break;
                        case "RemoveLineNumbers":
                            this.removeLineNumbers = value;
                            break;
                        default:
                            throw new IllegalArgumentException("Unexpected configuration option: " + tagName);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load configuration from " + "configuration.xml", e);
        }
    }

    private boolean parseBooleanValue(String value, String tagName) {
        if ("Enabled".equalsIgnoreCase(value)) {
            return true;
        } else if ("Disabled".equalsIgnoreCase(value)) {
            return false;
        } else {
            throw new IllegalArgumentException("Invalid value for " + tagName + ": " + value +". Expected 'Enabled' or 'Disabled'.");
        }
    }

    public boolean isRemoveSourceFile() {
        return removeSourceFile;
    }

    public boolean isRemoveLocalVariables() {
        return removeLocalVariables;
    }

    public boolean isRemoveLineNumbers() {
        return removeLineNumbers;
    }
}
