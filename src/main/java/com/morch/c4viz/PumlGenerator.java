package com.morch.c4viz;

import com.structurizr.Workspace;
import com.structurizr.dsl.StructurizrDslParser;
import com.structurizr.io.Diagram;
import com.structurizr.io.plantuml.AbstractPlantUMLExporter;
import com.structurizr.io.plantuml.C4PlantUMLExporter;
import com.structurizr.view.Styles;
import com.structurizr.view.ThemeUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;

public class PumlGenerator {
    String workspacePath;
    String outputPath;

    PumlGenerator(String workspacePath, String outputPath) {
        this.workspacePath = workspacePath;
        this.outputPath = outputPath;
    }

    void generate() {
        try {
            generateUnsafely();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void generateUnsafely() throws Exception {
        StructurizrDslParser structurizrDslParser = new StructurizrDslParser();
        File workspacePathFile = new File(workspacePath);
        structurizrDslParser.parse(workspacePathFile);
        Workspace workspace;
        workspace = structurizrDslParser.getWorkspace();
        long workspaceId = workspace.getId();
        ThemeUtils.loadThemes(workspace);

        addDefaultViewsAndStyles(workspace);

        if (outputPath == null) {
            outputPath = new File(workspacePathFile.getCanonicalPath()).getParent();
        }

        File outputDir = new File(outputPath);
        outputDir.mkdirs();

        AbstractPlantUMLExporter plantUMLExporter = new C4PlantUMLExporter();
        if (workspace.getViews().isEmpty()) {
            throw new RuntimeException("the workspace contains no views");
        }
        plantUMLExporter.setUseSequenceDiagrams(false);
        Collection<Diagram> diagrams = plantUMLExporter.export(workspace);

        for (Diagram diagram : diagrams) {
            File file = new File(outputPath, String.format("%s-%s.puml", prefix(workspaceId), diagram.getKey()));
            writeToFile(file, diagram.getDefinition());
        }
    }

    private void addDefaultViewsAndStyles(Workspace workspace) {
        if (workspace.getViews().isEmpty()) {
            System.out.println(" - no views defined; creating default views");
            workspace.getViews().createDefaultViews();
        }

        Styles styles = workspace.getViews().getConfiguration().getStyles();
        if (styles.getElements().isEmpty() && styles.getRelationships().isEmpty() && workspace.getViews().getConfiguration().getThemes() == null) {
            System.out.println(" - no styles or themes defined; use the \"default\" theme to add some default styles");
        }
    }

    private String prefix(long workspaceId) {
        if (workspaceId > 0) {
            return "structurizr-" + workspaceId;
        } else {
            return "structurizr";
        }
    }

    private void writeToFile(File file, String content) throws Exception {
        System.out.println(" - writing " + file.getCanonicalPath());
        BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8);
        writer.write(content);
        writer.close();
    }
}
