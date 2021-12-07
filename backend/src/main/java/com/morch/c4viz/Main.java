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
        OutputGenerator generator = new OutputGenerator(workspacePathAsString, outputPath);
        generator.generate();
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
