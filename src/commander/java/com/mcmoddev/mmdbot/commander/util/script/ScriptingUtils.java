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
package com.mcmoddev.mmdbot.commander.util.script;

import com.google.common.collect.Lists;
import com.mcmoddev.mmdbot.commander.quotes.Quotes;
import com.mcmoddev.mmdbot.commander.tricks.TrickContext;
import com.mcmoddev.mmdbot.commander.tricks.Tricks;
import com.mcmoddev.mmdbot.commander.util.script.object.ScriptEmbed;
import com.mcmoddev.mmdbot.commander.util.script.object.ScriptRegion;
import com.mcmoddev.mmdbot.commander.util.script.object.ScriptRoleIcon;
import com.mcmoddev.mmdbot.core.annotation.ExposeScripting;
import com.mcmoddev.mmdbot.core.common.ScamDetector;
import com.mcmoddev.mmdbot.core.util.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.RichPresence;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.EnvironmentAccess;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;

import javax.annotation.Nullable;
import java.io.Serial;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

public final class ScriptingUtils {

    public static final EnumSet<Message.MentionType> ALLOWED_MENTIONS = EnumSet.of(Message.MentionType.EMOTE,
        Message.MentionType.CHANNEL);

    public static final Engine ENGINE = Engine.newBuilder()
        .allowExperimentalOptions(true)
        .option("js.console", "false")
        .option("js.nashorn-compat", "true")
        .option("js.experimental-foreign-object-prototype", "true")
        .option("js.disable-eval", "true")
        .option("js.load", "false")
        .option("log.level", "OFF")
        .build();

    public static final HostAccess HOST_ACCESS;

    static {
        final var hostAccess = HostAccess.newBuilder()
            .allowAccessAnnotatedBy(ExposeScripting.class)
            .allowArrayAccess(true)
            .allowListAccess(true)
            .allowMapAccess(true)
            .allowImplementationsAnnotatedBy(ExposeScripting.class);
        final Class<?>[] allPublicAccess = {
            Instant.class, OffsetDateTime.class, Temporal.class
        };

        for (final var c : allPublicAccess) {
            for (var m : c.getMethods()) {
                hostAccess.allowAccess(m);
            }
        }

        HOST_ACCESS = hostAccess.build();
    }

    /**
     * Execute any script inside this thread pool if you think the script will be heavy. <br>
     * <b>BY DEFAULT, {@link #evaluate(String, ScriptingContext)} calls are NOT executed in another thread.</b>
     */
    public static final Executor THREAD_POOL = Executors.newFixedThreadPool(2, r -> Utils.setThreadDaemon(new Thread(r, "ScriptEvaluator"), true));

    public static void evaluate(String script, ScriptingContext context) {
        if (ScamDetector.containsScam(script)) {
            throw new ScriptingException("This script contained a scam link!");
        }
        try (var engine = Context.newBuilder("js")
            .engine(ENGINE)
            .allowNativeAccess(false)
            .allowIO(false)
            .allowCreateProcess(false)
            .allowEnvironmentAccess(EnvironmentAccess.NONE)
            .allowHostClassLoading(false)
            .allowValueSharing(true)
            .allowHostAccess(HOST_ACCESS)
            .build()) {

            final var bindings = engine.getBindings("js");
            bindings.removeMember("load");
            bindings.removeMember("loadWithNewGlobal");
            bindings.removeMember("eval");
            bindings.removeMember("exit");
            bindings.removeMember("quit");

            bindings.putMember("exit", functionObject(args -> {
                throw new IllegalCallerException("GG! You tried stopping me!");
            }));
            bindings.putMember("quit", functionObject(args -> {
                throw new IllegalCallerException("GG! You tried stopping me!");
            }));

            context.addInstantiatable(new String[]{"Embed", "EmbedBuilder"}, args -> {
                validateArgs(args, 0, 2);
                if (args.size() == 2) {
                    return new ScriptEmbed(new EmbedBuilder().setTitle(args.get(0).asString())
                        .setDescription(args.get(1).asString())).toProxyObject();
                }
                return new ScriptEmbed();
            });

            context.set("Utils", UTILS_CLASS);
            context.set("Math", MATH_CLASS);
            context.set("System", SYSTEM_CLASS);
            context.set("Instant", INSTANT_CLASS);

            if (!bindings.hasMember("parseString")) {
                context.setFunction("parseString", executeIfArgsValid(a -> a.get(0).toString(), 1));
            }

            context.applyTo(bindings);

            engine.eval("js", script);
        } catch (Exception e) {
            throw new ScriptingException(e);
        }
    }

