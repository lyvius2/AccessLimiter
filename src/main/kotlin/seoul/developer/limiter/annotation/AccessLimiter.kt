package seoul.developer.limiter.annotation

import seoul.developer.limiter.type.LimitMethodType

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class AccessLimiter(
    val limitMethodType: LimitMethodType = LimitMethodType.IP,
    val ketPrefix: String = "",
    val limitMaxCount: Int = 5,
    val isAutoReset: Boolean = false,
    val ttlHours: Long = 24L,
)
