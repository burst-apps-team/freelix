package burst.plotter.pocxor

import burst.common.XorUtil
import burst.kit.crypto.BurstCrypto
import burst.miner.pocxor.MiningPlot
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.parallel.ParallelFlowable
import io.reactivex.schedulers.Schedulers
import java.io.OutputStream
import java.util.function.Supplier

class XorPlotter(private val id: Long) {
    private var burstCrypto = BurstCrypto.getInstance()

    fun plot(output: OutputStream, startNonces: Array<Long>): Completable {
        return ParallelFlowable.fromArray(*startNonces)
                .flatMapSingle { Single.fromCallable { calculatePlotData(it) }.subscribeOn(Schedulers.computation()) }
                .flatMapCompletable { plotData -> Completable.fromAction { output.write(plotData.second, (MiningPlot.SCOOPS_PER_PLOT * MiningPlot.PLOT_SIZE * plotData.first).toInt(), plotData.second.size) }.observeOn(Schedulers.io()) }

    }

    private fun calculatePlotData(startNonce: Long): Pair<Long, ByteArray> {
        println("Calculating plot data for start nonce $startNonce")
        val data = ByteArray(MiningPlot.SCOOPS_PER_PLOT * MiningPlot.PLOT_SIZE)

        for (nonce in 0 until MiningPlot.SCOOPS_PER_PLOT) { // first set is straight copied
            println(nonce + startNonce)
            val plot = MiningPlot(Supplier { burstCrypto.shabal256 }, id, nonce + startNonce, 2)
            for (scoop in 0 until MiningPlot.SCOOPS_PER_PLOT) {
                System.arraycopy(plot.getScoop(scoop), 0, data, nonce * MiningPlot.PLOT_SIZE + scoop * MiningPlot.SCOOP_SIZE, MiningPlot.SCOOP_SIZE)
            }
        }

        println("starting 2nd")

        for (nonce in 0 until MiningPlot.SCOOPS_PER_PLOT) { // second set is nonce + SCOOPS_PER_PLOT and is xored
            println(nonce + MiningPlot.SCOOPS_PER_PLOT + startNonce)
            val plot = MiningPlot(Supplier { burstCrypto.shabal256 }, id, nonce + MiningPlot.SCOOPS_PER_PLOT + startNonce, 2)
            for (scoop in 0 until MiningPlot.SCOOPS_PER_PLOT) {
                XorUtil.xorArray(data, (nonce * MiningPlot.SCOOP_SIZE + scoop * MiningPlot.PLOT_SIZE), plot.getScoop(scoop))
            }
        }

        return Pair(startNonce, data)
    }
}
