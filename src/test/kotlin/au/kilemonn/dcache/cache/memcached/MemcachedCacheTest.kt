package au.kilemonn.dcache.cache.memcached

import au.kilemonn.dcache.cache.Cache
import au.kilemonn.dcache.config.ContextListener
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
    "dcache.cache.memcached-cache.value_class=java.lang.String"])
@ContextConfiguration(initializers = [MemcachedCacheTest.Initializer::class])
@Import(*[ContextListener::class])
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
                "dcache.cache.memcached-cache.port=${memcache.getMappedPort(MEMCACHED_PORT)}"
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
    private lateinit var memcachedCache: Cache<String, String>

    @Autowired
    private lateinit var manager: CacheManager

    @Test
    fun testMemcachedGetAndPut()
    {
        Assertions.assertEquals(1, manager.size)

        val key = "memcached-key"
        val value = "some-value"

        Assertions.assertNull(memcachedCache.get(key))

        memcachedCache.put(key, value)
        Assertions.assertEquals(value, memcachedCache.get(key))
    }
}
