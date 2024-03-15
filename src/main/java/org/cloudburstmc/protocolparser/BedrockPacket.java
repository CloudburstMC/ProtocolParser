package org.cloudburstmc.protocolparser;

import org.cloudburstmc.protocolparser.type.BedrockType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jsoup.select.Elements;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BedrockPacket implements Comparable<BedrockPacket> {
    private final int id;
    private final String name;
    private final String description;
    @Setter
    private BedrockType type;

    public static BedrockPacket parse(Elements columns) {
        int id;
        String name = "";
        String description = "";

        switch (columns.size()) {
            default:
                throw new IllegalArgumentException("Invalid column count " + columns.size());
            case 3:
                description = columns.get(2).text();
            case 2:
                name = columns.get(1).selectFirst("a").attr("href");
                name = name.substring(0, name.length() - 5); // Remove .html
            case 1:
                id = Integer.parseInt(columns.get(0).text());
        }
        return new BedrockPacket(id, name, description);
    }

    @Override
    public String toString() {
        return "# " + name + '\n' + '\n' +
                "__ID: " + id + "__" + '\n' + '\n' +
                description + '\n' + '\n' + type.toString();
    }

    @Override
    public int compareTo(BedrockPacket o) {
        return Integer.compare(id, o.id);
    }
}
