package performance;

import lombok.extern.slf4j.Slf4j;

import java.util.*;


@Slf4j
public class PerformanceService {

    public final int numberOfMessages;
    //threads are used only when using MQTT, PAHO probably can not multithreading, but solve it with asynchronous messages
    public final int numberOfThreads;
    public final String address;

    public int delay = 100;
    public int loops = 1;
    Long starttime = null;

    Deque<Message> messages = new ArrayDeque<>();
    List<PublishThread> threads = Collections.synchronizedList(new ArrayList<>());
    List<Message> basicMessages = new ArrayList<>();

    public PerformanceService(String address, int numberOfMessages, int numberOfThreads) {
        this.numberOfMessages = numberOfMessages;
        this.numberOfThreads = numberOfThreads;
        this.address = address;
    }

    public PerformanceService(String address, int numberOfMessages, int numberOfThreads, int delay, int loops) {
        this.numberOfMessages = numberOfMessages;
        this.numberOfThreads = numberOfThreads;
        this.delay = delay;
        this.loops = loops;
        this.address = address;
    }

    public void init(){
        log.info("load messages started");
        fillBasicMessages();
        log.info("load messages ended");
    }

    public void run() {
        int actual = 0;
        boolean runAgain = false;
        while (loops != actual){
            actual ++;
            try {
                doOperation(runAgain);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            runAgain = true;
        }
        System.out.println("Execution time: " + (System.currentTimeMillis() - starttime));
    }

    public void doOperation(boolean runAgain){
        int messagesPerThread = numberOfMessages / numberOfThreads;
        log.info("start loading threads");
        List<PublishThread> threadsOld = Collections.synchronizedList(new ArrayList<>(threads));
        threads = Collections.synchronizedList(new ArrayList<>());
        for (int i = 0; i < numberOfThreads; i++) {
            new InitThreads(threads, i, messagesPerThread, address, runAgain, threadsOld).start();
        }
        addAllMessages();
        while (threads.size() != numberOfThreads) {

        }
        log.info("end loading threads");

        if(starttime == null){
            starttime = System.currentTimeMillis();
        }
        log.info("start publish");
        long starttimeCurrentLoop = System.currentTimeMillis();
        startPublish();
        log.info("publish end");
        log.info("Execution time of one loop: " + (System.currentTimeMillis() - starttimeCurrentLoop));
    }

    private void addAllMessages() {
        int num = 0;
        UUID uuid = UUID.randomUUID();
        for (int i = 0; i < numberOfMessages; i++) {
            num ++;
            messages.add(new Message(basicMessages.get(num - 1), i, uuid.toString()));
            if (num == 10){
                num = 0;
            }
        }
    }

    private void fillBasicMessages() {
        basicMessages.add(
                new Message("{\"battery\":\"99.5\",\"battery_low\":\"false\",\"contact\":\"false\",\"linkquality\":\"22.62\",\"tamper\":\"false\",\"voltage\":\"952.32\"}",
                        "slt/moskevska/arrow/door/door_01_m/rls0124b002342cddc/state", false));
        basicMessages.add(new Message("{\"battery\":\"51.12\",\"battery_low\":\"true\",\"contact\":\"true\",\"linkquality\":\"32.06\",\"tamper\":\"false\",\"voltage\":\"3011.03\"}",
                "slt/moskevska/corridor/door/door_main_m/rlsFCCCCFFFF/state", false));
        basicMessages.add(new Message("{\"battery\":\"97.11\",\"humidity\":\"31.51\",\"linkquality\":\"37.35\",\"temperature\":\"10\",\"voltage\":\"2479.62\"}",
                "slt/moskevska/reception/sensor_temp_json/temp_hum_01_m/rls0124b00239de24c/state", false));
        basicMessages.add(new Message("{\"battery\":\"13.45\",\"humidity\":\"52.42\",\"linkquality\":\"22.12\",\"temperature\":\"40.3\",\"voltage\":\"2721.81\"}",
                "slt/moskevska/sqoop/sensor_temp_json/temp_hum_02_m/rls0124b0023a5270f/state", false));
        basicMessages.add(new Message("{\"co2\":\"5096.81\",\"heat_index\":\"9.44\",\"humidity\":\"51.41\",\"pressure\":\"1001.71\",\"temperature\":\"-2.13\",\"tvoc\":\"6771.39\"}",
                "slt/moskevska/boss/sensor_co2/air_quality_sensor_02_m/rls8ff44ea86ca783f/state", false));
        basicMessages.add(new Message("{\"co2\":\"8072.28\",\"heat_index\":\"30.28\",\"humidity\":\"55.8\",\"pressure\":\"704.57\",\"temperature\":\"30.28\",\"tvoc\":\"5578.84\"}",
                "slt/vrsovicka/crunch/sensor_co2/air_quality_sensor_01_m/rlsFDDDDFFFF/state", false));
        basicMessages.add(new Message("FFFFAAAAFFFF;;2021-08-03 17:03:39;;-11.11;;56.67;;-7.86;;28.9;;",
                "slt/vrsovicka/outside/sensor/temp_2_m/rlsb46e78e0f389/state/FFFFAAAAFFFFB/state", false));
        basicMessages.add(new Message("dev7e2055baf389;;2021-08-03 17:04:39;;30.17;;56.06;;27.88;;39;;",
                "slt/vrsovicka/drill/sensor/temp_1_m/rls7e2055baf389/state", false));
        basicMessages.add(new Message("MOVE_TO_TARGET#29:3",
                "slt/vrsovicka/mainfloor1/agv/develop3/65DB91DEDC52/order", false));
        basicMessages.add(new Message("65DB91DEDC52;;ON_WAY;;Moving;;[20, 13]2021-08-03 at 17:04:28",
                "slt/vrsovicka/mainfloor2/agv/develop3/65DB91DEDC52/state", false));

    }

    private void startPublish() {

        int modulo = numberOfMessages / numberOfThreads;
        messages.forEach(message -> {
            int messagePosition = message.getMessagePosition();
            int actualThreadNumber = messagePosition / modulo;
            if(messagePosition == 0){
                synchronized (this){
                    if(!threads.get(actualThreadNumber).isAlive()){
                        threads.get(actualThreadNumber).start();
                    }
                }
            } else if (messagePosition % modulo == 0) {
                synchronized (this){
                    if(!threads.get(actualThreadNumber).isAlive()) {
                        threads.get(actualThreadNumber).start();
                    }
                }
            }
            threads.get(actualThreadNumber).push(message);
        });
    }

}
