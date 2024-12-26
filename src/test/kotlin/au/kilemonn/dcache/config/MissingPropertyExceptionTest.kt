package au.kilemonn.dcache.config

import org.junit.jupiter.api.Assertions
import kotlin.test.Test


/**
 * A unit test class for the [MissingPropertyExceptionTest] for any specific tests related to this exception.
 *
 * @author github.com/Kilemonn
 */
class MissingPropertyExceptionTest
{
    /**
     * Ensure that [MissingPropertyExceptionTest] is a type of [RuntimeException].
     * Incase this is changed in the future.
     */
    @Test
    fun testTypeOfExceptionIsRuntime()
    {
        val e = MissingPropertyException("id", "property")
        Assertions.assertTrue(RuntimeException::class.isInstance(e))
    }
}
