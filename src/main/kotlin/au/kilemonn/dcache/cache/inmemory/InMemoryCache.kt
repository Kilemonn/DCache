package au.kilemonn.dcache.cache.inmemory

import au.kilemonn.dcache.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import kotlin.time.Duration

/**
 * Wraps the [Caffeine] cache.
 *
 * @author github.com/Kilemonn
 */
class InMemoryCache<K, V>(val keyClass: Class<K>, val valueClass: Class<V>) : Cache<K, V>
{
    private var cache: com.github.benmanes.caffeine.cache.Cache<K, V>
        = Caffeine.newBuilder().build()

    override fun get(key: K): V
    {
        return cache.get(key) { null }
    }

    override fun getWithDefault(key: K, default: V): V
    {
        return cache.get(key) { default }
    }

    override fun put(key: K, value: V): Boolean
    {
        cache.put(key, value)

        // TODO: Return val
        return true
    }

    override fun putIfAbsent(key: K, value: V): Boolean
    {
        TODO("Not yet implemented")
    }

    override fun putWithExpiry(key: K, value: V, duration: Duration): Boolean
    {
        TODO("Not yet implemented")
    }
}
