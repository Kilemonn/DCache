package au.kilemonn.dcache.cache.inmemory

import au.kilemonn.dcache.cache.DCache
import au.kilemonn.dcache.config.CacheConfiguration
import com.github.benmanes.caffeine.cache.Caffeine
import java.io.Serializable
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Wraps the [Caffeine] cache.
 *
 * @author github.com/Kilemonn
 */
class InMemoryDCache<K, V: Serializable>(keyClass: Class<K>, valueClass: Class<V>, val config: CacheConfiguration<K, V>) : DCache<K, V>(keyClass, valueClass)
{
    private val cache: com.github.benmanes.caffeine.cache.Cache<K, V>
    private val expiryCallbacks = ConcurrentHashMap<K, CompletableFuture<Unit>>()

    init {
        val builder = Caffeine.newBuilder()
        if (config.getMaxEntries() > 0)
        {
            builder.maximumSize(config.getMaxEntries())
        }
        if (config.getExpirationFromWrite() > 0)
        {
            builder.expireAfterWrite(config.getExpirationFromWrite(), TimeUnit.SECONDS)
        }
        cache = builder.build()
    }

    override fun getInternal(key: K): Result<V?>
    {
        return Result.success(cache.get(withPrefix(key)) { null })
    }

    override fun putInternal(key: K, value: V): Result<Boolean>
    {
        cache.put(withPrefix(key), value)
        return Result.success(true)
    }

    override fun putWithExpiry(key: K, value: V, duration: Duration): Boolean
    {
        val result = put(key, value)
        expiryCallbacks[key]?.cancel(true)

        val future = CompletableFuture.supplyAsync{ Thread.sleep(duration.toMillis()) }
        expiryCallbacks[key] = future
        future.whenComplete { result, exception ->
            if (exception == null)
            {
                invalidateInternal(key)
            }
            expiryCallbacks.remove(key, future)
        }
        return result
    }

    override fun invalidateInternal(key: K): Result<Unit>
    {
        cache.invalidate(withPrefix(key))
        return Result.success(Unit)
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
