package me.kirenai.commands.ping;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PingListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        log.debug("Ping message received event");

        if (event.getAuthor().isBot()) return;
        System.out.println(event.getAuthor());
        System.out.println(event.getMessage());
        System.out.println(event.getMember());
        System.out.println(event.getChannel());
        System.out.println(event.getChannelType());
        System.out.println(event.isWebhookMessage());

        Message message = event.getMessage();
        String content = message.getContentRaw();
        if (content.equals("!ping")) {
            MessageChannelUnion channel = event.getChannel();
            channel.sendMessage("Pong!").queue();
        }
    }
}