    public static ScriptingContext createChannel(Channel channel) {
        return ScriptingContext.of("Channel", channel)
            .set("name", channel.getName())
            .set("type", channel.getType().toString());
    }

    public static ScriptingContext createMessageChannel(MessageChannel channel) {
        return createMessageChannel(channel, false);
    }

    public static ScriptingContext createMessageChannel(MessageChannel channel, boolean canSendMessage) {
        final var context = ScriptingContext.of("MessageChannel", channel);
        context.flatAdd(createChannel(channel));
        context.setFunction("canBotTalk", args -> channel.canTalk());
        if (canSendMessage) {
            context.setFunctionVoid("sendMessage", args -> {
                validateArgs(args, 1);
                channel.sendMessage(args.get(0).asString()).allowedMentions(ALLOWED_MENTIONS).queue();
            });
            context.setFunctionVoid("sendEmbed", args -> {
                validateArgs(args, 1);
                final var v = args.get(0);
                final var embed = getEmbedFromValue(v);
                if (embed != null) {
                    channel.sendMessageEmbeds(embed).allowedMentions(ALLOWED_MENTIONS).queue();
                }
            });
            context.setFunctionVoid("sendEmbeds", args -> channel.sendMessageEmbeds(args.stream().map(ScriptingUtils::getEmbedFromValue)
                .filter(Objects::nonNull).limit(3).toList()).allowedMentions(ALLOWED_MENTIONS).queue());
        }
        return context;
    }

    public static ScriptingContext createTextChannel(TextChannel channel, boolean canSendMessage) {
        return ScriptingContext.of("TextChannel")
            .flatAdd(createMessageChannel(channel, canSendMessage))
            .set("slowmode", channel.getSlowmode())
            .set("topic", channel.getTopic())
            .set("isNSFW", channel.isNSFW())
            .set("isSynced", channel.isSynced())
            .setFunction("getGuild", a -> createGuild(channel.getGuild()).toProxyObject())
            .setFunction("getCategory", a -> channel.getParentCategory() == null ? null :
                createCategory(channel.getParentCategory()).toProxyObject());
    }

    public static ScriptingContext createTextChannel(TextChannel channel) {
        return createTextChannel(channel, false);
    }

    public static ScriptingContext createCategory(Category category) {
        return ScriptingContext.of("Category", category).set("name", category.getName())
            .set("position", category.getPosition())
            .setFunction("asMention", a -> category.getAsMention())
            .setFunction("getGuild", a -> createGuild(category.getGuild()).toProxyObject())
            .setFunction("getMembers", a -> category.getMembers().stream().map(m ->
                createMember(m).toProxyObject()).toList())
            .setFunction("getTextChannels", a -> category.getTextChannels().stream().map(c ->
                createTextChannel(c).toProxyObject()).toList());
    }

