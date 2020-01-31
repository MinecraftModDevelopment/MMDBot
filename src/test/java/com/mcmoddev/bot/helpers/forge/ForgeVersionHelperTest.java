package com.mcmoddev.bot.helpers.forge;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ForgeVersionHelperTest {

    @Test
    void parseVersion() {
        VersionMeta v = ForgeVersionHelper.getMCVersion("1.7.10-latest");
        assertEquals(v.state, "latest");
        assertEquals(v.version, "1.7.10");
    }

    @Test
    void readVersions() {
        try {
            Map<String, ForgeVersion> result = ForgeVersionHelper.getForgeVersions();

            assertTrue(result.size() > 0);
            assertTrue(result.containsKey("1.7.10"));
            assertEquals(result.get("1.7.10").getLatest(), "10.13.4.1614");
            assertEquals(result.get("1.7.10").getRecommended(), "10.13.4.1558");
        } catch(Exception e) {
            fail("Exception thrown", e);
        }
    }

    @Test
    void getLatest() {
        String latest = ForgeVersionHelper.getLatestVersion(Arrays.asList("1.7.10", "1.10.2", "1.10"));
        assertEquals(latest, "1.10.2");
    }
}
