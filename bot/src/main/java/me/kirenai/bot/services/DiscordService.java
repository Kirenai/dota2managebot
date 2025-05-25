package me.kirenai.bot.services;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import me.kirenai.commands.PingCommand;
import me.kirenai.commands.ReadyListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DiscordService {

    @Value("${discord.token}")
    private String discordToken;

    private final ReadyListener readyListener;
    private final PingCommand pingCommand;

    @PostConstruct
    public void init() {
        // Initialize Discord bot here
        JDA jpa = JDABuilder.createDefault(this.discordToken)
                .addEventListeners(this.readyListener)
                .addEventListeners(this.pingCommand)
                .build();

        jpa.updateCommands()
                .addCommands(
                        Commands.slash("ping", "Check if the bot is online")
                ).queue();

        try {
            jpa.awaitReady();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
