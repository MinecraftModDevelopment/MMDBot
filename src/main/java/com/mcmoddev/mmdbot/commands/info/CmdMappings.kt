package com.mcmoddev.mmdbot.commands.info

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import com.mcmoddev.mmdbot.core.Utils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.shedaniel.linkie.LinkieConfig
import me.shedaniel.linkie.Namespace
import me.shedaniel.linkie.Namespaces
import me.shedaniel.linkie.getMappedDesc
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
        Namespaces.init(LinkieConfig.DEFAULT)
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

        GlobalScope.launch {
            var hasPerfectMatch = false
            MappingsQuery.queryMember(QueryContext(namespace.getProvider("1.16.4"), event.args)) {it.members.asSequence()}.value
                    .sortedBy { it.score }
                    .also { seq ->
                        hasPerfectMatch = seq.any { it.score == 1.0 }
                    }
                    .filter { if (hasPerfectMatch) it.score == 1.0 else true }
                    .forEach {
                event.channel.sendMessage(EmbedBuilder()
                        .addField("Mapped Name", "`${it.value.second.mappedName}`", false)
                        .addField("Intermediary/SRG Name", "`${it.value.second.intermediaryName}`", false)
                        .addField("Obfuscated Name", "`${it.value.second.obfName.merged}`", false)
                        .addField("Member of Class", "`${it.value.first.mappedName ?: it.value.first.intermediaryName}`", false)
                        .addField("Descriptor", "`${it.value.second.getMappedDesc(namespace.getProvider("1.16.4").get())}`", false)
                        .build()
                ).queue()
            }
            MappingsQuery.queryClasses(QueryContext(namespace.getProvider("1.16.4"), event.args)).value
                    .sortedBy { it.score }
                    .also { seq ->
                        hasPerfectMatch = hasPerfectMatch || seq.any { it.score == 1.0 }
                    }
                    .filter { if (hasPerfectMatch) it.score == 1.0 else true }.forEach {
                event.channel.sendMessage(EmbedBuilder()
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
        val mappings = LinkieConfig(LinkieConfig.DEFAULT.cacheDirectory, LinkieConfig.DEFAULT.maximumLoadedVersions, listOf(YarnNamespace, MCPNamespace), LinkieConfig.DEFAULT.reloadCycleDuration)
    }
}