    public static ScriptingContext createTrickContext(TrickContext trickContext) {
        final var context = ScriptingContext.of("TrickContext");
        context.setFunction("createEmbed", a -> {
            if (a.size() == 2) {
                return new ScriptEmbed(new EmbedBuilder().setTitle(a.get(0).asString()).setDescription(a.get(1).asString()));
            } else {
                return new ScriptEmbed();
            }
        });
        context.set("guild", trickContext.getGuild() == null ? null : createGuild(trickContext.getGuild()));
        context.set("member", trickContext.getMember() == null ? null : createMember(trickContext.getMember(), true));
        context.set("user", createUser(trickContext.getUser(), true));
        context.set("args", trickContext.getArgs());
        context.set("channel", createMessageChannel(trickContext.getChannel(), true));
        context.set("textChannel", trickContext.getTextChannel() == null ? null : createTextChannel(trickContext.getTextChannel(), true));
        context.setFunctionVoid("reply", args -> {
            validateArgs(args, 1);
            trickContext.reply(args.get(0).asString());
        });
        context.setFunctionVoid("replyEmbeds", args -> {
            trickContext.replyEmbeds(args.stream().map(ScriptingUtils::getEmbedFromValue)
                .filter(Objects::nonNull).limit(3).toArray(MessageEmbed[]::new));
        });
        context.setFunctionVoid("replyEmbed", args -> {
            validateArgs(args, 1);
            final var v = args.get(0);
            final var embed = getEmbedFromValue(v);
            if (embed != null) {
                trickContext.replyEmbeds(embed);
            }
        });
        context.setFunctionVoid("runTrick", args -> {
            validateArgs(args, 1, 2);
            if (args.size() == 1) {
                Tricks.getTrick(args.get(0).asString()).ifPresent(trick -> trick.execute(trickContext));
            } else if (args.size() > 1) {
                Tricks.getTrick(args.get(0).asString())
                    .ifPresent(trick -> trick.execute(new TrickContext.DelegateWithArguments(trickContext, args.get(1).as(String[].class))));
            }
        });
        return context;
    }

    public static ScriptingContext createGuild(Guild guild) {
        final var context = ScriptingContext.of("Guild", guild);
        context.set("name", guild.getName());
        context.set("icon", guild.getIconUrl());
        context.set("iconId", guild.getIconId());
        context.set("splash", guild.getSplashUrl());
        context.set("splashId", guild.getSplashId());
        context.set("memberCount", guild.getMemberCount());
        context.setFunction("getRegions", args -> guild.retrieveRegions().complete()
            .stream().map(ScriptRegion::new).toList());
        context.setFunction("getOwner", a -> guild.getOwner() == null ? null :
            createMember(guild.getOwner()).toProxyObject());
        context.setFunction("getMemberById", args -> {
            validateArgs(args, 1);
            final var member = guild.getMemberById(args.get(0).asLong());
            return member == null ? null : createMember(member).toProxyObject();
        });
        context.setFunction("getRoleById", args -> {
            validateArgs(args, 1);
            final var role = guild.getRoleById(args.get(0).asLong());
            return role == null ? null : createRole(role).toProxyObject();
        });
        context.setFunction("getTextChannelById", args -> {
            validateArgs(args, 1);
            final var channel = guild.getTextChannelById(args.get(0).asLong());
            return channel == null ? null : createTextChannel(channel).toProxyObject();
        });
        context.setFunction("getEmoteById", args -> {
            validateArgs(args, 1);
            final var channel = guild.getEmoteById(args.get(0).asLong());
            return channel == null ? null : createEmote(channel).toProxyObject();
        });
        context.setFunction("getQuotes", args -> {
            validateArgs(args, 0);
            return IntStream.range(0, Quotes.getQuotesForGuild(guild.getIdLong()).size()).mapToObj(i -> Quotes.getQuote(guild.getIdLong(), i)).toList();
        });
        context.setFunction("getMembers", a -> guild.getMembers().stream().map(m -> createMember(m).toProxyObject()).toList());
        context.setFunction("getRoles", a -> guild.getRoles().stream().map(r -> createRole(r).toProxyObject()).toList());
        context.setFunction("getChannels", a -> guild.getChannels().stream().map(c -> createChannel(c).toProxyObject()).toList());
        context.setFunction("getTextChannels", a -> guild.getTextChannels().stream().map(c -> createTextChannel(c).toProxyObject()).toList());
        context.setFunction("getCategories", a -> guild.getCategories().stream().map(c -> createCategory(c).toProxyObject()).toList());
        context.setFunction("getEmotes", a -> guild.getEmotes().stream().map(c -> createEmote(c).toProxyObject()).toList());
        return context;
    }

