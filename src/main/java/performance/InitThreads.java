
package performance;

import java.util.List;

public class InitThreads extends Thread {

    private final List<PublishThread> threads;
    private final int threadNumber;
    private final int messagesPerTread;
    private final String address;

    private final boolean runAgain;
    private final List<PublishThread> threadsOld;

    public InitThreads(List<PublishThread> threads, int threadNumber,
                       int messagesPerThread, String address, boolean runAgain, List<PublishThread> threadsOld) {
        this.threads = threads;
        this.threadNumber = threadNumber;
        this.messagesPerTread = messagesPerThread;
        this.address = address;
        this.runAgain = runAgain;
        this.threadsOld = threadsOld;
    }

    @Override
    public void run() {
        if(runAgain){
            threads.add(new PublishThread(threadsOld.get(threadNumber).getClient(),
                    threadNumber, messagesPerTread, address));
        }else{
            threads.add(new PublishThread(threadNumber, messagesPerTread, address));
        }

    }
}
