package com.morch.c4viz;

import com.structurizr.Workspace;
import com.structurizr.dsl.StructurizrDslParser;
import com.structurizr.io.Diagram;
import com.structurizr.io.plantuml.C4PlantUMLExporter;
import com.structurizr.model.*;
import com.structurizr.view.*;

import java.io.BufferedWriter;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;

public class PumlGenerator {
    String workspacePath;
    String outputPath;

    // TODO: ask for an upstream setUrl(null) method.
    static private Field urlFieldAccessor = null;

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

        for (Diagram diagram : diagrams) {
            File file = new File(outputPath, String.format("%s.puml", getViewName(workspace, diagram.getView())));
            writeToFile(file, diagram.getDefinition());
        }
    }

    String getViewName(Workspace workspace, View view) {
        String prefix;
        long workspaceId = workspace.getId();
        if (workspaceId > 0) {
            prefix = "structurizr-" + workspaceId;
        } else {
            prefix = "structurizr";
        }

        return String.format("%s-%s.puml", prefix, view.getKey());
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
            // TODO: ask for an upstream setUrl(null) method.
            // Cannot set url to null with setUrl, so using reflection
            // element.setUrl(null);
            if (urlFieldAccessor == null) {
                try {
                    Class clazz = element.getClass();
                    while (clazz != null && ! clazz.getSimpleName().equals("ModelItem")) {
                        clazz = clazz.getSuperclass();
                    }
                    assert clazz != null;
                    urlFieldAccessor = clazz.getDeclaredField("url");
                    urlFieldAccessor.setAccessible(true);
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                urlFieldAccessor.set(element, null);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
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
        System.out.println(" - writing " + file.getCanonicalPath());
        BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8);
        writer.write(content);
        writer.close();
    }
}
