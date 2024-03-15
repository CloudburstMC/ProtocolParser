package org.cloudburstmc.protocolparser.type;

import com.nukkitx.digraph.DiGraph;
import com.nukkitx.digraph.DiGraphNode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BedrockArray extends BedrockStructure {

    private final String name;
    private final String notes;
    private final BedrockField size;
    private final List<BedrockStructure> element;

    public static BedrockArray parse(DiGraph graph, DiGraphNode node) {
        List<DiGraphNode> children = getChildren(graph, node);

        String name = (String) node.getAttribute("label");
        String notes = getNotes(node);
        BedrockField size = null;
        List<BedrockStructure> element;

        switch (children.size()) {
            default:
                throw new IllegalArgumentException("Unexpected children size: " + children.size());
            case 2: // Size
                size = BedrockField.parse(graph, children.get(0));
                element = parseStructures(graph, children.get(1));
                break;
            case 1: // Elements
                element = parseStructures(graph, children.get(0));
                break;
        }

        return new BedrockArray(name, notes, size, element);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (size != null) {
            builder.append("<b>").append(size.getName()).append(":</b> ").append(size.toString()).append("\n");
        }
        if (!notes.isEmpty()) {
            builder.append(notes).append("  \n");
        }

        builder.append("<table><thead><tr><th>Field</th><th>Info</th></tr></thead><tbody>\n");
        for (BedrockStructure structure : element) {
            if (structure == null) continue;
            builder.append("<tr><td>")
                    .append(structure.getName())
                    .append("</td><td>").append(structure.toString().replaceAll("\n", "\n  "))
                    .append("</td></tr>").append('\n');
        }
        builder.append("</tbody></table>");

        return builder.toString();
    }
}
