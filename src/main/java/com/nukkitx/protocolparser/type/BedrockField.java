package com.nukkitx.protocolparser.type;

import com.nukkitx.digraph.DiGraph;
import com.nukkitx.digraph.DiGraphNode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BedrockField extends BedrockStructure {
    private final String name;
    private final String type;
    private final String notes;

    public static BedrockField parse(DiGraph graph, DiGraphNode nameNode) {
        DiGraphNode typeNode = graph.getEdges().higherEntry(nameNode.getId()).getValue().getNode2();

        String name = (String) nameNode.getAttribute("label");
        String notes = getNotes(nameNode);
        String type = (String) typeNode.getAttribute("label");

        return new BedrockField(name, type, notes);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        if (notes.isEmpty()) {
            return type;
        } else {
            return "<table><tbody><tr><td>" + type + "</td><td>" + getMarkdownNotes(notes) + "</td></tr></tbody></table>";
        }
    }
}
