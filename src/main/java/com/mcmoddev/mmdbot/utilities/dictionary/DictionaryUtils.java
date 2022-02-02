package com.mcmoddev.mmdbot.utilities.dictionary;

import com.google.gson.JsonObject;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.core.References;

import java.io.IOException;
import java.io.Serial;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class DictionaryUtils {

    private static int lastCode = 0;
    private static String lastErrorMessage = "";

    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public static final String TARGET_URL = "https://owlbot.info/api/v4/dictionary/%s";
    private static final Map<String, DictionaryEntry> CACHE = Collections.synchronizedMap(new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(final Map.Entry<String, DictionaryEntry> eldest) {
            return size() > 50;
        }
    });

    public static DictionaryEntry getDefinition(final String token, final String word) throws DictionaryException {
        final var wordLowercase = word.toLowerCase(Locale.ROOT);
        if (CACHE.containsKey(wordLowercase)) {
            return CACHE.get(wordLowercase);
        }
        String definition;
        try {
            definition = post(token, word);
        } catch (IOException | InterruptedException ioe) {
            throw new DictionaryException(lastErrorMessage, lastCode);
        }

        if (lastCode == 404) {
            throw new DictionaryException("No definition", lastCode);
        }

        final var entry = DictionaryEntry.fromJson(References.GSON.fromJson(definition, JsonObject.class));
        CACHE.put(wordLowercase, entry);
        return entry;
    }

    public static DictionaryEntry getDefinition(final String word) throws DictionaryException {
        return getDefinition(MMDBot.getConfig().getOwlbotToken(), word);
    }

    public static DictionaryEntry getDefinitionNoException(final String token, final String word) {
        try {
            return getDefinition(token, word);
        } catch (DictionaryException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static DictionaryEntry getDefinitionNoException(final String word) {
        return getDefinitionNoException(MMDBot.getConfig().getOwlbotToken(), word);
    }

    private static String post(final String token, final String word) throws InterruptedException, IOException {
        final URI target = URI.create(TARGET_URL.formatted(word));

        var request = HttpRequest.newBuilder(target)
            .header("Content-Type", "application/json")
            .header("Authorization", "Token %s".formatted(token))
            .GET()
            .build();

        var response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assignLastCode(response);
        return response.body();
    }

    private static void assignLastCode(HttpResponse<String> response) {
        lastCode = response.statusCode();
        lastErrorMessage = response.body();
    }

    public static boolean hasToken() {
        return !MMDBot.getConfig().getOwlbotToken().isBlank();
    }

    public static final class DictionaryException extends Exception {

        @Serial
        private static final long serialVersionUID = -12801201302329300L;

        private final String error;
        private final int errorCode;

        DictionaryException(final String error, final int errorCode) {
            this.error = error;
            this.errorCode = errorCode;
        }

        public int getErrorCode() {
            return errorCode;
        }

        public String getError() {
            return error;
        }

        @Override
        public String toString() {
            return "HTTP error: %s, message: %s".formatted(errorCode, error);
        }

    }

}
