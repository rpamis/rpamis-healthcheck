package com.rpamis.healthcheck.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * rpamis健康检查组件Properties
 *
 * @author benym
 * @since 2026/3/6 17:33
 */
@ConfigurationProperties(HealthProperties.PREFIX)
public class HealthProperties {

    /**
     * 前缀
     */
    public static final String PREFIX = "rpamis.healthcheck";

    /**
     * 是否启用健康检查组件
     */
    private Boolean enable;

    /**
     * 强高可用
     */
    private Boolean highAvailability = Boolean.TRUE;

    /**
     * 提示响应码
     */
    private String code;

    /**
     * 提示消息
     */
    private String message;

    /**
     * 自定义切点
     */
    private String customPointcut = "";

    /**
     * k8s探活请求路径
     */
    private String livenessPath;

    /**
     * 白名单请求路径
     */
    private List<String> whiteListPaths = new ArrayList<>();

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public Boolean getHighAvailability() {
        return highAvailability;
    }

    public void setHighAvailability(Boolean highAvailability) {
        this.highAvailability = highAvailability;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCustomPointcut() {
        return customPointcut;
    }

    public void setCustomPointcut(String customPointcut) {
        this.customPointcut = customPointcut;
    }

    public String getLivenessPath() {
        return livenessPath;
    }

    public void setLivenessPath(String livenessPath) {
        // 去除首尾双引号,兼容yml和properties格式
        this.livenessPath = livenessPath.trim().replaceAll("^\"|\"$", "");
    }

    public List<String> getWhiteListPaths() {
        return whiteListPaths;
    }

    public void setWhiteListPaths(List<String> whiteListPaths) {
        // 去除首尾双引号,兼容yml和properties格式
        List<String> newWhiteListPaths = new ArrayList<>();
        if (CollectionUtils.isEmpty(whiteListPaths)) {
            return;
        }
        for (String whiteListPath : whiteListPaths) {
            String newWhiteListPath = whiteListPath.trim().replaceAll("^\"|\"$", "");
            newWhiteListPaths.add(newWhiteListPath);
        }
        this.whiteListPaths = newWhiteListPaths;
    }
}
