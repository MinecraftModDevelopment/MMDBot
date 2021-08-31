/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2021 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 * https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 */
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
