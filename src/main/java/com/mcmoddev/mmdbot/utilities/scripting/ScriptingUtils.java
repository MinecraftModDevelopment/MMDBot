package com.mcmoddev.mmdbot.utilities.scripting;

import com.mcmoddev.mmdbot.utilities.tricks.TrickContext;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.RichPresence;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.EnvironmentAccess;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public final class ScriptingUtils {

    public static final Context ENGINE = Context.newBuilder("js")
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
        .build();

    public static void evaluate(String script, ScriptingContext context) {
        context.applyTo(ENGINE.getBindings("js"));
        ENGINE.eval("js", script);
    }

    public static ScriptingContext createTextChannel(TextChannel channel) {
        final var context = ScriptingContext.of("TextChannel");
        context.set("id", channel.getId());
        context.set("guild", createGuild(channel.getGuild()));
        context.set("slowmode", channel.getSlowmode());
        context.setFunction("asMention", i -> channel.getAsMention());
        return context;
    }

    public static ScriptingContext createTrickContext(TrickContext trickContext) {
        final var context = ScriptingContext.of("TrickContext");
        context.set("guild", trickContext.getGuild() == null ? null : createGuild(trickContext.getGuild()));
        context.set("member", trickContext.getMember() == null ? null : createMember(trickContext.getMember()));
        context.set("user", createUser(trickContext.getUser()));
        context.set("args", trickContext.getArgs());
        context.set("channel", createTextChannel(trickContext.getChannel()));
        context.setFunction("reply", args -> {
            validateArgs(args, 1);
            trickContext.reply(args.get(0).asString());
            return null;
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
        context.set("activities", member.getActivities().stream().map(ScriptingUtils::createActivity).toArray(ScriptingContext[]::new));
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
        context.setFunction("asTag", args -> user.getAsTag());
        context.setFunction("asMention", args -> user.getAsMention());
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

}
