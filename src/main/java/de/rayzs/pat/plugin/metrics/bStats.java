package de.rayzs.pat.plugin.metrics;

import de.rayzs.pat.plugin.metrics.impl.BukkitMetrics;
import de.rayzs.pat.plugin.metrics.impl.BungeeMetrics;
import de.rayzs.pat.utils.Reflection;

public class bStats {

    public static void initialize(Object pluginObj) {
        if (Reflection.isProxyServer()) new BungeeMetrics(pluginObj, 20090);
        else new BukkitMetrics(pluginObj, 20089);
    }
}
