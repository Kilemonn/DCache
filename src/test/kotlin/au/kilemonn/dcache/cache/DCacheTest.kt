package au.kilemonn.dcache.cache

import org.junit.jupiter.api.Assertions
import java.time.Duration
import java.util.function.Supplier

/**
 * Contains different cache tests to share test scenarios between test classes.
 *
 * @author github.com/Kilemonn
 */
class DCacheTest
{
    companion object
    {
        private val GRACE_PERIOD = Duration.ofMillis(50)

        /**
         * Ensure the provided key does not exist (has a null value), then after putting it into the cache
         * the value is retrieved and compared with what is provided.
         */
        fun <K, V> testGetAndPut(key: K, value: V, DCache: DCache<K, V>)
        {
            Assertions.assertNull(DCache.get(key))
            Assertions.assertTrue { DCache.put(key, value) }
            Assertions.assertEquals(value, DCache.get(key))
        }

        fun <K, V> testGetWithDefault(key: K, value: V, DCache: DCache<K, V>)
        {
            Assertions.assertNull(DCache.get(key))
            Assertions.assertEquals(value, DCache.getWithDefault(key, value))
        }

        fun <K, V> testGetWithDefaultSupplier(key: K, supplier: Supplier<V>, DCache: DCache<K, V>)
        {
            Assertions.assertNull(DCache.get(key))
            Assertions.assertEquals(supplier.get(), DCache.getWithDefault(key, supplier))
        }

        fun <K, V> testPutIfAbsent(key: K, value: V, value2: V, DCache: DCache<K, V>)
        {
            Assertions.assertNull(DCache.get(key))
            Assertions.assertTrue { DCache.putIfAbsent(key, value) }
            Assertions.assertEquals(value, DCache.get(key))

            Assertions.assertFalse { DCache.putIfAbsent(key, value2) }
            Assertions.assertEquals(value, DCache.get(key))
        }

        fun <K, V> testInvalidate(key: K, value: V, DCache: DCache<K, V>)
        {
            Assertions.assertNull(DCache.get(key))
            Assertions.assertTrue { DCache.put(key, value) }
            Assertions.assertEquals(value, DCache.get(key))

            DCache.invalidate(key)
            Assertions.assertNull(DCache.get(key))
        }

        fun <K, V> testPutWithExpiry(key: K, value: V, DCache: DCache<K, V>)
        {
            val duration = Duration.ofSeconds(4)
            Assertions.assertNull(DCache.get(key))
            Assertions.assertTrue { DCache.putWithExpiry(key, value, duration) }

            Assertions.assertEquals(value, DCache.get(key))
            Thread.sleep(duration.toMillis() / 2)
            Assertions.assertEquals(value, DCache.get(key))

            Assertions.assertTrue { DCache.putWithExpiry(key, value, duration) }
            // Wait for the rest of the remaining timer to make sure it is still set
            Thread.sleep((duration.toMillis() / 2) + GRACE_PERIOD.toMillis())
            Assertions.assertEquals(value, DCache.get(key))

            Thread.sleep(duration.toMillis() + GRACE_PERIOD.toMillis())
            Assertions.assertNull(DCache.get(key))
        }

        fun <K, V> testPutIfAbsentWithExpiry(key: K, value: V, value2: V, DCache: DCache<K, V>)
        {
            val duration = Duration.ofSeconds(4)
            Assertions.assertNull(DCache.get(key))
            Assertions.assertTrue { DCache.putIfAbsentWithExpiry(key, value, duration) }

            Assertions.assertEquals(value, DCache.get(key))
            Thread.sleep(duration.toMillis() / 2)
            Assertions.assertEquals(value, DCache.get(key))
            Assertions.assertFalse { DCache.putIfAbsentWithExpiry(key, value2, duration) }
            Assertions.assertEquals(value, DCache.get(key))
            Thread.sleep((duration.toMillis() / 2) + GRACE_PERIOD.toMillis())
            Assertions.assertNull(DCache.get(key))
        }
    }
}
