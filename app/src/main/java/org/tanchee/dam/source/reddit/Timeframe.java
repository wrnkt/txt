package org.tanchee.dam.source.reddit;

public enum Timeframe {
    HOUR("hour"),
    DAY("day"),
    WEEK("week"),
    MONTH("month"),
    YEAR("year"),
    ALL("all");

    private final String optionName;

    Timeframe(String optionName) {
        this.optionName = optionName;
    }

    public String getOptionName() { return optionName; }

}
