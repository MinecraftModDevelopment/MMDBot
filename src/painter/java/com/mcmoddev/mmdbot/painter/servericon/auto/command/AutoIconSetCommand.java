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
package com.mcmoddev.mmdbot.painter.servericon.auto.command;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.painter.ThePainter;
import com.mcmoddev.mmdbot.painter.servericon.GenerateIconCommand;
import com.mcmoddev.mmdbot.painter.servericon.ServerIconCommand;
import com.mcmoddev.mmdbot.painter.servericon.ServerIconMaker;
import com.mcmoddev.mmdbot.painter.servericon.auto.AutomaticIconConfiguration;
import com.mcmoddev.mmdbot.painter.servericon.auto.DayCounter;
import com.mcmoddev.mmdbot.painter.util.CooldownManager;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class AutoIconSetCommand extends SlashCommand implements EventListener {
    public static final AutoIconSetCommand INSTANCE = new AutoIconSetCommand();
    static final String BUTTON_ID = "generate-autoicon-slideshow";

    private AutoIconSetCommand() {
        this.name = "set";
        this.help = "Set the auto-icon configuration for the server.";
        this.options = List.of(
            new OptionData(OptionType.STRING, "color-range", "The start and end colours, split by a space. Example: '#ffff00 #d9d916'", true),
            new OptionData(OptionType.INTEGER, "days", "The amount of days the auto-icon should last for; default: 30"),
            new OptionData(OptionType.BOOLEAN, "ring", "If the generated avatars should have rings; default: false"),
            new OptionData(OptionType.BOOLEAN, "enable", "If the auto-icon should be started by default; default: true"),
            new OptionData(OptionType.CHANNEL, "log-channel", "Channel in which to log the icon changes.")
        );
        this.subcommandGroup = ServerIconCommand.AUTO_ICON_SUBCOMMAND;
    }

    @Override
    protected void execute(final SlashCommandEvent event) {
        event.deferReply()
            .onSuccess(it -> createAndSave(event))
            .flatMap(it -> it.editOriginal(new MessageEditBuilder()
                .setContent("Successfully set server auto icon!")
                .setActionRow(Button.primary(BUTTON_ID, "Generate preview"))
                .build()))
            .queue();
    }

    private static final CooldownManager BUTTON_COOLDOWN = new CooldownManager(10, TimeUnit.SECONDS);
    @Override
    public void onEvent(@NotNull final GenericEvent ge) {
        if (!(ge instanceof ButtonInteractionEvent event)) return;
        if (!(Objects.equals(event.getButton().getId(), BUTTON_ID))) return;

        if (!BUTTON_COOLDOWN.check(event.getUser())) {
            event.reply("You can use this button " + BUTTON_COOLDOWN.coolDownFriendly(event.getUser()) + ".")
                .setEphemeral(true).queue();
            return;
        }

        event.deferReply().queue();
        try {
            final var cfg = AutomaticIconConfiguration.get(event.getGuild().getId());
            if (cfg == null) {
                event.getHook().editOriginal("Server has no icon configuration!").queue();
                return;
            }

            final var bytes = ServerIconMaker.createSlideshow(cfg);
            event.getHook().editOriginalAttachments(
                FileUpload.fromData(bytes, "icons.gif")
            ).queue();
        } catch (IOException exception) {
            event.getHook().editOriginal("Encountered exception: *" + exception.getMessage() + "*").queue();
            ThePainter.LOGGER.error("Encountered exception generating slideshow: ", exception);
        }
    }

    @SneakyThrows
    private void createAndSave(SlashCommandEvent event) {
        create(event).save(event.getGuild().getId());

        final var days = DayCounter.read();
        days.setDay(event.getGuild(), 0, false);
        days.write();
    }

    public static AutomaticIconConfiguration create(SlashCommandEvent event) {
        final String[] colorsString = event.getOption("color-range", "", OptionMapping::getAsString).split(" ");
        final int[] colors = new int[] {GenerateIconCommand.readColour(colorsString[0]), GenerateIconCommand.readColour(colorsString[1])};
        final List<Integer> orderedColors = createColorList(
            new Color(Math.min(colors[0], colors[1])), new Color(Math.max(colors[0], colors[1])),
            event.getOption("days", 30, OptionMapping::getAsInt),
            colors[0] > colors[1]
        );
        return new AutomaticIconConfiguration(
            orderedColors,
            event.getOption("log-channel", 0L, it -> it.getAsChannel().getIdLong()),
            event.getOption("ring", false, OptionMapping::getAsBoolean),
            event.getOption("enable", true, OptionMapping::getAsBoolean)
        );
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    private static List<Integer> createColorList(Color start, Color end, int days, boolean inversed) {
        final List<Integer> colors = new ArrayList<>();

        var stepAmountR = (int)Math.floor((end.getRed() - start.getRed()) / days);
        var stepAmountG = (int)Math.floor((end.getGreen() - start.getGreen()) / days);
        var stepAmountB = (int)Math.floor((end.getBlue() - start.getBlue()) / days);

        int fromR = start.getRed(), fromG = start.getGreen(), fromB = start.getBlue();

        colors.add(start.getRGB());

        for (int i = 1; i < days - 1; i++) {
            Calculator minMax;
            // Red
            minMax = stepAmountR > 0 ? Math::min : Math::max;
            fromR = minMax.calc(fromR + stepAmountR, end.getRed());

            // Green
            minMax = stepAmountG > 0 ? Math::min : Math::max;
            fromG = minMax.calc(fromG + stepAmountG, end.getGreen());

            // Blue
            minMax = stepAmountB > 0 ? Math::min : Math::max;
            fromB = minMax.calc(fromB + stepAmountB, end.getBlue());
            colors.add(new Color(fromR, fromG, fromB).getRGB());
        }

        colors.add(end.getRGB());
        if (inversed) {
            colors.sort(Comparator.<Integer>naturalOrder().reversed());
        }

        return colors;
    }

    private interface Calculator {
        int calc(int a, int b);
    }
}
