package com.morch.c4viz;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;

/*
 * This is for https://forum.plantuml.net/15083/why-does-this-4-line-puml-file-take-more-than-minute-to-render
 */

public class PlantMain {

    public static void main(String[] args) throws IOException {
        String source = "@startuml\n";
        source += "!includeurl https://gist.githubusercontent.com/pmorch/acc087c40df50c1877d328c0f0afcdfe/raw/3d8109a3725110ee4762506034404c8ad5029aae/emptyfile.puml\n";
        source += "Bob -> Alice : hello\n";
        source += "@enduml\n";

        SourceStringReader reader = new SourceStringReader(source);
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        // Write the first image to "os"
        String desc = reader.generateImage(os, new FileFormatOption(FileFormat.SVG));
        os.close();

        // The XML is stored into svg
        final String svg = new String(os.toByteArray(), Charset.forName("UTF-8"));
        System.out.println("Done " + new Date());
        // System.exit(0);
    }
}
