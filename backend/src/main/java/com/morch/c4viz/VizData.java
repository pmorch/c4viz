package com.morch.c4viz;

public class VizData {
    private String type;
    private String name;
    private String svg;

    // The PlantUML source
    private String puml;

    public VizData() {
    }

    public VizData(String type, String name, String svg, String puml) {
        this.type = type;
        this.name = name;
        this.svg = svg;
        this.puml = puml;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSvg() {
        return svg;
    }

    public void setSvg(String svg) {
        this.svg = svg;
    }

    public String getPuml() {
        return puml;
    }

    public void setPuml(String puml) {
        this.puml = puml;
    }
}
