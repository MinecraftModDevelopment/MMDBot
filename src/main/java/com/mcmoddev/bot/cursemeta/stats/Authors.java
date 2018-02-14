package com.mcmoddev.bot.cursemeta.stats;

import java.util.List;
import java.util.Map;

public class Authors implements Comparable<Authors> {

    public Map<String, List<Long>> member;

    public Map<String, List<Long>> owner;

    @Override
    public int compareTo (Authors o) {

        final int ourMods = getMods(this);
        final int theirMods = getMods(o);

        // We have more mods
        if (ourMods > theirMods) {

            return 1;
        }

        // We have the same amount of mods
        else if (ourMods == theirMods) {

            return 0;
        }

        // We have less mods.
        return -1;
    }

    public static int getMods (Authors o) {

        final List<Long> s = o.owner.get("Mods");

        return s != null ? s.size() : 0;
    }
}
