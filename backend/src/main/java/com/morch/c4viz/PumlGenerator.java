package com.morch.c4viz;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.structurizr.Workspace;
import com.structurizr.dsl.StructurizrDslParser;
import com.structurizr.io.Diagram;
import com.structurizr.io.plantuml.C4PlantUMLExporter;
import com.structurizr.model.*;
import com.structurizr.view.*;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
        ThemeUtils.loadThemes(workspace);

        addDefaultViewsAndStyles(workspace);

        if (outputPath == null) {
            outputPath = new File(workspacePathFile.getCanonicalPath()).getParent();
        }

        File outputDir = new File(outputPath);
        outputDir.mkdirs();

        if (workspace.getViews().isEmpty()) {
            throw new RuntimeException("the workspace contains no views");
        }

        Collection<Diagram> diagrams = export(workspace);

        long totalPlantUMLMillis = System.currentTimeMillis();
        List<VizData> vizData = new ArrayList<>();
        for (Diagram diagram : diagrams) {
            System.out.println(String.format("Writing %s/%s.*", outputPath, getViewName(workspace, diagram.getView())));

            File file = new File(outputPath, String.format("%s.puml", getViewName(workspace, diagram.getView())));
            writeToFile(file, diagram.getDefinition());

            SourceStringReader reader = new SourceStringReader(diagram.getDefinition());
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            // DiagramDescription desc =
            reader.outputImage(byteArrayOutputStream, new FileFormatOption(FileFormat.SVG));
            String svg = byteArrayOutputStream.toString(StandardCharsets.UTF_8);

            FileOutputStream fileOutputStream = new FileOutputStream(String.format("%s/%s.svg", outputPath, getViewName(workspace, diagram.getView())));
            fileOutputStream.write(svg.getBytes(StandardCharsets.UTF_8));
            fileOutputStream.close();

            String type = diagram.getView().getClass().getSimpleName();
            String name = getViewName(workspace, diagram.getView());

            vizData.add(new VizData(type, name, svg, diagram.getDefinition()));
        }
        ObjectMapper objectMapper = new ObjectMapper();
        String vizString = objectMapper.writeValueAsString(vizData);
        FileOutputStream fileOutputStream = new FileOutputStream(String.format("%s/c4viz.json", outputPath));
        fileOutputStream.write(vizString.getBytes(StandardCharsets.UTF_8));
        fileOutputStream.close();
        System.out.println(String.format(
                "Done - Writing %d output files took %dms", diagrams.size()+1,
                System.currentTimeMillis() - totalPlantUMLMillis
                ));
    }

    String getViewName(Workspace workspace, View view) {
        String prefix;
        long workspaceId = workspace.getId();
        if (workspaceId > 0) {
            prefix = "structurizr-" + workspaceId;
        } else {
            prefix = "structurizr";
        }

        return String.format("%s-%s", prefix, view.getKey());
    }

    private Collection<Diagram> export(Workspace workspace) {
        C4PlantUMLExporter exporter = new C4PlantUMLExporter();
        exporter.setUseSequenceDiagrams(false);
        if (workspace == null) {
            throw new IllegalArgumentException("A workspace must be provided.");
        }

        Collection<Diagram> diagrams = new ArrayList<>();

        for (SystemLandscapeView view : workspace.getViews().getSystemLandscapeViews()) {
            setSystemLandscapeElementURLs(workspace, view);
            Diagram diagram = exporter.export(view);
            resetElementURLs(view);
            if (diagram != null) {
                diagrams.add(diagram);
            }
        }

        for (SystemContextView view : workspace.getViews().getSystemContextViews()) {
            setSystemContextElementURLs(workspace, view);
            Diagram diagram = exporter.export(view);
            resetElementURLs(view);
            if (diagram != null) {
                diagrams.add(diagram);
            }
        }

        for (ContainerView view : workspace.getViews().getContainerViews()) {
            setContainerElementURLs(workspace, view);
            Diagram diagram = exporter.export(view);
            resetElementURLs(view);
            if (diagram != null) {
                diagrams.add(diagram);
            }
        }

        for (ComponentView view : workspace.getViews().getComponentViews()) {
            Diagram diagram = exporter.export(view);
            if (diagram != null) {
                diagrams.add(diagram);
            }
        }

        for (DynamicView view : workspace.getViews().getDynamicViews()) {
            Diagram diagram = exporter.export(view);
            if (diagram != null) {
                diagrams.add(diagram);
            }
        }

        for (DeploymentView view : workspace.getViews().getDeploymentViews()) {
            Diagram diagram = exporter.export(view);
            if (diagram != null) {
                diagrams.add(diagram);
            }
        }

        return diagrams;
    }

    private void setSystemLandscapeElementURLs(Workspace workspace, SystemLandscapeView view) {
        Collection<SystemContextView> views = view.getViewSet().getSystemContextViews();
        for (ElementView elementView : view.getElements()) {
            Element element = elementView.getElement();
            if (element instanceof SoftwareSystem) {
                for (View alternateView : views) {
                    if (alternateView.getSoftwareSystem() == element) {
                        element.setUrl("https://view/" + getViewName(workspace, alternateView));
                    }
                }
            }
        }
    }

    private void setSystemContextElementURLs(Workspace workspace, SystemContextView view) {
        Collection<ContainerView> views = view.getViewSet().getContainerViews();
        for (ElementView elementView : view.getElements()) {
            Element element = elementView.getElement();
            if (element instanceof SoftwareSystem) {
                for (View alternateView : views) {
                    if (alternateView.getSoftwareSystem() == element) {
                        element.setUrl("https://view/" + getViewName(workspace, alternateView));
                    }
                }
            }
        }
    }

    private void setContainerElementURLs(Workspace workspace, ContainerView view) {
        Collection<ComponentView> views = view.getViewSet().getComponentViews();
        for (ElementView elementView : view.getElements()) {
            Element element = elementView.getElement();
            if (element instanceof Container) {
                for (ComponentView alternateView : views) {
                    if (alternateView.getContainer() == element) {
                        element.setUrl("https://view/" + getViewName(workspace, alternateView));
                    }
                }
            }
        }
    }

    private void resetElementURLs(View view) {
        for (ElementView elementView : view.getElements()) {
            Element element = elementView.getElement();
            element.setUrl(null);
            // Older versions of setUrl ignored null - make sure this version doesn't
            // See Issue #169: Feature Request: Allow API to set ModelItem#url = null
            // https://github.com/structurizr/java/issues/169
            // TODO: Remove check after element.setUrl(null)
            if (element.getUrl() != null) {
                throw new RuntimeException("setUrl(null) didn't work");
            }
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

    private void writeToFile(File file, String content) throws Exception {
        BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8);
        writer.write(content);
        writer.close();
    }
}
