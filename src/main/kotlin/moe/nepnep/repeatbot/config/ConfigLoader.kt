package moe.nepnep.repeatbot.config

import io.github.cdimascio.dotenv.dotenv

object ConfigLoader {
    fun load(): AppConfig {
        val dotenv = dotenv {
            ignoreIfMissing = true
        }
        val token = dotenv["TELEGRAM_BOT_TOKEN"]
            ?: throw IllegalStateException("TELEGRAM_BOT_TOKEN is not set in .env file")
        val proxyAddress = dotenv["TELEGRAM_BOT_PROXY_ADDRESS"]
        return AppConfig(token = token, proxyAddress = proxyAddress)
    }
}