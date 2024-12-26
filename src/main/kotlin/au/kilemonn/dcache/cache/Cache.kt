package au.kilemonn.dcache.cache

import java.time.Duration
import java.util.function.Supplier

/**
 * Top level interface for all [Cache]s.
 *
 * @author github.com/Kilemonn
 */
interface Cache<K, V>
{
    fun get(key: K): V?
    fun getWithDefault(key: K, default: V): V
    fun getWithDefault(key: K, defaultSupplier: Supplier<V>): V

    fun put(key: K, value: V): Boolean
    fun putIfAbsent(key: K, value: V): Boolean

    /**
     * Emplace an entry with expiry.
     *
     * @param duration time from NOW when the entry should be invalidated
     */
    fun putWithExpiry(key: K, value: V, duration: Duration): Boolean

    fun putIfAbsentWithExpiry(key: K, value: V, duration: Duration): Boolean

    fun invalidate(key: K)

    /**
     * Add the provided prefix to the key, only if the key is of type [String].
     */
    fun withPrefix(key: K): K
    {
        val prefix = getPrefix()
        if (key is String && prefix.isNotBlank())
        {
            return (getPrefix() + key) as K
        }
        return key
    }

    /**
     * Get the key prefix from the [au.kilemonn.dcache.config.CacheConfiguration].
     */
    fun getPrefix(): String
}
