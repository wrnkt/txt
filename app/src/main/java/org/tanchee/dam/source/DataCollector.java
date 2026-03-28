package org.tanchee.dam.source;

import java.util.List;

import org.tanchee.dam.data.Datum;

public interface DataCollector {
    public Source getSource();
    public List<Datum> fetch();
}
