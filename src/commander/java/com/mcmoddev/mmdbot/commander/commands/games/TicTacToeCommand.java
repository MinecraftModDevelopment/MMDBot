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
package com.mcmoddev.mmdbot.commander.commands.games;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.annotation.RegisterSlashCommand;
import com.mcmoddev.mmdbot.core.commands.component.Component;
import com.mcmoddev.mmdbot.core.commands.component.ComponentListener;
import com.mcmoddev.mmdbot.core.commands.component.context.ButtonInteractionContext;
import com.mcmoddev.mmdbot.core.util.Constants;
import com.mcmoddev.mmdbot.core.util.MessageUtilities;
import com.mcmoddev.mmdbot.core.util.event.DismissListener;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;

import static com.mcmoddev.mmdbot.core.commands.component.Component.createIdWithArguments;

public class TicTacToeCommand extends SlashCommand {
    public static final Collection<Message.MentionType> ALLOWED_MENTIONS = EnumSet.of(Message.MentionType.USER);

    public static final int UNKNOWN = 0;
    public static final int X = 1;
    public static final int ZERO = 2;

    public static final int[][] DEFAULT_GAME = {
        {UNKNOWN, UNKNOWN, UNKNOWN},
        {UNKNOWN, UNKNOWN, UNKNOWN},
        {UNKNOWN, UNKNOWN, UNKNOWN}
    };

    @RegisterSlashCommand
    public static final TicTacToeCommand COMMAND = new TicTacToeCommand();

    public static final ComponentListener COMPONENT_LISTENER = TheCommander.getComponentListener("tic-tac-toe")
        .onButtonInteraction(COMMAND::onButtonInteraction)
        .build();

    private static final Random RANDOM = new Random();

    public TicTacToeCommand() {
        name = "tic-tac-toe";
        help = "Challenge a member to a Tic-Tac-Toe game!";
        guildOnly = true;
        options = List.of(new OptionData(OptionType.USER, "opponent", "The user to challenge!", true));
    }

    @Override
    protected void execute(final SlashCommandEvent event) {
        final var opponent = event.getOption("opponent", OptionMapping::getAsMember);
        if (opponent == null) {
            event.deferReply(true).setContent("Please mention a valid user to challenge!").queue();
            return;
        }
        if (opponent.getIdLong() == event.getUser().getIdLong()) {
            event.deferReply(true).setContent("You cannot challenge yourself!").queue();
            return;
        }
        if (opponent.getUser().isBot() || opponent.getUser().isSystem()) {
            event.deferReply(true).setContent("You cannot challenge a bot!").queue();
            return;
        }
        final var id = UUID.randomUUID();
        event.deferReply().setContent("%s, %s challenged you to a Tic-Tac-Toe game!".formatted(opponent.getAsMention(), event.getUser().getAsMention()))
            .allowedMentions(ALLOWED_MENTIONS)
            .addActionRow(
                Button.success(Component.createIdWithArguments(id.toString(), "accept"), "\u2714 Accept"),
                DismissListener.createDismissButton(opponent.getIdLong())
            )
            .flatMap(InteractionHook::retrieveOriginal)
            .queue(m -> COMPONENT_LISTENER.insertComponent(id, Component.Lifespan.TEMPORARY, opponent.getId(), event.getUser().getId(), m.getId()));
    }

    // Args: targetUser, opponent, originalMessageId
    private void setupGame(final ButtonInteractionContext context, final User user) {
        final var opponent = context.getMember();
        final var order = RANDOM.nextInt(2);
        final var gameId = UUID.randomUUID();
        final var game = DEFAULT_GAME.clone();
        final var x = order == 0 ? opponent : user;
        final var zero = order == 0 ? user : opponent;
        context.getEvent().getChannel()
            .sendTyping()
            .and(context.getEvent().getChannel()
                .retrieveMessageById(context.getArguments().get(2))
                .flatMap(m1 -> {
                    MessageUtilities.disableButtons(m1);
                    return m1.reply("""
                            Challenge accepted!
                            %s is **X**
                            %s is **0**"""
                            .formatted(x.getAsMention(), zero.getAsMention()))
                        .allowedMentions(List.of())
                        .flatMap(m -> {
                            COMPONENT_LISTENER.insertComponent(gameId, Component.Lifespan.TEMPORARY,
                                x.getId(),
                                zero.getId(),
                                String.valueOf(0),
                                gameToString(game),
                                m.getId()
                            );
                            return m.reply(nextTurn(gameId, game, x.getId(), "X").build());
                        });
                })
                .flatMap($ -> context.getEvent().deferEdit())
            )
            .queue();
    }

