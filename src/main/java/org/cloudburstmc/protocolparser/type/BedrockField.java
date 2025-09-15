package org.cloudburstmc.protocolparser.type;

import com.nukkitx.digraph.DiGraph;
import com.nukkitx.digraph.DiGraphNode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BedrockField extends BedrockStructure {
    private final String name;
    private final String type;
    private final String notes;

    public static BedrockField parse(DiGraph graph, DiGraphNode nameNode) {
        String fieldName = firstNonEmpty((String) nameNode.getAttribute("label"), "value");
        String notes = getNotes(nameNode);

        List<DiGraphNode> children = getChildren(graph, nameNode);
        String typeString = children.isEmpty()
                ? typeFromNode(nameNode) // primitive leaf
                : typeFromNode(children.get(0)); // child holds the type

        return new BedrockField(fieldName, typeString, notes);
    }

    private static String typeFromNode(DiGraphNode node) {
        Map<String, Object> cm = getComments(node);
        String commentType = (String) cm.get("typeName");
        String labelType = (String) node.getAttribute("label");
        String rawType = firstNonEmpty(commentType, labelType, "");
        return getSafeTypeName(rawType);
    }

    private static String firstNonEmpty(String... values) {
        for (String v : values) {
            if (v != null && !v.isEmpty()) return v;
        }
        return "";
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
