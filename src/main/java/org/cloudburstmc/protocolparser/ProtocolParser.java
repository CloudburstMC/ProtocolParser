package org.cloudburstmc.protocolparser;

import com.nukkitx.digraph.DiGraph;
import com.nukkitx.digraph.parser.GraphParser;
import org.cloudburstmc.protocolparser.type.BedrockType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class ProtocolParser {

    private final Path inputPath;
    private final Path outputPath;

    private final Map<String, BedrockType> types = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Map<String, BedrockPacket> packets = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Map<String, BedrockEnum> enums = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public ProtocolParser(Path inputPath, Path outputPath) {
        this.inputPath = Objects.requireNonNull(inputPath, "inputPath");
        this.outputPath = Objects.requireNonNull(outputPath, "outputPath");
    }

    public void generate() throws IOException {
        if (Files.notExists(this.outputPath)) {
            Files.createDirectories(this.outputPath);
        }
        this.cleanPath(this.outputPath.resolve("enums"));
        this.cleanPath(this.outputPath.resolve("packets"));
        this.cleanPath(this.outputPath.resolve("types"));
        this.cleanPath(this.outputPath.resolve("packets.md"));

        this.generateEnums();
        this.generatePackets();
        this.generateTypes();
        this.generateMarkdown();
    }

    private void cleanPath(Path path) throws IOException {
        if (Files.exists(path)) {
            FileUtils.deleteRecursively(path);
        }
    }

    private void generateEnums() throws IOException {
        Path htmlPath = this.inputPath.resolve("html");
        Path enumsPath = htmlPath.resolve("enums.html");

        Document document = Jsoup.parse(enumsPath.toFile(), "UTF-8");

        Element table = document.body().getElementsByTag("table").get(0).getElementsByTag("tbody").get(0);
        Elements rows = table.getElementsByTag("tr");

        boolean first = true;
        for (Element row : rows) {
            if (first) {
                first = false;
                continue;
            }

            Elements columns = row.getElementsByTag("td");

            BedrockEnum bedrockEnum = BedrockEnum.parse(columns);
            enums.put(bedrockEnum.getName(), bedrockEnum);
        }
    }

    private void generatePackets() throws IOException {
        Path htmlPath = this.inputPath.resolve("html");
        Path packetsPath = htmlPath.resolve("packets.html");

        Document document = Jsoup.parse(packetsPath.toFile(), "UTF-8");

        Element table = document.body().getElementsByTag("table").get(0).getElementsByTag("tbody").get(0);
        Elements rows = table.getElementsByTag("tr");

        boolean first = true;
        for (Element row : rows) {
            if (first) {
                first = false;
                continue;
            }

            Elements columns = row.getElementsByTag("td");

            BedrockPacket packet = BedrockPacket.parse(columns);

            this.packets.put(packet.getName(), packet);
        }
    }

    private void generateTypes() throws IOException {
        Path dotPath = this.inputPath.resolve("dot");

        for (Path path : Files.newDirectoryStream(dotPath, "*.dot")) {
            String name = path.getFileName().toString();
            name = name.substring(0, name.length() - 4); // Remove .dot

            try (InputStream stream = Files.newInputStream(path)) {
                DiGraph graph = GraphParser.parse(stream);

                BedrockType type;
                try {
                    type = BedrockType.parse(graph);
                } catch (Exception e) {
                    throw new IllegalStateException("Could not parse type: " + name);
                }

                if (this.packets.containsKey(name)) {
                    this.packets.get(name).setType(type);
                } else {
                    this.types.put(name, type);
                }
            }
        }
    }

    private void generateMarkdown() throws IOException {
        // Enums
        Path enumsDir = this.outputPath.resolve("enums");
        Files.createDirectory(enumsDir);

        for (BedrockEnum bedrockEnum : enums.values()) {
            Path enumPath = enumsDir.resolve(bedrockEnum.getName().replace("::", "_") + ".md");
            Files.write(enumPath, bedrockEnum.toString().getBytes(UTF_8), TRUNCATE_EXISTING, CREATE);
        }

        // Packets
        Path packetsDir = this.outputPath.resolve("packets");
        Files.createDirectory(packetsDir);

        for (BedrockPacket packet : packets.values()) {
            Path packetPath = packetsDir.resolve(packet.getName().replaceAll("[\\\\/:*?\"<>|]", "") + ".md");
            Files.write(packetPath, packet.toString().getBytes(UTF_8), TRUNCATE_EXISTING, CREATE);
        }

        // Types
        Path typesDir = this.outputPath.resolve("types");
        Files.createDirectory(typesDir);

        for (BedrockType type : types.values()) {
            Path typePath = typesDir.resolve(type.getName().replaceAll("[\\\\/:*?\"<>|]", "") + ".md");
            String content = "# " + type.getName() + "\n\n" + type.toString();
            Files.write(typePath, content.getBytes(UTF_8), TRUNCATE_EXISTING, CREATE);
        }

        // Packets.md
        Path packetsPath = this.outputPath.resolve("packets.md");

        StringBuilder builder = new StringBuilder("# Packets\n\n| ID | Name |\n|---|---|\n");

        List<BedrockPacket> packets = new ArrayList<>(this.packets.values());
        packets.sort(null);

        for (BedrockPacket packet : packets) {
            builder.append("| ").append(packet.getId()).append(" | [")
                    .append(packet.getName()).append("](packets/").append(packet.getName()).append(".md) |\n");
        }
        Files.write(packetsPath, builder.toString().getBytes(UTF_8), TRUNCATE_EXISTING, CREATE);
    }
}
