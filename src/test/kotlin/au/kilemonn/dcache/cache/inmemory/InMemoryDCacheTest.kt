package au.kilemonn.dcache.cache.inmemory

import au.kilemonn.dcache.cache.DCache
import au.kilemonn.dcache.cache.DCacheTest
import au.kilemonn.dcache.config.DCacheConfiguration
import au.kilemonn.dcache.manager.DCacheManager
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
@TestPropertySource(properties = [
    "dcache.cache.in-mem-name.type=IN_MEMORY",
    "dcache.cache.in-mem-name.key_class=java.lang.String",
    "dcache.cache.in-mem-name.value_class=java.lang.String",

    "dcache.cache.in-mem-name2.type=IN_MEMORY",
    "dcache.cache.in-mem-name2.key_class=java.lang.String",
    "dcache.cache.in-mem-name2.value_class=java.lang.String"])
@Import(*[DCacheConfiguration::class])
class InMemoryDCacheTest
{
    @Autowired
    @Qualifier("in-mem-name")
    private lateinit var dCache: DCache<String, String>

    @Autowired
    @Qualifier("in-mem-name")
    private lateinit var inMemoryDCache: InMemoryDCache<String, String>

    @Autowired
    @Qualifier("in-mem-name2")
    private lateinit var dCache2: DCache<String, String>

    @Autowired
    private lateinit var dCacheManager: DCacheManager

    @Test
    fun testCacheNames()
    {
        Assertions.assertEquals("in-mem-name", dCache.getCacheName())
        Assertions.assertEquals("in-mem-name", inMemoryDCache.getCacheName())
        Assertions.assertEquals("in-mem-name2", dCache2.getCacheName())
    }

    @Test
    fun testInMemoryCacheMultipleReferences()
    {
        Assertions.assertEquals(2, dCacheManager.size)

        val key = "testInMemoryCache"
        Assertions.assertNull(dCache.get(key))
        Assertions.assertNull(inMemoryDCache.get(key))
        Assertions.assertNull((dCacheManager.getCache<String, String>("in-mem-name").get()).get(key))
        Assertions.assertNull(dCache2.get(key))

        val value = "value"
        dCache.put(key, value)
        Assertions.assertEquals(value, dCache.get(key))
        Assertions.assertEquals(value, inMemoryDCache.get(key))
        Assertions.assertEquals(value, (dCacheManager.getCache<String, String>("in-mem-name").get()).get(key))
        Assertions.assertNull(dCache2.get(key))
    }

    @Test
    fun testGetAndPut()
    {
        val key = "test_in_memory"
        val value = "in_memory_value"
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
    fun testGetWithLoader()
    {
        val key = "testGetWithDefaultSupplier"
        val value = "testGetWithDefaultSupplier_value"
        DCacheTest.testGetWithLoader(key, { value }, dCache)
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
}
