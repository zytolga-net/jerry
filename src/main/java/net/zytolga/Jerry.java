package net.zytolga;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;

public class Jerry extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(Jerry.class);
    private final QuoteProvider quoteProvider = new QuoteProvider();

    public static void main(String[] args) {
        logger.info("Copyright (c) 2026 Zytolga");
        logger.info("Licensed under the Blue Oak Model License 1.0.0 (see https://blueoakcouncil.org/license/1.0.0 for details)");
        logger.info("Starting Jerry...");

        EnumSet<GatewayIntent> intents = EnumSet.noneOf(GatewayIntent.class);
        JDA jda = JDABuilder.createLight(System.getenv("DISCORD_TOKEN"), intents)
                .addEventListeners(new Jerry())
                .build();

        CommandListUpdateAction commands = jda.updateCommands();

        try {


        } catch(Exception e) {
            logger.error("Error trying to get AMP information", e);
        }


        try {
            jda.awaitReady(); // blocks until JDA has fully connected and cached data
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted while waiting for JDA to become ready", e);
            return;
        }

        try {
            jda.getGuildById("1276927121008230485")
                    .updateCommands()
                    .addCommands(
                            Commands.slash("jerry", "jerry :)"),
                            Commands.slash("random_quote", "Gives a random quote")
                    )
                    .queue(
                            success -> logger.info("Commands registered successfully"),
                            failure -> logger.error("Failed to register commands")
                    );

            //noinspection ResultOfMethodCallIgnored
//            commands.addCommands(
//                Commands.slash("jerry", "jerry :)")
//                    .setContexts(InteractionContextType.ALL)
//                    .setIntegrationTypes(IntegrationType.ALL),
//                Commands.slash("random_quote", "Gives a random quote")
//                    .setContexts(InteractionContextType.ALL)
//                    .setIntegrationTypes(IntegrationType.ALL)
//            ).queue(
//                    success -> logger.info("Commands registered successfully"),
//                    failure -> logger.error("Failed to register commands")
//            );
        } catch (IllegalArgumentException e) {
            logger.error("Error adding commands. Null or more than 100 commands, 15 user context commands, or 15 message context commands, are provided.", e);
        } catch (Exception e) {
            logger.error("Error adding commands", e);
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (event.getName()) {
            case "jerry":
                say(event, "https://klipy.com/gifs/creo-creomusic-1");
                break;
            case "random_quote":
                Quote quote = quoteProvider.randomQuote();
                event.replyEmbeds(quoteProvider.randomQuoteEmbed()).queue();
//                say(event, "Quote: " + quote.getQuote() + "\n" + "Author" + quote.getAuthor());
                break;
            default:
                event.reply("I can't handle that command right now :(")
                        .setEphemeral(true)
                        .queue();
        }
    }

    public void say(SlashCommandInteractionEvent event, String content) {
        event.reply(content).queue();
    }
}
