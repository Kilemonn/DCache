package au.kilemonn.dcache.cache.redis

import au.kilemonn.dcache.cache.DCache
import au.kilemonn.dcache.cache.DCacheInitialisationException
import au.kilemonn.dcache.cache.DCacheTest
import au.kilemonn.dcache.config.CacheConfiguration
import au.kilemonn.dcache.config.DCacheConfiguration
import au.kilemonn.dcache.config.DCacheType
import au.kilemonn.dcache.container.RedisContainerTest
import au.kilemonn.dcache.manager.DCacheManager
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
import java.util.Properties
import kotlin.test.Test

/**
 * A test for the [RedisDCache] initialisation and wiring.
 *
 * @author github.com/Kilemonn
 */
@ExtendWith(SpringExtension::class)
@TestPropertySource(properties = [
    // Endpoint and port are set below
    "dcache.cache.redis-cache.type=REDIS",
    "dcache.cache.redis-cache.key_class=java.lang.String",
    "dcache.cache.redis-cache.value_class=java.lang.String"])
@ContextConfiguration(initializers = [RedisDCacheTest.Initializer::class])
@Import(*[DCacheConfiguration::class])
class RedisDCacheTest: RedisContainerTest()
{
    /**
     * The test initialiser for [RedisDCacheTest] to initialise the container and test properties.
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
            TestPropertyValues.of(
                "dcache.cache.redis-cache.endpoint=${redisContainer.host}",
                "dcache.cache.redis-cache.port=${redisContainer.getMappedPort(REDIS_PORT)}"
            ).applyTo(configurableApplicationContext.environment)
        }
    }

    /**
     * Check the container is running before each test.
     */
    @BeforeEach
    fun beforeEach()
    {
        Assertions.assertTrue(redisContainer.isRunning)
    }

    @Autowired
    @Qualifier("redis-cache")
    private lateinit var dCache: DCache<String, String>

    @Autowired
    private lateinit var cacheManager: DCacheManager

    @Test
    fun testManagerWired()
    {
        Assertions.assertEquals(1, cacheManager.size)
    }

    @Test
    fun testGetAndPut()
    {
        val key = "redis-key"
        val value = "some-value"
        DCacheTest.testGetAndPut(key, value, dCache)
    }

    @Test
    fun testGetWithDefault()
    {
        val key = "testGetWithDefault"
        val value = "testGetWithDefault_value"
        DCacheTest.testGetWithDefault(key, value, dCache)
    }

    @Test
    fun testGetWithDefaultFunction()
    {
        val key = "testGetWithDefaultFunction"
        val value = "testGetWithDefaultFunction_value"
        DCacheTest.testGetWithDefaultFunction(key, { value }, dCache)
    }

    @Test
    fun testPutIfAbsent()
    {
        val key = "testPutIfAbsent"
        val value = "testPutIfAbsent_value"
        val value2 = "testPutIfAbsent_value2"
        Assertions.assertNotEquals(value, value2)
        DCacheTest.testPutIfAbsent(key, value, value2, dCache)
    }

    @Test
    fun testInvalidate()
    {
        val key = "testInvalidate"
        val value = "testInvalidate_value"
        DCacheTest.testInvalidate(key, value, dCache)
    }

    @Test
    fun testPutWithExpiry()
    {
        val key = "testPutWithExpiry"
        val value = "testPutWithExpiry_value"
        DCacheTest.testPutWithExpiry(key, value, dCache)
    }

    @Test
    fun testPutIfAbsentWithExpiry()
    {
        val key = "testPutIfAbsentWithExpiry"
        val value = "testPutIfAbsentWithExpiry_value"
        val value2 = "testPutIfAbsentWithExpiry_value2"
        DCacheTest.testPutIfAbsentWithExpiry(key, value, value2, dCache)
    }

    @Test
    fun testConstruct_endpointIsEmpty()
    {
        val config = CacheConfiguration("testConstruction_endpointIsEmpty", DCacheType.REDIS, String::class.java,
            Properties::class.java, HashMap())

        Assertions.assertTrue { config.getEndpoint().isBlank() }
        Assertions.assertThrows(DCacheInitialisationException::class.java) { config.buildCache() }
    }

    @Test
    fun testConstructor_portIsZero()
    {
        val options = HashMap<String, Any>()
        options[CacheConfiguration.ENDPOINT] = "localhost"
        val config = CacheConfiguration("testConstructor_portIsZero", DCacheType.REDIS, String::class.java,
        Properties::class.java, options)

        Assertions.assertTrue { config.getEndpoint().isNotBlank() }
        Assertions.assertEquals(0, config.getPort())
        config.buildCache()
    }
}
