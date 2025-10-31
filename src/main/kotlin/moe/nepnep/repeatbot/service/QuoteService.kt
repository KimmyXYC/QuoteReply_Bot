package moe.nepnep.repeatbot.service

import com.github.kotlintelegrambot.entities.Chat
import com.github.kotlintelegrambot.entities.Message
import org.slf4j.LoggerFactory

object QuoteService {
    private val logger = LoggerFactory.getLogger("QuoteReplyBot")

    fun generateReply(message: Message): String {
        val text = message.text ?: return ""

        logger.info(
            "Processing message: \"{}\" from chat: {} user: {}",
            text,
            message.chat.id,
            message.from?.id ?: "unknown"
        )

        if (text.length < 2) {
            logger.debug("Message too short, ignoring")
            return ""
        }

        val startsWithSlash = text.startsWith("/")
        val startsWithBackslash = text.startsWith("\\")

        if (!startsWithSlash && !startsWithBackslash) {
            logger.debug("Message doesn't start with / or \\, ignoring")
            return ""
        }

        // For ASCII commands, require the "$" prefix after the slash or backslash
        // Check only the character after the prefix (index 1)
        val charAfterPrefix = text[1]
        val isCharAfterPrefixAscii = charAfterPrefix.code <= 0x7F

        logger.debug(
            "Char after prefix: '{}' (code: {}), isAscii: {}",
            charAfterPrefix,
            charAfterPrefix.code,
            isCharAfterPrefixAscii
        )

        if (isCharAfterPrefixAscii) {
            if (startsWithSlash && !text.startsWith("/$")) {
                logger.debug("ASCII command without $ prefix after /, ignoring")
                return ""
            }
            if (startsWithBackslash && !text.startsWith("\\$")) {
                logger.debug("ASCII command without $ prefix after \\, ignoring")
                return ""
            }
        }

        // Build keywords: remove the first '$', drop leading slash or backslash, escape, then split by space (max 2)
        val withoutDollar = text.replaceFirst("$", "")
        val withoutPrefix = withoutDollar.drop(1)

        logger.debug("After removing prefix: \"{}\"", withoutPrefix)

        val escaped = escapeMarkdownV2(withoutPrefix)
        val keywords = escaped.split(" ", limit = 2)

        logger.debug("Keywords: {}", keywords)

        if (keywords.isEmpty()) {
            logger.debug("No keywords found, ignoring")
            return ""
        }

        // Sender info
        var senderName = escapeMarkdownV2(buildName(message.from?.firstName, message.from?.lastName))
        var senderUri = message.from?.id?.let { "tg://user?id=$it" } ?: ""

        message.senderChat?.let { chat ->
            senderName = escapeMarkdownV2(chat.title ?: "")
            senderUri = chat.toUsernameLink()
        }

        logger.debug("Sender: {} ({})", senderName, senderUri)

        // Get reply-to message
        val replyToMsg = message.replyToMessage

        var replyToName = ""
        var replyToUri = ""

        if (replyToMsg != null) {
            logger.debug("Processing reply-to message")
            // Base on user
            val rFrom = replyToMsg.from
            if (rFrom != null) {
                replyToName = escapeMarkdownV2(buildName(rFrom.firstName, rFrom.lastName))
                replyToUri = "tg://user?id=${rFrom.id}"
            }

            // If message is from a channel/sender chat
            replyToMsg.senderChat?.let { chat ->
                replyToName = escapeMarkdownV2(chat.title ?: "")
                replyToUri = chat.toUsernameLink()
            }

            // If starts with backslash, swap
            if (startsWithBackslash) {
                logger.debug("Backslash command, swapping sender and reply-to")
                val tmpName = senderName
                val tmpUri = senderUri
                senderName = replyToName
                senderUri = replyToUri
                replyToName = tmpName
                replyToUri = tmpUri
            }

            logger.debug("Reply-to: {} ({})", replyToName, replyToUri)
        } else {
            logger.debug("Not a reply message, checking for @username")
            // Not a reply: allow targeting by @username in the command keyword
            if (replyToName.isEmpty()) {
                logger.debug("Setting reply-to to 自己 (self)")
                replyToName = "自己"
                replyToUri = senderUri
            }
            logger.debug("Reply-to (fallback): {} ({})", replyToName, replyToUri)
        }

        val result = buildResult(senderName, senderUri, keywords, replyToName, replyToUri)
        logger.debug("Final result: {}", result)
        return result
    }

    private fun buildResult(
        senderName: String,
        senderUri: String,
        keywords: List<String>,
        replyToName: String,
        replyToUri: String
    ): String {
        return if (keywords.size < 2) {
            "[${senderName}](${senderUri}) ${keywords[0]}了 [${replyToName}](${replyToUri})！"
        } else {
            "[${senderName}](${senderUri}) ${keywords[0]} [${replyToName}](${replyToUri}) ${keywords[1]}！"
        }
    }

    private fun buildName(first: String?, last: String?): String {
        val f = first ?: ""
        val l = last ?: ""
        return (f + " " + l).trim()
    }

    @Suppress("unused")
    private fun isAscii(s: String): Boolean = s.all { it.code <= 0x7F }

    private fun escapeMarkdownV2(text: String): String {
        if (text.isEmpty()) return text
        val specials = setOf('_','*','[',']','(',')','~','`','>','#','+','-','=','|','{','}','.','!')
        val sb = StringBuilder(text.length * 2)
        for (ch in text) {
            if (ch == '\\' || ch in specials) {
                sb.append('\\')
            }
            sb.append(ch)
        }
        return sb.toString()
    }

    private fun Chat.toUsernameLink(): String {
        val username = this.username
        return if (!username.isNullOrBlank()) "https://t.me/$username" else "https://t.me/"
    }
}
