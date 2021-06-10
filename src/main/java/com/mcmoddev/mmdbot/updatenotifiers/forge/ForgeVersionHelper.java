package com.mcmoddev.mmdbot.updatenotifiers.forge;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author
 *
 */
public final class ForgeVersionHelper {

    /**
     *
     */
    private static final String VERSION_URL = "https://files.minecraftforge.net/net/minecraftforge/forge/promotions_slim.json";

    /**
     *
     */
    private static final Pattern VERSION_REGEX = Pattern.compile("(.+?)-(.+)");

    /**
     *
     */
    private static final Gson GSON = new Gson();


	/**
	 *
	 */
    private ForgeVersionHelper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     *
     * @param versions
     * @return
     */
    public static String getLatestVersion(final List<String> versions) {
        SemVer latest = new SemVer(versions.get(0));

        for (final String version : versions) {
            final SemVer ver = new SemVer(version);
            if (latest.compareTo(ver) < 0) {
                latest = ver;
            }
        }

        return latest.toString();
    }

    /**
     *
     * @param mcVersion
     * @return
     * @throws IOException
     * @throws ClassCastException
     * @throws NullPointerException
     */
    public static ForgeVersion getForgeVersionsForMcVersion(final String mcVersion) throws IOException, ClassCastException, NullPointerException {
        return getForgeVersions().get(mcVersion);
    }

    /**
     *
     * @return
     * @throws IOException
     * @throws JsonSyntaxException
     * @throws JsonIOException
     */
    public static MinecraftForgeVersion getLatestMcVersionForgeVersions() throws IOException, JsonSyntaxException, JsonIOException {
        final Map<String, ForgeVersion> versions = getForgeVersions();

        final String latest = getLatestVersion(new ArrayList<>(versions.keySet()));

        return new MinecraftForgeVersion(latest, versions.get(latest));
    }

    /**
     *
     * @return
     * @throws IOException
     */
    private static InputStreamReader openUrl() throws IOException {
        final URL urlObj = new URL(VERSION_URL);

        return new InputStreamReader(urlObj.openStream(), Charsets.UTF_8);
    }

    /**
     *
     * @return
     * @throws IOException
     * @throws JsonSyntaxException
     * @throws JsonIOException
     */
    public static Map<String, ForgeVersion> getForgeVersions() throws IOException, JsonSyntaxException, JsonIOException {
    	final InputStreamReader reader = openUrl();

    	final ForgePromoData data = GSON.fromJson(reader, ForgePromoData.class);

        // Remove this specific entry (differs from others with having the `_pre4` version)
        data.promos.remove("1.7.10_pre4-latest");

        // Collect version data
        final Map<String, ForgeVersion> versions = new HashMap<>();

        for (final Map.Entry<String, String> entry : data.promos.entrySet()) {
        	final String mc = entry.getKey();
        	final String forge = entry.getValue();

        	final VersionMeta meta = getMCVersion(mc);

        	if (meta != null) {
                if (versions.containsKey(meta.version)) {
                    final ForgeVersion version = versions.get(meta.version);
                    if (meta.state.equals("recommended")) {
                        version.setRecommended(forge);
                    } else {
                        version.setLatest(forge);
                    }
                } else {
                    final ForgeVersion version = new ForgeVersion();
                    if (meta.state.equals("recommended")) {
                        version.setRecommended(forge);
                    } else {
                        version.setLatest(forge);
                    }
                    versions.put(meta.version, version);
                }
            }
        }

        return versions;
    }

    /**
     *
     * @param version
     * @return
     */
    public static VersionMeta getMCVersion(final String version) {
    	final Matcher m = VERSION_REGEX.matcher(version);

        if (m.find()) {
            return new VersionMeta(m.group(1), m.group(2));
        } else {
            return null;
        }
    }
}