    public static ScriptingContext createActivity(Activity activity) {
        return ScriptingContext.of("Activity")
            .set("name", activity.getName())
            .set("url", activity.getUrl())
            .set("type", activity.getType().toString())
            .set("emoji", activity.getEmoji() == null ? null : activity.getEmoji().getAsMention())
            .setFunction("isRich", i -> activity.isRich())
            .setFunction("asRich", i -> activity.isRich() ? createActivityRich(activity.asRichPresence()).toProxyObject() : null);
    }

    public static ScriptingContext createActivityRich(RichPresence activity) {
        return ScriptingContext.of("RichPresence")
            .flatAdd(createActivity(activity))
            .set("details", activity.getDetails())
            .set("applicationId", activity.getApplicationId())
            .set("flags", activity.getFlags())
            .set("currentPartySize", activity.getParty() != null ? activity.getParty().getSize() : null);
    }

    public static ScriptingContext createMember(Member member) {
        return createMember(member, false);
    }

    public static ScriptingContext createMember(Member member, boolean canDm) {
        final var context = ScriptingContext.of("Member", member);
        context.set("user", createUser(member.getUser(), canDm));
        context.set("nickname", member.getNickname());
        context.set("color", member.getColorRaw());
        context.set("timeBoosted", member.getTimeBoosted());
        context.set("joinTime", member.getTimeJoined());
        context.setFunction("getStatus", a -> member.getOnlineStatus().getKey());
        context.set("activities", member.getActivities().stream().map(a -> createActivity(a).toProxyObject()).toArray(ScriptingContext[]::new));
        context.setFunction("getGuild", a -> createGuild(member.getGuild()).toProxyObject());
        context.setFunction("getRoles", a -> member.getRoles().stream().sorted(Comparator.comparing(Role::getPositionRaw).reversed())
            .map(r -> createRole(r).toProxyObject()).toList());
        return context;
    }

    public static ScriptingContext createUser(User user) {
        return createUser(user, false);
    }

    public static ScriptingContext createUser(User user, boolean canDm) {
        final var context = ScriptingContext.of("User", user);
        context.set("name", user.getName());
        context.set("discriminator", user.getDiscriminator());
        context.set("avatarId", user.getAvatarId());
        context.set("avatarUrl", user.getAvatarUrl());
        context.set("isBot", user.isBot());
        context.set("hasPrivateChannel", user.hasPrivateChannel());
        context.setFunction("asTag", args -> user.getAsTag());
        context.setFunction("openPrivateChannel", args -> {
            validateArgs(args, 0);
            final var privateChannel = user.openPrivateChannel().complete();
            return privateChannel == null ? null : createMessageChannel(privateChannel, canDm);
        });
        return context;
    }

    public static ScriptingContext createRole(Role role) {
        return ScriptingContext.of("Role", role)
            .set("name", role.getName())
            .set("color", role.getColorRaw())
            .set("timeCreated", role.getTimeCreated())
            .setFunction("getGuild", a -> createGuild(role.getGuild()))
            .setFunction("isHoisted", a -> role.isHoisted())
            .setFunction("isPublicRole", a -> role.isPublicRole())
            .setFunction("isManaged", a -> role.isManaged())
            .setFunction("isMentionable", a -> role.isMentionable())
            .setFunction("getRoleIcon", a -> role.getIcon() == null ? null : new ScriptRoleIcon(role.getIcon()));
    }

