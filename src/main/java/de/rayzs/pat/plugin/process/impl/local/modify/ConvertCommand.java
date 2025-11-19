package de.rayzs.pat.plugin.process.impl.local.modify;

import de.rayzs.pat.api.command.ProCommand;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.converter.Converter;
import de.rayzs.pat.plugin.converter.StorageConverter;
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
        Converter converter = StorageConverter.getConverter(converterName);
        if (converter == null) {
            sender.sendMessage(StringUtils.replace(Storage.ConfigSections.Messages.CONVERT.INVALID_CONVERTER,
                    "%converter%", converterName)
            );

            return true;
        }

        converterName = converter.getPluginName();
        converter.apply();

        sender.sendMessage(StringUtils.replace(Storage.ConfigSections.Messages.CONVERT.SUCCESS,
                "%converter%", converterName)
        );

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return StorageConverter.getConverters().stream().toList();
    }
}
