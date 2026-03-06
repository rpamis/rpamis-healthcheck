package com.rpamis.healthcheck.core.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;

/**
 * 健康检查Interceptor
 *
 * @author benym
 * @since 2026/3/6 17:33
 */
public class HealthInterceptor implements MethodInterceptor {

    private final HealthAspect healthAspect;

    public HealthInterceptor(HealthAspect healthAspect) {
        this.healthAspect = healthAspect;
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        if (!(methodInvocation instanceof ProxyMethodInvocation)) {
            throw new IllegalStateException(
                    "MethodInvocation is not a Spring ProxyMethodInvocation");
        }
        ProxyMethodInvocation pmi = (ProxyMethodInvocation) methodInvocation;
        ProceedingJoinPoint pjp = lazyInitJoinPoint(pmi);
        return doBefore(pjp);
    }

    private ProceedingJoinPoint lazyInitJoinPoint(ProxyMethodInvocation pmi) {
        return new MethodInvocationProceedingJoinPoint(pmi);
    }

    private Object doBefore(ProceedingJoinPoint pjp) throws Throwable {
        return healthAspect.before(pjp);
    }
}
