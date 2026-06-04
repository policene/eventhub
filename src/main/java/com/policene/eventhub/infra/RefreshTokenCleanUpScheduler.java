package com.policene.eventhub.infra;

import com.policene.eventhub.service.RefreshTokenService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenCleanUpScheduler {

    private final RefreshTokenService refreshTokenService;

    public RefreshTokenCleanUpScheduler(RefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
    }

    @Scheduled(cron = "${refresh.token.cleanup.cron}")
    public void cleanUp() {
        refreshTokenService.cleanInactiveTokens();
    }

}
