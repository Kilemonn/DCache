package dcache.cache.redis

import au.kilemonn.dcache.cache.Cache
import au.kilemonn.dcache.config.ContextListener
import au.kilemonn.dcache.manager.CacheManager
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import kotlin.test.Test

/**
 * A test for the [au.kilemonn.dcache.cache.redis.RedisCache] initialisation and wiring.
 *
 * @author github.com/Kilemonn
 */
@ExtendWith(SpringExtension::class)
@TestPropertySource(properties = [
    // Endpoint and port are set below
    "dcache.cache.redis-cache.type=REDIS",
    "dcache.cache.redis-cache.key_class=java.lang.String",
    "dcache.cache.redis-cache.value_class=java.lang.String"])
@ContextConfiguration(initializers = [RedisCacheTest.Initializer::class])
@Import(*[ContextListener::class])
class RedisCacheTest
{
    companion object
    {
        private const val REDIS_PORT: Int = 6379
        private const val REDIS_CONTAINER: String = "redis:7.2.3-alpine"

        lateinit var redis: GenericContainer<*>

        /**
         * Stop the container at the end of all the tests.
         */
        @AfterAll
        @JvmStatic
        fun afterClass()
        {
            redis.stop()
        }
    }

    /**
     * The test initialiser for [RedisCacheTest] to initialise the container and test properties.
     *
     * @author github.com/Kilemonn
     */
    internal class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext>
    {
        /**
         * Force start the container, so we can place its host and dynamic ports into the system properties.
         *
         * Set the environment variables before any of the beans are initialised.
         */
        override fun initialize(configurableApplicationContext: ConfigurableApplicationContext)
        {
            redis = GenericContainer(DockerImageName.parse(REDIS_CONTAINER))
                .withExposedPorts(REDIS_PORT).withReuse(false)
            redis.start()

            TestPropertyValues.of(
                "dcache.cache.redis-cache.endpoint=${redis.host}",
                "dcache.cache.redis-cache.port=${redis.getMappedPort(REDIS_PORT)}"
            ).applyTo(configurableApplicationContext.environment)
        }
    }

    /**
     * Check the container is running before each test.
     */
    @BeforeEach
    fun beforeEach()
    {
        Assertions.assertTrue(redis.isRunning)
    }

    @Autowired
    @Qualifier("redis-cache")
    private lateinit var redisCache: Cache<String, String>

    @Autowired
    private lateinit var manager: CacheManager

    @Test
    fun testRedisGetAndPut()
    {
        Assertions.assertEquals(1, manager.size)

        val key = "redis-key"
        val value = "some-value"

        Assertions.assertNull(redisCache.get(key))

        redisCache.put(key, value)
        Assertions.assertEquals(value, redisCache.get(key))
    }
}
