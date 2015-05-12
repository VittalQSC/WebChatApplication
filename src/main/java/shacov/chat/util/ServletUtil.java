package shacov.chat.util;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by VittalQSC on 09.05.2015.
 */
public final class ServletUtil {
  public static final String APPLICATION_JSON = "application/json";

  private ServletUtil() {
  }

  public static String getMessageBody(HttpServletRequest request) throws IOException {
    StringBuilder sb = new StringBuilder();
    BufferedReader reader = request.getReader();
    String line;
    while ((line = reader.readLine()) != null) {
      sb.append(line);
    }
    return sb.toString();
  }

}

