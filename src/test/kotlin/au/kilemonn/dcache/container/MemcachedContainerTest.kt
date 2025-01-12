package au.kilemonn.dcache.container

import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

/**
 * A base class for any tests that needs a running memcache instance.
 *
 * @author github.com/Kilemonn
 */
@Testcontainers
abstract class MemcachedContainerTest : ContainerTest()
{
    companion object
    {
        private const val MEMCACHED_CONTAINER: String = "memcached:1.6.34-alpine3.21"

        @JvmStatic
        protected val MEMCACHED_PORT: Int = 11211

        @JvmStatic
        @Container
        protected val memcacheContainer = GenericContainer(DockerImageName.parse(MEMCACHED_CONTAINER))
            .withExposedPorts(MEMCACHED_PORT).withReuse(false)!!
    }
}
