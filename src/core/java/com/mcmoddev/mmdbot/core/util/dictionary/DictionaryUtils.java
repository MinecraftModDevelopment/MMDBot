/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * Specifically version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 * https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 */
package com.mcmoddev.mmdbot.core.util.dictionary;

import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.ExecutionError;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.google.gson.JsonObject;
import com.mcmoddev.mmdbot.core.util.Constants;
import io.github.matyrobbrt.curseforgeapi.util.Utils;
import lombok.NonNull;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Serial;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public final class DictionaryUtils {

    private static int lastCode = 0;
    private static String lastErrorMessage = "";

    @NonNull
    private static String token = "";

    /**
     * Sets the OwlBot token used for dictionary lookup.
     *
     * @param token the token
     */
    public static void setToken(@Nullable String token) {
        if (token != null) {
            DictionaryUtils.token = token;
        }
    }

    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public static final DictionaryEntry CURLE_ENTRY = new DictionaryEntry("curle", "kəːle",
        List.of(new DictionaryDefinition("noun", "A seemingly time travelling computer woman with a technological curse, a teleporting penny, and no fucks to give whatsoever",
            "Curle is unique in her own way.", "https://cdn.discordapp.com/attachments/797440750971387925/939094064605831199/bolbmas.png", "<:bolbmas:879868298584526880>")));

    public static final String TARGET_URL = "https://owlbot.info/api/v4/dictionary/%s";

    private static final Cache<String, DictionaryEntry> CACHE = CacheBuilder.newBuilder()
        .initialCapacity(25)
        .maximumSize(50)
        .softValues()
        .build();

    public static DictionaryEntry getDefinition(final String token, final String word) throws DictionaryException {
        final var wordLowercase = word.toLowerCase(Locale.ROOT);
        if (wordLowercase.equals("curle")) {
            return CURLE_ENTRY;
        }

        try {
            return CACHE.get(wordLowercase, () -> {
                String definition;
                try {
                    definition = post(token, word);
                } catch (IOException | InterruptedException ioe) {
                    throw new DictionaryException(lastErrorMessage, lastCode);
                }

                if (lastCode == 404) {
                    throw new DictionaryException("No definition", lastCode);
                }

                return DictionaryEntry.fromJson(Constants.Gsons.GSON.fromJson(definition, JsonObject.class));
            });
        } catch (ExecutionException | UncheckedExecutionException | ExecutionError e) {
            // The only checked exception we expect is this one, any others are unchecked ones
            if (e.getCause() instanceof DictionaryException dictionaryEx) throw dictionaryEx;
            Throwables.throwIfUnchecked(e.getCause());
            throw new AssertionError(e); // See javadocs of throwIfUnchecked
        }
    }

    public static DictionaryEntry getDefinition(final String word) throws DictionaryException {
        return getDefinition(token, word);
    }

    public static DictionaryEntry getDefinitionNoException(final String token, final String word) {
        try {
            return getDefinition(token, word);
        } catch (DictionaryException e) {
            Utils.sneakyThrow(e);
            return null;
        }
    }

    public static DictionaryEntry getDefinitionNoException(final String word) {
        return getDefinitionNoException(token, word);
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
        return !token.isBlank();
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
