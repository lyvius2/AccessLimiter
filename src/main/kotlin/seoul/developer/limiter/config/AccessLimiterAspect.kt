package seoul.developer.limiter.config

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import seoul.developer.limiter.annotation.AccessLimiter
import seoul.developer.limiter.error.LimitExceededException
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import javax.servlet.http.HttpServletRequest

@Aspect
@Component
class AccessLimiterAspect(
    val redisTemplate: RedisTemplate<String, Any>
) {
    @Around("@annotation(seoul.developer.limiter.annotation.AccessLimiter)")
    fun aroundTargetMethod(pjp: ProceedingJoinPoint): Any? {
        val request: HttpServletRequest = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request
        val accessLimiter = getAnnotation(pjp, AccessLimiter::class.java)
        val limitMethodKey = accessLimiter.limitMethodType.findKey(request)
        val key = "${getKeyPrefix(pjp, accessLimiter)},${limitMethodKey}"

        val accessRecord = getAccessRecord(key, accessLimiter.limitMaxCount)

        if (accessRecord.first) {
            throw LimitExceededException(accessLimiter.limitMaxCount)
        }
        val result = pjp.proceed()

        setAccessRecord(key, accessLimiter)
        return result
    }

    private fun <T : Annotation> getAnnotation(pjp: ProceedingJoinPoint, t: Class<T>): T {
        val methodSignature = pjp.signature as MethodSignature
        return methodSignature.method.getAnnotation(t)
    }

    private fun getKeyPrefix(pjp: ProceedingJoinPoint, accessLimiter: AccessLimiter): String {
        if (accessLimiter.ketPrefix != "") {
            return accessLimiter.ketPrefix
        }
        val methodSignature = pjp.signature as MethodSignature
        return "${accessLimiter.limitMethodType.name},${methodSignature.method.declaringClass.name}.${methodSignature.method.name}"
    }

    private fun getAccessRecord(key: String, limit: Int): Pair<Boolean, Int> {
        try {
            val count = redisTemplate.opsForValue().get(key) ?: return Pair(false, 0)
            return Pair(count as Int >= limit, count)
        } catch (e: Exception) {
            println(e.message)
        }
        return Pair(false, 0)
    }

    private fun setAccessRecord(key: String, accessLimiter: AccessLimiter): Unit {
        val redisOps = redisTemplate.opsForValue()
        val currentCount = redisOps.get(key)
        if (currentCount == null) {
            redisOps.set(key, 1, getTtl(accessLimiter), TimeUnit.MINUTES)
            return
        }
        try {
            redisOps.increment(key)
        } catch (_: Exception) {
            redisOps.getAndDelete(key)
            redisOps.set(key, 1, getTtl(accessLimiter), TimeUnit.MINUTES)
        }
    }

    private fun getTtl(accessLimiter: AccessLimiter): Long {
        if (!accessLimiter.isAutoReset) {
            return accessLimiter.ttlHours * 60L
        }
        val currentDateTime = LocalDateTime.now(ZoneId.of("Asia/Seoul"))
        return ChronoUnit.MINUTES.between(currentDateTime, currentDateTime.plusDays(1).with(LocalTime.MIDNIGHT))
    }
}