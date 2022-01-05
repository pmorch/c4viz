package com.morch.c4viz;

import com.structurizr.dsl.StructurizrDslParserException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@RestController
public class RootController {
    private final SourceHandler sourceHandler;

    RootController(SourceHandler sourceHandler) {
        this.sourceHandler = sourceHandler;
    }
    @GetMapping("/api/c4viz")
    public VizOutput c4viz(
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String render
            ) throws IOException {
        return getVizOutput(source, render);
    }

    @GetMapping(path="/api/svg", produces = "image/svg+xml;charset=UTF-8")
    /** c4svg doesn't take a render parameter, because I figure if a website needs an .svg,
     * it makes no sense to not render it right away */
    public String c4svg(
            @RequestParam String svg,
            @RequestParam(required = false) String source
    ) throws IOException {
        VizOutput viz = getVizOutput(source, "true");
        VizResult result = (VizResult) viz;
        for (VizData data: result.viz) {
            if (data.getShortName().equals(svg)) {
                return data.getSvg();
            }
        }
        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "svg " + svg + " not found"
        );
    }

    private VizOutput getVizOutput(String source, String render) throws IOException {
        if (source == null) {
            source = System.getenv("C4VIZ_SOURCE");
        }
        if (source == null) {
            throw new IllegalArgumentException("Need a source parameter");
        }
        try {
            return sourceHandler.getResult(source, render);
        } catch (StructurizrDslParserException exc) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, exc.getMessage(), exc
            );
        }
    }
}