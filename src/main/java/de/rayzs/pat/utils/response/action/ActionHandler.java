package de.rayzs.pat.utils.response.action;

import de.rayzs.pat.utils.response.action.impl.*;
import de.rayzs.pat.utils.Reflection;
import java.util.UUID;

public class ActionHandler {

    private static final Action ACTION = Reflection.isProxyServer() ? Reflection.isVelocityServer()
            ? new VelocityAction() : new BungeeAction()
            : Reflection.getMinor() <= 16
            ? new OldBukkitAction() : new ModernBukkitAction();

    public static void initialize() {}

    public static void executeConsoleCommand(String action, UUID uuid, String command, String message) {
        ACTION.executeConsoleCommand(action, uuid, command, message);
    }

    public static void executePlayerCommand(String action, UUID uuid, String command, String message) {
        ACTION.executePlayerCommand(action, uuid, command, message);
    }

    public static void sendTitle(String action, UUID uuid, String command, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        ACTION.sendTitle(action, uuid, command, title, subTitle, fadeIn, stay, fadeOut);
    }

    public static void addPotionEffect(String action, UUID uuid, String potionEffectType, int duration, int amplifier) {
        ACTION.addPotionEffect(action, uuid, potionEffectType, duration, amplifier);
    }

    public static void playSound(String action, UUID uuid, String soundName, float volume, float pitch) {
        ACTION.playSound(action, uuid, soundName, volume, pitch);
    }

    public static void sendActionbar(String action, UUID uuid, String command, String message) {
        ACTION.sendActionbar(action, uuid, command, message);
    }
}
