package com.rpamis.healthcheck.autoconfigure;

import com.rpamis.healthcheck.core.StatusChecker;
import com.rpamis.healthcheck.core.StatusListener;
import com.rpamis.healthcheck.core.aop.HealthAdvisor;
import com.rpamis.healthcheck.core.aop.HealthAspect;
import com.rpamis.healthcheck.core.aop.HealthInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * 健康检查组件自动配置类
 *
 * @author benym
 * @since 2026/3/6 17:33
 */
@Configuration
@EnableConfigurationProperties(value = HealthProperties.class)
@ConditionalOnProperty(prefix = HealthProperties.PREFIX, value = "enable", havingValue = "true")
public class HealthAutoConfiguration {

    /**
     * 健康状态检查者
     *
     * @return StatusChecker
     */
    @Bean
    @Order(1)
    @ConditionalOnExpression("${framework.healthcheck.enable:true}")
    StatusChecker statusChecker() {
        return new StatusChecker();
    }


    /**
     * Springboot启动事件监听者
     *
     * @param statusChecker statusChecker
     * @return StatusListener
     */
    @Bean
    @ConditionalOnExpression("${rpamis.healthcheck.enable:true}")
    StatusListener statusListener(StatusChecker statusChecker) {
        return new StatusListener(statusChecker);
    }

    /**
     * 健康检查切面
     *
     * @param statusChecker statusChecker
     * @param healthProperties healthProperties
     * @return HealthAspect
     */
    @Bean
    @ConditionalOnExpression("${rpamis.healthcheck.enable:true}")
    HealthAspect healthAspect(StatusChecker statusChecker, HealthProperties healthProperties) {
        return new HealthAspect(statusChecker, healthProperties);
    }

    /**
     * 健康检查Interceptor
     *
     * @param healthAspect healthAspect
     * @return HealthInterceptor
     */
    @Bean
    @ConditionalOnExpression("${rpamis.healthcheck.enable:true}")
    HealthInterceptor healthInterceptor(HealthAspect healthAspect) {
        return new HealthInterceptor(healthAspect);
    }

    /**
     * 健康检查Advisor
     *
     * @param healthInterceptor healthInterceptor
     * @param healthProperties healthProperties
     * @return HealthAdvisor
     */
    @Bean
    @ConditionalOnExpression("${rpamis.healthcheck.enable:true}")
    HealthAdvisor healthAdvisor(HealthInterceptor healthInterceptor, HealthProperties healthProperties) {
        return new HealthAdvisor(healthInterceptor, healthProperties.getCustomPointcut());
    }
}
