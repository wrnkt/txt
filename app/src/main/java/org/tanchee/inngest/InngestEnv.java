package org.tanchee.inngest;

public enum InngestEnv {
    Dev("dev"),
    Prod("prod"),
    Other("other");

    public String value;

    private InngestEnv(String value) {
        this.value = value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
