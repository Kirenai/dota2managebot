package me.kirenai.bot.services;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import me.kirenai.bot.config.DiscordConfig;
import me.kirenai.commands.PingCommand;
import me.kirenai.commands.ReadyListener;
import me.kirenai.commands.ping.PingListener;
import me.kirenai.commands.team.TeamCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscordService {

    private final DiscordConfig config;

    private final ReadyListener readyListener;
    private final PingCommand pingCommand;
    private final PingListener pingListener;
    private final TeamCommand teamCommand;

    @PostConstruct
    public void init() {
        List<GatewayIntent> intents = List.of(
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.SCHEDULED_EVENTS,
                GatewayIntent.GUILD_EXPRESSIONS
        );

        JDA jpa = JDABuilder.createDefault(this.config.getToken(), intents)
                .addEventListeners(this.readyListener)
                .addEventListeners(this.pingCommand)
                .addEventListeners(this.pingListener)
                .addEventListeners(this.teamCommand)
                .build();

        jpa.updateCommands()
                .addCommands(
                        Commands.slash("team", "Create a new team")
                                .addOption(OptionType.USER, "player1", "The first player in the team", true)
                                .addOption(OptionType.USER, "player2", "The second player in the team", false)
                                .addOption(OptionType.USER, "player3", "The third player in the team", false)
                                .addOption(OptionType.USER, "player4", "The fourth player in the team", false)
                                .addOption(OptionType.USER, "player5", "The fifth player in the team", false),
                        Commands.slash("ping", "Check if the bot is online")
                ).queue();

        try {
            jpa.awaitReady();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
