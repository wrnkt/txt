package org.tanchee.inngest.config.serde;

import java.io.IOException;
import java.time.Duration;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class DurationSerializer extends StdSerializer<Duration> {

    public DurationSerializer() {
        super(Duration.class);
    }

    @Override
    public void serialize(Duration value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        gen.writeString(value.getSeconds() + "s");
    }
}
