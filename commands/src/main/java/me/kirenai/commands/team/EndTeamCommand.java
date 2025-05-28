package me.kirenai.commands.team;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class EndTeamCommand extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String commandName = event.getName();

        if (commandName.equals("endteam")) {
            handleEndTeam(event);
        }
    }

    private void handleEndTeam(SlashCommandInteractionEvent event) {
        log.debug("Recibido comando endteam");

        Member commander = event.getMember();
        if (commander == null) {
            event.reply("No se pudo identificar al usuario que ejecutó el comando.").setEphemeral(true).queue();
            return;
        }

        // Verificar si el usuario está en un canal de voz
        VoiceChannel currentChannel = commander.getVoiceState() != null && commander.getVoiceState().inAudioChannel()
                ? commander.getVoiceState().getChannel().asVoiceChannel()
                : null;

        if (currentChannel == null) {
            event.reply("Debes estar en el canal de voz del equipo para finalizarlo.").setEphemeral(true).queue();
            return;
        }

        // Verificar si el nombre del canal comienza con "Team-"
        String channelName = currentChannel.getName();
        if (!channelName.startsWith("Team-")) {
            event.reply("Este no parece ser un canal de equipo. Solo puedes finalizar canales creados con el comando /team.").setEphemeral(true).queue();
            return;
        }

        // Obtener el canal común al que se moverán los jugadores
        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("Error al obtener información del servidor.").setEphemeral(true).queue();
            return;
        }

        // Obtener ID del canal común desde un OptionMapping o usar uno predeterminado
        String commonChannelId = "634076015991324719"; // Reemplaza con el ID real del canal común

        VoiceChannel commonChannel = guild.getVoiceChannelById(commonChannelId);
        if (commonChannel == null) {
            event.reply("No se pudo encontrar el canal común para mover a los jugadores.").setEphemeral(true).queue();
            return;
        }

        AtomicBoolean hasError = new AtomicBoolean(false);
        StringBuilder errorMessage = new StringBuilder();
        AtomicInteger movedCount = new AtomicInteger(0);

        // Mover a todos los miembros al canal común
        List<Member> membersToMove = currentChannel.getMembers();
        int totalMembers = membersToMove.size();

        for (Member member : membersToMove) {
            try {
                guild.moveVoiceMember(member, commonChannel).queue(
                        success -> {
                            log.debug("Usuario {} movido exitosamente al canal común {}",
                                    member.getEffectiveName(), commonChannel.getName());
                            if (movedCount.incrementAndGet() == totalMembers) {
                                // Todos los miembros fueron movidos, ahora eliminar el canal
                                deleteTeamChannel(currentChannel, event, hasError, errorMessage);
                            }
                        },
                        error -> {
                            hasError.set(true);
                            String errorMsg = String.format("No se pudo mover a %s: %s",
                                    member.getEffectiveName(),
                                    error.getMessage());
                            errorMessage.append(errorMsg).append("\n");
                            log.error(errorMsg, error);

                            if (movedCount.incrementAndGet() == totalMembers) {
                                // Intentar eliminar el canal de todas formas
                                deleteTeamChannel(currentChannel, event, hasError, errorMessage);
                            }
                        }
                );
            } catch (Exception e) {
                hasError.set(true);
                errorMessage.append("Error al mover a ")
                        .append(member.getEffectiveName())
                        .append(": ")
                        .append(e.getMessage())
                        .append("\n");
                log.error("Error al mover a {}: {}", member.getEffectiveName(), e.getMessage(), e);

                if (movedCount.incrementAndGet() == totalMembers) {
                    // Intentar eliminar el canal de todas formas
                    deleteTeamChannel(currentChannel, event, hasError, errorMessage);
                }
            }
        }

        // Si no hay miembros en el canal, eliminar directamente
        if (totalMembers == 0) {
            deleteTeamChannel(currentChannel, event, hasError, errorMessage);
        }
    }

    private void deleteTeamChannel(VoiceChannel channel, SlashCommandInteractionEvent event,
                                   AtomicBoolean hasError, StringBuilder errorMessage) {
        channel.delete().queue(
                success -> {
                    log.info("Canal de equipo {} eliminado exitosamente", channel.getName());
                    String finalMessage = hasError.get()
                            ? "Equipo finalizado, pero hubo algunos problemas:\n" + errorMessage
                            : "¡Equipo finalizado exitosamente! Todos los miembros han sido movidos al canal común.";

                    // Responder al comando
                    if (event.isAcknowledged()) {
                        event.getHook().sendMessage(finalMessage).setEphemeral(true).queue();
                    } else {
                        event.reply(finalMessage).setEphemeral(true).queue();
                    }
                },
                error -> {
                    log.error("Error al eliminar el canal de equipo {}: {}", channel.getName(), error.getMessage(), error);
                    hasError.set(true);
                    errorMessage.append("No se pudo eliminar el canal del equipo: ")
                            .append(error.getMessage())
                            .append("\n");

                    String finalMessage = "Equipo finalizado con errores:\n" + errorMessage;

                    // Responder al comando
                    if (event.isAcknowledged()) {
                        event.getHook().sendMessage(finalMessage).setEphemeral(true).queue();
                    } else {
                        event.reply(finalMessage).setEphemeral(true).queue();
                    }
                }
        );
    }

}
