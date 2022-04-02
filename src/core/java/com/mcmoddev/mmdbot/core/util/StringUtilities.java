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
package com.mcmoddev.mmdbot.core.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@UtilityClass
public class StringUtilities {

    /**
     * Computes the candidate that matches the given query string best.
     *
     * It is given that, if the candidates contain the query literally, the query will also be the
     * returned match. If the candidates do not contain the query literally, the best match will be
     * determined. The measures for this are unspecified.
     *
     * @param query the query string to find a match for
     * @param candidates the stream of candidates to select a match from
     * @param <S> the type of the candidates
     * @return the best matching candidate, or empty iff the candidates are empty
     */
    public static <S extends CharSequence> Optional<S> closestMatch(@NonNull CharSequence query,
                                                                    @NonNull Stream<S> candidates) {
        return candidates
            .min(Comparator.comparingInt(candidate -> editDistance(query, candidate)));
    }

    /**
     * Distance to receive {@code destination} from {@code source} by editing.
     *
     * For example {@code editDistance("hello", "hallo")} is {@code 1}.
     *
     * @param source the source string to start with
     * @param destination the destination string to receive by editing the source
     * @return the edit distance
     */
    public static int editDistance(@NonNull CharSequence source,
                                   @NonNull CharSequence destination) {
        final var table = computeLevenshteinDistanceTable(source, destination);
        final var rows = table.length;
        final var columns = table[0].length;

        return table[rows - 1][columns - 1];
    }

    /**
     * Distance to receive a prefix of {@code destination} from {@code source} by editing that
     * minimizes the distance.
     *
     * For example {@code prefixEditDistance("foa", "foobar")} is {@code 1}.
     *
     * @param source the source string to start with
     * @param destination the destination string to receive a prefix of by editing the source
     * @return the prefix edit distance
     */
    public static int prefixEditDistance(@NonNull CharSequence source,
                                         @NonNull CharSequence destination) {
        final var table = computeLevenshteinDistanceTable(source, destination);
        final var lastRowIndex = table.length - 1;

        return Arrays.stream(table[lastRowIndex]).min().orElseThrow();
    }

    /**
     * Computes the Levenshtein distance table for the given strings. See
     * <a href="https://en.wikipedia.org/wiki/Levenshtein_distance">Levenshtein distance</a> for
     * details.
     *
     * An example for {@code "abc"} to {@code "abcdefg"} would be:
     *
     * <pre>
     *   | 0 a b c d e f g
     * -------------------
     * 0 | 0 1 2 3 4 5 6 7
     * a | 1 0 1 2 3 4 5 6
     * b | 2 1 0 1 2 3 4 5
     * c | 3 2 1 0 1 2 3 4
     * </pre>
     *
     * @param source the source string to start with
     * @param destination the destination string to receive by editing the source
     * @return the levenshtein distance table
     */
    private static int[][] computeLevenshteinDistanceTable(@NonNull CharSequence source, @NonNull CharSequence destination) {
        final var rows = source.length() + 1;
        final var columns = destination.length() + 1;
        final var table = new int[rows][columns];

        // Initialize first row and column for distances from the empty word to the target word
        for (int y = 0; y < columns; y++) {
            table[0][y] = y;
        }
        for (int x = 0; x < rows; x++) {
            table[x][0] = x;
        }

        // Process row by row, selecting diagonal candidates
        for (int x = 1; x < rows; x++) {
            for (int y = 1; y < columns; y++) {
                // Take minimum of all candidates
                int upperCandidate = table[x - 1][y] + 1;
                int leftCandidate = table[x][y - 1] + 1;
                int diagonalCandidate = table[x - 1][y - 1];
                if (source.charAt(x - 1) != destination.charAt(y - 1)) {
                    diagonalCandidate++;
                }

                final var bestCandidate = IntStream.of(upperCandidate, leftCandidate, diagonalCandidate)
                    .min()
                    .orElseThrow();
                table[x][y] = bestCandidate;
            }
        }

        return table;
    }

}
