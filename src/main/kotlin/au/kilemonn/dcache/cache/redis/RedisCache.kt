package au.kilemonn.dcache.cache.redis

import au.kilemonn.dcache.cache.Cache
import au.kilemonn.dcache.config.CacheConfiguration
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.util.Optional
import kotlin.time.Duration

class RedisCache<K, V>(val keyClass: Class<K>, val valueClass: Class<V>, val config: CacheConfiguration) : Cache<K, V>
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
            throw IllegalArgumentException("No endpoint provided for REDIS cache with ID [${config.id}].")
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

    private fun initialiseRedisTemplate(): RedisTemplate<K, V>
    {
        val connFactory = LettuceConnectionFactory(initialiseRedisConfiguration())
        connFactory.start()
        template.connectionFactory = connFactory
        template.keySerializer = StringRedisSerializer()
        template.afterPropertiesSet()
        return template
    }

    override fun get(key: K): V?
    {
        return template.opsForValue().get(withPrefix(key))
    }

    override fun getWithDefault(key: K, default: V): V
    {
        return Optional<V>.ofNullable(template.opsForValue().get(withPrefix(key))).orElse(default)
    }

    override fun put(key: K, value: V): Boolean
    {
        template.opsForValue().set(withPrefix(key), value)
        // TODO: return value
        return true
    }

    override fun putIfAbsent(key: K, value: V): Boolean
    {
        return template.opsForValue().setIfAbsent(withPrefix(key), value)
    }

    override fun putWithExpiry(key: K, value: V, duration: Duration): Boolean
    {
        TODO("Not yet implemented")
    }

    override fun withPrefix(key: K): K
    {
        if (key is String)
        {
            return (config.getPrefix() + key) as K
        }
        return key
    }
}
