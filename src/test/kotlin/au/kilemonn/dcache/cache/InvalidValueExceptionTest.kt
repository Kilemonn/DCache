package au.kilemonn.dcache.cache

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.Properties

/**
 * A unit test class for the [InvalidValueException] for any specific tests related to this exception.
 *
 * @author github.com/Kilemonn
 */
class InvalidValueExceptionTest
{
    @Test
    fun testTypeOfExceptionIsRuntime()
    {
        val e = InvalidValueException(String::class.java, Properties::class.java)
        Assertions.assertTrue(RuntimeException::class.isInstance(e))
    }
}