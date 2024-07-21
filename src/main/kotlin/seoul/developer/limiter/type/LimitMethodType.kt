package seoul.developer.limiter.type

import javax.servlet.http.HttpServletRequest

enum class LimitMethodType(
    val description: String
) {
    NONE("All Access"),
    IP("Client IP Address"),
    SESSION("Server Session"),
    COMPLEX("Complex IP and Session");

    fun findKey(request: HttpServletRequest): String {
        return when (this) {
            NONE -> ""
            IP -> request.remoteAddr
            SESSION -> request.requestedSessionId
            COMPLEX -> "${request.remoteAddr},${request.requestedSessionId}"
        }
    }
}