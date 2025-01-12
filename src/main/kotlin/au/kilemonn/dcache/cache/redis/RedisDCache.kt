package au.kilemonn.dcache.cache.redis

import au.kilemonn.dcache.cache.DCache
import au.kilemonn.dcache.cache.DCacheInitialisationException
import au.kilemonn.dcache.config.CacheConfiguration
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.io.Serializable
import java.time.Duration

/**
 * Wraps the [RedisTemplate].
 *
 * @author github.com/Kilemonn
 */
class RedisDCache<K, V: Serializable>(keyClass: Class<K>, valueClass: Class<V>, val config: CacheConfiguration<K, V>) : DCache<K, V>(keyClass, valueClass)
{
    companion object
    {
        const val REDIS_DEFAULT_PORT: Int = 6379
    }

    private val template: RedisTemplate<K, V> = RedisTemplate()

    init {
        initialiseRedisTemplate()
    }

    private fun initialiseRedisConfiguration(): RedisStandaloneConfiguration
    {
        if (config.getEndpoint().isBlank())
        {
            throw DCacheInitialisationException(config.id, "No endpoint provided for REDIS cache.")
        }
        var port = config.getPort()
        if (port == 0)
        {
            port = REDIS_DEFAULT_PORT
        }

        val redisConfiguration = RedisStandaloneConfiguration()
        redisConfiguration.hostName = config.getEndpoint()
        redisConfiguration.port = port
        return redisConfiguration
    }

    private fun initialiseRedisTemplate()
    {
        val builder = LettuceClientConfiguration.builder()
        builder.commandTimeout(Duration.ofSeconds(2))

        val connFactory = LettuceConnectionFactory(initialiseRedisConfiguration(), builder.build())
        connFactory.start()
        template.connectionFactory = connFactory
        template.keySerializer = StringRedisSerializer()
        template.afterPropertiesSet()
    }

    override fun getInternal(key: K): V?
    {
        return template.opsForValue().get(key)
    }

    override fun putInternal(key: K, value: V): Boolean
    {
        template.opsForValue().set(key, value)
        // TODO: return value
        return true
    }

    /**
     * Overriding, since redis has a proper call for this method.
     *
     * Since this is overriding we need to ensure we call the ensure functions.
     */
    override fun putIfAbsent(key: K, value: V): Boolean
    {
        ensureKeyType(key)
        ensureValueType(value)

        return template.opsForValue().setIfAbsent(withPrefix(key), value)
    }

    override fun putWithExpiry(key: K, value: V, duration: Duration): Boolean
    {
        val result = put(key, value)
        template.expire(withPrefix(key), duration)
        return result
    }

    override fun invalidate(key: K)
    {
        template.opsForValue().getAndDelete(withPrefix(key))
    }

    override fun getPrefix(): String
    {
        return config.getPrefix()
    }
}
