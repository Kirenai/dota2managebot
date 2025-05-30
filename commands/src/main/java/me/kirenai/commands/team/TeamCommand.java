package me.kirenai.commands.team;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

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

            int size = event.getOptions().size();
            System.out.println("size = " + size);
            // TODO: Cambiar size > 4 en producción
            if (size < 0 || size > 6) {
                event.reply("You must provide 4 players, and optionally a coach.").setEphemeral(true).queue();
                return;
            }

            AtomicBoolean hasError = new AtomicBoolean(false);
            StringBuilder errorMessage = new StringBuilder();

            if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.ADMINISTRATOR)) {
                event.reply("You do not have permission to create a team.").setEphemeral(true).queue();
                return;
            }

            String teamName = "Team-" + event.getUser().getName();
            Objects.requireNonNull(event.getGuild()).createVoiceChannel(teamName)
                    .setParent(event.getGuild().getCategoryById("634076015991324716"))
                    .setUserlimit(size + 1) // Incrementamos el límite para incluir al miembro que ejecuta el comando
                    .queue(voiceChannel -> {
                                log.info("Creando canal de voz '{}' con límite de {} usuarios", teamName, size + 1);

                                // Función para mover un miembro al canal de voz
                                Consumer<Member> moveMember = member -> {
                                    try {
                                        event.getGuild().moveVoiceMember(member, voiceChannel).queue(
                                                success -> log.debug("Usuario {} movido exitosamente al canal {}",
                                                        member.getEffectiveName(), voiceChannel.getName()),
                                                error -> {
                                                    hasError.set(true);
                                                    String errorMsg = String.format("No se pudo mover a %s: %s",
                                                            member.getEffectiveName(),
                                                            error.getMessage());
                                                    errorMessage.append(errorMsg).append("\n");
                                                    log.error(errorMsg, error);
                                                }
                                        );
                                    } catch (IllegalStateException e) {
                                        hasError.set(true);
                                        errorMessage.append("No se pudo mover a ")
                                                .append(member.getEffectiveName())
                                                .append(": El usuario no está en ningún canal de voz.\n");
                                        log.warn("No se pudo mover a {}: no está en un canal de voz",
                                                member.getEffectiveName());
                                    }
                                };

                                // Mover al miembro que ejecutó el comando
                                Member commandExecutor = event.getMember();
                                if (commandExecutor != null) {
                                    log.info("Intentando mover al ejecutor del comando: {}", commandExecutor.getEffectiveName());
                                    moveMember.accept(commandExecutor);
                                }

                                // Procesar cada miembro del equipo mencionado en las opciones
                                event.getOptions().forEach(option -> {
                                    Member member = option.getAsMember();
                                    if (member == null) {
                                        hasError.set(true);
                                        errorMessage.append("El usuario ")
                                                .append(option.getAsString())
                                                .append(" no fue encontrado en el servidor.\n");
                                        return;
                                    }

                                    // Evitar mover al ejecutor dos veces si está en las opciones
                                    if (commandExecutor != null && member.getIdLong() == commandExecutor.getIdLong()) {
                                        log.debug("Ejecutor del comando ya incluido, omitiendo duplicado");
                                        return;
                                    }

                                    moveMember.accept(member);
                                });

                                // Enviar respuesta al usuario
                                event.deferReply(true).queue();
                                String finalMessage = hasError.get()
                                        ? "Se creó el canal '" + teamName + "' pero hubo algunos problemas:\n" + errorMessage
                                        : "¡Canal de equipo creado exitosamente! Todos los miembros han sido movidos a '" + teamName + "'";
                                event.getHook().sendMessage(finalMessage).queue();
                            },
                            throwable -> {
                                log.error("Error al crear el canal de voz: {}", throwable.getMessage(), throwable);
                                event.reply("No se pudo crear el canal de voz: " + throwable.getMessage())
                                        .setEphemeral(true)
                                        .queue();
                            }
                    );
        }
    }
}
