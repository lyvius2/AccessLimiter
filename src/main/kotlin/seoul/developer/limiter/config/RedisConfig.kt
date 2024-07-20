package seoul.developer.limiter.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import redis.embedded.RedisServer
import seoul.developer.limiter.util.RandomPortCreator
import javax.annotation.PreDestroy

@Configuration
class RedisConfig(
    @Value("\${spring.redis.host:}") private val redisHost: String,
    @Value("\${spring.redis.port:0}") private val redisPort: Int,
) {
    private var redisServer: RedisServer? = null

    @Bean
    fun redisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.connectionFactory = redisConnectionFactory
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = GenericJackson2JsonRedisSerializer()
        return template
    }

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        if (redisHost != "" && redisPort != 0) {
            return LettuceConnectionFactory(redisHost, redisPort)
        }
        val port = RandomPortCreator.getPort()
        redisServer = RedisServer(port)
        val redisPorts = redisServer!!.ports()
        if (redisPorts == null || redisPorts.isEmpty()) {
            throw RuntimeException("Embedded Redis Server starting error.")
        }
        return LettuceConnectionFactory("127.0.0.1", port)
    }

    @PreDestroy
    fun destroy() {
        if (redisServer != null) {
            redisServer!!.stop()
        }
    }
}
