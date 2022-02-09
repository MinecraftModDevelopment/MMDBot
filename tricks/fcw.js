let urlSuffix = ""
if (args.length == 1) {
	urlSuffix = parseString(args[0])
} else if (args.length > 1) {
	urlSuffix = `${args[0]}/${args[1]}`
}
const url = `https://forge.gemwire.uk/wiki/${urlSuffix}`
const embed = new Embed()
embed.setTitle("Forge Community Wiki")
embed.setDescription(`There's an article at ${url} from the Forge Community Wiki that may be of interest to you.`)
embed.setFooter("FCW, hosted by Curle, managed by sciwhiz12")
embed.setColor(parseInt(Math.random() * 16777215)) // Generate a random colour
replyEmbed(embed)