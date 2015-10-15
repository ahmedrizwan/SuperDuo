package barqsoft.footballscores.activities;

/**
 * Created by ahmedrizwan on 15/10/2015.
 */
public class MessageEvent {
    String messageString;

    public MessageEvent(String messageString) {
        this.messageString = messageString;
    }
    public String getMessageString() {
        return messageString;
    }

    public void setMessageString(final String messageString) {
        this.messageString = messageString;
    }
}
