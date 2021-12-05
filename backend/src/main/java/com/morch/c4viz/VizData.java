package com.morch.c4viz;

public class VizData {
    private String type;
    private String shortName;
    private String displayTitle;
    private String svg;

    // The PlantUML source
    private String puml;

    public VizData() {
    }

    public VizData(String type, String shortName, String displayTitle, String svg, String puml) {
        this.type = type;
        this.shortName = shortName;
        this.displayTitle = displayTitle;
        this.svg = svg;
        this.puml = puml;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getDisplayTitle() {
        return displayTitle;
    }

    public void setDisplayTitle(String displayTitle) {
        this.displayTitle = displayTitle;
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
