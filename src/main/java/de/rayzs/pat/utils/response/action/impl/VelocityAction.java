package de.rayzs.pat.utils.response.action.impl;

import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.response.ResponseHandler;
import net.kyori.adventure.text.minimessage.MiniMessage;
import de.rayzs.pat.utils.response.action.Action;
import com.velocitypowered.api.proxy.Player;
import de.rayzs.pat.plugin.VelocityLoader;
import net.kyori.adventure.title.Title;
import de.rayzs.pat.utils.StringUtils;
import java.util.*;
import java.time.*;

public class VelocityAction implements Action {

    @Override
    public void executeConsoleCommand(String action, UUID uuid, String command, String message) {
        Optional<Player> optPlayer = VelocityLoader.getServer().getPlayer(uuid);
        message = ResponseHandler.replaceArgsVariables(message, command);

        if (optPlayer.isPresent())
            message = message.replace("%player%", optPlayer.get().getUsername());

        VelocityLoader.getServer().getCommandManager().executeAsync(VelocityLoader.getServer().getConsoleCommandSource(), message);
    }

    @Override
    public void executePlayerCommand(String action, UUID uuid, String command, String message) {
        Optional<Player> optPlayer = VelocityLoader.getServer().getPlayer(uuid);
        message = ResponseHandler.replaceArgsVariables(message, command);

        if (optPlayer.isPresent())
            message = command.replace("%player%", optPlayer.get().getUsername());

        VelocityLoader.getServer().getCommandManager().executeAsync(optPlayer.get(), message);
    }

    @Override
    public void sendTitle(String action, UUID uuid, String command, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        Optional<Player> optPlayer = VelocityLoader.getServer().getPlayer(uuid);
        title = ResponseHandler.replaceArgsVariables(title, command);
        subTitle = ResponseHandler.replaceArgsVariables(subTitle, command);

        if (!optPlayer.isPresent()) return;

        Player player = optPlayer.get();
        Title titleObj = Title.title(
                MiniMessage.miniMessage().deserialize(MessageTranslator.replaceMessage(StringUtils.replace(title,  "%player%", player.getUsername()))),
                MiniMessage.miniMessage().deserialize(MessageTranslator.replaceMessage(StringUtils.replace(subTitle, "%player%", player.getUsername()))),
                Title.Times.times(Duration.ofMillis(fadeIn), Duration.ofMillis(stay), Duration.ofMillis(fadeOut))
        );

        player.showTitle(titleObj);
    }

    @Override
    public void addPotionEffect(String action, UUID uuid, String potionEffectTypeName, int duration, int amplifier) {
    }

    @Override
    public void playSound(String action, UUID uuid, String soundName, float volume, float pitch) {
    }

    @Override
    public void sendActionbar(String action, UUID uuid, String command, String message) {
        Optional<Player> optPlayer = VelocityLoader.getServer().getPlayer(uuid);
        message = ResponseHandler.replaceArgsVariables(message, command);

        if (!optPlayer.isPresent())
            return;

        Player player = optPlayer.get();
        player.sendActionBar(MiniMessage.miniMessage().deserialize(MessageTranslator.replaceMessage(StringUtils.replace(message, "%player%", player.getUsername()))));
    }
}
