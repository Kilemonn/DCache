package au.kilemonn.dcache.cache.memcached

import au.kilemonn.dcache.cache.DCache
import au.kilemonn.dcache.cache.DCacheTest
import au.kilemonn.dcache.cache.redis.RedisDCache
import au.kilemonn.dcache.cache.redis.RedisDCacheWithFallbackTest
import au.kilemonn.dcache.config.DCacheConfiguration
import au.kilemonn.dcache.container.MemcachedContainerTest
import au.kilemonn.dcache.container.MemcachedContainerTest.Companion.MEMCACHED_PORT
import au.kilemonn.dcache.container.MemcachedContainerTest.Companion.memcachedContainer
import au.kilemonn.dcache.container.RedisContainerTest.Companion.redisContainer
import au.kilemonn.dcache.manager.DCacheManager
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

/**
 * A test for the [MemcachedDCache] when a fallback is configured.
 *
 * @author github.com/Kilemonn
 */
@ExtendWith(SpringExtension::class)
@TestPropertySource(properties = [
    // Endpoint and port are set below
    "dcache.cache.memcached-cache.type=MEMCACHED",
    "dcache.cache.memcached-cache.key_class=java.lang.String",
    "dcache.cache.memcached-cache.value_class=java.lang.String",
    "dcache.cache.memcached-cache.fallback=fallback-memory",
    "dcache.cache.memcached-cache.timeout=200",

    "dcache.cache.fallback-memory.type=IN_MEMORY",
    "dcache.cache.fallback-memory.key_class=java.lang.String",
    "dcache.cache.fallback-memory.value_class=java.lang.String"])
@ContextConfiguration(initializers = [MemcachedDCacheWithFallbackTest.Initializer::class])
@Import(*[DCacheConfiguration::class])
class MemcachedDCacheWithFallbackTest: MemcachedContainerTest()
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
                "dcache.cache.memcached-cache.endpoint=${memcachedContainer.host}:${memcachedContainer.getMappedPort(MEMCACHED_PORT)}",
            ).applyTo(configurableApplicationContext.environment)
        }
    }

    /**
     * Check the container is running before each test.
     */
    @BeforeEach
    fun beforeEach()
    {
        Assertions.assertTrue(memcachedContainer.isRunning)
        Assertions.assertTrue { dCache.hasFallback() }
    }

    @Autowired
    @Qualifier("memcached-cache")
    private lateinit var dCache: DCache<String, String>

    @Autowired
    @Qualifier("fallback-memory")
    private lateinit var fallbackCache: DCache<String, String>

    @Autowired
    private lateinit var cacheManager: DCacheManager

    @Test
    fun testManagerWired()
    {
        Assertions.assertEquals(2, cacheManager.size)
    }

    @Test
    fun testCacheNames()
    {
        Assertions.assertEquals("memcached-cache", dCache.getCacheName())
        Assertions.assertEquals("fallback-memory", fallbackCache.getCacheName())
    }

    @Test
    fun testFallback_getAndPut()
    {
        val key = "testFallback_getAndPut"
        val value = "testFallback_getAndPut_value"
        whilePaused(memcachedContainer) {
            DCacheTest.testGetAndPut(key, value, dCache)
            Assertions.assertEquals(value, fallbackCache.get(key))
        }
    }

    @Test
    fun testFallback_getWithDefault()
    {
        val key = "testFallback_getWithDefault"
        val value = "testFallback_getWithDefault_value"
        whilePaused(memcachedContainer) {
            DCacheTest.testGetWithDefault(key, value, dCache)
            Assertions.assertNull(fallbackCache.get(key))
        }
    }

    @Test
    fun testFallback_getWithLoader()
    {
        val key = "testFallback_getWithLoader"
        val value = "testFallback_getWithLoader_value"
        whilePaused(memcachedContainer) {
            DCacheTest.testGetWithLoader(key, {k -> value}, dCache)
            Assertions.assertEquals(value, fallbackCache.get(key))
        }
    }

    @Test
    fun testFallback_invalidate()
    {
        val key = "testFallback_invalidate"
        val value = "testFallback_invalidate_value"
        whilePaused(memcachedContainer) {
            DCacheTest.testInvalidate(key, value, dCache)
            Assertions.assertNull(fallbackCache.get(key))
        }
    }

    @Test
    fun testFallback_putIfAbsent()
    {
        val key = "testFallback_putIfAbsent"
        val value = "testFallback_putIfAbsent_value"
        whilePaused(memcachedContainer) {
            DCacheTest.testPutIfAbsent(key, value, value + "2", dCache)
            Assertions.assertEquals(value, fallbackCache.get(key))
        }
    }

    @Test
    fun testFallback_putWithExpiry()
    {
        val key = "testFallback_putWithExpiry"
        val value = "testFallback_putWithExpiry_value"
        whilePaused(memcachedContainer) {
            DCacheTest.testPutWithExpiry(key, value, dCache)
            Assertions.assertNull(fallbackCache.get(key))
        }
    }
}
