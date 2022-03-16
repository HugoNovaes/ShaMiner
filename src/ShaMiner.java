import javax.xml.bind.DatatypeConverter;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ShaMiner {
    public static void main(String[] args) {
        int numThreads = Runtime.getRuntime().availableProcessors();
        System.out.println("Starting miner with " + numThreads + " threads.");

        List<Miner> miners = new ArrayList<>();

        for (int i = 0; i < numThreads; i++) {
            Miner miner = new Miner(i);
            Thread thread = new Thread(miner);
            thread.start();
            miners.add(miner);

            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        TimerTask decreaseTimer = new TimerTask() {
            private LocalTime startTime = LocalTime.of(0, 10, 0);
            @Override
            public void run() {
                startTime = startTime.minusSeconds(1);
                System.out.print("\r" + startTime);

                if (startTime.getMinute() == 0 && startTime.getSecond() == 0) {
                    for (Miner miner : miners) {
                        miner.Stop();
                    }
                    final byte[] hash = Miner.getTargetHash();
                    final byte[] nonce = Miner.getFoundNonce();
                    String strHash = DatatypeConverter.printHexBinary(hash);
                    String strNonce = DatatypeConverter.printHexBinary(nonce);
                    System.out.println("\rBest hash found " + strHash + " with nonce " + strNonce);
                    System.exit(0);
                }
            }
        };

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(decreaseTimer, 1000, 1000);
    }
}
