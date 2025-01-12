package au.kilemonn.dcache.cache.memcached

import au.kilemonn.dcache.cache.DCache
import au.kilemonn.dcache.cache.DCacheInitialisationException
import au.kilemonn.dcache.cache.DCacheTest
import au.kilemonn.dcache.config.CacheConfiguration
import au.kilemonn.dcache.config.DCacheConfiguration
import au.kilemonn.dcache.config.DCacheType
import au.kilemonn.dcache.container.ContainerTest
import au.kilemonn.dcache.container.MemcachedContainerTest
import au.kilemonn.dcache.manager.DCacheManager
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import java.util.*

/**
 * A test for the [MemcachedDCache] initialisation and wiring.
 *
 * @author github.com/Kilemonn
 */
@ContextConfiguration(initializers = [MemcachedDCacheTest.Initializer::class])
@ExtendWith(SpringExtension::class)
@TestPropertySource(properties = [
    // Endpoint and port are set below
    "dcache.cache.memcached-cache.type=MEMCACHED",
    "dcache.cache.memcached-cache.key_class=java.lang.String",
    "dcache.cache.memcached-cache.value_class=java.lang.String",

    // Create second cache with prefix
    "dcache.cache.memcached-cache-with-prefix.type=MEMCACHED",
    "dcache.cache.memcached-cache-with-prefix.key_class=java.lang.String",
    "dcache.cache.memcached-cache-with-prefix.value_class=java.lang.String",
    "dcache.cache.memcached-cache-with-prefix.prefix=memcache-prefix-"
])
@Import(*[DCacheConfiguration::class])
class MemcachedDCacheTest : MemcachedContainerTest()
{
    /**
     * The test initialiser for [MemcachedDCacheTest] to initialise the container and test properties.
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
                "dcache.cache.memcached-cache.endpoint=${memcacheContainer.host}",
                "dcache.cache.memcached-cache.port=${memcacheContainer.getMappedPort(MEMCACHED_PORT)}",

                "dcache.cache.memcached-cache-with-prefix.endpoint=${memcacheContainer.host}",
                "dcache.cache.memcached-cache-with-prefix.port=${memcacheContainer.getMappedPort(MEMCACHED_PORT)}"
            ).applyTo(configurableApplicationContext.environment)
        }
    }

    /**
     * Check the container is running before each test.
     */
    @BeforeEach
    fun beforeEach()
    {
        Assertions.assertTrue(memcacheContainer.isRunning)
    }

    @Autowired
    @Qualifier("memcached-cache")
    private lateinit var dCache: DCache<String, String>

    @Autowired
    @Qualifier("memcached-cache-with-prefix")
    private lateinit var prefixDCache: DCache<String, String>

    @Autowired
    private lateinit var cacheManager: DCacheManager

    @Test
    fun testConstructor_nonStringKeyClass()
    {
        val keyClass = Integer::class.java
        val valueClass = String::class.java
        val options = HashMap<String, Any>()
        options[CacheConfiguration.ENDPOINT] = "localhost"
        val config = CacheConfiguration("testConstructor_nonStringKeyClass", DCacheType.MEMCACHED, keyClass, valueClass, options)
        Assertions.assertThrows(DCacheInitialisationException::class.java) { config.buildCache() }
    }

    @Test
    fun testConstructor_stringKeyClass()
    {
        val keyClass = String::class.java
        val valueClass = Integer::class.java
        val options = HashMap<String, Any>()
        options[CacheConfiguration.ENDPOINT] = "localhost"
        val config = CacheConfiguration("testConstructor_stringKeyClass", DCacheType.MEMCACHED, keyClass, valueClass, options)
        config.buildCache()
    }

    @Test
    fun testConstructor_javaStringKeyClass()
    {
        val keyClass = java.lang.String::class.java
        val valueClass = Integer::class.java
        val options = HashMap<String, Any>()
        options[CacheConfiguration.ENDPOINT] = "localhost"
        val config = CacheConfiguration("testConstructor_stringKeyClass", DCacheType.MEMCACHED, keyClass, valueClass, options)
        config.buildCache()
    }

