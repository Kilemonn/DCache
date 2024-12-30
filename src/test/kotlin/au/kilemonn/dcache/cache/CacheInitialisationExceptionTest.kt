package au.kilemonn.dcache.cache

import org.junit.jupiter.api.Assertions
import kotlin.test.Test

/**
 * A unit test class for the [CacheInitialisationException] for any specific tests related to this exception.
 *
 * @author github.com/Kilemonn
 */
class CacheInitialisationExceptionTest
{
    @Test
    fun testTypeOfExceptionIsRuntime()
    {
        val e = CacheInitialisationException("id", "reason")
        Assertions.assertTrue(RuntimeException::class.isInstance(e))
    }
}
