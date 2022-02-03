package com.mcmoddev.mmdbot.utilities.scripting;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.EnvironmentAccess;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public final class ScriptingUtils {

    public static void evaluate(String script, ScriptingContext context) {
        final var engine = Context.newBuilder("js")
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
            // .option("js.console", "false")
            .option("js.nashorn-compat", "true")
            .option("js.experimental-foreign-object-prototype", "true")
            .option("js.disable-eval", "true")
            .option("js.load", "false")
            .option("log.level", "OFF")
            .build();

        context.applyTo(engine.getBindings("js"));

        engine.eval("js", script);
    }

    public static ScriptingContext createGuild(Guild guild) {
        final var context = ScriptingContext.of("Guild");
        context.set("name", guild.getName());
        return context;
    }

    public static ScriptingContext createMember(Member member) {
        final var context = ScriptingContext.of("Member");
        context.set("user", createUser(member.getUser()));
        context.set("nickname", member.getNickname());
        context.set("color", member.getColorRaw());
        context.set("guild", createGuild(member.getGuild()));
        return context;
    }

    public static ScriptingContext createUser(User user) {
        final var context = ScriptingContext.of("User");
        context.set("name", user.getName());
        context.set("discriminator", user.getDiscriminator());
        context.set("avatarId", user.getAvatarId());
        context.set("avatarUrl", user.getAvatarUrl());
        context.set("id", user.getId());
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

}
