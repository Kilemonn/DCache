package au.kilemonn.dcache.cache.redis

import au.kilemonn.dcache.cache.DCache
import au.kilemonn.dcache.cache.DCacheInitialisationException
import au.kilemonn.dcache.config.CacheConfiguration
import org.springframework.dao.QueryTimeoutException
import org.springframework.data.redis.RedisConnectionFailureException
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
        builder.commandTimeout(Duration.ofMillis(config.getTimeout()))

        val connFactory = LettuceConnectionFactory(initialiseRedisConfiguration(), builder.build())
        connFactory.start()
        template.connectionFactory = connFactory
        template.keySerializer = StringRedisSerializer()
        template.afterPropertiesSet()
    }

    override fun getInternal(key: K): Result<V?>
    {
        return runCatching {
            return Result.success(template.opsForValue().get(key))
        }
    }

    override fun putInternal(key: K, value: V): Result<Boolean>
    {
        return runCatching {
            template.opsForValue().set(key, value)
            return Result.success(true)
        }
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

        // TODO: Handle connection failure
        return template.opsForValue().setIfAbsent(withPrefix(key), value)
    }

    override fun putWithExpiry(key: K, value: V, duration: Duration): Boolean
    {
        val result = put(key, value)
        // TODO: Handle connection failure
        template.expire(withPrefix(key), duration)
        return result
    }

    override fun invalidateInternal(key: K): Result<Unit>
    {
        return runCatching {
            template.opsForValue().getAndDelete(withPrefix(key))
            return Result.success(Unit)
        }
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
