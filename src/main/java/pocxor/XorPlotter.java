package pocxor;

import brs.util.Convert;
import brs.util.MiningPlot;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class XorPlotter {

    private final long id;

    public XorPlotter(long id) {
        this.id = id;
    }

    public void plot() {
        try (FileOutputStream file = new FileOutputStream(Convert.toUnsignedLong(id), false)) {
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
            MiningPlot plot = new MiningPlot(id, nonce);
            for (int scoop = 0; scoop < MiningPlot.SCOOPS_PER_PLOT; scoop++) {
                System.arraycopy(plot.getScoop(scoop), 0, data, (int) ((nonce * MiningPlot.PLOT_SIZE) + (scoop * MiningPlot.SCOOP_SIZE)), MiningPlot.SCOOP_SIZE);
            }
        }

        System.out.println("starting 2nd");

        for (long nonce = 0; nonce < MiningPlot.SCOOPS_PER_PLOT; nonce++) { // second set is nonce + SCOOPS_PER_PLOT and is xored
            MiningPlot plot = new MiningPlot(id, nonce + MiningPlot.SCOOPS_PER_PLOT);
            for (int scoop = 0; scoop < MiningPlot.SCOOPS_PER_PLOT; scoop++) {
                XorUtil.xorArray(data, (int) ((nonce * MiningPlot.SCOOP_SIZE) + (scoop * MiningPlot.PLOT_SIZE)), plot.getScoop(scoop));
            }
        }

        return data;
    }
}
