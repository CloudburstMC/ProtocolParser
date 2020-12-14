package com.nukkitx.protocolparser.type;

import com.nukkitx.digraph.DiGraph;
import com.nukkitx.digraph.DiGraphNode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BedrockType {
    private final String name;
    private final List<BedrockStructure> structures;


    public static BedrockType parse(DiGraph graph) {
        DiGraphNode root = graph.getNodes().values().iterator().next();
        String name = graph.getId();
        name = name.substring(1, name.length() - 1);

        List<BedrockStructure> structures;
        if (!root.getAttributes().isEmpty()) {
            structures = BedrockStructure.parseStructures(graph, root);
        } else {
            structures = Collections.emptyList();
        }

        return new BedrockType(name, structures);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("<table><thead><tr><th>Field</th><th>Info</th></tr></thead><tbody>\n");
        for (BedrockStructure structure : structures) {
            builder.append("<tr><td>")
                    .append(structure.getName())
                    .append("</td><td>").append(structure.toString().replaceAll("\n", "\n  "))
                    .append("</td></tr>").append('\n');
        }
        builder.append("</tbody></table>");
        return builder.toString();
    }

    public String getName() {
        return BedrockStructure.getSafeTypeName(name);
    }
}
