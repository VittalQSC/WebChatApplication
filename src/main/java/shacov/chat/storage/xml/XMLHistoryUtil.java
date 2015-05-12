package shacov.chat.storage.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import shacov.chat.model.Message;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by VittalQSC on 05.05.2015.
 */
public final class XMLHistoryUtil {
  private static final String STORAGE_LOCATION = System.getProperty("user.home") +  File.separator + "history.xml"; // history.xml will be located in the home directory
  private static final String MESSAGES = "messages";
  private static final String MESSAGE = "message";
  private static final String USER = "user";
  private static final String ID = "id";
  private static final String TEXT = "text";
  private static final String REMOVED = "removed";

  public static synchronized boolean doesStorageExist() {
    File file = new File(STORAGE_LOCATION);
    return file.exists();
  }

  public static synchronized void createStorage() throws ParserConfigurationException, TransformerException {
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

    Document doc = docBuilder.newDocument();
    Element rootElement = doc.createElement(MESSAGES);
    doc.appendChild(rootElement);

    Transformer transformer = getTransformer();

    DOMSource source = new DOMSource(doc);
    StreamResult result = new StreamResult(new File(STORAGE_LOCATION));
    transformer.transform(source, result);
  }

  private static Transformer getTransformer() throws TransformerConfigurationException {
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    // Formatting XML properly
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    return transformer;
  }

  public static synchronized void addData(Message message) throws ParserConfigurationException, SAXException, IOException, TransformerException {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    Document document = documentBuilder.parse(STORAGE_LOCATION);
    document.getDocumentElement().normalize();

    Element root = document.getDocumentElement(); // Root <messages> element

    Element messageElement = document.createElement(MESSAGE);
    root.appendChild(messageElement);

    String messageId = message.getId();
    messageElement.setAttribute(ID, messageId);

    Element user = document.createElement(USER);
    user.appendChild(document.createTextNode(message.getName()));
    messageElement.appendChild(user);

    Element text = document.createElement(TEXT);
    String messageText = message.getMessage();
    text.appendChild(document.createTextNode(messageText));
    messageElement.appendChild(text);

    Element removed = document.createElement(REMOVED);
    String messageIsRemoved = Boolean.toString(message.isRemoved());
    removed.appendChild(document.createTextNode(messageIsRemoved));
    messageElement.appendChild(removed);

    DOMSource source = new DOMSource(document);

    Transformer transformer = getTransformer();

    StreamResult result = new StreamResult(STORAGE_LOCATION);
    transformer.transform(source, result);
  }

  public static synchronized List<Message> getMessages() throws SAXException, IOException, ParserConfigurationException {
    return getSubMessagesByIndex(0); // Return all tasks from history
  }

  public static synchronized List<Message> getSubMessagesByIndex(int index) throws ParserConfigurationException, SAXException, IOException {
    List<Message> messages = new ArrayList<>();
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    Document document = documentBuilder.parse(STORAGE_LOCATION);
    document.getDocumentElement().normalize();
    Element root = document.getDocumentElement(); // Root <messages> element
    NodeList messageList = root.getElementsByTagName(MESSAGE);
    for (int i = index; i < messageList.getLength(); i++) {
      Element messageElement = (Element) messageList.item(i);
      String user = messageElement.getElementsByTagName(USER).item(0).getTextContent();
      String id = messageElement.getAttribute(ID);
      String text = messageElement.getElementsByTagName(TEXT).item(0).getTextContent();
      boolean removed = Boolean.valueOf(messageElement.getElementsByTagName(REMOVED).item(0).getTextContent());
      messages.add(new Message(user, id, text, removed));
    }
    return messages;
  }

  public static synchronized int getStorageSize() throws SAXException, IOException, ParserConfigurationException {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    Document document = documentBuilder.parse(STORAGE_LOCATION);
    document.getDocumentElement().normalize();
    Element root = document.getDocumentElement(); // Root <tasks> element
    return root.getElementsByTagName(MESSAGE).getLength();
  }
  public static synchronized void removeData(String id) throws ParserConfigurationException, SAXException, IOException,
    TransformerException, XPathExpressionException {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    Document document = documentBuilder.parse(STORAGE_LOCATION);
    document.getDocumentElement().normalize();

    Node messageToRemove = getNodeById(document,id);

    if (messageToRemove != null) {

      NodeList childNodes = messageToRemove.getChildNodes();

      for (int i = 0; i < childNodes.getLength(); i++) {

        Node node = childNodes.item(i);

        if (REMOVED.equals(node.getNodeName())) {
          node.setTextContent(Boolean.toString(true));
        }

      }

      Transformer transformer = getTransformer();

      DOMSource source = new DOMSource(document);
      StreamResult result = new StreamResult(new File(STORAGE_LOCATION));
      transformer.transform(source, result);
    } else {
      throw new NullPointerException();
    }
  }
    public static synchronized void updateData(Message message) throws ParserConfigurationException, SAXException, IOException, TransformerException, XPathExpressionException {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    Document document = documentBuilder.parse(STORAGE_LOCATION);
    document.getDocumentElement().normalize();
    Node messageToUpdate = getNodeById(document,message.getId());

    if (messageToUpdate != null) {

      NodeList childNodes = messageToUpdate.getChildNodes();

      for (int i = 0; i < childNodes.getLength(); i++) {

        Node node = childNodes.item(i);

        if (TEXT.equals(node.getNodeName())) {
          node.setTextContent(message.getMessage());
        }

        if (REMOVED.equals(node.getNodeName())) {
          node.setTextContent(Boolean.toString(message.isRemoved()));
        }

      }

      Transformer transformer = getTransformer();

      DOMSource source = new DOMSource(document);
      StreamResult result = new StreamResult(new File(STORAGE_LOCATION));
      transformer.transform(source, result);
    } else {
      throw new NullPointerException();
    }
  }

  private static Node getNodeById(Document doc, String id) throws XPathExpressionException {
    XPath xpath = XPathFactory.newInstance().newXPath();
    XPathExpression expr = xpath.compile("//" + MESSAGE + "[@id='" + id + "']");
    return (Node) expr.evaluate(doc, XPathConstants.NODE);
  }
}
