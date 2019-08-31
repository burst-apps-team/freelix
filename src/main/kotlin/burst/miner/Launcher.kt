package burst.miner

import burst.common.Constants
import burst.kit.service.BurstNodeService
import burst.miner.pocxor.UnoptimizedXorPlotReader
import com.google.gson.Gson
import java.io.FileReader

object Launcher {
    @JvmStatic
    fun main(args: Array<String>) {
        val config = Gson().fromJson(FileReader("config.json"), Config::class.java)
        val burstNodeService = BurstNodeService.getCompositeInstanceWithUserAgent(Constants.USERAGENT, *config.nodeAddresses)
        val plotReader = UnoptimizedXorPlotReader(config.plotFiles, 2)
        val burstMiner = BurstMiner(config, burstNodeService, plotReader)
        println("Starting Burst Miner " + Constants.VERSION)
        burstMiner.startMining()
        // Wait forever
        Thread.sleep(Long.MAX_VALUE)
    }
}