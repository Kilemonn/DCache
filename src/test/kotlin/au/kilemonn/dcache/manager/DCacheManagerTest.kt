package au.kilemonn.dcache.manager

import au.kilemonn.dcache.config.DCacheConfiguration
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.Properties
import kotlin.test.Test

/**
 * Tests for the [DCacheManager] and how it can retrieve underlying [au.kilemonn.dcache.cache.DCache] instances.
 *
 * @author github.com/Kilemonn
 */
@TestPropertySource(properties = [
    "dcache.cache.cache.type=IN_MEMORY",
    "dcache.cache.cache.key_class=java.lang.String",
    "dcache.cache.cache.value_class=java.lang.String"])
@ExtendWith(SpringExtension::class)
@Import(*[DCacheConfiguration::class])
class DCacheManagerTest
{
    @Autowired
    private lateinit var cacheManager: DCacheManager

    private val CACHE_NAME = "cache"

    @Test
    fun testGetCache_notFound()
    {
        val cache = cacheManager.getCache<String, String>("testCacheNotFound")
        Assertions.assertTrue { cache.isEmpty }
    }

    @Test
    fun testGetCache_keyIsCorrectValueIsNot()
    {
        val cache = cacheManager.getCache<String, Properties>(CACHE_NAME)
        Assertions.assertTrue { cache.isEmpty }
    }

    @Test
    fun testGetCache_valueIsCorrectKeyIsNot()
    {
        val cache = cacheManager.getCache<Properties, String>(CACHE_NAME)
        Assertions.assertTrue { cache.isEmpty }
    }

    @Test
    fun testGetCache_correctTypes()
    {
        val cache = cacheManager.getCache<String, String>(CACHE_NAME)
        Assertions.assertTrue { cache.isPresent }
    }
}
