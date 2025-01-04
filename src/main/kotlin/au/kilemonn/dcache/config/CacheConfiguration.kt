package au.kilemonn.dcache.config

import au.kilemonn.dcache.cache.Cache
import au.kilemonn.dcache.cache.CacheInitialisationException
import au.kilemonn.dcache.cache.inmemory.InMemoryCache
import au.kilemonn.dcache.cache.memcached.MemcachedCache
import au.kilemonn.dcache.cache.redis.RedisCache
import java.util.Optional


/**
 * Holds the defined configuration of a specific cache. These values are retrieved from the system properties.
 *
 * @author github.com/Kilemonn
 */
class CacheConfiguration<K, V>
{
    companion object
    {
        const val TYPE: String = "type"
        const val KEY_CLASS: String = "key_class"
        const val VALUE_CLASS: String = "value_class"
        const val PREFIX: String = "prefix"
        const val ENDPOINT: String = "endpoint"
        const val PORT: String = "port"
        const val MAX_ENTRIES: String = "max_entries"
        const val EXPIRATION_FROM_WRITE: String = "expiration_from_write"
    }

    val id: String
    val type: CacheType
    private val keyClass: Class<K>
    private val valueClass: Class<V>

    private var prefix: String = ""
    private var endpoint: String = ""
    private var port: Int = 0
    private var maxEntries: Long = 0
    private var expirationFromWrite: Int = 0

    constructor(id: String, type: CacheType, keyClass: Class<K>, valueClass: Class<V>, options: Map<String, Any>)
    {
        this.id = id
        this.type = type
        this.keyClass = keyClass
        this.valueClass = valueClass

        this.withPrefix(Optional<String>.ofNullable(options[PREFIX]).orElse("").toString())
            .withEndpoint(Optional<String>.ofNullable(options[ENDPOINT]).orElse("").toString())
            .withPort((Optional<Int>.ofNullable(options[PORT]).orElse(0)).toString().toInt())
            .withMaxEntries((Optional<Long>.ofNullable(options[MAX_ENTRIES]).orElse(0L)).toString().toLong())
            .withExpirationFromWrite((Optional<Int>.ofNullable(options[EXPIRATION_FROM_WRITE]).orElse(0)).toString().toInt())
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

    fun withMaxEntries(maxEntries: Long): CacheConfiguration<K, V>
    {
        this.maxEntries = maxEntries
        return this
    }

    fun getMaxEntries(): Long
    {
        return maxEntries
    }

    fun withExpirationFromWrite(expirationFromWrite: Int): CacheConfiguration<K, V>
    {
        this.expirationFromWrite = expirationFromWrite
        return this
    }

    fun buildCache(): Cache<K, V>
    {
        return when(type)
        {
            CacheType.IN_MEMORY -> InMemoryCache(keyClass, valueClass, this)
            CacheType.REDIS -> RedisCache(keyClass, valueClass, this)
            CacheType.MEMCACHED -> MemcachedCache(keyClass, valueClass, this)
        }
    }
}
