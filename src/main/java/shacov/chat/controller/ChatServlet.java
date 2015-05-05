package shacov.chat.controller;

import org.xml.sax.SAXException;
import shacov.chat.storage.xml.XMLHistoryUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.logging.Logger;

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
}
