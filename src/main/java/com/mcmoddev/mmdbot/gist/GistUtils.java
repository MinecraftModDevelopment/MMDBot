/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
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
package com.mcmoddev.mmdbot.gist;

import com.google.gson.JsonObject;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.core.References;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class GistUtils {

    private static int lastCode = 0;
    private static String lastErrorMessage = "";

    /**
     * Creates a new Gist
     * @param token the token of the account to create a gist for
     * @param gist the gist to create
     * @return the created gist
     * @throws GistException any exception that occurred while creating the gist (usually, and IO one)
     */
    public static ExistingGist create(final String token, final Gist gist) throws GistException {
        String newGist;
        try {
            newGist = post(token, "", gist.toString());
        } catch (IOException ioe) {
            int code = lastCode;
            if (code == 404) {
                return null;
            }
            throw new GistException(lastErrorMessage, lastCode);
        }

        return ExistingGist.fromJson(References.GSON.fromJson(newGist, JsonObject.class));
    }

    /**
     * Posts a request to GitHub
     * @param token a token for accessing GitHub
     * @param operation the operation to execute
     * @param postMessage the message to post
     * @return the response
     */
    private static String post(final String token, final String operation, final String postMessage) throws IOException {
        final URL target = new URL("https://api.github.com/gists" + operation);
        final HttpsURLConnection connection = (HttpsURLConnection) target.openConnection();

        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "token " + token);

        try (OutputStream output = connection.getOutputStream(); final DataOutputStream requestBody = new DataOutputStream(output)) {
            requestBody.writeBytes(postMessage);
        }

        String respone;
        try {
            respone = getResponse(connection.getInputStream());
        } finally {
            assignLastCode(connection);
        }

        return respone;
    }

    private static String getResponse(final InputStream stream) throws IOException {
        StringBuilder full = new StringBuilder();
        String line;

        try (stream; InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8); BufferedReader streamBuf = new BufferedReader(reader)) {
            while ((line = streamBuf.readLine()) != null) {
                full.append(line);
            }
        }

        return full.toString();
    }

    public static String readFile(File f) throws IOException {
        return readInputStream(new FileInputStream(f));
    }

    public static String readInputStream(InputStream stream) throws IOException {
        StringBuilder content = new StringBuilder();

        try (stream; InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8); BufferedReader buffer = new BufferedReader(reader)) {
            String line;
            while ((line = buffer.readLine()) != null) {
                content.append(line).append('\n');
            }
        }

        return content.toString();
    }

    private static void assignLastCode(HttpsURLConnection conn) {
        try {
            lastCode = conn.getResponseCode();
            lastErrorMessage = conn.getResponseMessage();
        } catch (IOException e) {
            lastCode = -1;
            lastErrorMessage = "Unknown";
        }
    }

    public static boolean hasToken() {
        return !MMDBot.getConfig().getGithubToken().isBlank();
    }

    public static final class GistException extends Exception {

        private final String error;
        private final int errorCode;

        GistException(final String error, final int errorCode) {
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
