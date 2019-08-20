package burst.plotter.pocxor

import burst.miner.pocxor.MiningPlot
import burst.plotter.PlotWriter
import java.io.RandomAccessFile
import kotlin.system.exitProcess

class UnoptimizedXorPlotWriter : PlotWriter {
    override fun writePlot(id: Long, startNonce: Long, nonceCount: Long, path: String) {
        val multipleRequirement = MiningPlot.SCOOPS_PER_PLOT * 2
        if (nonceCount % multipleRequirement != 0L) {
            throw IllegalArgumentException("Nonce count must be a multiple of $multipleRequirement")
        }
        val file = RandomAccessFile(path, "rw")
        val startNonces = mutableListOf<Long>()
        for (i in 0 until nonceCount / multipleRequirement) {
            startNonces.add(i * multipleRequirement)
        }
        XorPlotter(id).plot(file, startNonces.toTypedArray()).subscribe({
            file.close()
            println("Finished!")
            exitProcess(0)
        }, { it.printStackTrace() })
    }
}