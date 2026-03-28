package org.tanchee.txt.components.ingest;

import java.util.concurrent.CompletionStage;

import org.tanchee.txt.core.component.Component;
import org.tanchee.txt.core.component.ComponentContext;
import org.tanchee.txt.core.component.ComponentId;

public abstract class IngestComponent implements Component {
    protected final ComponentId id;
    protected ComponentContext context;

    protected IngestComponent(String id) {
        this.id = new ComponentId(id);
    }

    @Override
    public ComponentId id() {
        return id;
    }

    @Override
    public CompletionStage<Void> start(ComponentContext context) {
        this.context = context;
        return run();
    }

    protected abstract CompletionStage<Void> run();
}
