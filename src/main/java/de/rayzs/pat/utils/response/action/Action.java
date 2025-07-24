package de.rayzs.pat.utils.response.action;

import java.util.UUID;

public interface Action {

    void executeConsoleCommand(String action, UUID uuid, String command);
    void executePlayerCommand(String action, UUID uuid, String command);
    void sendTitle(String action, UUID uuid, String title, String subTitle, int fadeIn, int stay, int fadeOut);
    void addPotionEffect(String action, UUID uuid, String potionEffectTypeName, int duration, int amplifier);
    void playSound(String action, UUID uuid, String soundName, float volume, float pitch);
    void sendActionbar(String action, UUID uuid, String text);

}
