package burst.plotter

interface PlotWriter {
    fun writePlot(id: Long, startNonce: Long, nonceCount: Long, path: String)
}