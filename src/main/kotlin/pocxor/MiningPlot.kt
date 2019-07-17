package pocxor

import burst.kit.crypto.BurstCrypto
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*
import java.util.function.Supplier
import kotlin.experimental.xor

class MiningPlot(shabal256Supplier: Supplier<MessageDigest>, addr: Long, nonce: Long, pocVersion: Int) {

    private val data = ByteArray(PLOT_TOTAL_SIZE)

    init {
        System.arraycopy(BurstCrypto.getInstance().longToBytes(addr), 0, data, PLOT_SIZE, 8)
        System.arraycopy(BurstCrypto.getInstance().longToBytes(nonce), 0, data, PLOT_SIZE + 8, 8)
        val shabal256 = shabal256Supplier.get()
        var len: Int
        run {
            var i = PLOT_SIZE
            while (i > 0) {
                len = PLOT_TOTAL_SIZE - i
                if (len > HASH_CAP) {
                    len = HASH_CAP
                }
                shabal256.update(data, i, len)
                System.arraycopy(shabal256.digest(), 0, data, i - HASH_SIZE, HASH_SIZE)
                i -= HASH_SIZE
            }
        }
        val finalHash = shabal256.digest(data)
        var i = 0
        var j = 0
        while (i < PLOT_SIZE) {
            if (j == 32) j = 0
            data[i] = (data[i] xor finalHash[j])
            i++
            j++
        }

        // PoC2 Rearrangement
        if (pocVersion == 2) {
            val hashBuffer = ByteArray(HASH_SIZE)
            var revPos = PLOT_SIZE - HASH_SIZE // Start at second hash in last scoop
            var pos = 32
            while (pos < PLOT_SIZE / 2) { // Start at second hash in first scoop
                System.arraycopy(data, pos, hashBuffer, 0, HASH_SIZE) // Copy low scoop second hash to buffer
                System.arraycopy(data, revPos, data, pos, HASH_SIZE) // Copy high scoop second hash to low scoop second hash
                System.arraycopy(hashBuffer, 0, data, revPos, HASH_SIZE) // Copy buffer to high scoop second hash
                revPos -= 64 // move backwards
                pos += 64
            }
        }
    }

    fun getScoop(pos: Int): ByteArray {
        return Arrays.copyOfRange(data, pos * SCOOP_SIZE, (pos + 1) * SCOOP_SIZE)
    }

    fun hashScoop(shabal256: MessageDigest, pos: Int) {
        shabal256.update(data, pos * SCOOP_SIZE, SCOOP_SIZE)
    }

    companion object {
        const val HASH_SIZE = 32
        const val HASHES_PER_SCOOP = 2
        const val SCOOP_SIZE = HASHES_PER_SCOOP * HASH_SIZE
        const val SCOOPS_PER_PLOT = 4096
        val SCOOPS_PER_PLOT_BIGINT = BigInteger.valueOf(SCOOPS_PER_PLOT.toLong())
        const val PLOT_SIZE = SCOOPS_PER_PLOT * SCOOP_SIZE
        const val BASE_LENGTH = 16
        const val PLOT_TOTAL_SIZE = PLOT_SIZE + BASE_LENGTH

        private const val HASH_CAP = 4096
    }
}
