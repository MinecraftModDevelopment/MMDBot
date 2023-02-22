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
package com.mcmoddev.mmdbot.commander.docs;

import de.ialistannen.javadocapi.model.JavadocElement;
import de.ialistannen.javadocapi.querying.FuzzyQueryResult;
import de.ialistannen.javadocapi.rendering.LinkResolveStrategy;
import de.ialistannen.javadocapi.storage.ElementLoader;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public interface DocsSender {

    void replyWithResult(Function<MessageEditData, RestAction<Message>> replier, ElementLoader.LoadResult<JavadocElement> loadResult, boolean shortDescription, boolean omitTags, Duration queryDuration, LinkResolveStrategy linkResolveStrategy, long userId, UUID buttonId);

    void replyMultipleResults(Function<MessageEditData, RestAction<Message>> replier, boolean shortDescription, boolean omitTags, List<FuzzyQueryResult> results, long userId, UUID buttonId);
}
