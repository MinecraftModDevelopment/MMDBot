package net.darkhax.botbase.commands.mcp;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import au.com.bytecode.opencsv.CSVReader;

public class MCPData {

    public static final File MCP_DIR = new File("config/mcpdata/");
    // client - server - both

    public static Multimap<String, MappingData> FIELDS = ArrayListMultimap.create();

    public static void main (String... strings) {

        load();
    }

    public static void load () {

        // Download
        downloadCSV("http://export.mcpbot.bspk.rs/fields.csv");
        downloadCSV("http://export.mcpbot.bspk.rs/methods.csv");
        downloadCSV("http://export.mcpbot.bspk.rs/params.csv");

        // Process
        FIELDS = loadData("fields.csv");
    }

    public static Multimap<String, MappingData> loadData (String file) {

        final Multimap<String, MappingData> data = Multimaps.newMultimap(new CaseInsensitiveForwardingMap<>(), () -> Sets.newHashSet());

        try (CSVReader reader = new CSVReader(new FileReader(new File(MCP_DIR, file)))) {

            // Skip the first one, because it's title info.
            reader.readNext();
            String[] lineData;

            while ((lineData = reader.readNext()) != null) {

                // Invalid line, it is skipped.
                if (lineData.length != 4) {

                    continue;
                }

                final MappingData mapping = new MappingData(lineData[0], lineData[1], lineData[2], lineData[3]);
                data.put(lineData[0], mapping);
                data.put(lineData[1], mapping);
            }
        }

        catch (final IOException e) {

        }

        return data;
    }

    private static void downloadCSV (String url) {

        try {

            final URL downloadUrl = new URL(url);
            FileUtils.copyURLToFile(downloadUrl, new File(MCP_DIR, FilenameUtils.getName(downloadUrl.getPath())));
        }

        catch (final IOException e) {

            // TODO better log
            e.printStackTrace();
        }
    }

    public static class MappingData {

        private final String nameSearge;
        private final String nameMcp;
        private final String side;
        private final String description;

        public MappingData (String nameSearge, String nameMcp, String side, String description) {

            super();
            this.nameSearge = nameSearge;
            this.nameMcp = nameMcp;
            this.side = side;
            this.description = description;
        }

        public String getNameSearge () {

            return this.nameSearge;
        }

        public String getNameMcp () {

            return this.nameMcp;
        }

        public String getSide () {

            return "0".equalsIgnoreCase(this.side) ? "client" : "1".equalsIgnoreCase(this.side) ? "server" : "universal";
        }

        public String getDescription () {

            return this.description;
        }

        @Override
        public String toString () {

            return "MappingData [nameSearge=" + this.nameSearge + ", nameMcp=" + this.nameMcp + ", side=" + this.side + ", description=" + this.description + "]";
        }

    }
}
