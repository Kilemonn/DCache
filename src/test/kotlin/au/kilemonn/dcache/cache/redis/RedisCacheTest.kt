package au.kilemonn.dcache.cache.redis

import au.kilemonn.dcache.cache.Cache
import au.kilemonn.dcache.cache.CacheTest
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
 * A test for the [RedisCache] initialisation and wiring.
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
        private const val REDIS_CONTAINER: String = "redis:7.4.1-alpine"

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
    private lateinit var cache: Cache<String, String>

    @Autowired
    private lateinit var manager: CacheManager

    @Test
    fun testManagerWired()
    {
        Assertions.assertEquals(1, manager.size)
    }

    @Test
    fun testGetAndPut()
    {
        val key = "redis-key"
        val value = "some-value"
        CacheTest.testGetAndPut(key, value, cache)
    }

    @Test
    fun testGetWithDefault()
    {
        val key = "testGetWithDefault"
        val value = "testGetWithDefault_value"
        CacheTest.testGetWithDefault(key, value, cache)
    }

    @Test
    fun testGetWithDefaultSupplier()
    {
        val key = "testGetWithDefaultSupplier"
        val value = "testGetWithDefaultSupplier_value"
        CacheTest.testGetWithDefaultSupplier(key, { value }, cache)
    }

    @Test
    fun testPutIfAbsent()
    {
        val key = "testPutIfAbsent"
        val value = "testPutIfAbsent_value"
        val value2 = "testPutIfAbsent_value2"
        Assertions.assertNotEquals(value, value2)
        CacheTest.testPutIfAbsent(key, value, value2, cache)
    }

    @Test
    fun testInvalidate()
    {
        val key = "testInvalidate"
        val value = "testInvalidate_value"
        CacheTest.testInvalidate(key, value, cache)
    }

    @Test
    fun testPutWithExpiry()
    {
        val key = "testPutWithExpiry"
        val value = "testPutWithExpiry_value"
        CacheTest.testPutWithExpiry(key, value, cache)
    }
}
