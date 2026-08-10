package org.keycloak.quarkus.runtime.logging;

import java.io.IOException;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import org.keycloak.logging.MappedDiagnosticContextUtil;

/**
 * 响应阶段清除 MDC 中自定义属性的过滤器，避免线程复用时日志上下文泄漏。
 *
 * @author <a href="mailto:b.eicki@gmx.net">Björn Eickvoder</a>
 */
@Provider
@Priority(0)
public class ClearMappedDiagnosticContextFilter implements ContainerResponseFilter {

    /** 在响应写出前清空 Keycloak 写入的 MDC 条目。 */
    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        MappedDiagnosticContextUtil.clearMdc();
    }
}
