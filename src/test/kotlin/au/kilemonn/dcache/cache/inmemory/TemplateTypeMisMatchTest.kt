package au.kilemonn.dcache.cache.inmemory

import au.kilemonn.dcache.cache.DCache
import au.kilemonn.dcache.cache.InvalidKeyException
import au.kilemonn.dcache.cache.InvalidValueException
import au.kilemonn.dcache.config.DCacheConfiguration
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.Duration
import java.util.Properties
import kotlin.test.Test

/**
 * A test to check template types mismatch. And make sure the runtime exceptions are thrown correctly.
 *
 * @author github.com/Kilemonn
 */
@ExtendWith(SpringExtension::class)
@TestPropertySource(properties = [
    "dcache.cache.template-types-mismatch.type=IN_MEMORY",
    "dcache.cache.template-types-mismatch.key_class=java.lang.String",
    "dcache.cache.template-types-mismatch.value_class=java.util.Properties"
])
@Import(*[DCacheConfiguration::class])
class TemplateTypeMisMatchTest
{
    @Autowired
    @Qualifier("template-types-mismatch")
    private lateinit var wrongKeyDCache: DCache<Properties, Properties>

    @Autowired
    @Qualifier("template-types-mismatch")
    private lateinit var wrongValueDCache: DCache<String, String>

    @Test
    fun testWrongKey()
    {
        val keyProps = Properties()
        Assertions.assertFalse { wrongKeyDCache.getKeyClass().isAssignableFrom(keyProps::class.java) }
        Assertions.assertThrows(InvalidKeyException::class.java) { wrongKeyDCache.get(keyProps) }
        Assertions.assertThrows(InvalidKeyException::class.java) { wrongKeyDCache.getWithDefault(keyProps, Properties()) }
        Assertions.assertThrows(InvalidKeyException::class.java) { wrongKeyDCache.getWithDefault(keyProps) {Properties()} }

        Assertions.assertThrows(InvalidKeyException::class.java) { wrongKeyDCache.put(keyProps, Properties()) }
        Assertions.assertThrows(InvalidKeyException::class.java) { wrongKeyDCache.putWithExpiry(keyProps, Properties(), Duration.ofSeconds(10)) }
        Assertions.assertThrows(InvalidKeyException::class.java) { wrongKeyDCache.putIfAbsent(keyProps, Properties()) }
        Assertions.assertThrows(InvalidKeyException::class.java) { wrongKeyDCache.putIfAbsentWithExpiry(keyProps, Properties(), Duration.ofSeconds(10)) }
    }

    @Test
    fun testWrongValue()
    {
        val key = "testWrongValue"
        val value = "wrong-value"
        Assertions.assertTrue { wrongValueDCache.getKeyClass().isAssignableFrom(key::class.java) }
        Assertions.assertFalse { wrongValueDCache.getValueClass().isAssignableFrom(value::class.java) }
        Assertions.assertNull(wrongValueDCache.get(key))

        Assertions.assertThrows(InvalidValueException::class.java) { wrongValueDCache.put(key, value) }
        Assertions.assertThrows(InvalidValueException::class.java) { wrongValueDCache.putIfAbsent(key, value) }
        Assertions.assertThrows(InvalidValueException::class.java) { wrongValueDCache.putWithExpiry(key, value, Duration.ofSeconds(2)) }
        Assertions.assertThrows(InvalidValueException::class.java) { wrongValueDCache.putIfAbsentWithExpiry(key, value, Duration.ofSeconds(2)) }
    }
}
