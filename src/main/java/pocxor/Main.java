package pocxor;

import brs.util.Convert;
import brs.util.MiningPlot;

public class Main {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage:\nplot id\nget id scoop");
            return;
        }

        long id = Convert.parseUnsignedLong(args[1]);

        if (args[0].compareToIgnoreCase("plot") == 0) {
            XorPlotter xorPlotter = new XorPlotter(id);
            xorPlotter.plot();
        } else if (args[0].compareToIgnoreCase("get") == 0) {
            int scoop = args.length < 3 ? 0 : Integer.parseInt(args[2]);
            XorGetter xorGetter = new XorGetter(id, scoop);
            byte[][] results = xorGetter.get();

            for (int nonce = 0; nonce < results.length; nonce++) {
                System.out.println("n" + nonce + "s" + scoop + " " + Convert.toHexString(results[nonce]));
            }
        } else if (args[0].compareToIgnoreCase("test") == 0) { // just for checking results easily
            long nonce = args.length < 3 ? 0 : Long.parseLong(args[2]);
            int scoop = args.length < 4 ? 0 : Integer.parseInt(args[3]);
            MiningPlot miningPlot = new MiningPlot(id, nonce);
            System.out.println("n" + nonce + "s" + scoop + " " + Convert.toHexString(miningPlot.getScoop(scoop)));
        } else {
            System.out.println("Invalid op");
        }
    }
}