    // Args: x, zero, turn, game, originalMessage
    private void continueGame(final ButtonInteractionContext context) {
        final var event = context.getEvent();
        if (!event.isFromGuild()) return;
        final var x = context.getArguments().get(0);
        final var zero = context.getArguments().get(1);
        final int turn = context.getArgument(2, () -> 0, Integer::parseInt);
        if (turn == 0 && !x.equals(event.getUser().getId())) {
            event.deferEdit().queue();
            return;
        }
        if (turn == 1 && !zero.equals(event.getUser().getId())) {
            event.deferEdit().queue();
            return;
        }
        final var originalMessage = context.getArguments().get(4);
        final var current = turn == 0 ? x : zero;
        final var next = turn == 0 ? zero : x;
        final var gameCode = turn == 0 ? X : ZERO;
        final var game = resolveGameFromString(context.getArguments().get(3));
        event.deferEdit().queue();
        event.getMessage()
            .delete()
            .reason("User made a turn")
            .flatMap($ -> event.getChannel().sendTyping())
            .flatMap($ -> event.getChannel().retrieveMessageById(originalMessage))
            .flatMap(m -> {
                if (context.getItemComponentArguments().contains("giveup")) {
                    return m.reply("<@%s> gave up, and such, <@%s> won the __Tic-Tac-Toe__ game. The board:".formatted(current, next))
                        .setActionRows(createGameButtons(context.getComponentId().toString(), game, true))
                        .allowedMentions(ALLOWED_MENTIONS);
                }
                final int posX = Integer.parseInt(context.getItemComponentArguments().get(0));
                final int posY = Integer.parseInt(context.getItemComponentArguments().get(1));
                game[posX][posY] = gameCode;
                context.updateArguments(List.of(
                    x, zero, String.valueOf(turn == 0 ? 1 : 0), gameToString(game), originalMessage
                ));
                if (isGameCompleted(game)) {
                    context.updateArgument(0, current); // the opponent is the current user
                    final var buttons = createGameButtons(context.getComponentId().toString(), game, true);
                    buttons.add(ActionRow.of(Button.secondary(createIdWithArguments(context.getComponentId(), "revenge", next), "Revenge")));
                    return m.reply("<@%s> won the __Tic-Tac-Toe__ game against <@%s>! GG! The board:".formatted(current, next))
                        .setActionRows(buttons)
                        .allowedMentions(ALLOWED_MENTIONS);
                } else if (isEntireBoardUsed(game)) {
                    return m.reply("<@%s> and <@%s>'s Tic-Tac-Toe game ended in a tie! The board:".formatted(current, next))
                        .setActionRows(createGameButtons(context.getComponentId().toString(), game, true))
                        .allowedMentions(ALLOWED_MENTIONS);
                }
                return m.reply(nextTurn(
                    context.getComponentId(), game, next, gameCode == X ? "0" : "X"
                ).build());
            })
            .queue();
    }

