package com.morch.c4viz;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.structurizr.Workspace;
import com.structurizr.dsl.StructurizrDslParser;
import com.structurizr.dsl.StructurizrDslParserException;
import com.structurizr.io.Diagram;
import com.structurizr.io.plantuml.C4PlantUMLExporter;
import com.structurizr.model.*;
import com.structurizr.util.StringUtils;
import com.structurizr.view.*;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class OutputGenerator {
    String workspacePath;
    String outputPath;

    static private Logger logger = LoggerFactory.getLogger(OutputGenerator.class);

    OutputGenerator(String workspacePath, String outputPath) {
        this.workspacePath = workspacePath;
        this.outputPath = outputPath;
    }

    void generate() throws StructurizrDslParserException {
        try {
            generateUnsafely();
        } catch (StructurizrDslParserException e) {
            throw e;
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

        long outputStart = System.currentTimeMillis();
        long totalPlantUMLMillis = 0;
        List<VizData> vizData = new ArrayList<>();
        for (Diagram diagram : diagrams) {
            View view = diagram.getView();
            logger.debug("Writing {}/{}.*", outputPath, getViewName(workspace, diagram.getView()));

            String displayTitle = view.getTitle();
            if (StringUtils.isNullOrEmpty(displayTitle)) {
                displayTitle = view.getName();
            }

            File file = new File(outputPath, String.format("%s.puml", getViewName(workspace, diagram.getView())));
            writeToFile(file, diagram.getDefinition());

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            long plantUmlStart = System.currentTimeMillis();
            SourceStringReader reader = new SourceStringReader(diagram.getDefinition());
            // DiagramDescription desc =
            reader.outputImage(byteArrayOutputStream, new FileFormatOption(FileFormat.SVG));
            totalPlantUMLMillis += System.currentTimeMillis() - plantUmlStart;

            String svg = byteArrayOutputStream.toString(StandardCharsets.UTF_8);

            // Remove comments from svg. That reduces size of .svg files and hence the c4viz.json file by 60%...
            // https://stackoverflow.com/a/6806096/345716
            svg = svg.replaceAll( "(?s)<!--.*?-->", "" );

            FileOutputStream fileOutputStream = new FileOutputStream(String.format("%s/%s.svg", outputPath, getViewName(workspace, diagram.getView())));
            fileOutputStream.write(svg.getBytes(StandardCharsets.UTF_8));
            fileOutputStream.close();

            String type = diagram.getView().getClass().getSimpleName();
            String shortName = getViewName(workspace, diagram.getView());

            vizData.add(new VizData(type, shortName, displayTitle, svg, diagram.getDefinition()));
        }
        String c4vizFileName = String.format("%s/c4.viz.json", outputPath);
        logger.debug("Writing " + c4vizFileName);
        ObjectMapper objectMapper = new ObjectMapper();
        String vizString = objectMapper.writeValueAsString(vizData);
        FileOutputStream fileOutputStream = new FileOutputStream(c4vizFileName);
        fileOutputStream.write(vizString.getBytes(StandardCharsets.UTF_8));
        fileOutputStream.close();
        logger.info(
                "Done - Writing output files to {} took {} ms of which {} ms for PlantUML",
                outputPath,
                System.currentTimeMillis() - outputStart,
                totalPlantUMLMillis
                );
    }

    String getViewName(Workspace workspace, View view) {
        long workspaceId = workspace.getId();
        if (workspaceId > 0) {
            return String.format("%d-%s", workspaceId, view.getKey());
        } else {
            return view.getKey();
        }
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
            // Older versions of setUrl ignore element.setUrl(null) - make sure this version doesn't
            // See Issue #169: Feature Request: Allow API to set ModelItem#url = null
            // https://github.com/structurizr/java/issues/169
            if (element.getUrl() != null) {
                throw new RuntimeException("Couldn't element.setUrl(null)");
            }
        }
    }

    private void addDefaultViewsAndStyles(Workspace workspace) {
        if (workspace.getViews().isEmpty()) {
            logger.debug("no views defined; creating default views");
            workspace.getViews().createDefaultViews();
        }

        Styles styles = workspace.getViews().getConfiguration().getStyles();
        if (styles.getElements().isEmpty() && styles.getRelationships().isEmpty() && workspace.getViews().getConfiguration().getThemes() == null) {
            logger.debug("no styles or themes defined; use the \"default\" theme to add some default styles");
        }
    }

    private void writeToFile(File file, String content) throws Exception {
        BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8);
        writer.write(content);
        writer.close();
    }
}
