package seoul.developer.limiter.error

class LimitExceededException(
    private val limitMaxCount: Int
) : RuntimeException() {
    override var message: String? = "Access Limit Exceeded. (${limitMaxCount})"
}