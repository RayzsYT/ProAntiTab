package de.rayzs.pat.api.storage.config.messages;

import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;
import de.rayzs.pat.utils.configuration.helper.MultipleMessagesHelper;

public class ConvertSection extends ConfigStorage {

    public MultipleMessagesHelper MESSAGE;

    public ConvertSection() {
        super("convert");
    }

    public String INVALID_CONVERTER, SUCCESS;

    @Override
    public void load() {
        super.load();

        INVALID_CONVERTER = new ConfigSectionHelper<String>(this, "converter-not-found", "&cConverter or necessary files could not be found!").getOrSet();
        SUCCESS = new ConfigSectionHelper<String>(this, "success", "&aSuccessfully converted &e%converter% &ainto the PAT storage.yml!").getOrSet();

    }
}
