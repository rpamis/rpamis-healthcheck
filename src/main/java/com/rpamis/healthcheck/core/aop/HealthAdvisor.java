package com.rpamis.healthcheck.core.aop;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.util.StringUtils;

/**
 * 健康检查Advisor
 *
 * @author benym
 * @since 2026/3/6 17:33
 */
public class HealthAdvisor extends AbstractPointcutAdvisor implements BeanFactoryAware {

    private static final String DEFAULT_POINTCUT = "@within(org.springframework.stereotype.Controller) || " +
            "@within(org.springframework.web.bind.annotation.RestController)";

    @SuppressWarnings("all")
    private final Advice advice;

    @SuppressWarnings("all")
    private final Pointcut pointcut;

    private final String customPointcut;

    public HealthAdvisor(Advice advice, String customPointcut) {
        this.advice = advice;
        this.customPointcut = customPointcut;
        this.pointcut = buildPointCut();
    }

    private Pointcut buildPointCut(){
        AspectJExpressionPointcut ajpc = new AspectJExpressionPointcut();
        ajpc.setExpression(buildCutExpression(customPointcut));
        return ajpc;
    }

    private String buildCutExpression(String customPointcut) {
        StringBuilder sbf = new StringBuilder(DEFAULT_POINTCUT);
        if (StringUtils.hasLength(customPointcut)) {
            sbf.append(" || ").append(customPointcut);
        }
        return sbf.toString();
    }

    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }

    @Override
    public Advice getAdvice() {
        return advice;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (advice instanceof BeanFactoryAware) {
            ((BeanFactoryAware) advice).setBeanFactory(beanFactory);
        }
    }
}
