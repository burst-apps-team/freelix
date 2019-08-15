package burst.miner.pocxor

import burst.kit.crypto.BurstCrypto

import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile
import java.util.function.Supplier

class XorGetter(private val id: Long, private val scoop: Int, private val file: String, private val startNonce: Long, private val pocVersion: Int) {
    private var burstCrypto = BurstCrypto.getInstance()

    fun get(): Array<Pair<Long, ByteArray>?> {
        try {
            RandomAccessFile(file, "r").use { file ->

                val firstResults = firstHalf(file)
                val secondResults = secondHalf(file)

                val results = arrayOfNulls<Pair<Long, ByteArray>?>(firstResults.size + secondResults.size)
                System.arraycopy(firstResults, 0, results, 0, firstResults.size)
                System.arraycopy(secondResults, 0, results, firstResults.size, secondResults.size)

                return results
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            throw RuntimeException("", e)
        } catch (e: IOException) {
            e.printStackTrace()
            throw RuntimeException("", e)
        }

    }

    @Throws(IOException::class)
    private fun firstHalf(file: RandomAccessFile): Array<Pair<Long, ByteArray>?> {
        val results = arrayOfNulls<Pair<Long, ByteArray>>(MiningPlot.SCOOPS_PER_PLOT)
        val plot = MiningPlot(Supplier { burstCrypto.shabal256 }, id, scoop + MiningPlot.SCOOPS_PER_PLOT + startNonce, pocVersion)
        for (nonce in results.indices) {
            file.seek((nonce * MiningPlot.PLOT_SIZE + scoop * MiningPlot.SCOOP_SIZE).toLong())
            results[nonce] = Pair(nonce.toLong() + startNonce, ByteArray(MiningPlot.SCOOP_SIZE))
            file.read(results[nonce]!!.second)
            XorUtil.xorArray(results[nonce]!!.second, 0, plot.getScoop(nonce))
        }
        return results
    }

    @Throws(IOException::class)
    private fun secondHalf(file: RandomAccessFile): Array<Pair<Long, ByteArray>?> {
        val results = arrayOfNulls<Pair<Long, ByteArray>>(MiningPlot.SCOOPS_PER_PLOT)
        val plot = MiningPlot(Supplier { burstCrypto.shabal256 }, id, scoop.toLong() + startNonce, pocVersion)
        file.seek((scoop * MiningPlot.PLOT_SIZE).toLong())
        val data = ByteArray(MiningPlot.PLOT_SIZE)
        file.read(data)
        for (nonce in results.indices) {
            results[nonce] = Pair(nonce.toLong() + 4096 + startNonce, ByteArray(MiningPlot.SCOOP_SIZE))
            System.arraycopy(data, nonce * MiningPlot.SCOOP_SIZE, results[nonce]!!.second, 0, MiningPlot.SCOOP_SIZE)
            XorUtil.xorArray(results[nonce]!!.second, 0, plot.getScoop(nonce))
        }
        return results
    }
}
