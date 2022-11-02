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
package com.mcmoddev.mmdbot.watcher.serveravatar;

import com.mcmoddev.mmdbot.watcher.TheWatcher;
import com.mcmoddev.mmdbot.watcher.util.ImageUtils;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Objects;

public class ServerAvatarMaker {

    public static BufferedImage createAvatar(AvatarConfiguration configuration) throws IOException {
        BufferedImage finalImage = ImageUtils.tint(getImage("text"), new Color(configuration.textColour()), configuration.textAlpha());

        if (configuration.hasBackgroundPattern()) {
            final BufferedImage bgPattern = ImageUtils.tint(getImage("bg_pattern"), new Color(configuration.backgroundPatternColour()), configuration.backgroundPatternAlpha());
            bgPattern.getGraphics().drawImage(finalImage, 0, 0, null);
            finalImage = bgPattern;
        }
        if (configuration.hasBackground()) {
            final BufferedImage background = getImage("background");
            background.getGraphics().drawImage(finalImage, 0, 0, null);
            finalImage = background;
        }

        if (configuration.hasRing()) {
            final BufferedImage ring = ImageUtils.tint(getImage("ring"), new Color(configuration.ringColour()), configuration.ringAlpha());
            ring.getGraphics().drawImage(ImageUtils.centre(ImageUtils.cropToCircle(finalImage, finalImage.getWidth() - 24 * 2), 512, 512), 0, 0, null);
            finalImage = ring;
        }
        if (configuration.isCircular()) {
            finalImage = ImageUtils.cropToCircle(finalImage, 0);
        }

        return finalImage;
    }

    private static BufferedImage getImage(String name) throws IOException {
        try (final InputStream is = Files.newInputStream(TheWatcher.getInstance().getRunPath().resolve("serveravatar/" + name + ".png"))) {
            return ImageIO.read(Objects.requireNonNull(is));
        }
    }
}
