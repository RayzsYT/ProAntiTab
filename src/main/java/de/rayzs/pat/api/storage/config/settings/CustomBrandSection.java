package de.rayzs.pat.api.storage.config.settings;

import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;
import de.rayzs.pat.utils.configuration.helper.MultipleMessagesHelper;

import java.util.Arrays;

public class CustomBrandSection extends ConfigStorage {

    public boolean ENABLED;
    public int REPEAT_DELAY;
    public MultipleMessagesHelper BRANDS;

    public CustomBrandSection() {
        super("custom-server-brand");
    }

    @Override
    public void load() {
        super.load();
        ENABLED = new ConfigSectionHelper<Boolean>(this, "enabled", false).getOrSet();
        REPEAT_DELAY = new ConfigSectionHelper<Integer>(this, "repeat-delay", 3).getOrSet();
        BRANDS = new MultipleMessagesHelper(this, "brands", Arrays.asList(
                "&f&lP&froAntiTab |",
                "&fP&lr&foAntiTab /",
                "&fPr&lo&fAntiTab -",
                "&fPro&lA&fntiTab |",
                "&fProA&ln&ftiTab \\",
                "&fProAn&lt&fiTab |",
                "&fProAnt&li&fTab /",
                "&fProAnti&lT&fab -",
                "&fProAnti&lT&fab \\",
                "&fProAntiT&la&fb |",
                "&fProAntiTa&lb&f /",
                "&fProAntiTab -",
                "&fProAntiTab \\"
        ));
    }
}
