package net.zytolga;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.MessageReference;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Objects;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

public class Jerry extends ListenerAdapter {
    FileHandler fileHandler = new FileHandler("application.log", true);

    public static final Logger logger = LoggerFactory.getLogger(Jerry.class);
    private static AMPTest ampTest;
    private final QuoteProvider quoteProvider = new QuoteProvider();

    public Jerry() throws IOException {
        super();
    }

    public static void main(String[] args) throws IOException {
        logger.info("Copyright (c) 2026 Zytolga");
        logger.info("Licensed under the Blue Oak Model License 1.0.0 (see https://blueoakcouncil.org/license/1.0.0 for details)");
        logger.info("Starting Jerry...");

        EnumSet<GatewayIntent> intents = EnumSet.of(GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.DIRECT_MESSAGE_REACTIONS);

        JDA jda = JDABuilder.createLight(System.getenv("DISCORD_TOKEN"), intents).addEventListeners(new Jerry()).build();

//        CommandListUpdateAction commands = jda.updateCommands();
        try {
            jda.awaitReady(); // blocks until JDA has fully connected and cached data
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted while waiting for JDA to become ready", e);
            return;
        }

        try {
            Objects.requireNonNull(jda.getGuildById(System.getenv("GUILD_TOKEN"))).updateCommands().addCommands(Commands.slash("jerry", "jerry :)"), Commands.slash("random_quote", "Gives a random quote")).queue(success -> logger.info("Commands registered successfully"), failure -> logger.error("Failed to register commands"));
        } catch (NullPointerException e) {
            logger.error("Error adding commands. Guild not found.", e);
        } catch (IllegalArgumentException e) {
            logger.error("Error adding commands. Null or more than 100 commands, 15 user context commands, or 15 message context commands, are provided.", e);
        } catch (Exception e) {
            logger.error("Error adding commands", e);
        }

        try {
            AMPService ampService = new AMPService("https://amp.zytolga.net", System.getenv("USERNAME"), System.getenv("PASSWORD"));
            ampService.login();
        } catch (Exception e) {
            logger.error(e.getMessage(), e.getCause());
        }

//        try {
//            ampTest = new AMPTest();
//            String session = ampTest.getSession();
//            logger.info(session);
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e.getCause());
//        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                ampTest.logout();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }));

    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "jerry":
                say(event, "https://klipy.com/gifs/creo-creomusic-1");
                break;
            case "random_quote":
                event.replyEmbeds(quoteProvider.randomQuote()).queue();
                break;
            default:
                event.reply("I can't handle that command right now :(").setEphemeral(true).queue();
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        if (event.isFromType(ChannelType.PRIVATE)) {
            System.out.printf("\u001B[35m%s \u001B[36m[PM] \u001B[0m%s: %s\n", now.format(formatter), Objects.requireNonNull(event.getAuthor()).getEffectiveName(), event.getMessage().getContentRaw());
        } else {
            System.out.printf("\u001B[35m%s \u001B[36m[%s]\u001B[32m[%s] \u001B[0m%s: %s\n", now.format(formatter), event.getGuild().getName(), event.getChannel().getName(), Objects.requireNonNull(event.getMember()).getEffectiveName(), event.getMessage().getContentRaw());
        }
        if (event.getAuthor().isBot()) return;

        if (event.getMessage().getMessageReference() != null) {
            MessageReference reference = event.getMessage().getMessageReference();
            reference.resolve().queue(referencedMessage -> {
                if (referencedMessage.getAuthor().getIdLong() == event.getJDA().getSelfUser().getIdLong()) {
                    event.getMessage().reply("I SAID I DON'T FUCKING KNOW!").mentionRepliedUser(false).queue();
                }
            });

        } else if (event.getMessage().getContentRaw().contains("<@" + event.getJDA().getSelfUser().getId() + ">")) {
            event.getMessage().reply("I don't know").mentionRepliedUser(false).queue();
        }
    }

    public void say(@NotNull SlashCommandInteractionEvent event, String content) {
        event.reply(content).queue();
    }
}