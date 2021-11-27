package com.morch.c4viz;

public class Main {
    public static void main(String[] args) {
        String workspacePathAsString = "/home/peter/idea/c4/dsl/examples/big-bank-plc.dsl";
        String outputPath = "/home/peter/idea/c4/foobar";
        PumlGenerator generator = new PumlGenerator(workspacePathAsString, outputPath);
        generator.generate();
    }
}
