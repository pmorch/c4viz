package com.morch.c4viz;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

@RestController
public class RootController {
    private static class Foobar {
        public String foo="FOO";
        public int bar = 234;
    }

    @GetMapping("/api")
    public Foobar index() {
        return new Foobar();
    }

    @GetMapping("/api/c4viz")
    public VizData[] c4viz(@RequestParam(required = false) String source) throws IOException {
        if (source == null) {
            source = System.getenv("C4VIZ_SOURCE");
        }
        if (source == null) {
            throw new IllegalArgumentException("Need a source parameter");
        }
        File vizDataFile = new File(source);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(vizDataFile, VizData[].class);
    }
}
