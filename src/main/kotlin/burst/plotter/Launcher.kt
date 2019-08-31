package burst.plotter

import burst.common.Constants
import burst.plotter.pocxor.UnoptimizedXorPlotWriter

object Launcher {
    @JvmStatic
    fun main(args: Array<String>) {
        val id = 7009665667967103287L
        val startNonce = 0L
        val nonceCount = 8192L * 16 // 16GB of nonces, should perform like 32gb
        val path = "./plot"
        val plotWriter = UnoptimizedXorPlotWriter(id)
        println("Starting Burst Plotter " + Constants.VERSION)
        plotWriter.writePlot(id, startNonce, nonceCount, path)
        // Wait forever
        Thread.sleep(Long.MAX_VALUE)
    }
}