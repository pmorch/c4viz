package com.morch.c4viz;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.Hashing;
import com.structurizr.dsl.StructurizrDslParserException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class SourceHandler {
    final private String cacheDir;
    static private Logger logger = LoggerFactory.getLogger(SourceHandler.class);

    SourceHandler() throws FileNotFoundException {
        String tmpCacheDir = System.getenv("C4VIZ_CACHE");
        if (tmpCacheDir == null)
            tmpCacheDir = "/tmp";
        cacheDir = tmpCacheDir;
        if ( ! Files.exists(Paths.get(cacheDir)) || ! Files.isDirectory(Paths.get(cacheDir))) {
            throw new FileNotFoundException("cache dir " + cacheDir + " doesn't exist");
        }
    }

    private String getSourceHash(String source) {
        return Hashing.sha256()
                .hashString(source, StandardCharsets.UTF_8)
                .toString();
    }

    private Path getVizDirPathFromHash(String hash) throws IOException {
        Path cacheDir = Paths.get(this.cacheDir, hash);
        if ( ! Files.exists(cacheDir)) {
            Files.createDirectory(cacheDir);
        }
        return cacheDir;
    }

    private Path getVizSourcePathFromHash(String hash) throws IOException {
        getVizDirPathFromHash(hash);
        return Paths.get(this.cacheDir, hash, "c4.viz.json");
    }

    private VizResult render(String hash, Path source) throws IOException, StructurizrDslParserException {
        Path sourcePath = getVizSourcePathFromHash(hash);
        if ( ! Files.exists(sourcePath)) {
            Path sourceDir = getVizDirPathFromHash(hash);
            OutputGenerator generator = new OutputGenerator(source.toString(), sourceDir.toString());
            generator.generate();
        }
        return getResultFromSourceFile(sourcePath);
    }

    private VizOutput existingRender(String hash) throws IOException {
        Path sourcePath = getVizSourcePathFromHash(hash);
        if ( Files.exists(sourcePath)) {
            return getResultFromSourceFile(sourcePath);
        } else {
            return new VizPending();
        }
    }

    private VizResult getResultFromSourceFile(Path source) throws IOException {
        File vizDataFile = source.toFile();
        ObjectMapper objectMapper = new ObjectMapper();
        VizData[] vizData = objectMapper.readValue(vizDataFile, VizData[].class);
        VizResult result = new VizResult();
        result.viz = vizData;
        return result;
    }

    private Path resolveSource(String hash, String source) throws IOException {
        String lowerSource = source.toLowerCase();
        Path resolved;
        //noinspection HttpUrlsUsage
        if (lowerSource.startsWith("http://") || lowerSource.startsWith("https://")) {
            CloseableHttpClient client = HttpClientBuilder.create().build();
            Path etagPath = Paths.get(getVizDirPathFromHash(hash).toString(), "etag");
            Path outputPath = Paths.get(getVizDirPathFromHash(hash).toString(), "source");

            HttpGet request = new HttpGet(source);
            if (Files.exists(etagPath)) {
                String etag = Files.readAllLines(etagPath).get(0);
                request.setHeader("If-None-Match", etag);
            }
            CloseableHttpResponse response = client.execute(request);
            if (response.getCode() != HttpStatus.SC_NOT_MODIFIED) {
                if (response.getCode() != HttpStatus.SC_OK) {
                    throw new RuntimeException("Expected OK from " + source +
                            " but got response.getCode()== " + response.getCode());
                }
                Header etag;
                try {
                    etag = response.getHeader("Etag");
                } catch (ProtocolException pe) {
                    throw new RuntimeException(pe);
                }
                if (etag != null) {
                    // Save the etag for later
                    FileOutputStream fileOutputStream = new FileOutputStream(etagPath.toFile());
                    fileOutputStream.write(etag.getValue().getBytes(StandardCharsets.UTF_8));
                    fileOutputStream.close();
                }
                byte[] responseBytes = EntityUtils.toByteArray(response.getEntity());

                // Save the responseBody
                FileOutputStream fileOutputStream = new FileOutputStream(outputPath.toFile());
                fileOutputStream.write(responseBytes);
                fileOutputStream.close();
            }
            resolved = outputPath;
        } else {
            String regex = "^[a-zA-Z0-9.\\-]+$";
            if (! source.matches(regex)) {
                throw new RuntimeException(String.format("Source '%s' doesn't match: %s", source, regex));
            }
            String sourceDir = System.getenv("C4VIZ_SOURCE_DIR");
            if (sourceDir == null) {
                throw new RuntimeException("C4VIZ_SOURCE_DIR environment must be set");
            }
            resolved = Paths.get(sourceDir, source);
        }
        if (! Files.exists(resolved)) {
            throw new FileNotFoundException("Couldn't find " + resolved);
        }
        return resolved;
    }

    public VizOutput getResult(String source, String render) throws IOException, StructurizrDslParserException {
        String hash = getSourceHash(source);
        Path sourceFile = resolveSource(hash, source);
        if (sourceFile.toString().toLowerCase().endsWith(".viz.json")) {
            return getResultFromSourceFile(sourceFile);
        } else {
            if (render != null && render.equals("true")) {
                return render(hash, sourceFile);
            } else {
                return existingRender(hash);
            }
        }
    }
}
