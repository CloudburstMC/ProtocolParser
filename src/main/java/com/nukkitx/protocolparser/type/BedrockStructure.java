package com.nukkitx.protocolparser.type;

import com.nukkitx.digraph.DiGraph;
import com.nukkitx.digraph.DiGraphEdge;
import com.nukkitx.digraph.DiGraphNode;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class BedrockStructure {

    private static final Pattern SPLIT_PATTERN = Pattern.compile(", ", Pattern.MULTILINE);
    private static final Pattern INTEGER_PATTERN = Pattern.compile("^(.+): (-?[0-9]+)$", Pattern.MULTILINE);
    private static final Pattern STRING_PATTERN = Pattern.compile("^(.+): \"(.+)\"$", Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern VECTOR_PATTERN = Pattern.compile(
            "std::vector<class std::unique_ptr<class ([A-z]+),struct std::default_delete<class ([A-z]+)>>,class std::allocator<class std::unique_ptr<class ([A-z]+),struct std::default_delete<class ([A-z]+)>>>>");

    private static final Comparator<DiGraphNode> NODE_COMPARATOR = (o1, o2) -> {
        int i1 = Integer.parseInt(o1.getId());
        int i2 = Integer.parseInt(o2.getId());
        return Integer.compare(i1, i2);
    };

    public abstract String getName();

    static List<BedrockStructure> parseStructures(DiGraph graph, DiGraphNode parent) {
        List<BedrockStructure> structures = new ArrayList<>();
        for (DiGraphNode node : getChildren(graph, parent)) {
            structures.add(parseStructure(graph, node));
        }
        return structures;
    }

    static BedrockStructure parseStructure(DiGraph graph, DiGraphNode node) {
        Map<String, Object> comments = getComments(node);
        int attributes = (int) comments.get("attributes");

        switch (attributes) {
            case 0: // Raw type
                return BedrockField.parse(graph, node);
            case 2: // Condition
                return BedrockCondition.parse(graph, node);
            case 8: // Array
                return BedrockArray.parse(graph, node);
            case 256: // Type
                return BedrockLink.parse(graph, node);
            default:
                String rootName = (String) graph.getNodes().values().iterator().next().getAttribute("label");
                int id = (int) getComments(node).get("id");
                throw new IllegalStateException(String.format("Unknown attribute %d with id '%d' in '%s'", attributes, id, rootName));
        }
    }

    static List<DiGraphNode> getChildren(DiGraph graph, DiGraphNode parent) {
        SortedMap<String, DiGraphEdge> children = graph.getEdges()
                .subMap(parent.getId() + "-", true, parent.getId() + "-999", true);
        return children.values().stream().map(DiGraphEdge::getNode2).sorted(NODE_COMPARATOR).collect(Collectors.toList());
    }

    static Map<String, Object> getComments(DiGraphNode node) {
        String[] comments = SPLIT_PATTERN.split((String) node.getAttributes().getOrDefault("comment", ""));

        Map<String, Object> commentMap = new HashMap<>();

        for (String comment : comments) {
            Matcher matcher = INTEGER_PATTERN.matcher(comment);
            if (matcher.matches()) {
                commentMap.put(matcher.group(1), Integer.parseInt(matcher.group(2)));
            } else {
                matcher = STRING_PATTERN.matcher(comment);
                if (matcher.matches()) {
                    commentMap.put(matcher.group(1), matcher.group(2));
                }
            }
        }
        return commentMap;
    }

    static String getNotes(DiGraphNode node) {
        return (String) getComments(node).getOrDefault("notes", "");
    }

    private static final Pattern ENUM_PATTERN = Pattern.compile("^enumeration: (.+)$");

    static String getMarkdownNotes(String notes) {
        Matcher matcher = ENUM_PATTERN.matcher(notes);
        if (matcher.find()) {
            return "<a href=\"../enums/" + getSafeTypeName(matcher.group(1)) + ".md\">" + matcher.group(1) + "</a>";
        }
        return notes;
    }

    private static final Pattern NET_ID_PATTERN = Pattern.compile("^(Simple|Typed)(Server|Client)NetId<struct (.+),(?:unsigned int|int),0>$");
    private static final Pattern STD_OPTIONAL = Pattern.compile("std::optional<(.+)>");
    private static final String STD_STRING = "class std::basic_string<char,struct std::char_traits<char>,class std::allocator<char>>";

    static String getSafeTypeName(String name) {
        name = name.replace("> >", ">>").replace("> >", ">>");
        Matcher matcher = VECTOR_PATTERN.matcher(name);
        if (matcher.matches()) {
            return matcher.group(1) + "[]";
        }
        matcher = NET_ID_PATTERN.matcher(name);
        if (matcher.matches()) {
            return matcher.group(1) + matcher.group(2) + "NetId_" + matcher.group(3);
        }
        name = name.replace(STD_STRING, "String");

        matcher = STD_OPTIONAL.matcher(name);
        if (matcher.matches()) {
            name = "Optional_" + matcher.group(1);
        }
        return name.replace("::", "_");
    }
}
