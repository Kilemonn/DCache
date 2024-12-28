package au.kilemonn.dcache.config

import org.junit.jupiter.api.Assertions
import kotlin.test.Test

class ContextListenerTest
{
    private val listener = ContextListener()

    @Test
    fun testGetPropertyID()
    {
        // Empty string
        Assertions.assertEquals("", listener.getPropertyID(""))

        // String with length of atleast the prefix, but not matching the prefix
        val strBuilder = StringBuilder()
        for (i in 0..ContextListener.DCACHE_CONFIG_PREFIX.length*2)
        {
            strBuilder.append("a")
        }
        Assertions.assertTrue(ContextListener.DCACHE_CONFIG_PREFIX.length <= strBuilder.length)
        Assertions.assertEquals("", listener.getPropertyID(strBuilder.toString()))

        // Only the prefix
        Assertions.assertEquals("", listener.getPropertyID(ContextListener.DCACHE_CONFIG_PREFIX))

        // With defined prefix but without a trailing .
        val id = "my-test-id"
        var property = ContextListener.DCACHE_CONFIG_PREFIX + id
        Assertions.assertEquals("", listener.getPropertyID(property))

        // With prefix and with a trialing .
        Assertions.assertEquals(id, listener.getPropertyID("$property."))

        // With prefix and with a trialing . along with property after
        Assertions.assertEquals(id, listener.getPropertyID("$property.another-property"))
    }

    @Test
    fun testGetPropertyForID()
    {
        val id = "testGetPropertyForID"

        // Property length too short
        Assertions.assertEquals("", listener.getPropertyForID("test", id))

        // Long enough length but wrong prefix
        Assertions.assertEquals("", listener.getPropertyForID("My-really-long-and-valid-prefix.test", id))

        // With prefix and no property
        Assertions.assertEquals("", listener.getPropertyForID(ContextListener.DCACHE_CONFIG_PREFIX + id, id))
        // With prefix with "." and no property
        Assertions.assertEquals("", listener.getPropertyForID(ContextListener.DCACHE_CONFIG_PREFIX + id + ".", id))

        // With prefix and property
        val propertyName = "property-name"
        Assertions.assertEquals(propertyName, listener.getPropertyForID(
            ContextListener.DCACHE_CONFIG_PREFIX + id + "." + propertyName, id))
    }

    @Test
    fun testGetCacheIdsAndPropertiesMap_emptyMap()
    {
        val props = listener.getCacheIdsAndPropertiesMap(HashMap<String, Any>())
        Assertions.assertTrue { props.isEmpty() }
    }

    @Test
    fun testGetCacheIdsAndPropertiesMap()
    {
        val properties = mapOf(
            // No property but correct prefix
            Pair(ContextListener.DCACHE_CONFIG_PREFIX + "cache1", "correct-prefix"),

            // No property but with "." and correct prefix
            Pair(ContextListener.DCACHE_CONFIG_PREFIX + "cache1.", "with-period-delimiter"),

            // Correct property
            Pair(ContextListener.DCACHE_CONFIG_PREFIX + "cache1.property1", "value1"),

            // Long enough prefix in correct format but invalid prefix
            Pair("long-enough-prefix.invalid-id.property", "test"),

            // Too short, wrong prefix
            Pair("short", "short"),
        )

        val props = listener.getCacheIdsAndPropertiesMap(properties)
        Assertions.assertEquals(1, props.size)
        Assertions.assertNotNull(props["cache1"])
        Assertions.assertTrue(props["cache1"]!!.contains("property1"))
        Assertions.assertEquals("value1", props["cache1"]!!["property1"])
    }
}
