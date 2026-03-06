package com.rpamis.healthcheck.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

/**
 * 启动状态监听
 *
 * @author benym
 * @since 2026/3/6 17:31
 */
public class StatusListener implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(StatusListener.class);

    private final StatusChecker statusChecker;

    public StatusListener(StatusChecker statusChecker) {
        this.statusChecker = statusChecker;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        statusChecker.setStarted();
        logger.info("application start, health checker set started");
    }
}
