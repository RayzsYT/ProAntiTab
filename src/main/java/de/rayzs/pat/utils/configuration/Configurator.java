package de.rayzs.pat.utils.configuration;

import de.rayzs.pat.utils.configuration.impl.*;
import de.rayzs.pat.utils.*;
import java.util.HashMap;
import java.net.*;
import java.io.*;

public class Configurator {

    private static final HashMap<String, ConfigurationBuilder> CONFIGURATION_HASHES = new HashMap<>();

    private static final String FILE_PATH = "./plugins/ProAntiTab";

    public static ConfigurationBuilder get(String fileName, String filePath) {
        String keyName = filePath + "/" + fileName;
        if(CONFIGURATION_HASHES.containsKey(keyName))
            return CONFIGURATION_HASHES.get(keyName);

        ConfigurationBuilder configurationBuilder = Reflection.isProxyServer()
                ? new ProxyConfigurationBuilder(fileName, filePath)
                : new BukkitConfigurationBuilder(fileName, filePath);
        CONFIGURATION_HASHES.put(keyName, configurationBuilder);
        return configurationBuilder;
    }

    public static ConfigurationBuilder get(String fileName) {
        String keyName = FILE_PATH + "/" + fileName;
        if(CONFIGURATION_HASHES.containsKey(keyName))
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

    public static void createResourcedFile(String resourcePath, String exportResourcePath, boolean replace) {
        if (resourcePath == null || resourcePath.isEmpty())
            return;

        resourcePath = resourcePath.replace('\\', '/');
        InputStream inputStream = getResource(resourcePath);

        if (inputStream == null)
            return;

        File dataFolder = new File(FILE_PATH);
        File outputFile = new File(dataFolder, exportResourcePath);

        if (!dataFolder.exists())
            dataFolder.mkdirs();

        try {
            if (!outputFile.exists() || replace) {
                OutputStream outputStream = new FileOutputStream(outputFile);
                byte[] buffer = new byte[1024];
                int length;

                while((length = inputStream.read(buffer)) > 0)
                    outputStream.write(buffer, 0, length);

                outputStream.close();
                inputStream.close();
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
