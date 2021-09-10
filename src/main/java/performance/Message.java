package performance;

import lombok.Getter;

@Getter
public class Message {

    private String message;
    private String topic;
    private boolean retained;
    private int messagePosition;

    public Message(Message message, int messagePosition, String testId) {
        this.message = message.getMessage() + "||||" + testId;
        this.topic = message.getTopic();
        this.retained = message.isRetained();
        this.messagePosition = messagePosition;
    }


    public Message(String message, String topic, boolean retained) {
        this.message = message;
        this.topic = topic;
        this.retained = retained;
    }

    public String getMessageSpecial(){
        return this.message + "||||" + System.currentTimeMillis();
    }
}
