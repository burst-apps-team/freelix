package burst.miner.pocxor

import burst.common.Util
import burst.kit.crypto.BurstCrypto

import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile
import java.util.function.Supplier

class XorGetter(private val id: Long, private val scoop: Int, private val file: String, private val startSet: Long, private val setCount: Long, private val pocVersion: Int) {
    private var burstCrypto = BurstCrypto.getInstance()

    fun get(): Array<Pair<Long, ByteArray>?> {
        try {
            RandomAccessFile(file, "r").use { file ->

                val results = mutableListOf<Array<Pair<Long, ByteArray>?>>()
                for (i in 0 until setCount) {
                    val firstResults = firstHalf(file, (startSet + i) * 2 * MiningPlot.SCOOPS_PER_PLOT)
                    val secondResults = secondHalf(file, (startSet + i) * 2 * MiningPlot.SCOOPS_PER_PLOT)

                    // TODO this uses too much memory
                    val setResults = arrayOfNulls<Pair<Long, ByteArray>?>(firstResults.size + secondResults.size)
                    System.arraycopy(firstResults, 0, setResults, 0, firstResults.size)
                    System.arraycopy(secondResults, 0, setResults, firstResults.size, secondResults.size)
                    results.add(setResults)
                }

                // TODO ...what?
                return results.toTypedArray().flatten().toTypedArray()
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
    private fun firstHalf(file: RandomAccessFile, startNonce: Long): Array<Pair<Long, ByteArray>?> {
        val results = arrayOfNulls<Pair<Long, ByteArray>>(MiningPlot.SCOOPS_PER_PLOT)
        val plot = MiningPlot(Supplier { burstCrypto.shabal256 }, id, scoop + MiningPlot.SCOOPS_PER_PLOT + startNonce, pocVersion, ByteArray(MiningPlot.PLOT_TOTAL_SIZE))
        for (nonce in results.indices) {
            file.seek(((nonce + startNonce) * MiningPlot.PLOT_SIZE + scoop * MiningPlot.SCOOP_SIZE))
            results[nonce] = Pair(nonce.toLong() + startNonce, ByteArray(MiningPlot.SCOOP_SIZE))
            file.read(results[nonce]!!.second)
            Util.xorArray(results[nonce]!!.second, 0, plot.getScoop(nonce))
        }
        return results
    }

    @Throws(IOException::class)
    private fun secondHalf(file: RandomAccessFile, startNonce: Long): Array<Pair<Long, ByteArray>?> {
        val results = arrayOfNulls<Pair<Long, ByteArray>>(MiningPlot.SCOOPS_PER_PLOT)
        val plot = MiningPlot(Supplier { burstCrypto.shabal256 }, id, scoop.toLong() + startNonce, pocVersion, ByteArray(MiningPlot.PLOT_TOTAL_SIZE))
        file.seek((startNonce * MiningPlot.PLOT_SIZE + scoop * MiningPlot.PLOT_SIZE))
        val data = ByteArray(MiningPlot.PLOT_SIZE)
        file.read(data)
        for (nonce in results.indices) {
            results[nonce] = Pair(nonce.toLong() + 4096 + startNonce, ByteArray(MiningPlot.SCOOP_SIZE))
            System.arraycopy(data, nonce * MiningPlot.SCOOP_SIZE, results[nonce]!!.second, 0, MiningPlot.SCOOP_SIZE)
            Util.xorArray(results[nonce]!!.second, 0, plot.getScoop(nonce))
        }
        return results
    }
}
