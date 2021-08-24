package com.mcmoddev.mmdbot.modules.commands.info

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import com.mcmoddev.mmdbot.core.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.shedaniel.linkie.*
import me.shedaniel.linkie.namespaces.MCPNamespace
import me.shedaniel.linkie.namespaces.YarnNamespace
import me.shedaniel.linkie.utils.MappingsQuery
import me.shedaniel.linkie.utils.QueryContext
import me.shedaniel.linkie.utils.ResultHolder
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.Button
import java.util.*

class CmdTranslateMappings(name: String, private val namespace1: Namespace, private val namespace2: Namespace, vararg aliases: String?) : Command() {
    init {
        this.name = name.lowercase(Locale.ROOT)
        this.aliases = aliases
        help = "Turn $namespace1 into $namespace2"
    }

    /**
     * @param event The [CommandEvent] that triggered this Command.
     */
    override fun execute(event: CommandEvent) {
        if (!Utils.checkCommand(this, event)) return
        if (event.args.isEmpty()) {
            event.channel.sendMessage("No arguments given!").queue()
            return
        }

        val args = event.args.split(' ');

        val query = args[0];
        val version = args.getOrElse(1) { namespace1.getDefaultVersion() }

        scope.launch {
            val originProvider = namespace1.getProvider(version)
            val targetMappings = namespace2.getProvider(version).get()
            var hasPerfectMatch = false
            var embeds = (MappingsQuery.queryClasses(
                QueryContext(
                    originProvider,
                    query
                )
            ).value.asSequence() + MappingsQuery.queryMember(
                QueryContext(originProvider, query)
            ) { it.members.asSequence() }.value.asSequence())
                .sortedBy { it.score }
                .also { seq ->
                    hasPerfectMatch = hasPerfectMatch || seq.any { it.score == 1.0 }
                }
                .filter { if (hasPerfectMatch) it.score == 1.0 else true }
                .map { res -> res to translate(res, targetMappings) }
                .filter { it.second != null }
                .mapIndexed { idx, it ->
                        val originalResult = it.first
                        val translation = it.second

                        @Suppress("UNCHECKED_CAST")
                        when (translation) {
                            is Class -> {
                                val value = originalResult.value as Class
                                EmbedBuilder()
                                    .setTitle("$namespace1 -> $namespace2 Class mapping for $version:")
                                    .run {
                                        if (value.mappedName != null)
                                            addField("$namespace1 Name", "`${value.mappedName}`", false)
                                        else this
                                    }
                                    .addField("Intermediary/SRG Name", "`${value.intermediaryName}`", false)
                                    .addField("Obfuscated Name", "`${value.obfName.merged}`", false)
                                    .addField("$namespace2 Name", "`${translation.mappedName}`", false)
                            }
                            is MappingsMember -> {
                                val value = originalResult.value as Pair<Class, MappingsMember>
                                EmbedBuilder()
                                    .setTitle(
                                        "$namespace1 -> $namespace2 ${
                                            when (value.second) {
                                                is Field -> "Field"
                                                is Method -> "Method"
                                                else -> "Member"
                                            }
                                        } mapping for $version:"
                                    )
                                    .addField("$namespace1 Name", "`${value.second.mappedName}`", false)
                                    .addField(
                                        "Intermediary/SRG Name",
                                        "`${value.second.intermediaryName}`",
                                        false
                                    )
                                    .addField("Obfuscated Name", "`${value.second.obfName.merged}`", false)
                                    .addField("$namespace2 Name", "`${translation.mappedName}`", false)
                            }
                            else -> {
                                EmbedBuilder().setDescription("???")
                            }
                        }.setFooter("Page ${idx + 1} | Powered by linkie-core").build()
                }.iterator()

            if (!embeds.hasNext()) {
                embeds = listOf(EmbedBuilder().setTitle("$namespace1 -> $namespace2 Class mapping for $version:").setDescription("No results found.").setFooter("Powered by linkie-core").build()).iterator()
            }

            val msg = event.channel.sendMessageEmbeds(embeds.next()).apply {
                if (embeds.hasNext()) {
                    setActionRow(Button.primary("mappings-trans-next", "Next"))
                }
            }.complete()

            ButtonListener.embedsForMessage[msg.idLong] = embeds

            delay(180000L)

            ButtonListener.embedsForMessage.remove(msg.idLong)
            event.channel.editMessageById(msg.id, msg).setActionRows().complete()
        }
    }

    private fun translate(result: ResultHolder<*>, target: MappingsContainer): Any? {
        return when {
            result.value is Class -> {
                (result.value as Class).obfMergedName?.let { target.getClassByObfName(it) }
            }
            result.value is Pair<*, *> && (result.value as Pair<*, *>).second is Field -> {
                val parent = (result.value as Pair<*, *>).first as Class
                val member = (result.value as Pair<*, *>).second as Field

                member.obfMergedName?.let { memberName -> parent.obfMergedName?.let { className -> target.getClassByObfName(className)?.getFieldByObfName(memberName) } }
            }
            result.value is Pair<*, *> && (result.value as Pair<*, *>).second is Method -> {
                val parent = (result.value as Pair<*, *>).first as Class
                val member = (result.value as Pair<*, *>).second as Method

                member.obfMergedName?.let { memberName -> parent.obfMergedName?.let { className -> target.getClassByObfName(className)?.getMethodByObfName(memberName) } }
            }
            else -> null
        }
    }

    companion object {
        val mappings = LinkieConfig.DEFAULT.copy(namespaces = listOf(YarnNamespace, MCPNamespace))
        val scope = CoroutineScope(Dispatchers.Default)

        @JvmStatic
        fun createCommands(): Array<CmdTranslateMappings> = arrayOf(
            CmdTranslateMappings("yarnmojmap", YarnNamespace, MojangNamespace, "ymm"),
            CmdTranslateMappings("mcpmojmap", MCPNamespace, MojangNamespace, "mcpmm"),
            CmdTranslateMappings("mcpyarn", MCPNamespace, YarnNamespace, "mcpy", "dvf"),
            CmdTranslateMappings("yarnmcp", YarnNamespace, MCPNamespace, "ymcp", "vf"),
            CmdTranslateMappings("mojmapyarn", MojangNamespace, YarnNamespace, "mmy"),
            CmdTranslateMappings("mojmapmcp", MojangNamespace, MCPNamespace, "mmmcp")
        )
    }

    object ButtonListener : ListenerAdapter() {
        val embedsForMessage: MutableMap<Long, Iterator<MessageEmbed>> = mutableMapOf()

        override fun onButtonClick(event: ButtonClickEvent) {
            scope.launch {
                if (event.componentId == "mappings-trans-next") {
                    embedsForMessage[event.messageIdLong]?.let {
                        val newEmbed = it.next()
                        event.editMessageEmbeds(newEmbed).apply {
                            if (!it.hasNext()) {
                                setActionRow()
                            }
                        }.queue()
                    }
                }
            }
        }
    }
}
