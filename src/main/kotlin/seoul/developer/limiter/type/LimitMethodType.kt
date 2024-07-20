package seoul.developer.limiter.type

enum class LimitMethodType(
    val description: String
) {
    NONE("All Access"),
    IP("Client IP Address"),
    SESSION("Server Session"),
    COMPLEX("Complex IP and Session"),
}