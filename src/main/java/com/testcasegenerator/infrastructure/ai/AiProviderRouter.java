package com.testcasegenerator.infrastructure.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class AiProviderRouter {

    private final Map<String, AiProvider> providers;

    @Value("${app.ai.provider:dify}")
    private String defaultProvider;

    public AiProviderRouter(Map<String, AiProvider> providers) {
        this.providers = providers;
    }

    public AiProvider resolve(String preferred) {
        if (preferred != null && providers.containsKey(preferred)) {
            return providers.get(preferred);
        }
        AiProvider fallback = providers.get(defaultProvider);
        if (fallback == null) {
            fallback = providers.values().iterator().next();
        }
        log.debug("Using AI provider: {}", fallback.getClass().getSimpleName());
        return fallback;
    }
}
