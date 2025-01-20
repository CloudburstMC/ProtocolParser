package org.cloudburstmc.protocolparser;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.util.PathConverter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class Main {

    private static final Path PATH = Paths.get(".");

    public static void main(String[] args) {
        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();

        OptionSpec<Void> helpSpec = parser.accepts("help", "Shows this page").forHelp();
        OptionSpec<Path> inputSpec = parser.acceptsAll(Arrays.asList("input", "in", "i"), "Input directory")
                .withRequiredArg().required().ofType(File.class).withValuesConvertedBy(new PathConverter());
        OptionSpec<Path> outputSpec = parser.acceptsAll(Arrays.asList("output", "out", "o"), "Output directory")
                .withRequiredArg().required().ofType(File.class).withValuesConvertedBy(new PathConverter());

        OptionSet options = parser.parse(args);

        if (options.has(helpSpec)) {
            try {
                // Display help page
                parser.printHelpOn(System.out);
            } catch (IOException ignored) {
            }
            return;
        }

        Path inputPath = options.valueOf(inputSpec);
        Path outputPath = options.valueOf(outputSpec);

        ProtocolParser protocolParser = new ProtocolParser(inputPath, outputPath);

        try {
            protocolParser.generate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
