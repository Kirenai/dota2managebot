package me.kirenai.bot.services;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import me.kirenai.bot.config.DiscordConfig;
import me.kirenai.commands.PingCommand;
import me.kirenai.commands.ReadyListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DiscordService {

    private final DiscordConfig config;

    private final ReadyListener readyListener;
    private final PingCommand pingCommand;

    @PostConstruct
    public void init() {
        JDA jpa = JDABuilder.createDefault(this.config.getToken())
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
