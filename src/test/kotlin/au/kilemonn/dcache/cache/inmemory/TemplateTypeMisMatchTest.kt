package au.kilemonn.dcache.cache.inmemory

import au.kilemonn.dcache.cache.Cache
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
    private lateinit var wrongKeyCache: Cache<Properties, Properties>

    @Autowired
    @Qualifier("template-types-mismatch")
    private lateinit var wrongValueCache: Cache<String, String>

    @Test
    fun testWrongKey()
    {
        val keyProps = Properties()
        Assertions.assertFalse { wrongKeyCache.getKeyClass().isAssignableFrom(keyProps::class.java) }
        Assertions.assertThrows(InvalidKeyException::class.java) { wrongKeyCache.get(keyProps) }
        Assertions.assertThrows(InvalidKeyException::class.java) { wrongKeyCache.getWithDefault(keyProps, Properties()) }
        Assertions.assertThrows(InvalidKeyException::class.java) { wrongKeyCache.getWithDefault(keyProps) {Properties()} }

        Assertions.assertThrows(InvalidKeyException::class.java) { wrongKeyCache.put(keyProps, Properties()) }
        Assertions.assertThrows(InvalidKeyException::class.java) { wrongKeyCache.putWithExpiry(keyProps, Properties(), Duration.ofSeconds(10)) }
        Assertions.assertThrows(InvalidKeyException::class.java) { wrongKeyCache.putIfAbsent(keyProps, Properties()) }
        Assertions.assertThrows(InvalidKeyException::class.java) { wrongKeyCache.putIfAbsentWithExpiry(keyProps, Properties(), Duration.ofSeconds(10)) }
    }

    @Test
    fun testWrongValue()
    {
        val key = "testWrongValue"
        val value = "wrong-value"
        Assertions.assertTrue { wrongValueCache.getKeyClass().isAssignableFrom(key::class.java) }
        Assertions.assertFalse { wrongValueCache.getValueClass().isAssignableFrom(value::class.java) }
        Assertions.assertNull(wrongValueCache.get(key))

        Assertions.assertThrows(InvalidValueException::class.java) { wrongValueCache.put(key, value) }
        Assertions.assertThrows(InvalidValueException::class.java) { wrongValueCache.putIfAbsent(key, value) }
        Assertions.assertThrows(InvalidValueException::class.java) { wrongValueCache.putWithExpiry(key, value, Duration.ofSeconds(2)) }
        Assertions.assertThrows(InvalidValueException::class.java) { wrongValueCache.putIfAbsentWithExpiry(key, value, Duration.ofSeconds(2)) }
    }
}
