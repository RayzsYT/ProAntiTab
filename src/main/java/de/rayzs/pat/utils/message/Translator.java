package de.rayzs.pat.utils.message;

public interface Translator {
    void send(Object target, String text);
    void close();
}
