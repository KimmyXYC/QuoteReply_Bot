package moe.nepnep.repeatbot

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import io.github.cdimascio.dotenv.dotenv
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URI

fun main() {
    val dotenv = dotenv {
        ignoreIfMissing = true
    }

    val token = dotenv["TELEGRAM_BOT_TOKEN"] ?: throw IllegalStateException("TELEGRAM_BOT_TOKEN is not set in .env file")
    val proxyAddress = dotenv["TELEGRAM_BOT_PROXY_ADDRESS"]

    val bot = bot {
        this.token = token

        proxyAddress?.let { proxyUrl ->
            val uri = URI(proxyUrl)
            this.proxy = when (uri.scheme) {
                "socks5" -> Proxy(Proxy.Type.SOCKS, InetSocketAddress(uri.host, uri.port))
                "http", "https" -> Proxy(Proxy.Type.HTTP, InetSocketAddress(uri.host, uri.port))
                else -> throw IllegalArgumentException("Unsupported proxy type: ${uri.scheme}. Use socks5:// or http://")
            }
        }

        dispatch {
            text {
                bot.sendMessage(
                    chatId = ChatId.fromId(message.chat.id),
                    messageThreadId = message.messageThreadId,
                    text = text,
                    protectContent = true,
                    disableNotification = false,
                )
            }
        }
    }

    bot.startPolling()
}
