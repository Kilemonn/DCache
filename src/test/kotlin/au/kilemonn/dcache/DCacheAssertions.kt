package au.kilemonn.dcache

import org.junit.jupiter.api.Assertions

/**
 * Custom assertion functions.
 *
 * @author github.com/Kilemonn
 */
class DCacheAssertions
{
    companion object
    {
        fun <T: Throwable> assertThrowsAny(expectedExceptions: List<Class<T>>, executable: Runnable)
        {
            try
            {
                executable.run()
                Assertions.fail<Void>("Expected one of the exceptions: $expectedExceptions")
            }
            catch (thrownException: Throwable)
            {
                if (expectedExceptions.stream().anyMatch { e -> e.isInstance(thrownException) })
                {
                    return
                }
                Assertions.fail<Void>("Unexpected exception type thrown: " + thrownException.javaClass.getName());
            }
        }
    }
}
