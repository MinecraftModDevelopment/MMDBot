/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2023 <MMD - MinecraftModDevelopment>
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
package com.mcmoddev.mmdbot.painter.servericon;

import com.mcmoddev.mmdbot.painter.ThePainter;
import com.mcmoddev.mmdbot.painter.servericon.auto.AutomaticIconConfiguration;
import com.mcmoddev.mmdbot.painter.util.GifSequenceWriter;
import com.mcmoddev.mmdbot.painter.util.ImageUtils;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Objects;

public class ServerIconMaker {

    public static BufferedImage createIcon(IconConfiguration configuration) throws IOException {
        BufferedImage finalImage = ImageUtils.tint(getImage("text"), new Color(configuration.textColour()), configuration.textAlpha());

        if (configuration.hasBackgroundPattern()) {
            final BufferedImage bgPattern = ImageUtils.tint(getImage("bg_pattern"), new Color(configuration.backgroundPatternColour()), configuration.backgroundPatternAlpha());
            final Graphics2D g2d = bgPattern.createGraphics();
            ImageUtils.applyQualityRenderingHints(g2d);
            g2d.drawImage(finalImage, 0, 0, null);
            finalImage = bgPattern;
        }
        if (configuration.hasBackground()) {
            final BufferedImage background = getImage("background");
            final Graphics2D g2d = background.createGraphics();
            ImageUtils.applyQualityRenderingHints(g2d);
            g2d.drawImage(finalImage, 0, 0, null);
            finalImage = background;
        }

        if (configuration.hasRing()) {
            final BufferedImage ring = ImageUtils.tint(getImage("ring"), new Color(configuration.ringColour()), configuration.ringAlpha());
            final Graphics2D g2d = ring.createGraphics();
            ImageUtils.applyQualityRenderingHints(g2d);
            g2d.drawImage(ImageUtils.centre(ImageUtils.cropToCircle(finalImage, finalImage.getWidth() - 24 * 2), 512, 512), 0, 0, null);
            finalImage = ring;
        }
        if (configuration.isCircular()) {
            finalImage = ImageUtils.cropToCircle(finalImage, 0);
        }

        return finalImage;
    }

    public static byte[] createSlideshow(AutomaticIconConfiguration configuration) throws IOException {
        final var bos = new ByteArrayOutputStream();
        final var gif = new GifSequenceWriter(bos, BufferedImage.TYPE_INT_ARGB, 500, true);

        for (int day = 1; day <= configuration.colours().size(); day++) {
            final var image = configuration.createImage(day);
            final var g2 = image.createGraphics();
            final var font = new Font("Monospaced", Font.BOLD, 65);

            final var text = "Day " + day;
            g2.setFont(font);
            g2.drawString("Day " + day, (int) ((image.getWidth() - font.getStringBounds(text, g2.getFontRenderContext()).getWidth()) / 2), image.getHeight() - 50 - 40);

            g2.dispose();
            gif.writeToSequence(image);
        }

        gif.close();
        return bos.toByteArray();
    }

    private static BufferedImage getImage(String name) throws IOException {
        try (final InputStream is = Files.newInputStream(ThePainter.getInstance().getRunPath().resolve("servericon/" + name + ".png"))) {
            return ImageIO.read(Objects.requireNonNull(is));
        }
    }
}
