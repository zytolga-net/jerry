package net.zytolga;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.MessageReference;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.zytolga.amp.AMPInstance;
import net.zytolga.amp.AMPService;
import net.zytolga.dialogue.DialogueHandler;
import net.zytolga.records.JsonResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Jerry extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(Jerry.class);
    private static final Logger chatLogger = LoggerFactory.getLogger("Chat");

    private static final ObjectMapper mapper = new ObjectMapper();
    public static JsonNode emptyJsonNode = mapper.readTree("{}");
    public static JsonResponse emptyJsonResponseFail = new JsonResponse(emptyJsonNode, 500, false);

    private final QuoteProvider quoteProvider = new QuoteProvider();

    final DialogueHandler dialogueHandler = new DialogueHandler();

    private static AMPService ampService;

    public Jerry() throws IOException {
        super();
    }

    public static void main(String[] args) throws IOException {
        logger.info("Copyright (c) 2026 Zytolga");
        logger.info("Licensed under the Blue Oak Model License 1.0.0 (see https://blueoakcouncil.org/license/1.0.0 for details)");
        logger.info("Starting Jerry...");

        EnumSet<GatewayIntent> intents = EnumSet.of(
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.DIRECT_MESSAGE_REACTIONS);

        JDA jda = JDABuilder.createLight(System.getenv("DISCORD_TOKEN"), intents).addEventListeners(new Jerry()).build();

        CommandListUpdateAction commands = jda.updateCommands();
        try {
            jda.awaitReady(); // blocks until JDA has fully connected and cached data
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted while waiting for JDA to become ready", e);
            return;
        }

        try {
            commands
                    .addCommands()
                    .queue(
                            success -> logger.info("Global commands registered successfully"),
                            failure -> logger.error("Failed to register global commands"));
            Objects.requireNonNull(jda.getGuildById(System.getenv("GUILD_ID")))
                    .updateCommands()
                    .addCommands(
                            Commands.slash(
                                    "jerry",
                                    "jerry :)"),
                            Commands.slash(
                                    "random_quote",
                                    "Gives a random quote"),
                            Commands.slash(
                                            "buttontest",
                                            "button test")
                                    .setContexts(InteractionContextType.GUILD)
                                    .setDefaultPermissions(DefaultMemberPermissions.DISABLED),
                            Commands.slash(
                                            "status",
                                            "Gets the status of a server")
                                    .addOption(OptionType.STRING, "server", "The server to get the status of", true, true)
                                    .setContexts(InteractionContextType.GUILD)
                                    .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
                    ).queue(
                            success -> logger.info("Guild commands registered successfully"),
                            failure -> logger.error("Failed to register guild commands"));
        } catch (NullPointerException e) {
            logger.error("Error adding commands. Guild not found.", e);
        } catch (IllegalArgumentException e) {
            logger.error("Error adding commands. Null or more than 100 commands, 15 user context commands, or 15 message context commands, are provided.", e);
        } catch (Exception e) {
            logger.error("Error adding commands", e);
        }

        try {
            ampService = new AMPService("https://amp.zytolga.net", System.getenv("AMP_USERNAME"), System.getenv("AMP_PASSWORD"));
            ampService.login();
            ampService.StartInstance("TestingWorld01");
            ampService.GetInstances(false);
            ampService.AddUser("jerry");
            ampService.SetAMPUserRoleMembership("jerry", "Game Server Manager", true);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    ampService.logout();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        } catch (Exception e) {
            logger.error(e.getMessage(), e.getCause());
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "jerry":
                event.reply("https://klipy.com/gifs/creo-creomusic-1").queue();
                break;
            case "random_quote":
                event.replyEmbeds(quoteProvider.randomQuote()).queue();
                break;
            case "buttontest":
                event.reply("Click the button to say hello")
                        .addComponents(ActionRow.of(
                                Button.primary("hello", "Click Me")))
                        .queue();
                break;
            case "status":
                String server = Objects.requireNonNull(event.getOption("server")).getAsString();
                if (!getServerNames().contains(server.toLowerCase())) {
                    event.reply("Server " + server + " not found.").setEphemeral(true).queue();
                    break;
                }
                AMPInstance instance = ampService.GetInstanceByFriendlyName(server);
                if (instance == null) {
                    event.reply("Server " + server + " not found.").setEphemeral(true).queue();
                    break;
                }
                String statusMessage = "Instance ID: `" + instance.getInstanceID() + "`\n" +
                        "Instance Name: `" + instance.getInstanceName() + "`\n" +
                        "Friendly Name: `" + instance.getFriendlyName() + "`\n" +
                        "AMP Version: `" + instance.getAmpVersion() + "`\n" +
                        "Server Type: `" + instance.getServerType() + "`\n" +
                        "IP: `" + instance.getApplicationIP() + "`\n" +
                        "Ports: `" + instance.getPorts() + "`\n" +
                        "Max Memory: `" + instance.getMaxMemory() + "`\n" +
                        "Running: `" + instance.isRunning() + "`\n" +
                        "Players: `" + instance.getNumPlayers() + "`\n" +
                        "CPU Usage: `" + instance.getCpuUsage() + "`\n" +
                        "RAM Usage: `" + instance.getMemUsage() + "`";
                event.reply(statusMessage).queue();
                break;
            default:
                event.reply("I can't handle that command right now :(").setEphemeral(true).queue();
        }
    }

    private ArrayList<String> getServerNames() {
        ArrayList<String> instances = new ArrayList<>();

        ampService.GetInstances().forEach(instance -> {
            String name = instance.getFriendlyName();
            if (!name.equalsIgnoreCase("ADS01")) instances.add(name.toLowerCase());
        });

        return instances;
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if (event.getName().equals("status") && event.getFocusedOption().getName().equals("server")) {
            ArrayList<String> instances = new ArrayList<>();

            ampService.GetInstances().forEach(instance -> {
                if (!instance.getFriendlyName().equalsIgnoreCase("ADS01")) instances.add(instance.getFriendlyName());
            });

            List<Command.Choice> options = Stream.of(instances.toArray())
                    .filter(server -> server.toString().startsWith(event.getFocusedOption().getValue())) // only display words that start with the user's current input
                    .map(server -> new Command.Choice(server.toString(), server.toString()))
                    .collect(Collectors.toList());
            event.replyChoices(options).queue();
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.isFromType(ChannelType.PRIVATE)) {
            chatLogger.info("\u001B[36m[PM] \u001B[0m{}: {}",
                    Objects.requireNonNull(event.getAuthor()).getEffectiveName(),
                    event.getMessage().getContentRaw());
        } else {
            chatLogger.info("\u001B[36m[{}]\u001B[32m[{}] \u001B[0m{}: {}",
                    event.getGuild().getName(),
                    event.getChannel().getName(),
                    Objects.requireNonNull(event.getMember()).getEffectiveName(),
                    event.getMessage().getContentRaw());
        }
        if (event.getAuthor().isBot()) return;

        if (event.getMessage().getMessageReference() != null) {
            MessageReference reference = event.getMessage().getMessageReference();
            reference.resolve().queue(referencedMessage -> {
                if (referencedMessage.getAuthor().getIdLong() == event.getJDA().getSelfUser().getIdLong()) {
                    event.getMessage().reply(
                            dialogueHandler.handleMessage(
                                    event.getAuthor().getId(),
                                    event.getMessage().getContentRaw()
                            )).mentionRepliedUser(false).queue();
                }
            });

        } else if (event.getMessage().getContentRaw().contains("<@" + event.getJDA().getSelfUser().getId() + ">")) {
            event.getMessage().reply(
                    dialogueHandler.handleMessage(
                            event.getAuthor().getId(),
                            event.getMessage().getContentRaw()
                    )).mentionRepliedUser(false).queue();
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (event.getComponentId().equals("hello")) {
            event.reply("Hello :)").queue();
        }
    }
}