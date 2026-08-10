package org.keycloak.services.error;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.keycloak.Config;
import org.keycloak.OAuthErrorException;
import org.keycloak.forms.login.MessageType;
import org.keycloak.forms.login.freemarker.model.UrlBean;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakTransaction;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.ModelException;
import org.keycloak.models.ModelIllegalStateException;
import org.keycloak.models.ModelValidationException;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.idm.OAuth2ErrorRepresentation;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.messages.Messages;
import org.keycloak.theme.Theme;
import org.keycloak.theme.ThemeResources;
import org.keycloak.theme.ThemeResourcesParser;
import org.keycloak.theme.beans.AdvancedMessageFormatterMethod;
import org.keycloak.theme.beans.LocaleBean;
import org.keycloak.theme.beans.MessageBean;
import org.keycloak.theme.beans.MessageFormatterMethod;
import org.keycloak.theme.freemarker.FreeMarkerProvider;
import org.keycloak.utils.MediaType;
import org.keycloak.utils.MediaTypeMatcher;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.jboss.logging.Logger;

/**
 * Keycloak 全局异常处理器。
 * <p>将未捕获异常映射为 JSON {@link OAuth2ErrorRepresentation} 或 HTML 错误页（FreeMarker），并根据异常类型设置 HTTP 状态码。</p>
 */
@Provider
public class KeycloakErrorHandler implements ExceptionMapper<Throwable> {

    /** 日志记录器 */
    private static final Logger logger = Logger.getLogger(KeycloakErrorHandler.class);

    /** 从请求路径提取 realm 名称的正则 */
    private static final Pattern realmNamePattern = Pattern.compile(".*/realms/([^/]+).*");

    /** 未捕获服务端错误的日志文本 */
    public static final String UNCAUGHT_SERVER_ERROR_TEXT = "Uncaught server error";
    /** 非 5xx 错误响应的调试日志模板 */
    public static final String ERROR_RESPONSE_TEXT = "Error response {0}";

    /** 注入的 Keycloak 会话 */
    @Context
    KeycloakSession session;

    /** {@inheritDoc} 将异常转换为 HTTP 响应 */
    @Override
    public Response toResponse(Throwable throwable) {
        return getResponse(session, throwable);
    }

    /** 构建异常对应的 HTTP 响应（JSON 或 HTML）。
     * @param session Keycloak 会话
     * @param throwable 待处理的异常
     * @return JAX-RS 响应
     */
    public static Response getResponse(KeycloakSession session, Throwable throwable) {
        KeycloakTransaction tx = session.getTransactionManager();
        tx.setRollbackOnly();

        Response.Status responseStatus = getResponseStatus(throwable);
        boolean isServerError = responseStatus.getFamily().equals(Response.Status.Family.SERVER_ERROR);

        if (isServerError) {
            logger.error(UNCAUGHT_SERVER_ERROR_TEXT, throwable);
        } else {
            logger.debugv(throwable, ERROR_RESPONSE_TEXT, responseStatus);
        }

        HttpHeaders headers = session.getContext().getRequestHeaders();

        if (!MediaTypeMatcher.isHtmlRequest(headers)) {
            OAuth2ErrorRepresentation error = new OAuth2ErrorRepresentation();

            error.setError(getErrorCode(throwable));
            if (throwable.getCause() instanceof ModelException) {
                error.setErrorDescription(throwable.getMessage());
            } if (throwable instanceof ModelDuplicateException) {
                error.setErrorDescription(throwable.getMessage());
            } else if (throwable instanceof JsonProcessingException || throwable.getCause() instanceof JsonProcessingException) {
                error.setErrorDescription("Cannot parse the JSON");
            } else if (isServerError) {
                error.setErrorDescription("For more on this error consult the server log.");
            }

            return Response.status(responseStatus)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(error)
                    .build();
        }

        try {
            RealmModel realm = resolveRealm(session);

            Theme theme = session.theme().getTheme(Theme.Type.LOGIN);

            Locale locale = session.getContext().resolveLocale(null);

            FreeMarkerProvider freeMarker = session.getProvider(FreeMarkerProvider.class);
            Map<String, Object> attributes = initAttributes(session, realm, theme, locale, responseStatus);

            String templateName = "error.ftl";

            String content = freeMarker.processTemplate(attributes, templateName, theme);
            return Response.status(responseStatus).type(MediaType.TEXT_HTML_UTF_8_TYPE).entity(content).build();
        } catch (Throwable t) {
            logger.error("Failed to create error page", t);
            return Response.serverError().build();
        }
    }

