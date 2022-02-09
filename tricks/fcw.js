const embed = new Embed()
if (args.length < 1 || !args[0]) {
	embed.setDescription(`There's a collection of information maintained by the community at https://forge.gemwire.uk that you might find useful.`)
} else {
	const url = `https://forge.gemwire.uk/wiki/${args.length > 1 ? (args[0] + "/" + args[1]) : args[0]}`
	embed.setDescription(`There's an article at ${url} from the Forge Community Wiki that may be of interest to you.`)
}
embed.setTitle("Forge Community Wiki")
embed.setFooter("FCW, hosted by Curle, managed by sciwhiz12")
embed.setColor(parseInt(Math.random() * 16777215)) // Generate a random colour
replyEmbed(embed)