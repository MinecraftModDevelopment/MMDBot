package com.mcmoddev.bot.cursemeta.discord;

import java.util.StringJoiner;

import com.mcmoddev.bot.cursemeta.Attachment;
import com.mcmoddev.bot.cursemeta.Author;
import com.mcmoddev.bot.cursemeta.Project;
import com.mcmoddev.bot.util.Utilities;

import sx.blah.discord.util.EmbedBuilder;

public class ProjectMessage extends EmbedBuilder {

    public ProjectMessage (Project project) {

        super();

        this.setLenient(true);

        this.withTitle(project.getName());

        // Adds the authors as hyper links
        final StringJoiner authorList = new StringJoiner(", ");

        for (final Author author : project.getAuthors())
            authorList.add(author.getHyperlink());

        this.appendField("Authors", authorList.toString(), false);

        // Fields
        this.appendField("Description", project.getSummary(), false);
        this.withDesc(Utilities.makeHyperlink("Check out the mod!", "https://minecraft.curseforge.com/projects/" + project.getId()));

        // Use the first default attachment image as icon
        for (final Attachment attachment : project.getAttachments())
            if (attachment.getIsDefault()) {

                this.withThumbnail(attachment.getUrl());
                break;
            }
    }
}