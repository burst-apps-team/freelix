package burst.miner.pocxor

import burst.kit.crypto.BurstCrypto

import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.function.Supplier

class XorPlotter(private val id: Long) {
    private var burstCrypto = BurstCrypto.getInstance()

    fun plot() {
        try {
            FileOutputStream(java.lang.Long.toUnsignedString(id), false).use { file ->
                val data = calculatePlotData()

                file.write(data)
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun calculatePlotData(): ByteArray {
        val data = ByteArray(MiningPlot.SCOOPS_PER_PLOT * MiningPlot.PLOT_SIZE)

        for (nonce in 0 until MiningPlot.SCOOPS_PER_PLOT) { // first set is straight copied
            println(nonce)
            val plot = MiningPlot(Supplier { burstCrypto.shabal256 }, id, nonce.toLong(), 2)
            for (scoop in 0 until MiningPlot.SCOOPS_PER_PLOT) {
                System.arraycopy(plot.getScoop(scoop), 0, data, nonce * MiningPlot.PLOT_SIZE + scoop * MiningPlot.SCOOP_SIZE, MiningPlot.SCOOP_SIZE)
            }
        }

        println("starting 2nd")

        for (nonce in 0 until MiningPlot.SCOOPS_PER_PLOT) { // second set is nonce + SCOOPS_PER_PLOT and is xored
            val plot = MiningPlot(Supplier { burstCrypto.shabal256 }, id, (nonce + MiningPlot.SCOOPS_PER_PLOT).toLong(), 2)
            for (scoop in 0 until MiningPlot.SCOOPS_PER_PLOT) {
                XorUtil.xorArray(data, nonce * MiningPlot.SCOOP_SIZE + scoop * MiningPlot.PLOT_SIZE, plot.getScoop(scoop))
            }
        }

        return data
    }
}
