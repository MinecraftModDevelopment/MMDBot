package com.mcmoddev.mmdbot.utilities.scripting;

import com.mcmoddev.mmdbot.modules.logging.misc.ScamDetector;
import com.mcmoddev.mmdbot.utilities.Utils;
import com.mcmoddev.mmdbot.utilities.quotes.Quote;
import com.mcmoddev.mmdbot.utilities.quotes.QuoteList;
import com.mcmoddev.mmdbot.utilities.scripting.object.ScriptEmbed;
import com.mcmoddev.mmdbot.utilities.tricks.TrickContext;
import com.mcmoddev.mmdbot.utilities.tricks.Tricks;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.RichPresence;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.RoleIcon;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.EnvironmentAccess;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;

import javax.annotation.Nullable;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
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
            .allowExperimentalOptions(true)
            .allowNativeAccess(false)
            .allowIO(false)
            .allowCreateProcess(false)
            .allowEnvironmentAccess(EnvironmentAccess.NONE)
            .allowHostClassLoading(false)
            .allowHostAccess(
                HostAccess.newBuilder()
                    .allowArrayAccess(true)
                    .allowListAccess(true)
                    .build()
            )
            .option("js.console", "false")
            .option("js.nashorn-compat", "true")
            .option("js.experimental-foreign-object-prototype", "true")
            .option("js.disable-eval", "true")
            .option("js.load", "false")
            .option("log.level", "OFF")
            .build()) {
            final var bindings = engine.getBindings("js");
            bindings.removeMember("load");
            bindings.removeMember("loadWithNewGlobal");
            bindings.removeMember("eval");
            bindings.removeMember("exit");
            bindings.removeMember("quit");
            context.applyTo(bindings);

            engine.eval("js", script);
        } catch (Exception e) {
            throw new ScriptingException(e);
        }
    }

    public static ScriptingContext createMessageChannel(MessageChannel channel) {
        final var context = ScriptingContext.of("MessageChannel");
        context.set("id", channel.getId());
        context.set("name", channel.getName());
        context.set("type", channel.getType().toString());
        context.set("timeCreated", channel.getTimeCreated());
        context.setFunction("asMention", a -> channel.getAsMention());
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
            .filter(Objects::nonNull).toList()).allowedMentions(ALLOWED_MENTIONS).queue());
        context.setFunction("asMention", i -> channel.getAsMention());
        return context;
    }

    public static ScriptingContext createTextChannel(TextChannel channel) {
        final var context = createMessageChannel(channel);
        context.set("guild", createGuild(channel.getGuild()));
        context.set("slowmode", channel.getSlowmode());
        context.set("topic", channel.getTopic());
        context.set("isNSFW", channel.isNSFW());
        context.set("isSynced", channel.isSynced());
        return context;
    }

    public static ScriptingContext createTrickContext(TrickContext trickContext) {
        final var context = ScriptingContext.of("TrickContext");
        context.set("Utils", UTILS_CLASS);
        final var canSendEmbed = trickContext.getGuild() == null || trickContext.getMember().hasPermission(trickContext.getChannel(), Permission.MESSAGE_EMBED_LINKS);
        context.setFunction("createEmbed", a -> {
            if (a.size() == 2) {
                return new ScriptEmbed(new EmbedBuilder().setTitle(a.get(0).asString()).setDescription(a.get(1).asString()));
            } else {
                return new ScriptEmbed();
            }
        });
        context.set("guild", trickContext.getGuild() == null ? null : createGuild(trickContext.getGuild()));
        context.set("member", trickContext.getMember() == null ? null : createMember(trickContext.getMember()));
        context.set("user", createUser(trickContext.getUser()));
        context.set("args", trickContext.getArgs());
        context.set("channel", createTextChannel(trickContext.getChannel()));
        context.setFunctionVoid("reply", args -> {
            validateArgs(args, 1);
            trickContext.reply(args.get(0).asString());
        });
        if (canSendEmbed) {
            context.setFunctionVoid("replyEmbeds", args -> {
                trickContext.replyEmbeds(args.stream().map(ScriptingUtils::getEmbedFromValue)
                    .filter(Objects::nonNull).toArray(MessageEmbed[]::new));
            });
            context.setFunctionVoid("replyEmbed", args -> {
                validateArgs(args, 1);
                final var v = args.get(0);
                final var embed = getEmbedFromValue(v);
                if (embed != null) {
                    trickContext.replyEmbeds(embed);
                }
            });
        }
        context.setFunctionVoid("runTrick", args -> {
            validateArgs(args, 1);
            Tricks.getTrick(args.get(0).asString()).ifPresent(trick -> trick.execute(trickContext));
        });
        return context;
    }

    public static ScriptingContext createGuild(Guild guild) {
        final var context = ScriptingContext.of("Guild");
        context.set("name", guild.getName());
        context.set("splashUrl", guild.getSplashUrl());
        context.set("splashId", guild.getSplashId());
        context.setFunction("getMemberById", args -> {
            validateArgs(args, 1);
            final var member = guild.getMemberById(args.get(0).asLong());
            return member == null ? null : createMember(member);
        });
        context.setFunction("getRoleById", args -> {
            validateArgs(args, 1);
            final var role = guild.getRoleById(args.get(0).asLong());
            return role == null ? null : createRole(role);
        });
        context.setFunction("getTextChannelById", args -> {
            validateArgs(args, 1);
            final var channel = guild.getTextChannelById(args.get(0).asLong());
            return channel == null ? null : createTextChannel(channel);
        });
        context.setFunction("getQuotes", args -> {
            validateArgs(args, 0);
            return IntStream.range(0, QuoteList.getQuoteSlot()).mapToObj(i -> {
                final var quote = QuoteList.getQuote(i);
                return quote == null ? null : createQuote(quote);
            }).toList();
        });
        context.setFunction("getMembers", a -> guild.getMembers().stream().map(ScriptingUtils::createMember).toList());
        context.setFunction("getRoles", a -> guild.getRoles().stream().map(ScriptingUtils::createRole).toList());
        context.setFunction("getTextChannels", a -> guild.getTextChannels().stream().map(ScriptingUtils::createTextChannel).toList());
        return context;
    }

    public static ScriptingContext createActivity(Activity activity) {
        final var context = ScriptingContext.of("Activity");
        context.set("name", activity.getName());
        context.set("url", activity.getUrl());
        context.set("type", activity.getType().toString());
        context.set("emoji", activity.getEmoji() == null ? null : activity.getEmoji().getAsMention());
        context.setFunction("isRich", i -> activity.isRich());
        context.setFunction("asRich", i -> activity.isRich() ? createActivityRich(activity.asRichPresence()) : null);
        return context;
    }

    public static ScriptingContext createActivityRich(RichPresence activity) {
        final var context = ScriptingContext.of("Activity");
        context.flatAdd(createActivity(activity));
        context.set("details", activity.getDetails());
        context.set("applicationId", activity.getApplicationId());
        context.set("flags", activity.getFlags());
        context.set("currentPartySize", activity.getParty() != null ? activity.getParty().getSize() : null);
        return context;
    }

    public static ScriptingContext createMember(Member member) {
        final var context = ScriptingContext.of("Member");
        context.set("user", createUser(member.getUser()));
        context.set("nickname", member.getNickname());
        context.set("color", member.getColorRaw());
        context.set("guild", createGuild(member.getGuild()));
        context.set("status", member.getOnlineStatus().getKey());
        context.set("activities", member.getActivities().stream().map(ScriptingUtils::createActivity).toArray(ScriptingContext[]::new));
        context.set("joinTime", Utils.getMemberJoinTime(member));
        context.set("timeBoosted", member.getTimeBoosted());
        context.setFunction("getRoles", a -> member.getRoles().stream().map(ScriptingUtils::createRole).toList());
        return context;
    }

    public static ScriptingContext createUser(User user) {
        final var context = ScriptingContext.of("User");
        context.set("name", user.getName());
        context.set("discriminator", user.getDiscriminator());
        context.set("avatarId", user.getAvatarId());
        context.set("avatarUrl", user.getAvatarUrl());
        context.set("id", user.getId());
        context.set("isBot", user.isBot());
        context.set("hasPrivateChannel", user.hasPrivateChannel());
        context.setFunction("asTag", args -> user.getAsTag());
        context.setFunction("asMention", args -> user.getAsMention());
        context.setFunction("openPrivateChannel", args -> {
            validateArgs(args, 0);
            final var privateChannel = user.openPrivateChannel().complete();
            return privateChannel == null ? null : createMessageChannel(privateChannel);
        });
        return context;
    }

    public static ScriptingContext createRole(Role role) {
        final var context = ScriptingContext.of("Role");
        context.set("name", role.getName());
        context.set("guild", role.getGuild());
        context.set("color", role.getColorRaw());
        context.set("timeCreated", role.getTimeCreated());
        context.setFunction("asMention", a -> role.getAsMention());
        context.setFunction("getRoleIcon", a -> role.getIcon() == null ? null : createRoleIcon(role.getIcon()));
        return context;
    }

    public static ScriptingContext createRoleIcon(RoleIcon icon) {
        final var context = ScriptingContext.of("RoleIcon");
        context.set("id", icon.getIconId());
        context.set("url", icon.getIconUrl());
        context.set("emoji", icon.getEmoji());
        return context;
    }

    public static ScriptingContext createQuote(Quote quote) {
        final var context = ScriptingContext.of("Quote");
        context.set("quote", quote.getQuoteText() == null ? null : quote.getQuoteText());
        context.set("quotee", quote.getQuotee() == null ? null : quote.getQuotee().resolveReference());
        context.set("quoteAuthor", quote.getQuoteAuthor() == null ? null : quote.getQuoteAuthor().resolveReference());
        return context;
    }

    public static ProxyExecutable functionObject(Function<List<Value>, Object> function) {
        return new ScriptingContext.NameableProxyExecutable() {

            @Override
            public Object execute(final Value... args) {
                final var any = function.apply(Arrays.asList(args));
                if (any instanceof ScriptingContext context) {
                    return context.toProxyObject();
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
        context.setFunction("createEmbed", a -> {
            if (a.size() == 2) {
                return new ScriptEmbed(new EmbedBuilder().setTitle(a.get(0).asString()).setDescription(a.get(1).asString()));
            } else {
                return new ScriptEmbed();
            }
        });
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
