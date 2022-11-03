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
package com.mcmoddev.mmdbot.painter.serveravatar;

public record AvatarConfiguration(
    int textColour, int backgroundPatternColour, int ringColour,
    float textAlpha, float backgroundPatternAlpha, float ringAlpha,
    boolean isCircular, boolean hasRing, boolean hasBackgroundPattern, boolean hasBackground
) {

    public static final float
        DEFAULT_TEXT_ALPHA = 0.65f,
        DEFAULT_BG_PATTERN_ALPHA = 0.01f,
        DEFAULT_RING_ALPHA = 0.65f;

    public static Builder builder() {
        return new Builder();
    }

    @SuppressWarnings("UnusedReturnValue")
    public static final class Builder {
        private int textColour, backgroundPatternColour, ringColour;
        private float textAlpha = DEFAULT_TEXT_ALPHA, backgroundPatternAlpha = DEFAULT_BG_PATTERN_ALPHA, ringAlpha = DEFAULT_RING_ALPHA;
        private boolean isCircular, hasRing, hasBackgroundPattern = true, hasBackground = true;

        public Builder setColour(int colour) {
            if (textColour == 0) setTextColour(colour);
            if (ringColour == 0) setRingColour(colour);
            if (backgroundPatternColour == 0) setBackgroundPatternColour(colour);
            return this;
        }

        public Builder setTextColour(int textColour) {
            this.textColour = textColour;
            return this;
        }

        public Builder setBackgroundPatternColour(final int backgroundPatternColour) {
            this.backgroundPatternColour = backgroundPatternColour;
            return this;
        }

        public Builder setRingColour(final int ringColour) {
            this.ringColour = ringColour;
            return this;
        }

        public Builder setTextAlpha(final float textAlpha) {
            this.textAlpha = textAlpha;
            return this;
        }

        public Builder setBackgroundPatternAlpha(final float backgroundPatternAlpha) {
            this.backgroundPatternAlpha = backgroundPatternAlpha;
            return this;
        }

        public Builder setRingAlpha(final float ringAlpha) {
            this.ringAlpha = ringAlpha;
            return this;
        }

        public Builder setCircular(final boolean circular) {
            isCircular = circular;
            return this;
        }

        public Builder setHasRing(final boolean hasRing) {
            this.hasRing = hasRing;
            return this;
        }

        public Builder setHasBackgroundPattern(final boolean hasBackgroundPattern) {
            this.hasBackgroundPattern = hasBackgroundPattern;
            return this;
        }

        public Builder setHasBackground(final boolean hasBackground) {
            this.hasBackground = hasBackground;
            return this;
        }

        public AvatarConfiguration build() {
            return new AvatarConfiguration(
                textColour, backgroundPatternColour, ringColour,
                textAlpha, backgroundPatternAlpha, ringAlpha,
                isCircular, hasRing, hasBackgroundPattern, hasBackground
            );
        }
    }
}
