package au.kilemonn.dcache.cache.redis

import au.kilemonn.dcache.cache.DCache
import au.kilemonn.dcache.cache.DCacheTest
import au.kilemonn.dcache.config.DCacheConfiguration
import au.kilemonn.dcache.container.RedisContainerTest
import au.kilemonn.dcache.manager.DCacheManager
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
import kotlin.test.Test


/**
 * A test for the [RedisDCache] when a fallback is configured.
 *
 * @author github.com/Kilemonn
 */
@ExtendWith(SpringExtension::class)
@TestPropertySource(properties = [
    // Endpoint and port are set below
    "dcache.cache.redis-cache.type=REDIS",
    "dcache.cache.redis-cache.key_class=java.lang.String",
    "dcache.cache.redis-cache.value_class=java.lang.String",
    "dcache.cache.redis-cache.fallback=fallback-memory",
    "dcache.cache.redis-cache.timeout=200",

    "dcache.cache.fallback-memory.type=IN_MEMORY",
    "dcache.cache.fallback-memory.key_class=java.lang.String",
    "dcache.cache.fallback-memory.value_class=java.lang.String"])
@ContextConfiguration(initializers = [RedisDCacheWithFallbackTest.Initializer::class])
@Import(*[DCacheConfiguration::class])
class RedisDCacheWithFallbackTest: RedisContainerTest()
{
    /**
     * The test initialiser for [RedisDCacheWithFallbackTest] to initialise the container and test properties.
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
                "dcache.cache.redis-cache.endpoint=${redisContainer.host}:${redisContainer.getMappedPort(REDIS_PORT)}",
            ).applyTo(configurableApplicationContext.environment)
        }
    }

    @Autowired
    @Qualifier("redis-cache")
    private lateinit var dCache: DCache<String, String>

    @Autowired
    @Qualifier("fallback-memory")
    private lateinit var fallbackCache: DCache<String, String>

    @Autowired
    private lateinit var cacheManager: DCacheManager

    @BeforeEach
    fun setup()
    {
        Assertions.assertTrue(dCache.hasFallback())
    }

    @Test
    fun testManagerWired()
    {
        Assertions.assertEquals(2, cacheManager.size)
    }

    @Test
    fun testFallback_getAndPut()
    {
        val key = "testFallback_getAndPut"
        val value = "testFallback_getAndPut_value"
        whilePaused(redisContainer) {
            DCacheTest.testGetAndPut(key, value, dCache)
            Assertions.assertEquals(value, fallbackCache.get(key))
        }
    }

    @Test
    fun testFallback_getWithDefault()
    {
        val key = "testFallback_getWithDefault"
        val value = "testFallback_getWithDefault_value"
        whilePaused(redisContainer) {
            DCacheTest.testGetWithDefault(key, value, dCache)
            Assertions.assertNull(fallbackCache.get(key))
        }
    }

    @Test
    fun testFallback_getWithLoader()
    {
        val key = "testFallback_getWithLoader"
        val value = "testFallback_getWithLoader_value"
        whilePaused(redisContainer) {
            DCacheTest.testGetWithLoader(key, {k -> value}, dCache)
            Assertions.assertEquals(value, fallbackCache.get(key))
        }
    }

    @Test
    fun testFallback_invalidate()
    {
        val key = "testFallback_invalidate"
        val value = "testFallback_invalidate_value"
        whilePaused(redisContainer) {
            DCacheTest.testInvalidate(key, value, dCache)
            Assertions.assertNull(fallbackCache.get(key))
        }
    }
}
