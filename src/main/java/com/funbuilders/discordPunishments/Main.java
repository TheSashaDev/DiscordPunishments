package com.funbuilders.discordPunishments;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private DiscordBot discordBot;
    private PunishmentHandler punishmentHandler;

    @Override
    public void onEnable() {
        // Save default config if it doesn't exist
        saveDefaultConfig();

        // Initialize Discord bot
        String token = getConfig().getString("discord.token");
        discordBot = new DiscordBot();
        discordBot.startBot(token);

        // Initialize punishment handler
        punishmentHandler = new PunishmentHandler();

        // Register command
        getCommand("discordpunish").setExecutor(new DiscordPunishCommand(this));

        // Register Discord button and modal listeners
        discordBot.getJDA().addEventListener(new ButtonListener(this));
        discordBot.getJDA().addEventListener(new ModalListener(this, punishmentHandler));
    }

    @Override
    public void onDisable() {
        if (discordBot != null && discordBot.getJDA() != null) {
            discordBot.getJDA().shutdown();
        }
    }

    // Inner class for Discord bot initialization
    private class DiscordBot {
        private JDA jda;

        public void startBot(String token) {
            try {
                jda = JDABuilder.createDefault(token)
                        .setActivity(Activity.playing("Управление наказаниями")) // Managing Punishments -> Управление наказаниями
                        .build();
                jda.awaitReady();
            } catch (Exception e) {
                getLogger().severe("Не удалось запустить Discord бота: " + e.getMessage()); // Failed to start Discord bot -> Не удалось запустить Discord бота
            }
        }

        public JDA getJDA() {
            return jda;
        }
    }

    // Command to set up the control panel
    private class DiscordPunishCommand implements CommandExecutor {
        private final Main plugin;

        public DiscordPunishCommand(Main plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!sender.hasPermission("discordpunishments.admin")) {
                sender.sendMessage("У вас нет прав!"); // You don't have permission! -> У вас нет прав!
                return true;
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("setup")) {
                String channelId = plugin.getConfig().getString("control_panel.channel_id");
                TextChannel channel = plugin.discordBot.getJDA().getTextChannelById(channelId);

                if (channel == null) {
                    sender.sendMessage("Неверный ID канала в конфиге!"); // Invalid channel ID in config! -> Неверный ID канала в конфиге!
                    return true;
                }

                channel.sendMessage("**Панель управления наказаниями Discord**\nИспользуйте кнопки ниже для выдачи наказаний.") // **Discord Punishments Control Panel**\nUse the buttons below to issue punishments. -> **Панель управления наказаниями Discord**\nИспользуйте кнопки ниже для выдачи наказаний.
                        .setActionRow(
                                net.dv8tion.jda.api.interactions.components.buttons.Button.primary("ban", "Бан"), // Ban -> Бан
                                net.dv8tion.jda.api.interactions.components.buttons.Button.primary("mute", "Мут"), // Mute -> Мут
                                net.dv8tion.jda.api.interactions.components.buttons.Button.primary("kick", "Кик")  // Kick -> Кик
                        ).queue(message -> {
                            plugin.getConfig().set("control_panel.message_id", message.getId());
                            plugin.saveConfig();
                            sender.sendMessage("Панель управления установлена в канале Discord!"); // Control panel set up in Discord channel! -> Панель управления установлена в канале Discord!
                        });
                return true;
            }
            return false;
        }
    }

    // Listener for Discord button interactions
    private class ButtonListener extends ListenerAdapter {
        private final Main plugin;

        public ButtonListener(Main plugin) {
            this.plugin = plugin;
        }

        @Override
        public void onButtonInteraction(ButtonInteractionEvent event) {
            String buttonId = event.getButton().getId();

            // Create a modal for entering punishment details
            TextInput playerName = TextInput.create("player", "Имя игрока", TextInputStyle.SHORT) // Player Name -> Имя игрока
                    .setPlaceholder("Введите никнейм игрока") // Enter the player's nickname -> Введите никнейм игрока
                    .setRequired(true)
                    .build();

            TextInput duration = TextInput.create("duration", "Длительность", TextInputStyle.SHORT) // Duration -> Длительность
                    .setPlaceholder("например, 7д, 1ч, навсегда") // e.g., 7d, 1h, permanent -> например, 7д, 1ч, навсегда
                    .setRequired(true)
                    .build();

            TextInput reason = TextInput.create("reason", "Причина", TextInputStyle.PARAGRAPH) // Reason -> Причина
                    .setPlaceholder("Введите причину наказания") // Enter the reason for the punishment -> Введите причину наказания
                    .setRequired(true)
                    .build();

            String punishmentName = buttonId.substring(0, 1).toUpperCase() + buttonId.substring(1);
            String modalTitle = "Выдать " + punishmentName + " наказание"; // Issue Ban/Mute/Kick Punishment -> Выдать Бан/Мут/Кик наказание
            if (punishmentName.equals("Бан")) modalTitle = "Выдать бан";
            if (punishmentName.equals("Мут")) modalTitle = "Выдать мут";
            if (punishmentName.equals("Кик")) modalTitle = "Выдать кик";


            Modal modal = Modal.create("punish_" + buttonId, modalTitle) // Issue Ban/Mute/Kick Punishment -> Выдать Бан/Мут/Кик наказание (Shortened for modal title)
                    .addActionRows(
                            net.dv8tion.jda.api.interactions.components.ActionRow.of(playerName),
                            net.dv8tion.jda.api.interactions.components.ActionRow.of(duration),
                            net.dv8tion.jda.api.interactions.components.ActionRow.of(reason)
                    )
                    .build();

            event.replyModal(modal).queue();
        }
    }

    // Listener for modal submissions
    private class ModalListener extends ListenerAdapter {
        private final Main plugin;
        private final PunishmentHandler punishmentHandler;

        public ModalListener(Main plugin, PunishmentHandler punishmentHandler) {
            this.plugin = plugin;
            this.punishmentHandler = punishmentHandler;
        }

        @Override
        public void onModalInteraction(net.dv8tion.jda.api.events.interaction.ModalInteractionEvent event) {
            if (!event.getModalId().startsWith("punish_")) return;

            String punishmentType = event.getModalId().split("_")[1];
            String playerName = event.getValue("player").getAsString();
            String duration = event.getValue("duration").getAsString();
            String reason = event.getValue("reason").getAsString();
            String moderator = event.getUser().getName() + "#" + event.getUser().getDiscriminator();
            String fullReason = reason + " от " + moderator; // reason by moderator -> reason от moderator (by in Russian context of author)

            String commandTemplate = plugin.getConfig().getString("punishments." + punishmentType + ".command");
            if (commandTemplate == null) {
                event.reply("Команда для " + punishmentType + " не настроена!").setEphemeral(true).queue(); // No command configured for ban/mute/kick! -> Команда для бан/мут/кик не настроена!
                return;
            }

            // Replace placeholders in the command
            String command = commandTemplate
                    .replace("{player}", playerName)
                    .replace("{duration}", duration)
                    .replace("{reason}", fullReason);

            // Execute the command on the server
            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            });

            event.reply("Успешно выдан " + punishmentType + " для " + playerName + " на " + duration + " с причиной: " + fullReason) // Successfully issued ban/mute/kick to player for duration with reason -> Успешно выдан бан/мут/кик для игрока на длительность с причиной
                    .setEphemeral(true)
                    .queue();
        }
    }

    // Punishment handler class (fallback for direct punishment execution if needed)
    private class PunishmentHandler {
        public void banPlayer(String playerName, String duration, String reason) {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
                    "ban " + playerName + " " + duration + " " + reason);
        }

        public void kickPlayer(String playerName, String reason) {
            if (Bukkit.getPlayer(playerName) != null) {
                Bukkit.getPlayer(playerName).kickPlayer(reason);
            }
        }

        public void mutePlayer(String playerName, String duration, String reason) {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
                    "mute " + playerName + " " + duration + " " + reason);
        }
    }
}