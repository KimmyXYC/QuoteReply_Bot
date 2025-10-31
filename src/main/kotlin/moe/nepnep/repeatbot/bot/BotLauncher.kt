package moe.nepnep.repeatbot.bot

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import moe.nepnep.repeatbot.config.AppConfig
import moe.nepnep.repeatbot.service.QuoteService
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URI

private val logger = LoggerFactory.getLogger("QuoteReplyBot")

fun startBot(config: AppConfig) {
    val bot = bot {
        this.token = config.token

        config.proxyAddress?.let { proxyUrl ->
            val uri = URI(proxyUrl)
            this.proxy = when (uri.scheme) {
                "socks5" -> Proxy(Proxy.Type.SOCKS, InetSocketAddress(uri.host, uri.port))
                "http", "https" -> Proxy(Proxy.Type.HTTP, InetSocketAddress(uri.host, uri.port))
                else -> throw IllegalArgumentException("Unsupported proxy type: ${uri.scheme}. Use socks5:// or http://")
            }
        }

        dispatch {
            text {
                val reply = QuoteService.generateReply(message)
                if (reply.isNotEmpty()) {
                    logger.info("Sending reply message to chat: {}", message.chat.id)
                    logger.debug("Reply text: {}", reply)
                    val result = bot.sendMessage(
                        chatId = ChatId.fromId(message.chat.id),
                        text = reply,
                        parseMode = ParseMode.MARKDOWN_V2,
                        disableWebPagePreview = true,
                        protectContent = true,
                        disableNotification = false,
                    )
                    result.fold(
                        { response ->
                            logger.info("Message sent successfully: messageId={}", response.messageId)
                        },
                        { error ->
                            logger.error("Failed to send message: {}", error.toString())
                        }
                    )
                } else {
                    logger.debug("Reply is empty, not sending message")
                }
            }
        }
    }

    bot.startPolling()
}
