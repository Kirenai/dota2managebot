package me.kirenai.bot.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class DiscordConfig(
    @Value("\${discord.token}") val token: String,
)