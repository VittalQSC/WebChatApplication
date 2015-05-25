package shacov.chat.util;

import org.json.simple.JSONObject;
import shacov.chat.model.Message;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.UUID;

/**
 * Created by VittalQSC on 06.05.2015.
 */
public final class MessageUtil {
  public static final String TOKEN = "token";
  public static final String LAST_MODIFIED = "lastModified";
  public static final String MESSAGES = "messages";
  private static final String TN = "TN";
  private static final String EN = "EN";
  private static final String USER = "user";
  private static final String ID = "id";
  private static final String TEXT = "text";

  public static String getToken(int index) {
    Integer number = index * 8 + 11;
    return TN + number + EN;
  }

  //private static Integer idF = 0;
  public static String uniqueId() {
    //String id = Integer.toString(++idF);
    String id = UUID.randomUUID().toString();
    return id;
  }

  public static int getIndex(String token) {
    return (Integer.valueOf(token.substring(2, token.length() - 2)) - 11) / 8;
  }

  public static JSONObject stringToJson(String data) throws ParseException {
    JSONParser parser = new JSONParser();
    return (JSONObject) parser.parse(data.trim());
  }

  public static Message jsonToMessage(JSONObject json) {
    Object user = json.get(USER);
    Object text = json.get(TEXT);

    if (user != null && text!= null) {
      return new Message((String)user, uniqueId(), (String)text);
    }
    return null;
  }
  public static Message jsonToExistingMessage(JSONObject json) {
    Object text = json.get(TEXT);
    Object id = json.get(ID);

    if (text!= null) {
      return new Message("temp", (String)id, (String)text);
    }

    return null;
  }
}
