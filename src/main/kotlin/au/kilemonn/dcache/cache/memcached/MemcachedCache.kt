package au.kilemonn.dcache.cache.memcached

import au.kilemonn.dcache.cache.Cache
import au.kilemonn.dcache.cache.redis.RedisCache
import au.kilemonn.dcache.config.CacheConfiguration
import net.rubyeye.xmemcached.MemcachedClient
import net.rubyeye.xmemcached.XMemcachedClientBuilder
import java.net.InetSocketAddress
import java.util.Optional
import kotlin.time.Duration

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
        if (!String::class.java.equals(keyClass) && !java.lang.String::class.equals(keyClass))
        {
            // TODO: Exception type
            throw IllegalArgumentException("Only key class of type \"java.lang.String\" is available for MEMCACHED cache with ID [${config.id}].")
        }

        if (config.getEndpoint().isBlank())
        {
            // TODO: Exception type
            throw IllegalArgumentException("Empty endpoint provided for MEMCACHED cache with ID [${config.id}].")
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

    override fun put(key: K, value: V): Boolean
    {
        return cache.set(withPrefix(key) as String, 0, value)
    }

    override fun putIfAbsent(key: K, value: V): Boolean
    {
        TODO("Not yet implemented")
    }

    override fun putWithExpiry(key: K, value: V, duration: Duration): Boolean
    {
        return cache.set(withPrefix(key) as String, duration.inWholeSeconds.toInt(), value)
    }

    override fun getPrefix(): String
    {
        return config.getPrefix()
    }
}
