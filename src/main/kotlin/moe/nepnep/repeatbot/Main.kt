package moe.nepnep.repeatbot

import moe.nepnep.repeatbot.bot.startBot
import moe.nepnep.repeatbot.config.ConfigLoader

fun main() {
    val config = ConfigLoader.load()
    startBot(config)
}
