package com.rpamis.healthcheck.core;

/**
 * 状态检查者
 *
 * @author benym
 * @since 2026/3/6 17:25
 */
public class StatusChecker {

    private volatile boolean started = false;

    public void setStarted() {
        started = true;
    }

    public boolean isStarted() {
        return started;
    }
}
