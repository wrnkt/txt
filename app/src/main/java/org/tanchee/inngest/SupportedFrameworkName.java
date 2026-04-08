package org.tanchee.inngest;

public enum SupportedFrameworkName {
    SpringBoot("springboot"),
    JAVALIN("javalin");

    private final String value;

    private SupportedFrameworkName(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
    
}
