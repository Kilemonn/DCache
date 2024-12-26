package dcache.cache.inmemory

import au.kilemonn.dcache.cache.Cache
import au.kilemonn.dcache.cache.inmemory.InMemoryCache
import au.kilemonn.dcache.config.ContextListener
import au.kilemonn.dcache.manager.CacheManager
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.test.Test

/**
 * A test for the cache initialisation and wiring.
 *
 * @author github.com/Kilemonn
 */
@ExtendWith(SpringExtension::class)
@TestPropertySource(properties = ["dcache.cache.in-mem-name.type=IN_MEMORY",
    "dcache.cache.in-mem-name.key_class=java.lang.String",
    "dcache.cache.in-mem-name.value_class=java.lang.String",

    "dcache.cache.in-mem-name2.type=IN_MEMORY",
    "dcache.cache.in-mem-name2.key_class=java.lang.String",
    "dcache.cache.in-mem-name2.value_class=java.lang.String"])
@Import(*[ContextListener::class])
class InMemoryCacheTest
{
    @Autowired
    @Qualifier("in-mem-name")
    private lateinit var cache: Cache<String, String>

    @Autowired
    @Qualifier("in-mem-name")
    private lateinit var inmem: InMemoryCache<String, String>

    @Autowired
    @Qualifier("in-mem-name2")
    private lateinit var cache2: Cache<String, String>

    @Autowired
    private lateinit var cacheManager: CacheManager

    @Test
    fun testInMemoryCache()
    {
        Assertions.assertEquals(2, cacheManager.size)

        val key = "testInMemoryCache"
        Assertions.assertNull(cache.get(key))
        Assertions.assertNull(inmem.get(key))
        Assertions.assertNull(cache2.get(key))

        val value = "value"
        cache.put(key, value)
        Assertions.assertEquals(value, cache.get(key))
        Assertions.assertEquals(value, inmem.get(key))
        Assertions.assertNull(cache2.get(key))
    }
}
