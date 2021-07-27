package com.mcmoddev.mmdbot.helpers.forge;

import com.mcmoddev.mmdbot.utilities.updatenotifiers.forge.ForgeVersion;
import com.mcmoddev.mmdbot.utilities.updatenotifiers.forge.ForgeVersionHelper;
import com.mcmoddev.mmdbot.utilities.updatenotifiers.forge.VersionMeta;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 *
 * @author
 *
 */
class ForgeVersionHelperTest {

	/**
	 *
	 */
	private static final String ONE_SEVEN_TEN = "1.7.10";

	/**
	 *
	 */
    @Test
    void parseVersion() {
        final VersionMeta vMeta = ForgeVersionHelper.getMCVersion("1.7.10-latest");
        assertEquals(vMeta.getState(), "latest");
        assertEquals(vMeta.getVersion(), ONE_SEVEN_TEN);
    }

    /**
     *
     */
    @Test
    void readVersions() {
        try {
            final Map<String, ForgeVersion> result = ForgeVersionHelper.getForgeVersions();

            assertFalse(result.isEmpty());
            assertTrue(result.containsKey(ONE_SEVEN_TEN));
            assertEquals(result.get(ONE_SEVEN_TEN).getLatest(), "10.13.4.1614");
            assertEquals(result.get(ONE_SEVEN_TEN).getRecommended(), "10.13.4.1614");
        } catch (final RuntimeException ex) {
        	throw ex;
        } catch (final Exception ex) {
            fail("Exception thrown", ex);
        }
    }

    /**
     *
     */
    @Test
    void getLatest() {
        final String latest = ForgeVersionHelper.getLatestVersion(Arrays.asList(ONE_SEVEN_TEN, "1.10.2", "1.10"));
        assertEquals(latest, "1.10.2");
    }
}
