package burst.miner.pocxor

class MiningPlot {
    companion object {
        const val HASH_SIZE = 32
        const val HASHES_PER_SCOOP = 2
        const val SCOOP_SIZE = HASHES_PER_SCOOP * HASH_SIZE
        const val SCOOPS_PER_PLOT = 4096
        const val PLOT_SIZE = SCOOPS_PER_PLOT * SCOOP_SIZE
        const val BASE_LENGTH = 16
        const val PLOT_TOTAL_SIZE = PLOT_SIZE + BASE_LENGTH

        private const val HASH_CAP = SCOOPS_PER_PLOT
    }
}
