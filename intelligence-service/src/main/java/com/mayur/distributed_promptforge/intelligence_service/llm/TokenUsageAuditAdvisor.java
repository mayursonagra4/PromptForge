package com.mayur.distributed_promptforge.intelligence_service.llm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

public class TokenUsageAuditAdvisor implements CallAdvisor, StreamAdvisor {

    private static final Logger logger = LoggerFactory.getLogger(TokenUsageAuditAdvisor.class);

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);
        ChatResponse chatResponse = chatClientResponse.chatResponse();
        if (chatResponse != null && chatResponse.getMetadata() != null) {
            Usage usage = chatResponse.getMetadata().getUsage();
            if (usage != null) {
                logger.info("Token usage for this call - Prompt Tokens: {}, Completion Tokens: {}, Total Tokens: {}",
                        usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());
            } else {
                logger.warn("No token usage information available in the response metadata.");
            }
        }
        return chatClientResponse;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain streamAdvisorChain) {
        java.util.concurrent.atomic.AtomicReference<Usage> lastUsage = new java.util.concurrent.atomic.AtomicReference<>();
        return streamAdvisorChain.nextStream(request)
                .doOnNext(response -> {
                    ChatResponse chatResponse = response.chatResponse();
                    if (chatResponse != null && chatResponse.getMetadata() != null) {
                        Usage usage = chatResponse.getMetadata().getUsage();
                        if (usage != null && usage.getTotalTokens() != null && usage.getTotalTokens() > 0) {
                            lastUsage.set(usage);
                        }
                    }
                })
                .doFinally(signalType -> {
                    Usage usage = lastUsage.get();
                    if (usage != null) {
                        logger.info("Token usage for stream - Prompt Tokens: {}, Completion Tokens: {}, Total Tokens: {}",
                                usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());
                    }
                });
    }

    @Override
    public String getName() {
        return "TokenUsageAuditAdvisor";
    }

    @Override
    public int getOrder() {
        return 50;
    }
}
