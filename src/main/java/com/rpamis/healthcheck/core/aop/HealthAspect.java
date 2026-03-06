package com.rpamis.healthcheck.core.aop;

import com.rpamis.common.dto.response.Response;
import com.rpamis.healthcheck.autoconfigure.HealthProperties;
import com.rpamis.healthcheck.core.StatusChecker;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;

/**
 * 健康检查切面
 *
 * @author benym
 * @since 2026/3/6 17:33
 */
public class HealthAspect implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthAspect.class);

    private final StatusChecker statusChecker;

    private final HealthProperties healthProperties;

    public HealthAspect(StatusChecker statusChecker, HealthProperties healthProperties) {
        this.statusChecker = statusChecker;
        this.healthProperties = healthProperties;
    }

    public Object before(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        // 如果服务已经成功启动
        if (statusChecker.isStarted()) {
            return proceedingJoinPoint.proceed(proceedingJoinPoint.getArgs());
        }
        String defaultCode = "1";
        String defaultMessage = "服务启动中,请稍候再试";
        if (StringUtils.hasLength(healthProperties.getCode())) {
            defaultCode = healthProperties.getCode();
        }
        if (StringUtils.hasLength(healthProperties.getMessage())) {
            defaultMessage = healthProperties.getMessage();
        }
        List<String> whiteListPaths = healthProperties.getWhiteListPaths();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        // 如果请求不是来源于Web请求，比如来源于Job或者Kafka等，直接按照返回值处理
        // 在升级到SpringBoot3.4.0之后，这段代码依旧要保留，虽然IDEA提示always false但实际上Job切点进来后获取不到RequestAttributes依旧是null
        if (requestAttributes == null) {
            return this.processByReturnType(proceedingJoinPoint, defaultCode, defaultMessage);
        }
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest();
        String requestUrl = request.getRequestURL().toString();
        // 非强高可用模式下，探活接口放行
        if (requestUrl.contains(healthProperties.getLivenessPath()) && Boolean.TRUE.equals(!healthProperties.getHighAvailability())) {
            return proceedingJoinPoint.proceed(proceedingJoinPoint.getArgs());
        }
        // 在强高可用模式下开启
        // 如果请求路径为k8s探活请求路径，则强制探活失败，返回503，为了让pod在完全启动后才进行新pod替换
        // 现有k8s替换规则原理为k8s原生的livenessProbe原理，探活返回状态码2xx、3xx时，会替换老pod
        // 如果探活返回状态码4xx、5xx，则不会替换
        if (Boolean.TRUE.equals(requestUrl.contains(healthProperties.getLivenessPath()) && healthProperties.getHighAvailability()) && !statusChecker.isStarted()) {
            HttpServletResponse response = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                    .getResponse();
            response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
            return "wait application start";
        }
        if (!CollectionUtils.isEmpty(whiteListPaths) && this.isPathInWhiteList(requestUrl, whiteListPaths)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.info("request url: {} is in white list, skip health check", requestUrl);
            }
            return proceedingJoinPoint.proceed(proceedingJoinPoint.getArgs());
        }
        return this.processByReturnType(proceedingJoinPoint, defaultCode, defaultMessage);
    }

    /**
     * 按照返回值类型处理
     *
     * @param proceedingJoinPoint proceedingJoinPoint
     * @param defaultCode         默认code
     * @param defaultMessage      默认message
     * @return Object
     * @throws Throwable Throwable
     */
    private Object processByReturnType(ProceedingJoinPoint proceedingJoinPoint, String defaultCode, String defaultMessage) throws Throwable {
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        Class<?> returnType = signature.getReturnType();
        // 仅处理Response和String返回类型，否则原路返回
        if (returnType.isAssignableFrom(Response.class)) {
            return Response.fail(defaultCode, defaultMessage);
        } else if (returnType.isAssignableFrom(String.class)) {
            return defaultMessage;
        } else {
            return proceedingJoinPoint.proceed(proceedingJoinPoint.getArgs());
        }
    }

    /**
     * 是否在白名单请求路径列表中
     *
     * @param requestPath    requestPath
     * @param whiteListPaths whiteListPaths
     * @return boolean
     */
    private boolean isPathInWhiteList(String requestPath, List<String> whiteListPaths) {
        for (String whiteListPath : whiteListPaths) {
            if (requestPath.contains(whiteListPath)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!StringUtils.hasLength(healthProperties.getLivenessPath())) {
            throw new IllegalArgumentException("rpamis healthcheck liveness path is null, please set");
        }
        LOGGER.info("rpamis healthcheck properties, enable:{}, highAvailability:{}, code:{}, message:{}, customPointcut:{}," +
                        "livenessPath:{}, whiteListPaths:{}", healthProperties.getEnable(), healthProperties.getHighAvailability(),
                healthProperties.getCode(), healthProperties.getMessage(), healthProperties.getCustomPointcut(),
                healthProperties.getLivenessPath(), healthProperties.getWhiteListPaths());
    }
}
