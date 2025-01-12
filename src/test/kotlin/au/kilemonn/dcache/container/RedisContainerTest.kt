package au.kilemonn.dcache.container

import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

/**
 * A base class for any tests that needs a running redis instance.
 *
 * @author github.com/Kilemonn
 */
@Testcontainers
abstract class RedisContainerTest : ContainerTest()
{
    companion object
    {
        private const val REDIS_CONTAINER: String = "redis:7.4.1-alpine"

        @JvmStatic
        protected val REDIS_PORT: Int = 6379

        @JvmStatic
        @Container
        protected val redisContainer = GenericContainer(DockerImageName.parse(REDIS_CONTAINER))
            .withExposedPorts(REDIS_PORT).withReuse(false)!!
    }
}