    /** 根据异常类型推断 HTTP 状态码。
     * @param throwable 异常
     * @return HTTP 状态
     */
    private static Response.Status getResponseStatus(Throwable throwable) {
        if (throwable instanceof WebApplicationException ex) {
            return Response.Status.fromStatusCode(ex.getResponse().getStatus());
        }

        if (throwable instanceof JsonProcessingException || throwable instanceof ModelValidationException) {
            return Response.Status.BAD_REQUEST;
        }

        if (throwable instanceof ModelIllegalStateException) {
            return Response.Status.INTERNAL_SERVER_ERROR;
        }

        if (throwable instanceof ModelDuplicateException) {
            return Response.Status.CONFLICT;
        }

        return Response.Status.INTERNAL_SERVER_ERROR;
    }

    /** 提取 OAuth2 错误码字符串。
     * @param throwable 异常
     * @return 错误码
     */
    private static String getErrorCode(Throwable throwable) {
        Throwable cause = throwable.getCause();

        if (cause instanceof JsonParseException) {
            return OAuthErrorException.INVALID_REQUEST;
        }

        if (cause instanceof ModelDuplicateException || throwable instanceof ModelDuplicateException) {
            return "conflict";
        }

        if (throwable instanceof WebApplicationException && throwable.getMessage() != null) {
            return throwable.getMessage();
        }

        return "unknown_error";
    }

    /** 从 URI 或默认 admin realm 解析当前领域。
     * @param session Keycloak 会话
     * @return 领域模型
     */
    private static RealmModel resolveRealm(KeycloakSession session) {
        String path = session.getContext().getUri().getPath();
        Matcher m = realmNamePattern.matcher(path);
        String realmName;
        if(m.matches()) {
            realmName = m.group(1);
        } else {
            realmName = Config.getAdminRealm();
        }

        RealmManager realmManager = new RealmManager(session);
        RealmModel realm = realmManager.getRealmByName(realmName);
        if (realm == null) {
            realm = realmManager.getRealmByName(Config.getAdminRealm());
        }

        session.getContext().setRealm(realm);

        return realm;
    }

    /** 初始化 FreeMarker 错误页模板属性。
     * @param session Keycloak 会话
     * @param realm 领域
     * @param theme 登录主题
     * @param locale 区域设置
     * @param responseStatus HTTP 状态
     * @return 模板属性映射
     * @throws IOException 主题资源加载失败时
     */
    private static Map<String, Object> initAttributes(KeycloakSession session, RealmModel realm, Theme theme, Locale locale, Response.Status responseStatus) throws IOException {
        Map<String, Object> attributes = new HashMap<>();
        Properties messagesBundle = theme.getEnhancedMessages(realm, locale);

        final var localeBean =  new LocaleBean(realm, locale, session.getContext().getUri().getRequestUriBuilder(), messagesBundle);
        final var lang = realm.isInternationalizationEnabled() ? localeBean.getCurrentLanguageTag() : Locale.ENGLISH.toLanguageTag();

        attributes.put("pageId", "error");
        attributes.put("statusCode", responseStatus.getStatusCode());

        attributes.put("realm", realm);
        attributes.put("url", new UrlBean(realm, theme, session.getContext().getUri().getBaseUri(), null));
        attributes.put("locale", localeBean);
        attributes.put("lang", lang);

        String errorKey = responseStatus == Response.Status.NOT_FOUND ? Messages.PAGE_NOT_FOUND : Messages.INTERNAL_SERVER_ERROR;
        String errorMessage = messagesBundle.getProperty(errorKey);

        attributes.put("message", new MessageBean(errorMessage, MessageType.ERROR));
        // 若后续确定深色模式失败，默认启用深色模式
        attributes.put("darkMode", true);

        attributes.put("msg", new MessageFormatterMethod(locale, messagesBundle));
        attributes.put("advancedMsg", new AdvancedMessageFormatterMethod(locale, messagesBundle));

        try {
            Properties properties = theme.getProperties();
            attributes.put("properties", properties);
            attributes.put("themeResources", ThemeResourcesParser.parse(properties));
            attributes.put("darkMode", "true".equals(properties.getProperty("darkMode"))
                    && realm.getAttribute("darkMode", true));
        } catch (IOException e) {
            logger.warn("Failed to load theme properties", e);
            attributes.put("themeResources", ThemeResources.empty());
        }

        return attributes;
    }

}
