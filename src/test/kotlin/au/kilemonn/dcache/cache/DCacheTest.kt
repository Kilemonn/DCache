package au.kilemonn.dcache.cache

import org.junit.jupiter.api.Assertions
import java.io.Serializable
import java.time.Duration
import java.util.function.Function

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
        fun<K, V: Serializable>testGetAndPut(key: K, value: V, dCache: DCache<K, V>)
        {
            Assertions.assertNull(dCache.get(key))
            Assertions.assertTrue { dCache.put(key, value) }
            Assertions.assertEquals(value, dCache.get(key))
        }

        fun<K, V: Serializable>testGetWithDefault(key: K, value: V, dCache: DCache<K, V>)
        {
            Assertions.assertNull(dCache.get(key))
            Assertions.assertEquals(value, dCache.getWithDefault(key, value))
        }

        fun<K, V: Serializable>testGetWithLoader(key: K, loader: Function<K, V>, dCache: DCache<K, V>)
        {
            Assertions.assertNull(dCache.get(key))
            Assertions.assertEquals(loader.apply(key), dCache.getWithLoader(key, loader, Duration.ZERO))
            Assertions.assertEquals(loader.apply(key), dCache.get(key))
        }

        fun<K, V: Serializable>testPutIfAbsent(key: K, value: V, value2: V, dCache: DCache<K, V>)
        {
            Assertions.assertNull(dCache.get(key))
            Assertions.assertTrue { dCache.putIfAbsent(key, value) }
            Assertions.assertEquals(value, dCache.get(key))

            Assertions.assertFalse { dCache.putIfAbsent(key, value2) }
            Assertions.assertEquals(value, dCache.get(key))
        }

        fun<K, V: Serializable>testInvalidate(key: K, value: V, dCache: DCache<K, V>)
        {
            Assertions.assertNull(dCache.get(key))
            Assertions.assertTrue { dCache.put(key, value) }
            Assertions.assertEquals(value, dCache.get(key))

            dCache.invalidate(key)
            Assertions.assertNull(dCache.get(key))
        }

        fun<K, V: Serializable>testPutWithExpiry(key: K, value: V, dCache: DCache<K, V>)
        {
            val duration = Duration.ofSeconds(4)
            Assertions.assertNull(dCache.get(key))
            Assertions.assertTrue { dCache.putWithExpiry(key, value, duration) }

            Assertions.assertEquals(value, dCache.get(key))
            Thread.sleep(duration.toMillis() / 2)
            Assertions.assertEquals(value, dCache.get(key))

            Assertions.assertTrue { dCache.putWithExpiry(key, value, duration) }
            // Wait for the rest of the remaining timer to make sure it is still set
            Thread.sleep((duration.toMillis() / 2) + GRACE_PERIOD.toMillis())
            Assertions.assertEquals(value, dCache.get(key))

            Thread.sleep(duration.toMillis() + GRACE_PERIOD.toMillis())
            Assertions.assertNull(dCache.get(key))
        }

        fun<K, V: Serializable>testPutIfAbsentWithExpiry(key: K, value: V, value2: V, dCache: DCache<K, V>)
        {
            val duration = Duration.ofSeconds(4)
            Assertions.assertNull(dCache.get(key))
            Assertions.assertTrue { dCache.putIfAbsentWithExpiry(key, value, duration) }

            Assertions.assertEquals(value, dCache.get(key))
            Thread.sleep(duration.toMillis() / 2)
            Assertions.assertEquals(value, dCache.get(key))
            Assertions.assertFalse { dCache.putIfAbsentWithExpiry(key, value2, duration) }
            Assertions.assertEquals(value, dCache.get(key))
            Thread.sleep((duration.toMillis() / 2) + GRACE_PERIOD.toMillis())
            Assertions.assertNull(dCache.get(key))
        }
    }
}
