package shacov.chat.model;

import java.util.Date;

/**
 * Created by VittalQSC on 31.03.2015.
 */
public class Message {
  private String user;
  private String id;
  private String text;
  private boolean removed;
  private Date updated_at;
  public Message(){this.removed = false;}
  public Message(String user, String id, String message){
    this.user = user;
    this.id = id;
    this.text = message;
    this.removed = false;
    this.updated_at = new Date();
  }

  public Message(String user, String id, String message, boolean removed, Date updated_at){
    this.user = user;
    this.id = id;
    this.text = message;
    this.removed = removed;
    this.updated_at = updated_at;
  }
//  Message(String message){
//    this.id = Integer.parseInt(message.substring(message.indexOf("\"id\":\"")+6, message.indexOf("\",\"user\":\"")));
//    this.user = message.substring(message.indexOf("\",\"user\":\"") + 10, message.indexOf("\",\"text\":\""));
//    this.text = message.substring(message.indexOf("\",\"text\":\"")+10,message.indexOf("\"}"));
//    this.removed = false;
//  }

  public String getName(){return this.user;}

  public String getId(){return this.id;}

  public String getMessage(){return this.text;}

  public Date getUpdatedAt() { return this.updated_at; }

  public void setName(String name){ this.user = name;}

  public void setId(String id) {
    this.id = id;
  }

  public void setMessage(String message) {
    this.text = message;
  }

  public boolean isRemoved() {
    return removed;
  }

  public void setRemoved(boolean removed) {
    this.removed = removed;
  }
/*
  public Message editInfo(JSONObject obj){
    this.id = Integer.parseInt(obj.get("id").toString());
    this.text = (String) obj.get("text");
    return this;
  }
*/
  @Override
  public String toString() {
    return "{" +
      "\"id\":\"" + id + "\"," +
      "\"user\":\"" + user + "\"," +
      "\"text\":\"" + text + "\"," +
      "\"removed\":" + removed  +
      "}";
  }
}
