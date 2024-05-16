package de.rayzs.pat.utils.message;

public interface Translator {

    String translate(String text);
    void send(Object target, String text);
    void close();
}
