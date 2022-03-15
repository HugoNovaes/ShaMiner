import java.util.concurrent.ThreadFactory;

public class ShaMiner {
    public static void main(String[] args) {
        int numThreads = Runtime.getRuntime().availableProcessors();
        System.out.println("Starting miner with " + numThreads + " threads.");

        for (int i = 0; i < numThreads; i++) {
            Miner miner = new Miner(i);
            Thread thread = new Thread(miner);
            thread.start();
        }

    }

}