    public void onButtonInteraction(final ButtonInteractionContext context) {
        final var componentType = context.getItemComponentArguments().get(0);
        switch (componentType) {
            case "accept" -> {
                if (context.getArguments().get(0).equals(context.getEvent().getUser().getId())) {
                    Objects.requireNonNull(context.getEvent().getGuild())
                        .retrieveMemberById(context.getArguments().get(1))
                        .queue(opponent -> setupGame(context, opponent.getUser()), e -> {
                        });
                } else {
                    context.getEvent().deferEdit().queue();
                }
            }
            case "revenge" -> {
                final var looser = context.getItemComponentArguments().get(1);
                if (context.getUser().getId().equals(looser)) {
                    final var id = UUID.randomUUID();
                    final var opponent = context.getArguments().get(0);
                    context.getEvent().deferReply().setContent("<@%s>, %s wants a revenge for the Tic-Tac-Toe game you won!".formatted(opponent, context.getUser().getAsMention()))
                        .allowedMentions(ALLOWED_MENTIONS)
                        .addActionRow(
                            Button.success(Component.createIdWithArguments(id.toString(), "accept"), "\u2714 Accept"),
                            DismissListener.createDismissButton(opponent)
                        )
                        .flatMap(InteractionHook::retrieveOriginal)
                        .queue(m -> COMPONENT_LISTENER.insertComponent(id, Component.Lifespan.TEMPORARY, opponent, context.getUser().getId(), m.getId()));
                } else {
                    context.getEvent().deferEdit().queue();
                }
            }
            default -> continueGame(context);
        }
    }

    public static MessageBuilder nextTurn(final UUID gameId, final int[][] game, final String user, final String letter) {
        final var idStr = gameId.toString();
        final var rows = createGameButtons(idStr, game);
        rows.add(ActionRow.of(Button.secondary(createIdWithArguments(idStr, "giveup"), "\uD83C\uDFF3 Give Up")));
        return new MessageBuilder()
            .setAllowedMentions(ALLOWED_MENTIONS)
            .append("<@%s> your turn (you are **%s**):".formatted(user, letter))
            .setActionRows(rows);
    }

    public static List<ActionRow> createGameButtons(final String buttonIds, final int[][] game) {
        return createGameButtons(buttonIds, game, false);
    }

    public static List<ActionRow> createGameButtons(final String buttonIds, final int[][] game, final boolean disabled) {
        final var rows = new ArrayList<ActionRow>();
        for (var rowIndex = 0; rowIndex < 3; rowIndex++) {
            final var row = game[rowIndex];
            final int finalRowIndex = rowIndex;
            final var actionRow = ActionRow.of(
                IntStream.range(0, 3)
                    .mapToObj(y -> getButton(buttonIds, finalRowIndex, y, row[y]))
                    .map(btn -> btn.isDisabled() ? btn : btn.withDisabled(disabled))
                    .toList()
            );
            rows.add(actionRow);
        }
        return rows;
    }

    public static Button getButton(final String id, final int positionX, final int positionY, final int gameCode) {
        return switch (gameCode) {
            case UNKNOWN -> Button.primary(createIdWithArguments(id, positionX, positionY), "\u200b");
            case X -> Button.success(createIdWithArguments(id, positionX, positionY), "X").asDisabled();
            case ZERO -> Button.danger(createIdWithArguments(id, positionX, positionY), "0").asDisabled();
            default -> throw new UnsupportedOperationException();
        };
    }

    private static boolean isGameCompleted(final int[][] game) {
        for (var x = 0; x < 3; x++) {
            final var row = game[x];
            if (row[0] != UNKNOWN) {
                if (row[0] == row[1] && row[1] == row[2]) return true;
            }
        }
        for (var y = 0; y < 3; y++) {
            if (game[0][y] != UNKNOWN) {
                if (game[0][y] == game[1][y] && game[1][y] == game[2][y]) return true;
            }
        }
        if (game[0][0] != UNKNOWN) {
            if (game[0][0] == game[1][1] && game[1][1] == game[2][2]) return true;
        }
        if (game[0][2] != UNKNOWN) {
            return game[0][2] == game[1][1] && game[1][1] == game[2][0];
        }
        return false;
    }

    private static boolean isEntireBoardUsed(final int[][] board) {
        for (var x = 0; x < 3; x++) {
            for (var y = 0; y < 3; y++) {
                if (board[x][y] == UNKNOWN) return false;
            }
        }
        return true;
    }

    private static int[][] resolveGameFromString(final String str) {
        return Constants.Gsons.NO_PRETTY_PRINTING.fromJson(str, int[][].class);
    }

    private static String gameToString(final int[][] game) {
        return Constants.Gsons.NO_PRETTY_PRINTING.toJson(game, int[][].class);
    }
}
