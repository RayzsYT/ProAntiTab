package de.rayzs.pat.utils.configuration;

import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.configuration.impl.BukkitConfigurationBuilder;
import de.rayzs.pat.utils.configuration.impl.ProxyConfigurationBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

public class Configurator {

    private static final HashMap<String, ConfigurationBuilder> CONFIGURATION_HASHES = new HashMap<>();

    public static ConfigurationBuilder get(String fileName, String filePath) {
        String keyName = filePath + "/" + fileName;
        if (CONFIGURATION_HASHES.containsKey(keyName))
            return CONFIGURATION_HASHES.get(keyName);

        ConfigurationBuilder configurationBuilder = Reflection.isProxyServer()
                ? new ProxyConfigurationBuilder(fileName, filePath)
                : new BukkitConfigurationBuilder(fileName, filePath);
        CONFIGURATION_HASHES.put(keyName, configurationBuilder);
        return configurationBuilder;
    }

    public static ConfigurationBuilder get(String fileName) {
        String keyName = "./plugins/ProAntiTab/" + fileName;
        if (CONFIGURATION_HASHES.containsKey(keyName))
            return CONFIGURATION_HASHES.get(keyName);

        ConfigurationBuilder configurationBuilder = Reflection.isProxyServer()
                ? new ProxyConfigurationBuilder(fileName)
                : new BukkitConfigurationBuilder(fileName);
        CONFIGURATION_HASHES.put(keyName, configurationBuilder);
        return configurationBuilder;
    }

    public static InputStream getResource(String filename) {
        try {
            URL url = Configurator.class.getClassLoader().getResource(filename);
            if (url == null) {
                return null;
            } else {
                URLConnection connection = url.openConnection();
                connection.setUseCaches(false);
                return connection.getInputStream();
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
        }
    }

    public static void createResourcedFile(File dataFolder, String resourcePath, String exportRessourcePath, boolean replace) {
        if (resourcePath == null || resourcePath.equals("")) return;
        resourcePath = resourcePath.replace('\\', '/');
        InputStream inputStream = getResource(resourcePath);
        if (inputStream == null) return;

        File outputFile = new File(dataFolder, exportRessourcePath);
        if (!dataFolder.exists()) dataFolder.mkdirs();

        try {
            if (!outputFile.exists() || replace) {
                OutputStream outputStream = new FileOutputStream(outputFile);
                byte[] buffer = new byte[1024];
                int length;

                while ((length = inputStream.read(buffer)) > 0) outputStream.write(buffer, 0, length);
                outputStream.close();
                inputStream.close();
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public static void createResourcedFile(String dataFolder, String resourcePath, String exportRessourcePath, boolean replace) {
        createResourcedFile(new File(dataFolder), resourcePath, exportRessourcePath, replace);
    }
}
