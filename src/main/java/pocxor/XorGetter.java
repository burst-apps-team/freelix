package pocxor;

import burst.kit.crypto.BurstCrypto;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class XorGetter {

    BurstCrypto burstCrypto = BurstCrypto.getInstance();
    private final long id;
    private final int scoop;

    public XorGetter(long id, int scoop) {
        this.id = id;
        this.scoop = scoop;
    }

    public byte[][] get() {
        try (RandomAccessFile file = new RandomAccessFile(Long.toUnsignedString(id), "r")) {

            byte[][] firstResults = firstHalf(file);
            byte[][] secondResults = secondHalf(file);

            byte[][] results = new byte[firstResults.length + secondResults.length][];
            System.arraycopy(firstResults, 0, results, 0, firstResults.length);
            System.arraycopy(secondResults, 0, results, firstResults.length, secondResults.length);

            return results;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("", e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("", e);
        }
    }

    private byte[][] firstHalf(RandomAccessFile file) throws IOException {
        byte[][] results = new byte[MiningPlot.SCOOPS_PER_PLOT][];
        MiningPlot plot = new MiningPlot(burstCrypto::getShabal256, id, scoop + MiningPlot.SCOOPS_PER_PLOT, 2);
        for (int nonce = 0; nonce < results.length; nonce++) {
            file.seek((nonce * MiningPlot.PLOT_SIZE) + (scoop * MiningPlot.SCOOP_SIZE));
            results[nonce] = new byte[MiningPlot.SCOOP_SIZE];
            file.read(results[nonce]);
            XorUtil.xorArray(results[nonce], 0, plot.getScoop(nonce));
        }
        return results;
    }

    private byte[][] secondHalf(RandomAccessFile file) throws IOException {
        byte[][] results = new byte[MiningPlot.SCOOPS_PER_PLOT][];
        MiningPlot plot = new MiningPlot(burstCrypto::getShabal256, id, scoop, 2);
        file.seek(scoop * MiningPlot.PLOT_SIZE);
        byte[] data = new byte[MiningPlot.PLOT_SIZE];
        file.read(data);
        for (int nonce = 0; nonce < results.length; nonce++) {
            results[nonce] = new byte[MiningPlot.SCOOP_SIZE];
            System.arraycopy(data, nonce * MiningPlot.SCOOP_SIZE, results[nonce], 0, MiningPlot.SCOOP_SIZE);
            XorUtil.xorArray(results[nonce], 0, plot.getScoop(nonce));
        }
        return results;
    }
}
