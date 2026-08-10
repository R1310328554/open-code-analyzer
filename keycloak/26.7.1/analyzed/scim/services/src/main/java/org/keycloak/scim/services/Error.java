package org.keycloak.scim.services;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Properties;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.ModelValidationException;
import org.keycloak.scim.filter.ScimFilterException;
import org.keycloak.scim.protocol.ForbiddenException;
import org.keycloak.scim.protocol.response.ErrorResponse;
import org.keycloak.theme.Theme;

import org.jboss.logging.Logger;

/**
 * SCIM 服务层异常到 HTTP 响应的转换工具。
 * <p>将模型校验、过滤、权限及未知错误映射为符合 SCIM 规范的错误 JSON。</p>
 */
class Error {

    /** 日志记录器。 */
    private static final Logger logger = Logger.getLogger(Error.class);

    /**
     * 将任意异常转换为 SCIM 错误响应。
     * <p>识别校验失败、重复资源、过滤语法、权限拒绝等已知类型。</p>
     */
    static Response toResponse(KeycloakSession session, Exception e) {
        if (e instanceof ModelValidationException mve) {
            String language = session.getContext().getRequestHeaders().getHeaderString(HttpHeaders.ACCEPT_LANGUAGE);
            Properties messages = getMessageBundle(session, language);
            String format = messages.getProperty(mve.getMessage(), mve.getMessage())
                    .replace("{{", "{").replace("}}", "}")
                    .replace("'", "");
            String message = MessageFormat.format(format, mve.getParameters());
            session.getTransactionManager().setRollbackOnly();
            return invalidSyntax(message);
        } else if (e instanceof ModelDuplicateException) {
            return errorResponse(Status.CONFLICT, "uniqueness", "A resource with the same unique attribute already exists");
        } else if (e instanceof ScimFilterException) {
            return badRequest("invalidFilter", e.getMessage());
        } else if (e instanceof ForbiddenException) {
            logger.debug("SCIM request denied: caller does not have the required permissions");
            return forbidden();
        } else if (e instanceof jakarta.ws.rs.ForbiddenException fe) {
            throw fe;
        }

        logger.error("Unexpected error processing SCIM request", e);
        return errorResponse(Status.INTERNAL_SERVER_ERROR, "An unexpected error occurred when processing the request");
    }

    /** 返回 404，表示指定 id 的资源不存在。 */
    static Response resourceNotFound(String id) {
        return errorResponse(Status.NOT_FOUND, "Resource not found with id " + id);
    }

    /** 构造 400 错误，可附带 SCIM 错误类型与详情。 */
    static Response badRequest(String type, String detail) {
        return errorResponse(Status.BAD_REQUEST, type, detail);
    }

    /** 构造无类型的 400 错误。 */
    static Response badRequest(String detail) {
        return badRequest(null, detail);
    }

    /** 返回 invalidSyntax 类型的 400 错误。 */
    static Response invalidSyntax(String detail) {
        return badRequest("invalidSyntax", detail);
    }

    /** 返回 403 禁止访问响应。 */
    static Response forbidden() {
        return errorResponse(Status.FORBIDDEN, null);
    }

    /** 组装 {@link ErrorResponse} 实体并设置 HTTP 状态与 SCIM 类型。 */
    private static Response errorResponse(Status status, String type, String detail) {
        ErrorResponse error = new ErrorResponse(detail, status.getStatusCode());
        error.setScimType(type);
        return Response.status(error.getStatusInt()).type(MediaType.APPLICATION_JSON).entity(error).build();
    }

    /** 无 SCIM 类型的通用错误响应。 */
    private static Response errorResponse(Status status, String detail) {
        return errorResponse(status, null, detail);
    }

    /** 按 Accept-Language 加载管理主题消息包，失败时返回空 Properties。 */
    private static Properties getMessageBundle(KeycloakSession session, String lang) {
        try {
            Theme theme = session.theme().getTheme(Theme.Type.ADMIN);
            Locale locale = lang != null ? Locale.forLanguageTag(lang) : Locale.ENGLISH;
            return theme.getMessages(locale);
        } catch (IOException e) {
            return new Properties();
        }
    }
}
