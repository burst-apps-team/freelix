package burst.plotter.pocxor

import burst.common.XorUtil
import burst.kit.crypto.BurstCrypto
import burst.miner.pocxor.MiningPlot
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.parallel.ParallelFlowable
import io.reactivex.schedulers.Schedulers
import java.io.OutputStream
import java.util.function.Supplier
import kotlin.math.roundToLong

class XorPlotter(private val id: Long) {
    private var burstCrypto = BurstCrypto.getInstance()

    fun plot(output: OutputStream, startNonces: Array<Long>): Completable {
        val method = 2
        return when(method) {
            1 -> Flowable.fromArray(*startNonces)
                    .parallel((getFreeMemory() / 1073741824 * 0.6).toInt()) // Use 60% of the available memory
                    .runOn(Schedulers.computation())
                    .map { calculatePlotData(it) }
                    .sequential()
                    .observeOn(Schedulers.io())
                    .flatMapCompletable{ plotData -> Completable.fromAction { output.write(plotData.second, (MiningPlot.SCOOPS_PER_PLOT * MiningPlot.PLOT_SIZE * plotData.first).toInt(), plotData.second.size) } }
            2 -> Observable.fromArray(*startNonces)
                    .flatMapSingle { Single.fromCallable { calculatePlotData(it) }.subscribeOn(Schedulers.computation()) }
                    .flatMapCompletable { plotData -> Completable.fromAction { output.write(plotData.second, (MiningPlot.SCOOPS_PER_PLOT * MiningPlot.PLOT_SIZE * plotData.first).toInt(), plotData.second.size) }.subscribeOn(Schedulers.io()) }
            else -> throw IllegalArgumentException()
        }
    }

    private fun getFreeMemory(): Long {
        val runtime = Runtime.getRuntime()
        return runtime.maxMemory() - (runtime.totalMemory() - runtime.freeMemory())
    }

    private fun calculatePlotData(startNonce: Long): Pair<Long, ByteArray> {
        println("Calculating plot data for start nonce $startNonce")
        val data = ByteArray(MiningPlot.SCOOPS_PER_PLOT * MiningPlot.PLOT_SIZE)

        fun printProgress(currentNonce: Int) {
            // Update every 1%
            if (currentNonce % (MiningPlot.SCOOPS_PER_PLOT / 50 /* scoopsPerPlot*2/100 */) == 0) {
                println("Start nonce $startNonce: ${(currentNonce.toDouble() / (MiningPlot.SCOOPS_PER_PLOT * 2).toDouble() * 100).roundToLong()}% (currentNonce: $currentNonce)")
            }
        }

        for (nonce in 0 until MiningPlot.SCOOPS_PER_PLOT) { // first set is straight copied
            printProgress(nonce)
            val plot = MiningPlot(Supplier { burstCrypto.shabal256 }, id, nonce + startNonce, 2)
            for (scoop in 0 until MiningPlot.SCOOPS_PER_PLOT) {
                System.arraycopy(plot.getScoop(scoop), 0, data, nonce * MiningPlot.PLOT_SIZE + scoop * MiningPlot.SCOOP_SIZE, MiningPlot.SCOOP_SIZE)
            }
        }

        println("starting 2nd")

        for (nonce in 0 until MiningPlot.SCOOPS_PER_PLOT) { // second set is nonce + SCOOPS_PER_PLOT and is xored
            printProgress(nonce + MiningPlot.SCOOPS_PER_PLOT)
            val plot = MiningPlot(Supplier { burstCrypto.shabal256 }, id, nonce + MiningPlot.SCOOPS_PER_PLOT + startNonce, 2)
            for (scoop in 0 until MiningPlot.SCOOPS_PER_PLOT) {
                XorUtil.xorArray(data, (nonce * MiningPlot.SCOOP_SIZE + scoop * MiningPlot.PLOT_SIZE), plot.getScoop(scoop))
            }
        }

        return Pair(startNonce, data)
    }
}
