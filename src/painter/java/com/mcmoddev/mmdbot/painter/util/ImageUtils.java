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
package com.mcmoddev.mmdbot.painter.util;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageUtils {
    @SuppressWarnings("unused")
    private static GraphicsConfiguration getGraphicsConfiguration() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    }

    private static BufferedImage createCompatibleImage(int width, int height) {
        // BufferedImage image = getGraphicsConfiguration().createCompatibleImage(width, height, transparency);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        image.coerceData(true);
        return image;
    }

    public static void applyQualityRenderingHints(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    public static BufferedImage generateMask(BufferedImage imgSource, Color color, float alpha) {
        int imgWidth = imgSource.getWidth();
        int imgHeight = imgSource.getHeight();

        BufferedImage imgMask = createCompatibleImage(imgWidth, imgHeight);
        Graphics2D g2 = imgMask.createGraphics();
        applyQualityRenderingHints(g2);

        g2.drawImage(imgSource, 0, 0, null);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, alpha));
        g2.setColor(color);

        g2.fillRect(0, 0, imgSource.getWidth(), imgSource.getHeight());
        g2.dispose();

        return imgMask;
    }

    public static BufferedImage tint(BufferedImage image, Color colour, float alpha) {
        final var mask = generateMask(image, colour, alpha);
        return tint(image, mask);
    }

    public static BufferedImage tint(BufferedImage master, BufferedImage tint) {
        int imgWidth = master.getWidth();
        int imgHeight = master.getHeight();

        BufferedImage tinted = createCompatibleImage(imgWidth, imgHeight);
        Graphics2D g2 = tinted.createGraphics();
        applyQualityRenderingHints(g2);
        g2.drawImage(master, 0, 0, null);
        g2.drawImage(tint, 0, 0, null);
        g2.dispose();

        return tinted;
    }

    @SuppressWarnings("unused") // To be used as fallback in case the other method doesn't work on the bot host
    private static BufferedImage tintImage(Image original, int r, int g, int b) {
        int width = original.getWidth(null);
        int height = original.getHeight(null);
        BufferedImage tinted = new BufferedImage(width, height, BufferedImage.TRANSLUCENT);
        Graphics graphics = tinted.getGraphics();
        graphics.drawImage(original, 0, 0, width, height, null);
        Color c = new Color(r, g, b,128);
        Color n = new Color(0,0,0,0);
        BufferedImage tint = new BufferedImage(width, height, BufferedImage.TRANSLUCENT);
        for (int i = 0 ; i < width ; i++){
            for (int j = 0 ; j < height ; j++){
                if (tinted.getRGB(i, j) != n.getRGB()){
                    tint.setRGB(i, j, c.getRGB());
                }
            }
        }
        graphics.drawImage(tint, 0, 0, null);
        graphics.dispose();
        return tinted;
    }

    public static BufferedImage cropToCircle(BufferedImage img, int diameter) {
        int d = diameter > 0 ? diameter : Math.min(img.getWidth(), img.getHeight());
        BufferedImage newImage = createCompatibleImage(d, d);
        Graphics2D graphics = newImage.createGraphics();
        applyQualityRenderingHints(graphics);
        Shape circle = new Ellipse2D.Double(0,0, d, d);
        graphics.setClip(circle);
        graphics.drawImage(img, -((img.getHeight() - newImage.getHeight()) / 2), -((img.getWidth() - newImage.getWidth()) / 2), null);
        graphics.dispose();
        return newImage;
    }

    public static BufferedImage centre(BufferedImage source, int width, int height) {
        BufferedImage img = createCompatibleImage(width, height);
        Graphics2D graphics = img.createGraphics();
        applyQualityRenderingHints(graphics);
        graphics.drawImage(source, (width - source.getWidth()) / 2, (height - source.getHeight()) / 2, null);
        graphics.dispose();
        return img;
    }

    public static byte[] toBytes(RenderedImage image, String format) throws IOException {
        final var bos = new ByteArrayOutputStream();
        ImageIO.write(image, format, bos);
        return bos.toByteArray();
    }
}
