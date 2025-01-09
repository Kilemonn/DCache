package au.kilemonn.dcache.cache

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.Properties

/**
 * A unit test class for the [InvalidKeyException] for any specific tests related to this exception.
 *
 * @author github.com/Kilemonn
 */
class InvalidKeyExceptionTest
{
    @Test
    fun testTypeOfExceptionIsRuntime()
    {
        val e = InvalidKeyException(Properties::class.java, String::class.java)
        Assertions.assertTrue(RuntimeException::class.isInstance(e))
    }
}
