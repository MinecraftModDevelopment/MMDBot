/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2021 <MMD - MinecraftModDevelopment>
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
package com.mcmoddev.mmdbot.modules.commands.general.mappings

import com.jagrosh.jdautilities.command.SlashCommand
import com.mcmoddev.mmdbot.utilities.Utils
import kotlinx.coroutines.*
import me.shedaniel.linkie.*
import me.shedaniel.linkie.namespaces.MCPNamespace
import me.shedaniel.linkie.namespaces.MojangNamespace
import me.shedaniel.linkie.namespaces.YarnNamespace
import me.shedaniel.linkie.utils.MappingsQuery
import me.shedaniel.linkie.utils.MatchAccuracy
import me.shedaniel.linkie.utils.QueryContext
import me.shedaniel.linkie.utils.ResultHolder
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.components.Button
import java.awt.Color
import java.util.*

/**
 * Service a request for mappings.
 * Handles MCP, Yarn and Mojang Mapping types via abstraction.
 *
 *
 * @author Will BL
 * @author Curle
 */
class CmdMappings(name: String, private val namespace: Namespace, vararg aliases: String?) : SlashCommand() {
    init {
        this.name = name.lowercase(Locale.ROOT)
        this.aliases = aliases
        help = "Search for a mapping with $name."

        options = arrayListOf(
            OptionData(OptionType.STRING, "query", "A mapping to query.").setRequired(true),
            OptionData(OptionType.STRING, "version", "The version of Minecraft to check.").setRequired(false)
        )
    }

    /**
     * @param event The [SlashCommandEvent] that triggered this Command.
     */
    override fun execute(event: SlashCommandEvent) {
        if (!Utils.checkCommand(this, event)) {
            return
        }


        val queryString = event.getOption("query")?.asString ?: ""
        val version = event.getOption("version")?.asString ?: namespace.getDefaultVersion()

        scope.launch {
            val provider = namespace.getProvider(version)
            var embeds = query(provider, queryString)
                .mapIndexed { idx, it ->
                    async {
                        @Suppress("UNCHECKED_CAST")
                        when (it.value) {
                            is Class -> {
                                val value = it.value as Class
                                EmbedBuilder()
                                    .setTitle("${provider.get().name} Class mapping for $version:")
                                    .run {
                                        if (value.mappedName != null)
                                            addField("Mapped Name", "`${value.mappedName}`", false)
                                        else this
                                    }
                                    .addField("Intermediary/SRG Name", "`${value.intermediaryName}`", false)
                                    .addField("Obfuscated Name", "`${value.obfName.merged}`", false)
                            }
                            is Pair<*, *> -> {
                                val value = it.value as Pair<Class, MappingsMember>
                                EmbedBuilder()
                                    .setTitle(
                                        "${provider.get().name} ${
                                            when (value.second) {
                                                is Field -> "Field"
                                                is Method -> "Method"
                                                else -> "Member"
                                            }
                                        } mapping for $version:"
                                    )
                                    .addField("Mapped Name", "`${value.second.mappedName}`", false)
                                    .addField(
                                        "Intermediary/SRG Name",
                                        "`${value.second.intermediaryName}`",
                                        false
                                    )
                                    .addField("Obfuscated Name", "`${value.second.obfName.merged}`", false)
                                    .addField(
                                        "Member of Class",
                                        "`${value.first.mappedName ?: value.first.intermediaryName}`",
                                        false
                                    )
                                    .addField(
                                        "Descriptor",
                                        "`${value.second.getMappedDesc(provider.get())}`",
                                        false
                                    )
                                    .addField(
                                        "Mixin Target",
                                        "`L${value.first.optimumName};${value.second.optimumName}:${
                                            value.second.getMappedDesc(
                                                provider.get()
                                            )
                                        }`",
                                        false
                                    )
                            }
                            else -> {
                                EmbedBuilder().setDescription("???")
                            }
                        }.setFooter("Page ${idx + 1} | Powered by linkie-core").build()
                    }
                }.iterator()

            if (!embeds.hasNext()) {
                embeds = listOf(async {
                    EmbedBuilder()
                        .setTitle("$namespace mapping for $version:")
                        .setDescription("No results found.")
                        .setFooter("Powered by linkie-core")
                        .setColor(Color.RED)
                        .build()
                }).iterator()
            }

            val msg = event.replyEmbeds(embeds.next().await()).apply {
                if (embeds.hasNext()) {
                    addActionRow(Button.primary("mappings-next", "Next"))
                }
                setEphemeral(true)
            }.complete()

            ButtonListener.embedsForMessage[msg.interaction.idLong] = embeds

            delay(180000L)

            ButtonListener.embedsForMessage.remove(msg.interaction.idLong)
            msg.editOriginalComponents().complete()
        }
    }

    companion object {
        val mappings = LinkieConfig.DEFAULT.copy(namespaces = listOf(YarnNamespace, MCPNamespace, MojangNamespace))
        val scope = CoroutineScope(Dispatchers.Default)

        @JvmStatic
        fun createCommands(): Array<CmdMappings> = arrayOf(
            CmdMappings("yarn", YarnNamespace, "y"),
            CmdMappings("mcp", MCPNamespace, "mcp"),
            CmdMappings("mojmap", MojangNamespace, "mm")
        )

        suspend fun query(provider: MappingsProvider, query: String): Sequence<ResultHolder<out Any>> {
            val context = QueryContext(
                provider,
                query.replace(Regex("\\(\\w*\\)"), "").replace(Regex("[.#]"), "/"),
                MatchAccuracy.Fuzzy
            )

            var hasPerfectMatch = false
            return (
                    MappingsQuery.queryClasses(context).value.asSequence()
                            + MappingsQuery.queryMember(context) { it.members.asSequence() }.value.asSequence()
                    )
                .sortedBy { it.score }
                .also { seq ->
                    hasPerfectMatch = hasPerfectMatch || seq.any { it.score == 1.0 }
                }
                .filter { if (hasPerfectMatch) it.score == 1.0 else true }
        }
    }

    object ButtonListener : ListenerAdapter() {
        val embedsForMessage: MutableMap<Long, Iterator<Deferred<MessageEmbed>>> = mutableMapOf()

        override fun onButtonClick(event: ButtonClickEvent) {
            scope.launch {
                if (event.componentId == "mappings-next") {
                    embedsForMessage[event.messageIdLong]?.let {
                        val newEmbed = it.next().await()
                        event.editMessageEmbeds(newEmbed).apply {
                            if (!it.hasNext()) {
                                setActionRows()
                            }
                        }.queue()
                    }
                }
            }
        }
    }
}
