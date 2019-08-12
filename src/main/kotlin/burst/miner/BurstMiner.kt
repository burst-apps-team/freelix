package burst.miner

import burst.kit.crypto.BurstCrypto
import burst.kit.service.BurstNodeService
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class BurstMiner(private val config: Config, private val burstNodeService: BurstNodeService, private val plotReader: PlotReader) {

    private val burstCrypto = BurstCrypto.getInstance()

    private var currentRoundSearch: Disposable? = null
    private val currentRoundSearchLock = Any()

    init {
        burstNodeService.miningInfo
                .subscribeOn(Schedulers.io())
                .subscribe({ miningInfo -> onNewRound(miningInfo.generationSignature, miningInfo.baseTarget, miningInfo.height) }, { onFetchNewRoundError(it) })
    }

    private fun onNewRound(generationSignature: ByteArray, baseTarget: Long, height: Long) {
        synchronized(currentRoundSearchLock) {
            currentRoundSearch?.dispose()
            val scoop = burstCrypto.calculateScoop(generationSignature, height)
            // We let the plotReader assign its own schedulers as it will likely need both IO and computation schedulers.
            currentRoundSearch = plotReader.fetchBestDeadlines(generationSignature, scoop, baseTarget, config.pocVersion)
                    .subscribe({ onNewDeadlineFound(it) }, { onReadPlotError(it) })
        }
    }

    private fun onNewDeadlineFound(deadline: Deadline) {
        // TODO
        println("Found new best deadline, nonce: ${deadline.nonce}, deadline: ${deadline.deadline}")
    }

    private fun onReadPlotError(error: Throwable) {
        // TODO handle error properly
        error.printStackTrace()
    }

    private fun onFetchNewRoundError(error: Throwable) {
        // TODO handle error properly
        error.printStackTrace()
    }
}