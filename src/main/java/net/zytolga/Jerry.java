package net.zytolga;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;

public class Jerry extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(Jerry.class);

    public static void main(String[] args) {
        logger.info("Copyright (c) 2026 Zytolga");
        logger.info("Licensed under the Blue Oak Model License 1.0.0 (see https://blueoakcouncil.org/license/1.0.0 for details)");
        logger.info("Starting Jerry...");

        // slash commands don't need any intents
        EnumSet<GatewayIntent> intents = EnumSet.noneOf(GatewayIntent.class);
        JDA jda = JDABuilder.createLight(System.getenv("DISCORD_TOKEN"), intents)
                .addEventListeners(new Jerry())
                .build();

        CommandListUpdateAction commands = jda.updateCommands();

        try {
            //noinspection ResultOfMethodCallIgnored
            commands.addCommands(Commands.slash("jerry", "jerry :)")
                    .setContexts(InteractionContextType.ALL)
                    .setIntegrationTypes(IntegrationType.ALL));

            commands.queue();
        } catch (IllegalArgumentException e) {
            logger.error("Error adding commands. Null or more than 100 commands, 15 user context commands, or 15 message context commands, are provided.", e);
        } catch (Exception e) {
            logger.error("Error adding commands", e);
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        // Only accept commands from guilds
        if (event.getGuild() == null) {
            return;
        }
        //noinspection SwitchStatementWithTooFewBranches
        switch (event.getName()) {
            case "jerry":
                say(event, "https://klipy.com/gifs/creo-creomusic-1");
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
