package com.mcmoddev.mmdbot.commander.curseforge;

import static java.lang.System.lineSeparator;
import com.mcmoddev.mmdbot.commander.TheCommander;
import io.github.matyrobbrt.curseforgeapi.request.AsyncRequest;
import io.github.matyrobbrt.curseforgeapi.request.helper.AsyncRequestHelper;
import io.github.matyrobbrt.curseforgeapi.schemas.mod.Mod;
import io.github.matyrobbrt.curseforgeapi.util.CurseForgeException;
import io.github.matyrobbrt.curseforgeapi.util.Pair;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jsoup.Jsoup;

import java.awt.Color;
import java.time.Instant;
import java.util.Optional;

@UtilityClass
public class CFUtils {

    public static AsyncRequest<EmbedBuilder> createFileEmbed(Mod mod, int fileId) throws CurseForgeException {
        return getAsyncApiHelper().getModFile(mod.id(), fileId)
            .and(getAsyncApiHelper().getModFileChangelog(mod.id(), fileId))
            .map(Pair::mapResponses)
            .map(Optional::orElseThrow)
            .map(p -> p.map((file, changelog) -> new EmbedBuilder()
                .setTimestamp(Instant.parse(file.fileDate()))
                .setTitle(mod.name(), linkFromSlug(mod.slug()))
                .setColor(Color.DARK_GRAY)
                .setThumbnail(mod.logo().thumbnailUrl())
                .appendDescription("New file detected for CurseForge Project")
                .appendDescription(lineSeparator())
                .appendDescription(lineSeparator())
                .appendDescription("Release Type: `%s`".formatted(file.releaseType()))
                .appendDescription(lineSeparator())
                .appendDescription("File Name: `%s`".formatted(file.fileName()))
                .appendDescription(lineSeparator())
                .appendDescription("Game Versions: `%s`".formatted(String.join(", ", file.gameVersions())))
                .appendDescription(lineSeparator())
                .appendDescription("Download URL: [CurseForge](%s)".formatted(file.downloadUrl()))
                .appendDescription(lineSeparator())
                .appendDescription(lineSeparator())
                .appendDescription("""
                    Changelog:
                    ```
                    %s
                    ```""".formatted(Jsoup.parse(changelog).text())))
            );
    }

    public static String linkFromSlug(String slug) {
        return "https://www.curseforge.com/minecraft/mc-mods/" + slug;
    }

    private static AsyncRequestHelper getAsyncApiHelper() {
        return TheCommander.getInstance().getCurseForgeManager().orElseThrow().api().getAsyncHelper();
    }

}
