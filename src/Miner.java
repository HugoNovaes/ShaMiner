import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalTime;

public class Miner implements Runnable {

    private final int minerID;
    private Thread thisThread;
    private static byte[] foundNonce = null;
    private static byte[] targetHash = {
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};

    public Miner(int minerID) {
        this.minerID = minerID + 1;
    }

    public static byte[] getTargetHash() {
        synchronized (Miner.class) {
            return Miner.targetHash;
        }
    }

    public static byte[] getFoundNonce(){
        synchronized ( Miner.class) {
            return Miner.foundNonce;
        }
    }

    public void Stop() {
        this.thisThread.interrupt();
    }

    private int compare(byte[] b1, byte[] b2) {

        if (b1 == null && b2 == null) return 0;
        if (b1 == null) return -1;
        if (b2 == null) return 1;

        int minLen = Math.min(b1.length, b2.length);
        for (int i = 0; i < minLen; i++) {
            int byte1 = b1[i] & 0xFF;
            int byte2 = b2[i] & 0xFF;
            if (byte1 < byte2) return -1;
            if (byte1 > byte2) return 1;
        }

        return 0;
    }

    private boolean setTargetHash(byte[] hash, byte[] nonce) {
        int cmp = this.compare(Miner.targetHash, hash);
        if (cmp == 1) {
            synchronized (Miner.class) {
                if (this.compare(Miner.targetHash, hash) <= 0) return false;
                Miner.targetHash = new byte[hash.length];
                Miner.foundNonce = new byte[nonce.length];
                System.arraycopy(hash, 0, Miner.targetHash, 0, hash.length);
                System.arraycopy(nonce, 0, Miner.foundNonce, 0, nonce.length);
            }
            return true;
        }
        return false;
    }

    private static byte[] longToBytes(long l) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (l & 0xFF);
            l >>= 8;
        }
        return result;
    }

    @Override
    public void run() {
        System.out.println("Miner " + this.minerID + " started!");
        this.thisThread = Thread.currentThread();

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] nonce = new byte[16];
            SecureRandom rnd = new SecureRandom();
            long nonceNum = (this.minerID + 1) * System.currentTimeMillis();
            final byte[] bytes = longToBytes(nonceNum);
            System.arraycopy(bytes, 0, nonce, 8, bytes.length);

            while (this.thisThread.isAlive()) {
                byte[] hash = messageDigest.digest(nonce);
                messageDigest.reset();
                if (this.setTargetHash(hash, nonce)) {
                    String strHash = DatatypeConverter.printHexBinary(hash);
                    String strNonce = DatatypeConverter.printHexBinary(nonce);
                    System.out.println("\r" + LocalTime.now() +
                            " Hash:" + strHash + " Nonce:" + strNonce + " MinerID: " + this.minerID);
                }
                rnd.nextBytes(nonce);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
