package com.mayur.distributed_promptforge.intelligence_service.service;

public interface UsageService {
    void recordTokenUsage(Long userId, int actualTokens);
    int getTodayTokenUsage(Long userId);
    void ensureWithinPlanBeforeCall(Long userId, String promptText);
}
