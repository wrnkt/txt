package org.tanchee.inngest;

import java.util.Map;

public class Event {
    private String id;
    private String name;
    private Map<String, Object> data;
    private Map<String, Object> user;
    private long ts;
    private Object v;

    public Event(String id, String name, Map<String, Object> data, Map<String, Object> user, long ts, Object v) {
        this.id = id;
        this.name = name;
        this.data = data;
        this.user = user;
        this.ts = ts;
        this.v = v;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Map<String, Object> getUser() {
        return user;
    }

    public long getTs() {
        return ts;
    }

    public Object getV() {
        return v;
    }

}
