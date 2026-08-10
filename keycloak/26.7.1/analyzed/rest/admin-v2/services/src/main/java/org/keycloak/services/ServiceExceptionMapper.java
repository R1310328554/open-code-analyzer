package org.keycloak.services;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.error.KeycloakErrorHandler;

/**
 * 将 {@link ServiceException} 映射为 {@link jakarta.ws.rs.WebApplicationException} 的异常转换器。
 * <p>
 * 用于把服务层异常转换为 JAX-RS 响应。
 */
@Provider
public class ServiceExceptionMapper implements ExceptionMapper<ServiceException> {

    @Context
    KeycloakSession session;

    @Override
    public Response toResponse(ServiceException exception) {
        return KeycloakErrorHandler.getResponse(session, exception.toWebApplicationException());
    }
}
