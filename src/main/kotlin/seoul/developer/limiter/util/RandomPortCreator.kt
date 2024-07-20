package seoul.developer.limiter.util

import java.net.ServerSocket
import java.security.SecureRandom

class RandomPortCreator {
    companion object {
        @JvmStatic
        fun getPort(): Int {
            val minimumPort = 1024
            val maximumPort = 65535
            val secureRandom = SecureRandom.getInstanceStrong()

            for (i in 1..(maximumPort - minimumPort)) {
                try {
                    val randomPort = secureRandom.nextInt() * (maximumPort - minimumPort + 1) + minimumPort
                    val serverSocket = ServerSocket(randomPort)
                    serverSocket.close()
                    return randomPort
                } catch (_: Exception) {
                    println("Retry create random port number...")
                }
            }
            return 0
        }
    }
}