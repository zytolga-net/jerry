package net.zytolga;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.Objects;

public class Jerry extends ListenerAdapter {
    public static final Logger logger = LoggerFactory.getLogger(Jerry.class);
    private final QuoteProvider quoteProvider = new QuoteProvider();
    private static AMPTest ampTest;

    public Jerry() {
        super();
    }

    public static void main(String[] args) {
        logger.info("Copyright (c) 2026 Zytolga");
        logger.info("Licensed under the Blue Oak Model License 1.0.0 (see https://blueoakcouncil.org/license/1.0.0 for details)");
        logger.info("Starting Jerry...");

        EnumSet<GatewayIntent> intents = EnumSet.of(
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.DIRECT_MESSAGE_REACTIONS
        );

        JDA jda = JDABuilder.createLight(System.getenv("DISCORD_TOKEN"), intents)
                .addEventListeners(new Jerry())
                .build();

//        CommandListUpdateAction commands = jda.updateCommands();
        try {
            jda.awaitReady(); // blocks until JDA has fully connected and cached data
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted while waiting for JDA to become ready", e);
            return;
        }

        try {
            Objects.requireNonNull(jda.getGuildById(System.getenv("GUILD_TOKEN"))).updateCommands().addCommands(
                            Commands.slash("jerry", "jerry :)"),
                            Commands.slash("random_quote", "Gives a random quote")
                    )
                    .queue(
                            success -> logger.info("Commands registered successfully"),
                            failure -> logger.error("Failed to register commands")
                    );
        } catch (NullPointerException e) {
            logger.error("Error adding commands. Guild not found.", e);
        } catch (IllegalArgumentException e) {
            logger.error("Error adding commands. Null or more than 100 commands, 15 user context commands, or 15 message context commands, are provided.", e);
        } catch (Exception e) {
            logger.error("Error adding commands", e);
        }

        try {
            ampTest = new AMPTest();
            String session = ampTest.getSession();
            logger.info(session);
        } catch (Exception e) {
            logger.error(e.getMessage(), e.getCause());
        }

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
                event.reply("I can't handle that command right now :(")
                        .setEphemeral(true)
                        .queue();
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.isFromType(ChannelType.PRIVATE)) {
            System.out.printf("[PM] %s: %s\n", Objects.requireNonNull(event.getAuthor()).getEffectiveName(),
                    event.getMessage().getContentRaw());
        } else {
            System.out.printf("[%s][%s] %s: %s\n", event.getGuild().getName(),
                    event.getChannel().getName(), Objects.requireNonNull(event.getMember()).getEffectiveName(),
                    event.getMessage().getContentRaw());
            if (event.getMessage().getContentRaw().contains("<@" + event.getJDA().getSelfUser().getId() + ">")) {
                event.getChannel().asTextChannel().sendMessage("<@" + event.getMember().getId() + "> kys").queue();
            }
        }
    }

    public void say(@NotNull SlashCommandInteractionEvent event, String content) {
        event.reply(content).queue();
    }
}