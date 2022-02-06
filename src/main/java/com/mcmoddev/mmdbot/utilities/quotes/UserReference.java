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
package com.mcmoddev.mmdbot.utilities.quotes;

import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.utilities.scripting.ExposeScripting;
import net.dv8tion.jda.api.entities.User;

/**
 * The information containing "who" or "what" a quote is referencing.
 * Can be for either the thing being quoted, or the thing that created the quote.
 * <p>
 * Designed to only be directly read by the Quotes utility class.
 * Some design decisions are implemented to take advantage of that while maintaining flexibility.
 *
 * @author Curle
 */
public final class UserReference {

    /**
     * Identifies which of the fields in this class should be read to retrieve the proper data.
     */
    private ReferenceType referenceType;

    /**
     * The data for a SNOWFLAKE reference.
     * <p>
     * Encodes a Discord User ID as a Snowflake.
     */
    private long snowflakeData;

    /**
     * The data for a STRING reference.
     * <p>
     * Encodes an arbitrary source that can be appended to the output.
     */
    private String stringData;

    /**
     * The data for an ANONYMOUS reference.
     * <p>
     * Encodes the string "Anonymous" which is always returned as the source.
     */
    private static final String anonymousData = "Anonymous";

    /**
     * Identifies which of the fields in this class should be read to retrieve the proper data.
     *
     * @return The Reference Type of this User Reference.
     */
    @ExposeScripting
    public ReferenceType getReferenceType() {
        return referenceType;
    }

    /**
     * Set the Type of the current Reference.
     *
     * @param pReferenceType The new Type to take over
     */
    public void setReferenceType(final ReferenceType pReferenceType) {
        this.referenceType = pReferenceType;
    }

    /**
     * The data for a SNOWFLAKE reference.
     * <p>
     * Encodes a Discord User ID as a Snowflake.
     *
     * @return The encoded user ID as a long form snowflake.
     */
    @ExposeScripting
    public long getSnowflakeData() {
        return snowflakeData;
    }

    /**
     * Set the snowflake data of the current Reference.
     *
     * @param pSnowflakeData The Snowflake data to take over
     */
    public void setSnowflakeData(final long pSnowflakeData) {
        this.snowflakeData = pSnowflakeData;
    }

    /**
     * The data for a STRING reference.
     * <p>
     * Encodes an arbitrary source that can be appended to the output.
     *
     * @return The encoded String reference,
     */
    @ExposeScripting
    public String getStringData() {
        return stringData;
    }

    /**
     * Set the String data of the current Reference.
     *
     * @param pStringData The String data to take over
     */
    public void setStringData(final String pStringData) {
        this.stringData = pStringData;
    }

    /**
     * The data for an ANONYMOUS reference.
     * <p>
     * Encodes the string "Anonymous" which is always returned as the source.
     *
     * @return The string "Anonymous".
     */
    @ExposeScripting
    public String getAnonymousData() {
        return anonymousData;
    }

    /**
     * Convert the Reference to its closest String representation.
     *
     * @return A mention if the user is a Snowflake and is in the current guild,
     * An ID if the user is a snowflake and is not in the current guild,
     * A message if the user is anonymous or String.
     */
    @ExposeScripting
    public String resolveReference() {
        switch (getReferenceType()) {
            case SNOWFLAKE:
                // Try to find the user's data in a server
                User user = MMDBot.getInstance().getUserById(getSnowflakeData());
                // If we have it...
                if (user != null) {
                    return user.getAsTag();
                } else {
                    return String.valueOf(getSnowflakeData());
                }
            case STRING:
                return getStringData();

            case ANONYMOUS: // Intentional fallthrough.
            default:
                return getAnonymousData();
        }
    }

    /**
     * A Quote can reference one of these things:
     * - A Discord user
     * - An external source
     * - An anonymous source.
     * <p>
     * The String name is encoded into this enum to facilitate de/serialization.
     * TODO: this can probably be replaced with a two-way map.
     */
    public enum ReferenceType {
        /**
         * Discord User ID.
         */
        SNOWFLAKE("snowflake"),

        /**
         * External named source.
         */
        STRING("string"),

        /**
         * Hacker group. Er, an unnamed source.
         */
        ANONYMOUS("anonymous");

        /**
         * Holds the String representation of the name of the enum entry.
         */
        private final String name;

        /**
         * Construct a new ReferenceType.
         * Internal use only.
         *
         * @param pName String representation of the name.
         */
        ReferenceType(final String pName) {
            this.name = pName;
        }

        /**
         * Retrieve the name of this enum entry, as a String.
         * For serialization.
         *
         * @return The name of this enum entry, as a String.
         */
        @ExposeScripting
        public String getName() {
            return this.name;
        }

        /**
         * Get ReferenceType instance from the name.
         * For deserialization.
         *
         * @param pName The name of the enum value to retrieve.
         * @return The ReferenceType value requested.
         */
        public static ReferenceType of(final String pName) {
            return switch (pName) {
                case "snowflake" -> SNOWFLAKE;
                case "string" -> STRING;
                case "anonymous" -> ANONYMOUS;
                default -> ANONYMOUS;
            };
        }

    }


    /**
     * No attribution constructor.
     * Sets the referenceType to ANONYMOUS.
     */
    public UserReference() {
        setReferenceType(ReferenceType.ANONYMOUS);
        setSnowflakeData(0);
        setStringData("");
    }

    /**
     * SNOWFLAKE attribution constructor.
     * Sets the referenceType to SNOWFLAKE.
     * Sets the snowflakeData field to the given ID.
     *
     * @param id The Discord User ID as a Snowflake for the user to reference.
     */
    public UserReference(final long id) {
        setReferenceType(ReferenceType.SNOWFLAKE);
        setSnowflakeData(id);
        setStringData("");
    }

    /**
     * STRING attribution constructor.
     * Sets the referenceType to STRING.
     * Sets the stringData field to the given String.
     *
     * @param source The String to use as the reference.
     */
    public UserReference(final String source) {
        setReferenceType(ReferenceType.STRING);
        setStringData(source);
        setSnowflakeData(0);
    }

}
