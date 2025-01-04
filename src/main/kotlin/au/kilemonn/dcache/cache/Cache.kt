package au.kilemonn.dcache.cache

import au.kilemonn.dcache.config.CacheConfiguration
import java.time.Duration
import java.util.Optional
import java.util.function.Supplier

/**
 * Top level interface for all [Cache]s.
 *
 * @author github.com/Kilemonn
 */
abstract class Cache<K, V>(private val keyClass: Class<K>, private val valueClass: Class<V>)
{
    fun get(key: K): V?
    {
        ensureKeyType(key)
        return getInternal(withPrefix(key))
    }

    /**
     * Provided with the key already being prefixed. Attempt to retrieve the related value from the underlying storage.
     */
    protected abstract fun getInternal(key: K): V?

    fun getWithDefault(key: K, default: V): V
    {
        return Optional<V>.ofNullable(get(key)).orElse(default)
    }

    fun getWithDefault(key: K, defaultSupplier: Supplier<V>): V
    {
        return Optional<V>.ofNullable(get(key)).orElse(defaultSupplier.get())
    }

    /**
     * Having the key already prefixed, place it into the cache.
     * Returning true if the value was emplaced correctly.
     */
    protected abstract fun putInternal(key: K, value: V): Boolean

    fun put(key: K, value: V): Boolean
    {
        ensureKeyType(key)
        ensureValueType(value)

        return putInternal(withPrefix(key), value)
    }

    open fun putIfAbsent(key: K, value: V): Boolean
    {
        get(key)?.let { // Value exists
            return false
        } ?: run { // Value does not exist
            // Don't perform prefix here
            return put(key, value)
        }
    }

    /**
     * Emplace an entry with expiry.
     *
     * @param duration time from NOW when the entry should be invalidated
     */
    abstract fun putWithExpiry(key: K, value: V, duration: Duration): Boolean

    fun putIfAbsentWithExpiry(key: K, value: V, duration: Duration): Boolean
    {
        get(key)?.let { // Value exists
            return false
        } ?: run { // Value does not exist
            // Don't perform prefix here
            return putWithExpiry(key, value, duration)
        }
    }

    abstract fun invalidate(key: K)

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
    abstract fun getPrefix(): String

    fun getKeyClass(): Class<K>
    {
        return keyClass
    }

    fun getValueClass(): Class<V>
    {
        return valueClass
    }

    fun ensureKeyType(key: K)
    {
        if (!getKeyClass().isAssignableFrom(key!!::class.java))
        {
            throw InvalidKeyException(key::class.java, getKeyClass())
        }
    }

    fun ensureValueType(value: V)
    {
        if (!getValueClass().isAssignableFrom(value!!::class.java))
        {
            throw InvalidValueException(value::class.java, getValueClass())
        }
    }
}