    public static ScriptingContext createEmote(Emote emote) {
        return ScriptingContext.of("Emote", emote)
            .set("name", emote.getName())
            .set("url", emote.getImageUrl())
            .setFunction("isAnimated", args -> emote.isAnimated())
            .setFunction("canProvideRoles", args -> emote.canProvideRoles())
            .setFunction("isAvailable", args -> emote.isAvailable())
            .setFunction("getRoles", args -> emote.getRoles().stream().map(r -> createRole(r).toProxyObject()).toList())
            .setFunction("getGuild", args -> emote.getGuild() == null ? null : createGuild(emote.getGuild()));
    }

    public static ProxyExecutable functionObject(Function<List<Value>, Object> function) {
        return new ScriptingContext.NameableProxyExecutable() {

            @Override
            public Object execute(final Value... args) {
                final var any = function.apply(Arrays.asList(args));
                if (any instanceof ScriptingContext context) {
                    return context.toProxyObject();
                } else if (any instanceof List list) {
                    final var objects = new ArrayList<>();
                    for (var obj : list) {
                        if (obj instanceof ScriptingContext context) {
                            objects.add(context);
                        } else {
                            objects.add(obj);
                        }
                    }
                    return objects;
                }
                return any;
            }

            @Override
            public String getName() {
                return null;
            }
        };
    }

    public static void validateArgs(final List<Value> args, int... size) {
        final var list = new ArrayList<Integer>();
        for (var s : size) {
            list.add(s);
        }
        if (!list.contains(args.size())) {
            throw new IllegalArgumentException("Invalid amount of arguments!");
        }
    }

    public static <T> Function<List<Value>, T> executeIfArgsValid(Function<List<Value>, T> function, int... size) {
        return args -> {
            final var list = new ArrayList<Integer>();
            for (var s : size) {
                list.add(s);
            }
            if (!list.contains(args.size())) {
                throw new IllegalArgumentException("Invalid amount of arguments!");
            } else {
                return function.apply(args);
            }
        };
    }

    public static final class ScriptingException extends RuntimeException {

        @Serial
        private static final long serialVersionUID = 1280293039202930L;

        public ScriptingException(Throwable e) {
            super(e);
        }

        public ScriptingException(String e) {
            super(e);
        }
    }

    public static final ScriptingContext UTILS_CLASS = makeContext("Utils", context -> {
        context.setFunction("parseInt", executeIfArgsValid(a -> Integer.parseInt(a.get(0).asString()), 1));
        context.setFunction("parseString", executeIfArgsValid(a -> a.get(0).toString(), 1));
        context.setFunction("parseLong", executeIfArgsValid(a -> Long.parseLong(a.get(0).asString()), 1));

        context.setFunction("equals", executeIfArgsValid(a -> a.get(0).equals(a.get(2)), 2));
        context.setFunction("range", executeIfArgsValid(a -> {
            final var min = a.size() == 1 ? 0 : a.get(0).asInt();
            final var max = a.get(a.size() - 1).asInt();
            return Lists.newArrayList(IntStream.range(min, max + 1));
        }, 1, 2));

        context.setFunction("format", executeIfArgsValid(args -> {
            if (args.size() < 2) {
                throw new IllegalArgumentException("Invalid amount of arguments provided!");
            }
            return String.format(args.get(0).asString(), args.subList(1, args.size()).stream().map(Value::asString).toArray(Object[]::new));
        }));

        context.setFunction("getFunctions", executeIfArgsValid(args -> {
            final var member = args.get(0);
            final var memberKeys = member.getMemberKeys();
            if (args.size() < 2) {
                return memberKeys.stream().filter(k -> member.getMember(k).canExecute()).toList();
            } else {
                final var blacklist = List.of(args.get(1).as(String[].class));
                return memberKeys.stream().filter(k -> member.getMember(k).canExecute() && !blacklist.contains(k)).toList();
            }
        }, 1, 2));
        context.setFunction("getConstructors", executeIfArgsValid(args -> {
            final var member = args.get(0);
            final var memberKeys = member.getMemberKeys();
            if (args.size() < 2) {
                return memberKeys.stream().filter(k -> member.getMember(k).canInstantiate()).toList();
            } else {
                final var blacklist = List.of(args.get(1).as(String[].class));
                return memberKeys.stream().filter(k -> member.getMember(k).canInstantiate() && !blacklist.contains(k)).toList();
            }
        }, 1, 2));
        context.setFunction("getFields", executeIfArgsValid(args -> {
            final var member = args.get(0);
            final var memberKeys = member.getMemberKeys();
            if (args.size() < 2) {
                return memberKeys.stream().filter(k -> {
                    final var m = member.getMember(k);
                    return !m.canExecute() && !m.canInstantiate();
                }).toList();
            } else {
                final var blacklist = List.of(args.get(1).as(String[].class));
                return memberKeys.stream().filter(k -> {
                    final var m = member.getMember(k);
                    return !m.canExecute() && !m.canInstantiate() && !blacklist.contains(k);
                }).toList();
            }
        }, 1, 2));
    });

