package au.kilemonn.dcache.config

import au.kilemonn.dcache.cache.DCache
import au.kilemonn.dcache.cache.inmemory.InMemoryDCache
import au.kilemonn.dcache.cache.memcached.MemcachedDCache
import au.kilemonn.dcache.cache.redis.RedisDCache
import java.io.Serializable
import java.util.Optional

/**
 * Holds the defined configuration of a specific cache. These values are retrieved from the system properties.
 *
 * @author github.com/Kilemonn
 */
class CacheConfiguration<K, V: Serializable>
{
    companion object
    {
        const val TYPE: String = "type"
        const val KEY_CLASS: String = "key_class"
        const val VALUE_CLASS: String = "value_class"
        const val PREFIX: String = "prefix"
        const val ENDPOINT: String = "endpoint"
        const val PORT: String = "port"
        const val FALLBACK: String = "fallback"
        const val TIMEOUT: String = "timeout"

        const val DEFAULT_TIMEOUT: Int = 2 * 1000 // In milliseconds
    }

    val id: String
    val type: DCacheType
    val keyClass: Class<K>
    val valueClass: Class<V>

    private var prefix: String = ""
    private var endpoint: String = ""
    private var port: Int = 0
    private var fallback: String = ""
    private var timeoutMillis: Long = 0 // Milliseconds

    constructor(id: String, type: DCacheType, keyClass: Class<K>, valueClass: Class<V>, options: Map<String, Any>)
    {
        this.id = id
        this.type = type
        this.keyClass = keyClass
        this.valueClass = valueClass

        this.withPrefix(Optional<String>.ofNullable(options[PREFIX]).orElse("").toString())
            .withEndpoint(Optional<String>.ofNullable(options[ENDPOINT]).orElse("").toString())
            .withPort((Optional<Int>.ofNullable(options[PORT]).orElse(0)).toString().toInt())
            .withFallback((Optional<String>.ofNullable(options[FALLBACK]).orElse("")).toString())
            .withTimeout((Optional<Long>.ofNullable(options[TIMEOUT]).orElse(DEFAULT_TIMEOUT)).toString().toLong())
    }

    fun withPrefix(prefix: String): CacheConfiguration<K, V>
    {
        this.prefix = prefix
        return this
    }

    fun getPrefix(): String
    {
        return prefix
    }

    fun withEndpoint(endpoint: String): CacheConfiguration<K, V>
    {
        this.endpoint = endpoint
        return this
    }

    fun getEndpoint(): String
    {
        return endpoint
    }

    fun withPort(port: Int): CacheConfiguration<K, V>
    {
        this.port = port
        return this
    }

    fun getPort(): Int
    {
        return port
    }

    fun withFallback(fallback: String): CacheConfiguration<K, V>
    {
        this.fallback = fallback
        return this
    }

    fun getFallback(): String
    {
        return fallback
    }

    fun withTimeout(timeout: Long): CacheConfiguration<K, V>
    {
        this.timeoutMillis = timeout
        return this
    }

    fun getTimeout(): Long
    {
        return timeoutMillis
    }

    fun buildCache(): DCache<K, V>
    {
        return when(type)
        {
            DCacheType.IN_MEMORY -> InMemoryDCache(keyClass, valueClass, this)
            DCacheType.REDIS -> RedisDCache(keyClass, valueClass, this)
            DCacheType.MEMCACHED -> MemcachedDCache(keyClass, valueClass, this)
        }
    }
}
