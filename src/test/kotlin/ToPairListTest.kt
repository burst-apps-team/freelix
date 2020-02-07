import burst.common.toPairList
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ToPairListTest {
    @Test
    fun testToPairList() {
        val data = listOf(1, 2, 3, 4, 5)
        val result = listOf(Pair(1, 2), Pair(3, 4))
        assertEquals(result, data.toPairList())
    }
}