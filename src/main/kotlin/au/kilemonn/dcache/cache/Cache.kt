package au.kilemonn.dcache.cache

import kotlin.time.Duration

/**
 * Top level interface for all [Cache]s.
 *
 * @author github.com/Kilemonn
 */
interface Cache<K, V>
{
    fun get(key: K): V?
    fun getWithDefault(key: K, default: V): V

    fun put(key: K, value: V): Boolean
    fun putIfAbsent(key: K, value: V): Boolean
    fun putWithExpiry(key: K, value: V, duration: Duration): Boolean

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