    @Test
    fun testConstructor_javaLookedUpStringKeyClass()
    {
        val keyClass = Class.forName("java.lang.String")
        val valueClass = Integer::class.java
        val options = HashMap<String, Any>()
        options[CacheConfiguration.ENDPOINT] = "localhost"
        val config = CacheConfiguration("testConstructor_stringKeyClass", DCacheType.MEMCACHED, keyClass, valueClass, options)
        config.buildCache()
    }

    @Test
    fun testConstructor_javaClassStringKeyClass()
    {
        val keyClass = String.javaClass
        val valueClass = Integer::class.java
        val options = HashMap<String, Any>()
        options[CacheConfiguration.ENDPOINT] = "localhost"
        val config = CacheConfiguration("testConstructor_javaClassStringKeyClass", DCacheType.MEMCACHED, keyClass, valueClass, options)
        Assertions.assertThrows(DCacheInitialisationException::class.java) { config.buildCache() }
    }

    @Test
    fun testConstructor_noEndpoint()
    {
        val keyClass = String::class.java
        val valueClass = Integer::class.java
        val config = CacheConfiguration("testConstructor_noEndpoint", DCacheType.MEMCACHED, keyClass, valueClass, HashMap())
        Assertions.assertTrue { String::class.java == config.keyClass || java.lang.String::class == config.keyClass }
        Assertions.assertTrue { config.getEndpoint().isBlank() }
        Assertions.assertThrows(DCacheInitialisationException::class.java) { config.buildCache() }
    }

    @Test
    fun testManagerWired()
    {
        Assertions.assertEquals(2, cacheManager.size)
    }

    @Test
    fun testGetAndPut()
    {
        val key = "memcached-key"
        val value = "some-value"
        DCacheTest.testGetAndPut(key, value, dCache)
        DCacheTest.testGetAndPut(key, value, prefixDCache)
    }

    @Test
    fun testGetWithDefault()
    {
        val key = "testGetWithDefault"
        val value = "testGetWithDefault_value"
        DCacheTest.testGetWithDefault(key, value, dCache)
        DCacheTest.testGetWithDefault(key, value, prefixDCache)
    }

    @Test
    fun testGetWithDefaultFunction()
    {
        val key = "testGetWithDefaultSupplier"
        val value = "testGetWithDefaultSupplier_value"
        DCacheTest.testGetWithDefaultFunction(key, { value }, dCache)
        DCacheTest.testGetWithDefaultFunction(key, { value }, prefixDCache)
    }

    @Test
    fun testPutIfAbsent()
    {
        val key = "testPutIfAbsent"
        val value = "testPutIfAbsent_value"
        val value2 = "testPutIfAbsent_value2"
        Assertions.assertNotEquals(value, value2)
        DCacheTest.testPutIfAbsent(key, value, value2, dCache)
        DCacheTest.testPutIfAbsent(key, value, value2, prefixDCache)
    }

    @Test
    fun testInvalidate()
    {
        val key = "testInvalidate"
        val value = "testInvalidate_value"
        DCacheTest.testInvalidate(key, value, dCache)
        DCacheTest.testInvalidate(key, value, prefixDCache)
    }

    @Test
    fun testPutWithExpiry()
    {
        val key = "testPutWithExpiry"
        val value = "testPutWithExpiry_value"
        DCacheTest.testPutWithExpiry(key, value, dCache)
        DCacheTest.testPutWithExpiry(key, value, prefixDCache)
    }

    @Test
    fun testPutIfAbsentWithExpiry()
    {
        val key = "testPutIfAbsentWithExpiry"
        val value = "testPutIfAbsentWithExpiry_value"
        val value2 = "testPutIfAbsentWithExpiry_value2"
        DCacheTest.testPutIfAbsentWithExpiry(key, value, value2, dCache)
        DCacheTest.testPutIfAbsentWithExpiry(key, value, value2, prefixDCache)
    }

    @Test
    fun testConstructor_portIsZero()
    {
        val options = HashMap<String, Any>()
        options[CacheConfiguration.ENDPOINT] = "localhost"
        val config = CacheConfiguration("testConstructor_portIsZero", DCacheType.MEMCACHED, String::class.java,
            Properties::class.java, options)

        Assertions.assertTrue { String::class.java == config.keyClass || java.lang.String::class == config.keyClass }
        Assertions.assertTrue { config.getEndpoint().isNotBlank() }
        Assertions.assertEquals(0, config.getPort())
        config.buildCache()
    }
}
