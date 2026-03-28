package org.tanchee.dam.source;

import java.util.List;

import org.tanchee.dam.data.Datum;

public interface Tagger {
    List<String> produceTags(Datum datum);
}
