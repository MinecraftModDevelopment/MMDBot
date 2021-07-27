package com.mcmoddev.mmdbot.commands.info

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import com.mcmoddev.mmdbot.core.Utils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.shedaniel.linkie.*
import me.shedaniel.linkie.namespaces.MCPNamespace
import me.shedaniel.linkie.namespaces.YarnNamespace
import me.shedaniel.linkie.utils.MappingsQuery
import me.shedaniel.linkie.utils.QueryContext
import net.dv8tion.jda.api.EmbedBuilder
import java.util.*

class CmdMappings(name: String, private val namespace: Namespace, vararg aliases: String?) : Command() {
    init {
        this.name = name.toLowerCase(Locale.ROOT)
        this.aliases = aliases
        help = "Search for something using $name."
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
        val version = args.getOrElse(1) { namespace.getDefaultVersion() }

        GlobalScope.launch {
            val provider = namespace.getProvider(version)
            var hasPerfectMatch = false
            MappingsQuery.queryMember(QueryContext(provider, event.args)) {it.members.asSequence()}.value.asSequence()
                .also { seq ->
                    hasPerfectMatch = seq.any { it.score == 1.0 }
                }
                .filter { if (hasPerfectMatch) it.score == 1.0 else true }
                .take(5)
                .forEach {
                    event.channel.sendMessage(EmbedBuilder()
                        .setTitle("Yarn ${when(it.value.second) {
                            is Field -> "Field"
                            is Method -> "Method"
                            else -> "Member"
                        }} mapping for $version:")
                        .addField("Mapped Name", "`${it.value.second.mappedName}`", false)
                        .addField("Intermediary/SRG Name", "`${it.value.second.intermediaryName}`", false)
                        .addField("Obfuscated Name", "`${it.value.second.obfName.merged}`", false)
                        .addField("Member of Class", "`${it.value.first.mappedName ?: it.value.first.intermediaryName}`", false)
                        .addField("Descriptor", "`${it.value.second.getMappedDesc(provider.get())}`", false)
                        .build()
                    ).queue()
                }
            MappingsQuery.queryClasses(QueryContext(provider, event.args)).value
                .sortedBy { it.score }
                .also { seq ->
                    hasPerfectMatch = hasPerfectMatch || seq.any { it.score == 1.0 }
                }
                .filter { if (hasPerfectMatch) it.score == 1.0 else true }
                .take(5)
                .forEach {
                    event.channel.sendMessage(EmbedBuilder()
                        .setTitle("Yarn Class mapping for $version:")
                        .run {
                            if (it.value.mappedName != null)
                                addField("Mapped Name", "`${it.value.mappedName}`", false)
                            else this
                        }
                        .addField("Intermediary/SRG Name", "`${it.value.intermediaryName}`", false)
                        .addField("Obfuscated Name", "`${it.value.obfName.merged}`", false)
                        .build()
                    ).queue()
                }
        }
    }

    companion object {
        val mappings = LinkieConfig.DEFAULT.copy(namespaces=listOf(YarnNamespace, MCPNamespace))
    }
}
