package au.kilemonn.dcache.cache.memcached

import au.kilemonn.dcache.cache.Cache
import au.kilemonn.dcache.cache.CacheTest
import au.kilemonn.dcache.config.CacheConfiguration
import au.kilemonn.dcache.config.CacheType
import au.kilemonn.dcache.config.DCacheConfiguration
import au.kilemonn.dcache.manager.CacheManager
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
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

/**
 * A test for the [MemcachedCache] initialisation and wiring.
 *
 * @author github.com/Kilemonn
 */
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
@ContextConfiguration(initializers = [MemcachedCacheTest.Initializer::class])
@Import(*[DCacheConfiguration::class])
class MemcachedCacheTest
{
    companion object
    {
        private const val MEMCACHED_PORT: Int = 11211
        private const val MEMCACHED_CONTAINER: String = "memcached:1.6.34-alpine3.21"

        lateinit var memcache: GenericContainer<*>

        /**
         * Stop the container at the end of all the tests.
         */
        @AfterAll
        @JvmStatic
        fun afterClass()
        {
            memcache.stop()
        }
    }

    /**
     * The test initialiser for [MemcachedCacheTest] to initialise the container and test properties.
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
            memcache = GenericContainer(DockerImageName.parse(MEMCACHED_CONTAINER))
                .withExposedPorts(MEMCACHED_PORT).withReuse(false)
            memcache.start()

            TestPropertyValues.of(
                "dcache.cache.memcached-cache.endpoint=${memcache.host}",
                "dcache.cache.memcached-cache.port=${memcache.getMappedPort(MEMCACHED_PORT)}",

                "dcache.cache.memcached-cache-with-prefix.endpoint=${memcache.host}",
                "dcache.cache.memcached-cache-with-prefix.port=${memcache.getMappedPort(MEMCACHED_PORT)}"
            ).applyTo(configurableApplicationContext.environment)
        }
    }

    /**
     * Check the container is running before each test.
     */
    @BeforeEach
    fun beforeEach()
    {
        Assertions.assertTrue(memcache.isRunning)
    }

    @Autowired
    @Qualifier("memcached-cache")
    private lateinit var cache: Cache<String, String>

    @Autowired
    @Qualifier("memcached-cache-with-prefix")
    private lateinit var prefixCache: Cache<String, String>

    @Autowired
    private lateinit var manager: CacheManager

    @Test
    fun testConstructor_nonStringKeyClass()
    {
        val keyClass = Integer::class.java.name
        val valueClass = String.javaClass.name
        val config = CacheConfiguration("testConstructor_nonStringKeyClass", CacheType.MEMCACHED, keyClass, valueClass)
        Assertions.assertThrows(IllegalArgumentException::class.java) { config.buildCache() }
    }

    @Test
    fun testConstructor_noEndpoint()
    {
        val keyClassName = String::class.java.name
        val keyClass = Class.forName(keyClassName)
        val valueClass = Integer::class.java.name
        val config = CacheConfiguration("testConstructor_noEndpoint", CacheType.MEMCACHED, keyClassName, valueClass)
        Assertions.assertTrue { String::class.java == keyClass || java.lang.String::class == keyClass }
        Assertions.assertTrue { config.getEndpoint().isBlank() }
        Assertions.assertThrows(IllegalArgumentException::class.java) { config.buildCache() }
    }

    @Test
    fun testManagerWired()
    {
        Assertions.assertEquals(2, manager.size)
    }

    @Test
    fun testGetAndPut()
    {
        val key = "memcached-key"
        val value = "some-value"
        CacheTest.testGetAndPut(key, value, cache)
        CacheTest.testGetAndPut(key, value, prefixCache)
    }

    @Test
    fun testGetWithDefault()
    {
        val key = "testGetWithDefault"
        val value = "testGetWithDefault_value"
        CacheTest.testGetWithDefault(key, value, cache)
        CacheTest.testGetWithDefault(key, value, prefixCache)
    }

    @Test
    fun testGetWithDefaultSupplier()
    {
        val key = "testGetWithDefaultSupplier"
        val value = "testGetWithDefaultSupplier_value"
        CacheTest.testGetWithDefaultSupplier(key, { value }, cache)
        CacheTest.testGetWithDefaultSupplier(key, { value }, prefixCache)
    }

    @Test
    fun testPutIfAbsent()
    {
        val key = "testPutIfAbsent"
        val value = "testPutIfAbsent_value"
        val value2 = "testPutIfAbsent_value2"
        Assertions.assertNotEquals(value, value2)
        CacheTest.testPutIfAbsent(key, value, value2, cache)
        CacheTest.testPutIfAbsent(key, value, value2, prefixCache)
    }

    @Test
    fun testInvalidate()
    {
        val key = "testInvalidate"
        val value = "testInvalidate_value"
        CacheTest.testInvalidate(key, value, cache)
        CacheTest.testInvalidate(key, value, prefixCache)
    }

    @Test
    fun testPutWithExpiry()
    {
        val key = "testPutWithExpiry"
        val value = "testPutWithExpiry_value"
        CacheTest.testPutWithExpiry(key, value, cache)
        CacheTest.testPutWithExpiry(key, value, prefixCache)
    }

    @Test
    fun testPutIfAbsentWithExpiry()
    {
        val key = "testPutIfAbsentWithExpiry"
        val value = "testPutIfAbsentWithExpiry_value"
        val value2 = "testPutIfAbsentWithExpiry_value2"
        CacheTest.testPutIfAbsentWithExpiry(key, value, value2, cache)
        CacheTest.testPutIfAbsentWithExpiry(key, value, value2, prefixCache)
    }
}
