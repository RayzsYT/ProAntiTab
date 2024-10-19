package de.rayzs.pat.utils;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConnectionBuilder {

    private String url = null, response = null;
    private List<String> responseList = new ArrayList<>();
    private Object[] parameters = null;

    public ConnectionBuilder setUrl(String url) {
        this.url = url;
        return this;
    }

    public ConnectionBuilder setProperties(Object... parameters) {
        this.parameters = parameters;
        return this;
    }

    public ConnectionBuilder connect() {
        try {
            String rawUrl = url;
            URL url = new URL(rawUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            if (parameters != null && parameters.length > 0) {
                Object firstParam = null, secondParam = null;
                for (Object parameter : parameters) {
                    if (firstParam == null) firstParam = parameter;
                    else if (secondParam == null) secondParam = parameter;
                    else {
                        connection.setRequestProperty((String) firstParam, (String) secondParam);
                        firstParam = null;
                        secondParam = null;
                    }
                }
            }

            Scanner scanner = new Scanner(connection.getInputStream());
            StringBuilder builder = new StringBuilder("\\");
            String next;

            while (scanner.hasNextLine()) {
                next = scanner.nextLine();
                responseList.add(next);
                builder.append(" ").append(next);
            }

            response = builder.toString().replace("\\ ", "");

        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return this;
    }

    public boolean hasResponse() {
        return response != null;
    }

    public String getResponse() {
        return response;
    }

    public List<String> getResponseList() {
        return responseList;
    }
}