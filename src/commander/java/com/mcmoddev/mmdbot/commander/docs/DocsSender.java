package com.mcmoddev.mmdbot.commander.docs;

import de.ialistannen.javadocapi.model.JavadocElement;
import de.ialistannen.javadocapi.rendering.LinkResolveStrategy;
import de.ialistannen.javadocapi.storage.ElementLoader;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.RestAction;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Function;

public interface DocsSender {

    void replyWithResult(Function<Message, RestAction<Message>> replier, ElementLoader.LoadResult<JavadocElement> loadResult, boolean shortDescription, boolean omitTags, Duration queryDuration, LinkResolveStrategy linkResolveStrategy, long userId, UUID buttonId);

}