    public static final ScriptingContext INSTANT_CLASS = makeContext("Instant", context -> {
        context.setFunction("now", a -> Instant.now());
        context.setFunction("ofSeconds", a -> {
            validateArgs(a, 1);
            return Instant.ofEpochSecond(a.get(0).asLong());
        });
        context.setFunction("ofMillis", a -> {
            validateArgs(a, 1);
            return Instant.ofEpochMilli(a.get(0).asLong());
        });
    });

    public static final ScriptingContext MATH_CLASS = makeContext("Math", context -> {
        context.setFunction("random", executeIfArgsValid(a -> Math.random(), 0));
        context.setFunction("sqrt", executeIfArgsValid(a -> Math.sqrt(a.get(0).asDouble()), 1));
        context.setFunction("floor", executeIfArgsValid(a -> Math.floor(a.get(0).asDouble()), 1));
        context.setFunction("ceil", a -> Math.ceil(a.get(0).asDouble()));
        context.setFunction("abs", executeIfArgsValid(a -> Math.abs(a.get(0).asDouble()), 1));
        context.setFunction("sin", executeIfArgsValid(a -> Math.sin(a.get(0).asDouble()), 1));
        context.setFunction("cos", executeIfArgsValid(a -> Math.cos(a.get(0).asDouble()), 1));
        context.setFunction("tan", executeIfArgsValid(a -> Math.tan(a.get(0).asDouble()), 1));
        context.setFunction("min", args -> args.stream().mapToDouble(Value::asDouble).min().orElse(0));
        context.setFunction("max", args -> args.stream().mapToDouble(Value::asDouble).max().orElse(0));
        context.setFunction("any", args -> args.stream().anyMatch(Value::asBoolean));
        context.setFunction("all", args -> args.stream().allMatch(Value::asBoolean));

        context.setFunction("ceil", executeIfArgsValid(a -> Math.pow(a.get(0).asDouble(), a.get(1).asDouble()), 2));
    });

    public static final ScriptingContext SYSTEM_CLASS = makeContext("System", context -> {
        context.setFunction("currentTimeMillis", executeIfArgsValid(a -> System.currentTimeMillis(), 0));
        context.setFunction("nanoTime", executeIfArgsValid(a -> System.nanoTime(), 0));
        context.setFunction("lineSeparator", executeIfArgsValid(a -> System.lineSeparator(), 0));
    });

    @Nullable
    public static MessageEmbed getEmbedFromValue(Value value) {
        try {
            return value.as(MessageEmbed.class);
        } catch (ClassCastException e) {
            if (value.hasMember("build")) {
                try {
                    return value.invokeMember("build").as(MessageEmbed.class);
                } catch (ClassCastException ignored) {
                }
            }
        }
        return null;
    }

    public static ScriptingContext makeContext(String name, Consumer<ScriptingContext> consumer) {
        final var context = ScriptingContext.of(name);
        consumer.accept(context);
        return context;
    }
}
