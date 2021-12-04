package com.morch.c4viz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {
    private static void usage() {
        System.err.println("Usage: input.dsl outputdir");
        System.exit(1);
    }

    private static void cliMain(String[] args) {
        if (args.length < 2) {
            usage();
        }
        String workspacePathAsString = args[0];
        String outputPath = args[1];
        PumlGenerator generator = new PumlGenerator(workspacePathAsString, outputPath);
        generator.generate();
        // See https://forum.plantuml.net/15083/why-does-this-4-line-puml-file-take-more-than-minute-to-render
        System.exit(0);
    }

    private static void springMain(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    public static void main(String[] args) {
        if (System.getenv("SERVER_PORT") == null) {
            cliMain(args);
        } else {
            springMain(args);
        }
    }
}
