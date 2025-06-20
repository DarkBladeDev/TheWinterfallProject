package com.darkbladedev.utils;

public enum TemperatureState {
    SEVERE_HYPOTHERMIA("Hipotermia severa", "<color:#6b70f3>", Integer.MIN_VALUE, -6),
    HYPOTHERMIA("Hipotermia", "<blue>", -5, 5),
    NORMAL("Normal", "<green>", 16, 25),
    HYPERTHERMIA("Hipertermia", "<red>", 30, 34),
    SEVERE_HYPERTHERMIA("Hipertermia severa", "<dark_red>", 35, Integer.MAX_VALUE),
    FALLBACK("Normal (fallback)", "<green>", 0, 0); // Default/fallback case

    private final String name;
    private final String color;
    private final int minTemp;
    private final int maxTemp;

    TemperatureState(String name, String color, int minTemp, int maxTemp) {
        this.name = name;
        this.color = color;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
    }

    public String getFormatted() {
        return color + name + "</" + color.replaceAll("[<>]", "") + ">";
    }

    public boolean inRange(int temp) {
        return temp >= minTemp && temp <= maxTemp;
    }

    public static TemperatureState fromTemperature(int temp) {
        for (TemperatureState state : values()) {
            if (state.inRange(temp)) {
                return state;
            }
        }
        return FALLBACK;
    }
}

