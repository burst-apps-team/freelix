import burst.kit.crypto.BurstCrypto;
import burst.miner.pocxor.MiningPlot;
import org.bouncycastle.util.encoders.Hex;

public class Test {
    public static void main(String[] args) {
        System.out.println(Hex.toHexString(new MiningPlot(() -> BurstCrypto.getInstance().getShabal256(), 7009665667967103287L, 16387, 2).getScoop(0)));
    }
}
