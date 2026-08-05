package de.rayzs.pat.plugin.command.commands.local.modify;

import de.rayzs.pat.plugin.command.commands.ProCommand;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.system.converter.Converter;
import de.rayzs.pat.plugin.system.converter.StorageConverter;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.sender.CommandSender;

import java.util.List;

public class ConvertCommand extends ProCommand {

    public ConvertCommand() {
        super(
                "convert",
                ""
        );
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if (args.length != 1)
            return false;

        String converterName = args[0];
        Converter converter = StorageConverter.get().getConverter(converterName);
        if (converter == null) {
            sender.sendMessage(StringUtils.replace(Storage.ConfigSections.Messages.CONVERT.INVALID_CONVERTER,
                    "%converter%", converterName)
            );

            return true;
        }

        converterName = converter.getPluginName();
        sender.sendMessage(StringUtils.replace(Storage.ConfigSections.Messages.CONVERT.SUCCESS,
                "%converter%", converterName)
        );

        converter.apply(sender);
        Storage.reload();
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return StorageConverter.get().getConverters().stream().toList();
    }
}
