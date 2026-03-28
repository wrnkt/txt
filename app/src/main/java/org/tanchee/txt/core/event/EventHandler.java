package org.tanchee.txt.core.event;

import java.util.concurrent.CompletionStage;

public interface EventHandler<E extends Event> {
    CompletionStage<Void> handle(E event);
}
