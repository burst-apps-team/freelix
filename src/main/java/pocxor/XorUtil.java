package pocxor;

public class XorUtil {
    public static void xorArray(byte[] dst, int offset, byte[] other) {
        for (int i = 0; i < other.length; i++) {
            dst[offset + i] ^= other[i];
        }
    }
}
