package com.mcmoddev.mmdbot.client.util;

import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

public enum Fonts {
    ACME_FONT("AcmeFont"),
    AGENCY_FB("Agency FB"),
    ALFREDO("Alfredo"),
    ALGERIAN("Algerian"),
    ARIAL("Arial"),
    CALVIN("Calvin"),
    GIG("Gigi"),
    HARVEST("Harvest"),
    MONOSPACED("Monospaced");

    private final String name;

    Fonts(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Font make() {
        return Font.font(name);
    }

    public Font make(double size) {
        return Font.font(name, size);
    }

    public Font make(FontWeight weight, double size) {
        return Font.font(name, weight, size);
    }

    public Font make(FontWeight weight, FontPosture posture, double size) {
        return Font.font(name, weight, posture, size);
    }

}
