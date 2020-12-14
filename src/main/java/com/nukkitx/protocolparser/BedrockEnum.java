package com.nukkitx.protocolparser;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BedrockEnum {
    private static final Pattern SPLIT_PATTERN = Pattern.compile("\n");
    private static final Pattern ENTRY_PATTERN = Pattern.compile(" = ");

    private final String name;
    private final Map<String, String> values;

    public static BedrockEnum parse(Elements columns) {
        String name = columns.get(0).text();
        String[] entries = SPLIT_PATTERN.split(text(columns.get(1)));

        Map<String, String> values = new LinkedHashMap<>();

        for (String entry : entries) {
            String[] parts = ENTRY_PATTERN.split(entry, 2);
            if (parts.length < 2) continue;
            values.put(parts[0], parts[1]);
        }

        return new BedrockEnum(name, values);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        // header
        builder.append("# ").append(name);
        builder.append("\n\n");
        // table header
        builder.append("Name | Value").append('\n');
        builder.append("--- | ---");

        values.forEach((name, value) -> {
            builder.append('\n').append(name).append(" | ").append(value);
        });

        return builder.toString();
    }

    /*          HACK - Fix break lines           */

    private static final Method APPEND_NORMALISED_TEXT;
    private static final Method LAST_CHAR_IS_WHITESPACE;
    private static final Field TAG_FIELD;

    static {
        try {
            APPEND_NORMALISED_TEXT = Element.class.getDeclaredMethod("appendNormalisedText", StringBuilder.class, TextNode.class);
            TAG_FIELD = Element.class.getDeclaredField("tag");
            LAST_CHAR_IS_WHITESPACE = TextNode.class.getDeclaredMethod("lastCharIsWhitespace", StringBuilder.class);

            APPEND_NORMALISED_TEXT.setAccessible(true);
            TAG_FIELD.setAccessible(true);
            LAST_CHAR_IS_WHITESPACE.setAccessible(true);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    private static String text(Element element) {
        final StringBuilder accum = StringUtil.borrowBuilder();
        NodeTraversor.traverse(new NodeVisitor() {
            @SneakyThrows
            public void head(Node node, int depth) {
                if (node instanceof TextNode) {
                    TextNode textNode = (TextNode) node;
                    APPEND_NORMALISED_TEXT.invoke(null, accum, textNode);
                } else if (node instanceof Element) {
                    Element element = (Element) node;
                    if (accum.length() > 0 &&
                            (element.isBlock() || ((Tag) TAG_FIELD.get(element)).getName().equals("br")) &&
                            !((boolean) LAST_CHAR_IS_WHITESPACE.invoke(null, accum)))
                        accum.append('\n');
                }
            }

            @SneakyThrows
            public void tail(Node node, int depth) {
                // make sure there is a space between block tags and immediately following text nodes <div>One</div>Two should be "One Two".
                if (node instanceof Element) {
                    Element element = (Element) node;
                    if (element.isBlock() && (node.nextSibling() instanceof TextNode) && !((boolean) LAST_CHAR_IS_WHITESPACE.invoke(null, accum)))
                        accum.append(' ');
                }

            }
        }, element);

        return StringUtil.releaseBuilder(accum).trim();
    }
}
