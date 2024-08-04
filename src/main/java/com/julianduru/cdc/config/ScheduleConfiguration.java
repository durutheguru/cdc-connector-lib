package com.julianduru.cdc.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 *
 */
@EnableScheduling
@ConditionalOnProperty(value = "code.config.connector.processor-config.sync", havingValue = "REDIS", matchIfMissing = false)
public class ScheduleConfiguration {



    @Scheduled(fixedRate = 60000)
    public void refreshToken() {
//        for (var manager : authenticationManagers) {
//            manager.fetchToken();
//        }
    }



}
