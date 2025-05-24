package me.kirenai.bot.services;

import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DiscordService {

    @Value("${discord.token}")
    private String discordToken;

    @PostConstruct
    public void init() {
        // Initialize Discord bot here
        JDA jpa = JDABuilder.createDefault(this.discordToken)
                .build();

        try {
            jpa.awaitReady();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
