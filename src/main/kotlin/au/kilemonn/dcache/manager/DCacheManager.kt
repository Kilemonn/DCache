package au.kilemonn.dcache.manager

import au.kilemonn.dcache.cache.DCache
import java.io.Serializable
import java.util.Optional

/**
 * The cache manager that holds all instantiated [DCache] instances.
 *
 * @author github.com/Kilemonn
 */
class DCacheManager(private val caches: Map<String, DCache<*,*>>)
{
    val size: Int
        get() = caches.size

    inline fun <reified K, reified V: Serializable> getCache(id: String): Optional<DCache<K, V>> = getCache(id, K::class.java, V::class.java)

    fun <K, V: Serializable> getCache(id: String, keyClass: Class<K>, valueClass: Class<V>): Optional<DCache<K, V>>
    {
        val cache = caches[id]
        if (cache != null && cache.getKeyClass() == keyClass && cache.getValueClass() == valueClass)
        {
            return Optional.of(cache) as Optional<DCache<K, V>>
        }
        return Optional.empty()
    }
}
