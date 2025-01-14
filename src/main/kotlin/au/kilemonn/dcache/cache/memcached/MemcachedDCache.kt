package au.kilemonn.dcache.cache.memcached

import au.kilemonn.dcache.cache.DCache
import au.kilemonn.dcache.cache.DCacheInitialisationException
import au.kilemonn.dcache.config.CacheConfiguration
import net.rubyeye.xmemcached.MemcachedClient
import net.rubyeye.xmemcached.XMemcachedClientBuilder
import java.io.Serializable
import java.net.InetSocketAddress
import java.time.Duration
import java.util.concurrent.TimeoutException

/**
 * Wraps the [net.rubyeye.xmemcached.XMemcachedClient].
 *
 * @author github.com/Kilemonn
 */
class MemcachedDCache<K, V: Serializable>(keyClass: Class<K>, valueClass: Class<V>, val config: CacheConfiguration<K, V>): DCache<K, V>(keyClass, valueClass)
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
            throw DCacheInitialisationException(config.id, "Only key classes of type \"java.lang.String\" is available for MEMCACHED cache")
        }

        if (config.getEndpoint().isBlank())
        {
            throw DCacheInitialisationException(config.id, "No endpoint provided for MEMCACHED cache.")
        }

        var port = config.getPort()
        if (port == 0)
        {
            port = DEFAULT_MEMCACHED_PORT
        }

        val builder = XMemcachedClientBuilder(listOf(InetSocketAddress.createUnresolved(config.getEndpoint(), port)))
        builder.opTimeout = 2 * 1000
        cache = builder.build()
    }

    override fun getInternal(key: K): V?
    {
        return try
        {
            cache.get(key as String)
        }
        catch (e: TimeoutException)
        {
            null
        }
    }

    override fun putInternal(key: K, value: V): Boolean
    {
        return cache.set(key as String, 0, value)
    }

    override fun putWithExpiry(key: K, value: V, duration: Duration): Boolean
    {
        return cache.set(withPrefix(key) as String, duration.seconds.toInt(), value)
    }

    override fun invalidate(key: K)
    {
        cache.delete(withPrefix(key) as String)
    }

    override fun getPrefix(): String
    {
        return config.getPrefix()
    }

    override fun getCacheName(): String
    {
        return config.id
    }
}
