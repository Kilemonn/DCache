package au.kilemonn.dcache.config

import org.junit.jupiter.api.Assertions
import kotlin.jvm.javaClass
import kotlin.test.Test

class CacheConfigurationTest
{
    @Test
    fun testFrom_MissingRequiredValues()
    {
        val map = HashMap<String, Any>()
        Assertions.assertNull(map[CacheConfiguration.TYPE])
        Assertions.assertThrows(MissingPropertyException::class.java) { CacheConfiguration.from("id", map) }

        map[CacheConfiguration.TYPE] = CacheType.IN_MEMORY.toString()
        Assertions.assertNotNull(map[CacheConfiguration.TYPE])
        Assertions.assertNull(map[CacheConfiguration.KEY_CLASS])
        Assertions.assertThrows(MissingPropertyException::class.java) { CacheConfiguration.from("id", map) }

        map[CacheConfiguration.KEY_CLASS] = String.javaClass.name
        Assertions.assertNotNull(map[CacheConfiguration.KEY_CLASS])
        Assertions.assertNull(map[CacheConfiguration.VALUE_CLASS])
        Assertions.assertThrows(MissingPropertyException::class.java) { CacheConfiguration.from("id", map) }

        map[CacheConfiguration.VALUE_CLASS] = String.javaClass.name
        Assertions.assertNotNull(map[CacheConfiguration.VALUE_CLASS])
        CacheConfiguration.from("id", map)
    }

    @Test
    fun testFrom_invalidCacheType()
    {
        val map = HashMap<String, Any>()
        map[CacheConfiguration.KEY_CLASS] = Integer::class.java.name
        map[CacheConfiguration.VALUE_CLASS] = Integer::class.java.name
        map[CacheConfiguration.TYPE] = "Invalid-Cache-Type"

        Assertions.assertThrows(IllegalArgumentException::class.java) { CacheConfiguration.from("testFrom_invalidCacheType", map) }
    }


}
