package de.rayzs.pat.utils.message;

import de.rayzs.pat.api.communication.Communicator;
import de.rayzs.pat.utils.configuration.helper.MultipleMessagesHelper;
import de.rayzs.pat.utils.message.replacer.PlaceholderReplacer;
import de.rayzs.pat.utils.message.translators.*;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.*;
import java.util.*;

public class MessageTranslator {

    private static final HashMap<Character, String> colors = new HashMap<>(), endingColors = new HashMap<>();
    private static boolean support;
    private static Translator translator = null;

    public static void initialize() {
        colors.put('1', "<dark_blue>");
        colors.put('2', "<dark_green>");
        colors.put('3', "<dark_aqua>");
        colors.put('4', "<dark_red>");
        colors.put('5', "<dark_purple>");
        colors.put('6', "<gold>");
        colors.put('7', "<gray>");
        colors.put('8', "<dark_gray>");
        colors.put('9', "<blue>");
        colors.put('a', "<green>");
        colors.put('b', "<aqua>");
        colors.put('c', "<red>");
        colors.put('d', "<light_purple>");
        colors.put('e', "<yellow>");
        colors.put('f', "<white>");
        colors.put('m', "<st>");
        colors.put('k', "<obf>");
        colors.put('r', "<reset>");
        colors.put('o', "<i>");
        colors.put('l', "<b>");

        for (Map.Entry<Character, String> entry : colors.entrySet())
            endingColors.put(entry.getKey(), "</" + entry.getValue().substring(1));

        support = !Storage.USE_SIMPLECLOUD && (Reflection.getMinor() >= 18 || Reflection.isProxyServer());

        if(support)
            translator = Reflection.isVelocityServer() ? new VelocityMessageTranslator()
                    : Reflection.isProxyServer() ? new BungeeMessageTranslator()
                    : new BukkitMessageTranslator();
    }

    public static String translateLegacy(String text) {
        text = replaceMessage(text);
        if(!text.contains("§")) return text;

        for (Map.Entry<Character, String> entry : colors.entrySet())
            text = text.replace("§" + entry.getKey(), entry.getValue());

        return text;
    }

    public static String colorless(String text) {
        text = replaceMessage(text);
        if(!text.contains("§")) return text;

        for (Map.Entry<Character, String> entry : colors.entrySet())
            text = text.replace("§" + entry.getKey(), "");

        return text;
    }

    public static void send(Object target, MultipleMessagesHelper texts) {
        String stringList = StringUtils.buildStringList(texts.getLines());
        send(target, stringList);
    }

    public static void send(Object target, MultipleMessagesHelper texts, String... replacements) {
        String stringList = StringUtils.buildStringList(texts.getLines());
        send(target, stringList, replacements);
    }

    public static void send(Object target, List<String> texts) {
        String stringList = StringUtils.buildStringList(texts);
        send(target, stringList);
    }

    public static void send(Object target, List<String> texts, String... replacements) {
        texts = replaceMessageList(texts, replacements);
        String stringList = StringUtils.buildStringList(texts);
        send(target, stringList);
    }

    public static void send(Object target, String text, String... replacements) {
        if(text.isEmpty()) return;

        text = replaceMessageString(target, text, replacements);

        if(text.contains("%no-message%")) return;

        if(translator == null) {
            CommandSender sender = target instanceof CommandSender ? (CommandSender) target : new CommandSender(target);

            if(!PlaceholderReplacer.process(sender, text, sender::sendMessage))
                sender.sendMessage(text);

            return;
        }

        if(PlaceholderReplacer.process(target, text, result -> translator.send(target, result))) return;

        translator.send(target, text);
    }

    public static String replaceMessage(String text) {
        return replaceMessage(null, text);
    }

    public static String replaceMessage(Object targetObj, String text) {
        CommandSender sender = targetObj instanceof CommandSender ? (CommandSender) targetObj : new CommandSender(targetObj);
        text = text.replace("%executor%", sender.isPlayer() ? sender.getName() : "").replace("&", "§").replace("%prefix%", Storage.ConfigSections.Messages.PREFIX.PREFIX).replace("%token%", Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED || Reflection.isProxyServer() ? Storage.TOKEN : "/").replace("%sync_server_name%", Storage.SERVER_NAME == null ? "/" : Storage.SERVER_NAME).replace("%current_version%", Storage.CURRENT_VERSION).replace("%newest_version%", Storage.NEWER_VERSION).replace("\\n", "\n");
        return PlaceholderReplacer.replace(targetObj, text);
    }

    public static String replaceMessageString(String rawText, String... replacements) {
        return replaceMessage(StringUtils.replace(rawText, replacements));
    }

    public static String replaceMessageString(Object targetObj, String rawText, String... replacements) {
        return replaceMessage(targetObj, StringUtils.replace(rawText, replacements));
    }

    public static List<String> replaceMessageList(MultipleMessagesHelper rawText, String... replacements) {
        return replaceMessageList(rawText.getLines(), replacements);
    }

    public static List<String> replaceMessageList(List<String> rawText, String... replacements) {
        final HashMap<String, String> REPLACEMENTS = new HashMap<>();
        List<String> result = new ArrayList<>();

        if(replacements != null) {
            String firstReplacementInput = null, secondReplacementInput = null;
            for (String replacement : replacements) {
                if (firstReplacementInput == null) firstReplacementInput = replacement;
                else secondReplacementInput = replacement;

                if (firstReplacementInput != null && secondReplacementInput != null) {
                    REPLACEMENTS.put(firstReplacementInput, secondReplacementInput);
                    firstReplacementInput = null;
                    secondReplacementInput = null;
                }
            }
        }

        rawText.forEach(text -> {
            if(replacements != null)
                for (Map.Entry<String, String> entry : REPLACEMENTS.entrySet())
                    text = text.replace(entry.getKey(), entry.getValue());
            String resultText = replaceMessage(text);
            result.add(resultText);
        });

        return result;
    }

    public static String translateIntoMiniMessage(String text) {
        return translator.translate(replaceMessage(text));
    }

    public static void closeAudiences() {
        if(translator == null) return;
        translator.close();
    }

    public static boolean isSupported() {
        return support;
    }
}
