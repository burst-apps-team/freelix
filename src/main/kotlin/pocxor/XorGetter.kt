package pocxor

import burst.kit.crypto.BurstCrypto

import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile
import java.util.function.Supplier

class XorGetter(private val id: Long, private val scoop: Int) {
    private var burstCrypto = BurstCrypto.getInstance()

    fun get(): Array<ByteArray?> {
        try {
            RandomAccessFile(java.lang.Long.toUnsignedString(id), "r").use { file ->

                val firstResults = firstHalf(file)
                val secondResults = secondHalf(file)

                val results = arrayOfNulls<ByteArray>(firstResults.size + secondResults.size)
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
    private fun firstHalf(file: RandomAccessFile): Array<ByteArray?> {
        val results = arrayOfNulls<ByteArray>(MiningPlot.SCOOPS_PER_PLOT)
        val plot = MiningPlot(Supplier { burstCrypto.shabal256 }, id, (scoop + MiningPlot.SCOOPS_PER_PLOT).toLong(), 2)
        for (nonce in results.indices) {
            file.seek((nonce * MiningPlot.PLOT_SIZE + scoop * MiningPlot.SCOOP_SIZE).toLong())
            results[nonce] = ByteArray(MiningPlot.SCOOP_SIZE)
            file.read(results[nonce])
            XorUtil.xorArray(results[nonce]!!, 0, plot.getScoop(nonce))
        }
        return results
    }

    @Throws(IOException::class)
    private fun secondHalf(file: RandomAccessFile): Array<ByteArray?> {
        val results = arrayOfNulls<ByteArray>(MiningPlot.SCOOPS_PER_PLOT)
        val plot = MiningPlot(Supplier { burstCrypto.shabal256 }, id, scoop.toLong(), 2)
        file.seek((scoop * MiningPlot.PLOT_SIZE).toLong())
        val data = ByteArray(MiningPlot.PLOT_SIZE)
        file.read(data)
        for (nonce in results.indices) {
            results[nonce] = ByteArray(MiningPlot.SCOOP_SIZE)
            System.arraycopy(data, nonce * MiningPlot.SCOOP_SIZE, results[nonce], 0, MiningPlot.SCOOP_SIZE)
            XorUtil.xorArray(results[nonce]!!, 0, plot.getScoop(nonce))
        }
        return results
    }
}
