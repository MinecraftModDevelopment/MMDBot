if (args.length < 1) {
    reply("Please provide the regex to search for.") 
} else {
    const quotes = guild.getQuotes()
    const regStr = args[0] 
    let regex = new RegExp(regStr) 
	if (args.length > 1) {
		regex = new RegExp(regStr, args[1])
	}
    const embed = new Embed()
    embed.setTitle(`Quotes matching ${regStr}`)
    for (let i = 0; i < quotes.length; i++) {
        const quote = quotes[i]
        if (regex.test(quote.getQuoteText())) {
            embed.appendDescription(`
${quote.getID()}: ${quote.getQuoteText()} - **${quote.getQuotee() == null ? "Unknown Quote" : quote.getQuotee().resolveReference()}**`)
        }
    }
    replyEmbed(embed)
} 