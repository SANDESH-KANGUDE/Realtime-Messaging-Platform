package com.company.chatplatform.aiservice.provider;

import java.util.List;
import java.util.function.Consumer;

public interface AiProvider {
    void streamChat(
            List<ChatMessage> history,
            String systemInstruction,
            Consumer<String> chunkConsumer,
            Consumer<Throwable> errorConsumer,
            Runnable onComplete
    );
}
