package burst.plotter.pocxor

import burst.plotter.PlotWriter
import java.io.RandomAccessFile
import kotlin.system.exitProcess

class UnoptimizedXorPlotWriter : PlotWriter {
    override fun writePlot(id: Long, startNonce: Long, nonceCount: Long, path: String) {
        if (nonceCount % 8192 != 0L) {
            throw IllegalArgumentException("Nonce count must be a multiple of 8192")
        }
        val file = RandomAccessFile(path, "rw")
        val startNonces = mutableListOf<Long>()
        for (i in 0 until nonceCount / 8192) {
            startNonces.add(i * 8192)
        }
        XorPlotter(id).plot(file, startNonces.toTypedArray()).subscribe({
            file.close()
            println("Finished!")
            exitProcess(0)
        }, { it.printStackTrace() })
    }
}