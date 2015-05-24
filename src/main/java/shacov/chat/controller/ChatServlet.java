package shacov.chat.controller;

import static shacov.chat.util.MessageUtil.TOKEN;
import static shacov.chat.util.MessageUtil.MESSAGES;
import static shacov.chat.util.MessageUtil.getIndex;
import static shacov.chat.util.MessageUtil.getToken;
import static shacov.chat.util.MessageUtil.stringToJson;
import static shacov.chat.util.MessageUtil.jsonToMessage;
import static shacov.chat.util.MessageUtil.jsonToExistingMessage;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.xml.sax.SAXException;
import shacov.chat.model.Message;
import shacov.chat.storage.xml.XMLHistoryUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import shacov.chat.util.ServletUtil;


@WebServlet("/chat")
public class ChatServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private static Logger logger = Logger.getLogger(ChatServlet.class.getName());

  @Override
  public void init() throws ServletException {
    try {
      loadHistory();
    } catch (SAXException | IOException | ParserConfigurationException | TransformerException e) {
      logger.error(e);
    }
  }

  private void loadHistory() throws SAXException, IOException, ParserConfigurationException, TransformerException {
    if (!XMLHistoryUtil.doesStorageExist()) { // creating storage and history if not exist
      XMLHistoryUtil.createStorage();
      addStubData();
    }
  }

  private void addStubData() throws ParserConfigurationException, TransformerException {
    Message[] stubMessages = {};
    for (Message message : stubMessages) {
      try {
        XMLHistoryUtil.addData(message);
      } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
        logger.error(e);
      }
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    logger.info("doGet");
    String token = request.getParameter(TOKEN);
    logger.info("Token " + token);

    try {
      if (token != null && !"".equals(token)) {
        int index = getIndex(token);
        logger.info("Index " + index);
        String messages;
        messages = formResponse(index);
        response.setContentType(ServletUtil.APPLICATION_JSON);
        PrintWriter out = response.getWriter();
        out.print(messages);
        out.flush();
      } else {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "'token' parameter needed");
      }
    } catch (SAXException | ParserConfigurationException e) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    logger.info("doPost");
    String data = ServletUtil.getMessageBody(request);
    logger.info(data);
    try {
      JSONObject json = stringToJson(data);
      Message message = jsonToMessage(json);
      XMLHistoryUtil.addData(message);
      response.setStatus(HttpServletResponse.SC_OK);
    } catch (ParseException | ParserConfigurationException | SAXException | TransformerException e) {
      logger.error(e);
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }
  }

  @Override
  protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    logger.info("doPut");
    String data = ServletUtil.getMessageBody(request);
    logger.info(data);
    try {
      JSONObject json = stringToJson(data);
      Message message = jsonToExistingMessage(json);
      XMLHistoryUtil.updateData(message);
    } catch (ParseException | ParserConfigurationException | SAXException | TransformerException | XPathExpressionException e) {
      logger.error(e);
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }
  }

  protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    logger.info("doDelete");
    String data = ServletUtil.getMessageBody(request);
    logger.info(data);
    try {
      JSONObject json = stringToJson(data);
      Object id = json.get("id");
      XMLHistoryUtil.removeData((String)id);
    }catch (Exception e) {}
  }

  @Override
  protected long getLastModified(HttpServletRequest request) {
    logger.info("getLastModified");

    String token = request.getParameter(TOKEN);
    logger.info("Token " + token);

    long lastUpdate = -1;
    try {
      if (token != null && !"".equals(token)) {
        int index = getIndex(token);
        logger.info("Index " + index);
        List<Message> messages = XMLHistoryUtil.getSubMessagesByIndex(index);
        for (Message message : messages) {
          long time = 1000 * message.getUpdatedAt().getTime();
          if(time > lastUpdate)
            lastUpdate = time;
        }
      }
    } catch (IOException | SAXException | ParserConfigurationException e) {
      lastUpdate = -1;
    }
    return lastUpdate;
  }

  @SuppressWarnings("unchecked")
  private String formResponse(int index) throws SAXException, IOException, ParserConfigurationException {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put(MESSAGES, XMLHistoryUtil.getSubMessagesByIndex(index));
    jsonObject.put(TOKEN, getToken(XMLHistoryUtil.getStorageSize()));
    return jsonObject.toJSONString();
  }

}
