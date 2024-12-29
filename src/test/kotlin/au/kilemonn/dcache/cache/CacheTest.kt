package au.kilemonn.dcache.cache

import org.junit.jupiter.api.Assertions
import java.time.Duration
import java.util.function.Supplier

/**
 * Contains different cache tests to share test scenarios between test classes.
 *
 * @author github.com/Kilemonn
 */
class CacheTest
{
    companion object
    {
        private val GRACE_PERIOD = Duration.ofMillis(50)

        /**
         * Ensure the provided key does not exist (has a null value), then after putting it into the cache
         * the value is retrieved and compared with what is provided.
         */
        fun <K, V> testGetAndPut(key: K, value: V, cache: Cache<K, V>)
        {
            Assertions.assertNull(cache.get(key))
            Assertions.assertTrue { cache.put(key, value) }
            Assertions.assertEquals(value, cache.get(key))
        }

        fun <K, V> testGetWithDefault(key: K, value: V, cache: Cache<K, V>)
        {
            Assertions.assertNull(cache.get(key))
            Assertions.assertEquals(value, cache.getWithDefault(key, value))
        }

        fun <K, V> testGetWithDefaultSupplier(key: K, supplier: Supplier<V>, cache: Cache<K, V>)
        {
            Assertions.assertNull(cache.get(key))
            Assertions.assertEquals(supplier.get(), cache.getWithDefault(key, supplier))
        }

        fun <K, V> testPutIfAbsent(key: K, value: V, value2: V, cache: Cache<K, V>)
        {
            Assertions.assertNull(cache.get(key))
            Assertions.assertTrue { cache.putIfAbsent(key, value) }
            Assertions.assertEquals(value, cache.get(key))

            Assertions.assertFalse { cache.putIfAbsent(key, value2) }
            Assertions.assertEquals(value, cache.get(key))
        }

        fun <K, V> testInvalidate(key: K, value: V, cache: Cache<K, V>)
        {
            Assertions.assertNull(cache.get(key))
            Assertions.assertTrue { cache.put(key, value) }
            Assertions.assertEquals(value, cache.get(key))

            cache.invalidate(key)
            Assertions.assertNull(cache.get(key))
        }

        fun <K, V> testPutWithExpiry(key: K, value: V, cache: Cache<K, V>)
        {
            val duration = Duration.ofSeconds(4)
            Assertions.assertNull(cache.get(key))
            Assertions.assertTrue { cache.putWithExpiry(key, value, duration) }

            Assertions.assertEquals(value, cache.get(key))
            Thread.sleep(duration.toMillis() / 2)
            Assertions.assertEquals(value, cache.get(key))

            Assertions.assertTrue { cache.putWithExpiry(key, value, duration) }
            // Wait for the rest of the remaining timer to make sure it is still set
            Thread.sleep((duration.toMillis() / 2) + GRACE_PERIOD.toMillis())
            Assertions.assertEquals(value, cache.get(key))

            Thread.sleep(duration.toMillis() + GRACE_PERIOD.toMillis())
            Assertions.assertNull(cache.get(key))
        }

        fun <K, V> testPutIfAbsentWithExpiry(key: K, value: V, value2: V, cache: Cache<K, V>)
        {
            val duration = Duration.ofSeconds(4)
            Assertions.assertNull(cache.get(key))
            Assertions.assertTrue { cache.putIfAbsentWithExpiry(key, value, duration) }

            Assertions.assertEquals(value, cache.get(key))
            Thread.sleep(duration.toMillis() / 2)
            Assertions.assertEquals(value, cache.get(key))
            Assertions.assertFalse { cache.putIfAbsentWithExpiry(key, value2, duration) }
            Assertions.assertEquals(value, cache.get(key))
            Thread.sleep((duration.toMillis() / 2) + GRACE_PERIOD.toMillis())
            Assertions.assertNull(cache.get(key))
        }
    }
}
