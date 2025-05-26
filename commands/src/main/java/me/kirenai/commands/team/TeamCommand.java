package me.kirenai.commands.team;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class TeamCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("team")) {
            log.debug("Received a team command");

            String fullCommandName = event.getFullCommandName();
            System.out.println("Full command name: " + fullCommandName);


            // Create a voice channel for the team

            Objects.requireNonNull(event.getGuild()).createVoiceChannel("Team Channel")
                    .setParent(event.getGuild().getCategoryById("634076015991324716"))
                    .setUserlimit(5)
                    .queue(voiceChannel -> {
                                log.info("Created voice channel: {}", voiceChannel.getName());
                                event
                                        .getOptions()
                                        .forEach(option -> {
                                            System.out.println("Option: " + option.getName() + " = " + option.getAsString());
                                            Member member = option.getAsMember();
                                            if (member != null) {
                                                event.getGuild().moveVoiceMember(member, voiceChannel)
                                                        .queue();
                                            }
                                        });
                            },
                            throwable -> log.error("Failed to create voice channel", throwable)
                    );


            event.deferReply(true).queue();
            event.getHook().sendMessage("This command is currently under development.").queue();

            return;
        }
    }

}
