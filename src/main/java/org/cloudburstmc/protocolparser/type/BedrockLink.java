package org.cloudburstmc.protocolparser.type;

import com.nukkitx.digraph.DiGraph;
import com.nukkitx.digraph.DiGraphNode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BedrockLink extends BedrockStructure {
    private final String name;
    private final String type;

    public static BedrockLink parse(DiGraph graph, DiGraphNode nameNode) {
        DiGraphNode typeNode = graph.getEdges().higherEntry(nameNode.getId()).getValue().getNode2();

        String name = (String) nameNode.getAttribute("label");
        String type = (String) typeNode.getAttribute("label");

        return new BedrockLink(name, type);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "<a href=\"../types/" + getSafeTypeName(type) + ".md\">" + type + "</a>";
    }
}
