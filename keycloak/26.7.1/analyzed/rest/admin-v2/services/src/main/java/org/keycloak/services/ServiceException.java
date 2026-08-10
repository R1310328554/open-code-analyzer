package org.keycloak.services;

import java.util.Optional;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

/**
 * 服务层运行时异常，可携带建议的 HTTP 响应状态码。
 */
public class ServiceException extends RuntimeException {
    private Response.Status suggestedHttpResponseStatus;

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ServiceException(String message, Response.Status suggestedStatus) {
        this(message);
        this.suggestedHttpResponseStatus = suggestedStatus;
    }

    public ServiceException(Response.Status suggestedStatus) {
        super();
        this.suggestedHttpResponseStatus = suggestedStatus;
    }

    /** 返回建议的 HTTP 状态码（若未设置则为空）。 */
    public Optional<Response.Status> getSuggestedResponseStatus() {
        return Optional.ofNullable(suggestedHttpResponseStatus);
    }

    /** 转为 {@link WebApplicationException}，默认状态为 {@code BAD_REQUEST}。 */
    public WebApplicationException toWebApplicationException() {
        return toWebApplicationException(Response.Status.BAD_REQUEST);
    }

    /**
     * 转为 {@link WebApplicationException}。
     *
     * @param orReturnStatus 未指定建议状态时的回退 HTTP 状态
     */
    public WebApplicationException toWebApplicationException(Response.Status orReturnStatus) {
        if (getMessage() != null) {
            return new WebApplicationException(getMessage(), getSuggestedResponseStatus().orElse(orReturnStatus));
        } else {
            return new WebApplicationException(getSuggestedResponseStatus().orElse(orReturnStatus));
        }
    }

}
