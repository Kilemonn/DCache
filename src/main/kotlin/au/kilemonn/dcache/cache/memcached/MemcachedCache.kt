package au.kilemonn.dcache.cache.memcached

import au.kilemonn.dcache.cache.Cache
import au.kilemonn.dcache.cache.CacheInitialisationException
import au.kilemonn.dcache.config.CacheConfiguration
import net.rubyeye.xmemcached.MemcachedClient
import net.rubyeye.xmemcached.XMemcachedClientBuilder
import java.net.InetSocketAddress
import java.time.Duration
import java.util.Optional
import java.util.function.Supplier

/**
 * Wraps the [net.rubyeye.xmemcached.XMemcachedClient].
 *
 * @author github.com/Kilemonn
 */
class MemcachedCache<K, V>(keyClass: Class<K>, valueClass: Class<V>, val config: CacheConfiguration): Cache<K, V>
{
    companion object
    {
        const val DEFAULT_MEMCACHED_PORT: Int = 11211
    }

    private val cache: MemcachedClient

    init {
        // Memcached only supports string keys
        if (String::class.java != keyClass && java.lang.String::class != keyClass)
        {
            throw CacheInitialisationException(config.id, "Only key classes of type \"java.lang.String\" is available for MEMCACHED cache")
        }

        if (config.getEndpoint().isBlank())
        {
            throw CacheInitialisationException(config.id, "No endpoint provided for MEMCACHED cache.")
        }

        var port = config.getPort()
        if (port == 0)
        {
            port = DEFAULT_MEMCACHED_PORT
        }

        val builder = XMemcachedClientBuilder(listOf(InetSocketAddress.createUnresolved(config.getEndpoint(), port)))
        cache = builder.build()
    }

    override fun get(key: K): V?
    {
        return cache.get(withPrefix(key) as String)
    }

    override fun getWithDefault(key: K, default: V): V
    {
        return Optional.ofNullable<V>(cache.get(withPrefix(key) as String)).orElse(default)
    }

    override fun getWithDefault(key: K, defaultSupplier: Supplier<V>): V
    {
        return Optional.ofNullable<V>(cache.get(withPrefix(key) as String)).orElse(defaultSupplier.get())
    }

    override fun put(key: K, value: V): Boolean
    {
        return cache.set(withPrefix(key) as String, 0, value)
    }

    override fun putIfAbsent(key: K, value: V): Boolean
    {
        get(key)?.let { // Value exists
            return false
        } ?: run { // Value does not exist
            // Don't perform prefix here
            return put(key, value)
        }
    }

    override fun putWithExpiry(key: K, value: V, duration: Duration): Boolean
    {
        return cache.set(withPrefix(key) as String, duration.seconds.toInt(), value)
    }

    override fun putIfAbsentWithExpiry(key: K, value: V, duration: Duration): Boolean
    {
        get(key)?.let { // Value exists
            return false
        } ?: run { // Value does not exist
            // Don't perform prefix here
            return putWithExpiry(key, value, duration)
        }
    }

    override fun invalidate(key: K)
    {
        cache.delete(withPrefix(key) as String)
    }

    override fun getPrefix(): String
    {
        return config.getPrefix()
    }
}
