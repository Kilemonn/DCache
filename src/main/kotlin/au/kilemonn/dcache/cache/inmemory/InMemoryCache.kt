package au.kilemonn.dcache.cache.inmemory

import au.kilemonn.dcache.cache.Cache
import au.kilemonn.dcache.config.CacheConfiguration
import com.github.benmanes.caffeine.cache.Caffeine
import java.time.Duration
import java.util.function.Supplier

/**
 * Wraps the [Caffeine] cache.
 *
 * @author github.com/Kilemonn
 */
class InMemoryCache<K, V>(keyClass: Class<K>, valueClass: Class<V>, val config: CacheConfiguration) : Cache<K, V>
{
    private var cache: com.github.benmanes.caffeine.cache.Cache<K, V>

    init {
        val builder = Caffeine.newBuilder()
        if (config.getMaxEntries() > 0)
        {
            builder.maximumSize(config.getMaxEntries())
        }
        cache = builder.build()
    }

    override fun get(key: K): V
    {
        return cache.get(withPrefix(key)) { null }
    }

    override fun getWithDefault(key: K, default: V): V
    {
        return cache.get(withPrefix(key)) { default }
    }

    override fun getWithDefault(key: K, defaultSupplier: Supplier<V>): V
    {
        return cache.get(withPrefix(key)) { defaultSupplier.get() }
    }

    override fun put(key: K, value: V): Boolean
    {
        cache.put(withPrefix(key), value)

        // TODO: Return val
        return true
    }

    override fun putIfAbsent(key: K, value: V): Boolean
    {
        get(withPrefix(key))?.let { // Value exists
            return false
        } ?: run { // Value does not exist
            // Don't perform prefix here
            return put(key, value)
        }
    }

    override fun putWithExpiry(key: K, value: V, duration: Duration): Boolean
    {
        val result = put(key, value)

        return result
    }

    override fun putIfAbsentWithExpiry(key: K, value: V, duration: Duration): Boolean
    {
        get(withPrefix(key))?.let { // Value exists
            return false
        } ?: run { // Value does not exist
            // Don't perform prefix here
            return putWithExpiry(key, value, duration)
        }
    }

    override fun invalidate(key: K)
    {
        cache.invalidate(withPrefix(key))
    }

    override fun getPrefix(): String
    {
        return config.getPrefix()
    }
}
