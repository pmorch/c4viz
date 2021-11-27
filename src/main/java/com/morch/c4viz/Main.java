package com.morch.c4viz;

public class Main {
    private static void usage() {
        System.err.println("Usage: input.dsl outputdir");
        System.exit(1);
    }
    public static void main(String[] args) {
        String workspacePathAsString = args[0];
        String outputPath = args[1];
        if (workspacePathAsString == null || outputPath == null) {
            usage();
        }
        PumlGenerator generator = new PumlGenerator(workspacePathAsString, outputPath);
        generator.generate();
    }
}
