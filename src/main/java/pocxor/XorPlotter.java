package pocxor;

import burst.kit.crypto.BurstCrypto;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class XorPlotter {


    BurstCrypto burstCrypto = BurstCrypto.getInstance();
    private final long id;

    public XorPlotter(long id) {
        this.id = id;
    }

    public void plot() {
        try (FileOutputStream file = new FileOutputStream(Long.toUnsignedString(id), false)) {
            byte[] data = calculatePlotData();

            file.write(data);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] calculatePlotData() {
        byte data[] = new byte[MiningPlot.SCOOPS_PER_PLOT * MiningPlot.PLOT_SIZE];

        for (long nonce = 0; nonce < MiningPlot.SCOOPS_PER_PLOT; nonce++) { // first set is straight copied
            System.out.println(nonce);
            MiningPlot plot = new MiningPlot(burstCrypto::getShabal256, id, nonce, 2);
            for (int scoop = 0; scoop < MiningPlot.SCOOPS_PER_PLOT; scoop++) {
                System.arraycopy(plot.getScoop(scoop), 0, data, (int) ((nonce * MiningPlot.PLOT_SIZE) + (scoop * MiningPlot.SCOOP_SIZE)), MiningPlot.SCOOP_SIZE);
            }
        }

        System.out.println("starting 2nd");

        for (long nonce = 0; nonce < MiningPlot.SCOOPS_PER_PLOT; nonce++) { // second set is nonce + SCOOPS_PER_PLOT and is xored
            MiningPlot plot = new MiningPlot(burstCrypto::getShabal256, id, nonce + MiningPlot.SCOOPS_PER_PLOT, 2);
            for (int scoop = 0; scoop < MiningPlot.SCOOPS_PER_PLOT; scoop++) {
                XorUtil.xorArray(data, (int) ((nonce * MiningPlot.SCOOP_SIZE) + (scoop * MiningPlot.PLOT_SIZE)), plot.getScoop(scoop));
            }
        }

        return data;
    }
}
