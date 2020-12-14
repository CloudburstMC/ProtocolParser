package com.nukkitx.protocolparser.type;

import com.nukkitx.digraph.DiGraph;
import com.nukkitx.digraph.DiGraphNode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BedrockCondition extends BedrockStructure {
    private final String name;
    private final Map<String, List<BedrockStructure>> conditions;

    public static BedrockCondition parse(DiGraph graph, DiGraphNode node) {
        String name = (String) node.getAttribute("label");

        Map<String, List<BedrockStructure>> conditions = new LinkedHashMap<>();

        for (DiGraphNode condition : getChildren(graph, node)) {
            List<DiGraphNode> children = getChildren(graph, condition);
            if (children.size() == 1 && "[No Data]".equals(children.get(0).getAttribute("label"))) {
                continue;
            }

            String conditionName = (String) condition.getAttribute("label");

            conditions.put(conditionName, parseStructures(graph, condition));
        }

        return new BedrockCondition(name, conditions);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("<hr>\n");
        conditions.forEach((condition, structures) -> {
            StringBuilder builder = new StringBuilder("<b>").append(condition).append("</b><br>\n");

            builder.append("<table><thead><tr><th>Field</th><th>Info</th></tr></thead><tbody>\n");
            for (BedrockStructure structure : structures) {
                builder.append("<tr><td>")
                        .append(structure.getName())
                        .append("</td><td>").append(structure.toString().replaceAll("\n", "\n  "))
                        .append("</td></tr>").append('\n');
            }
            builder.append("</tbody></table>");
            joiner.add(builder.toString());
        });

        return joiner.toString();
    }
}
