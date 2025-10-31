package moe.nepnep.repeatbot.config

/**
 * Application configuration values loaded from environment/.env
 */
data class AppConfig(
    val token: String,
    val proxyAddress: String?
)
