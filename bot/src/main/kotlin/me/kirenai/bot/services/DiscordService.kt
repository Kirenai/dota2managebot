package me.kirenai.bot.services

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service

@Service
class DiscordService {

    @PostConstruct
    fun init() {
        // Initialize Discord bot here
        println("Discord bot initialized")
    }

}