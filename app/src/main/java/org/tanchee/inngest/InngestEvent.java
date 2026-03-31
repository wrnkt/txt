package org.tanchee.inngest;

public final class InngestEvent {
    private final String name;
    private final Object data;

    public InngestEvent(String name, Object data) {
        this.name = name;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public Object getData() {
        return data;
    }
}
