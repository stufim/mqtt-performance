package performance;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

@Slf4j
@Getter
public class PublishThread extends Thread {

    final int maxMessages;
    int publishes = 0;

    Message actualMessage = null;
    Deque<Message> stack = new ArrayDeque<>();
    private final int threadNumber;
    private final String address;
    private Mqtt5BlockingClient client;

    public PublishThread(int threadNumber, int maxMessages, String address) {
        this.maxMessages = maxMessages;
        this.threadNumber = threadNumber;
        this.address = address;
        initClient();
    }

    public PublishThread(Mqtt5BlockingClient client, int threadNumber, int maxMessages, String address) {
        this.maxMessages = maxMessages;
        this.threadNumber = threadNumber;
        this.address = address;
        this.client = client;
    }

    @Override
    public void run() {
        while (publishes != maxMessages) {
            if (actualMessage != null) {
                publish();
            }
        }
        try {
          //  mqttAsyncClient.disconnect();
            //client.();
           // client.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
     //   client.disconnect();
      //  log.info("end game thread {}", threadNumber);
    }

    public synchronized void push(Message message) {
        if (actualMessage == null) {
            actualMessage = message;
        } else {
            stack.add(message);
        }
    }

    private void publish() {
        //log.info("sdfsd {}",actualMessage.getMessagePosition());
        publishToServer(actualMessage);
        publishes++;
        synchronized (this) {
            if (!stack.isEmpty()) {
                actualMessage = stack.getFirst();
                stack.removeFirst();
            } else {
                actualMessage = null;
            }
        }

    }

    private void publishToServer(Message actualMessage) {
        try {
            //String message = actualMessage.getTopic() + UUID.randomUUID().toString().getBytes();
           // mqttAsyncClient.publish(actualMessage.getTopic(), actualMessage.getMqttMessage());
            /*connection.publish(actualMessage.getTopic(),
                    actualMessage.getMessage().getBytes(),
                    QoS.AT_MOST_ONCE, actualMessage.isRetained());*/

           /* connection.getDispatchQueue().execute(new Runnable(){
                public void run() {
                    connection.publish( ..... );
                }
            });*/

            client.toAsync().publishWith()
                    .topic(actualMessage.getTopic())
                    .payload(actualMessage.getMessageSpecial().getBytes())
                    .qos(MqttQos.EXACTLY_ONCE)
                    .send()
                    .whenCompleteAsync((mqtt3Publish, throwable) -> {
                        if (throwable != null) {
                           log.error("Error", throwable);
                        } else {
                        }
                    });

        } catch (Exception e) {
            log.error("Error", e);
        }
    }

    private void initClient() {
        client = Mqtt5Client.builder().identifier(UUID.randomUUID().toString()) // the unique identifier of the MQTT client. The ID is randomly generated between
                .serverHost(address)  // the host name or IP address of the MQTT server. Kept it localhost for testing. localhost is default if not specified.
                .serverPort(1883)
                // specifies the port of the server
                .buildBlocking();  // creates t
        client.connect();

    }
}
