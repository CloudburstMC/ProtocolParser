package org.cloudburstmc.protocolparser;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.cloudburstmc.protocolparser.type.BedrockType;
import org.jsoup.select.Elements;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BedrockPacket implements Comparable<BedrockPacket> {
    private final int id;
    private final String name;
    private final String purpose;
    private final String description;
    @Setter
    private BedrockType type;

    public static BedrockPacket parse(Elements columns) {
        if (columns.size() < 3 || columns.size() > 4) {
            throw new IllegalArgumentException("Invalid column count " + columns.size());
        }

        int id = Integer.parseInt(columns.get(0).text().trim());

        String name = columns.get(1)
                .selectFirst("a[href$=.html]")
                .attr("href");
        name = name.substring(0, name.length() - 5);

        String purpose = "";
        String description;

        if (columns.size() == 4) {
            purpose = clean(columns.get(2).text());
            description = clean(columns.get(3).text());
        } else {
            description = clean(columns.get(2).text());
        }

        return new BedrockPacket(id, name, purpose, description);
    }

    private static String clean(String text) {
        text = text.trim();
        return text.equals("-") ? "" : text;
    }

    @Override
    public String toString() {
        return "# " + name + '\n' + '\n' +
                "**ID: " + id + "**  \n" +
                (purpose.isEmpty() ? "" : "**Purpose: " + purpose + "**  \n") +
                (description.isEmpty() ? "" : '\n' + description + '\n') +
                (type != null ? '\n' + type.toString() : "");
    }

    @Override
    public int compareTo(BedrockPacket o) {
        return Integer.compare(id, o.id);
    }
}
