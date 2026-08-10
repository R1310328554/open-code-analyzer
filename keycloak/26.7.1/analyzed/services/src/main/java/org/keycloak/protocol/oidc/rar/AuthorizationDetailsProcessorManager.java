package org.keycloak.protocol.oidc.rar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserSessionModel;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.AuthorizationDetailsJSONRepresentation;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.core.type.TypeReference;
import org.jboss.logging.Logger;

/**
 * 授权详情（authorization_details）处理器管理器。
 * <p>协调各 {@link AuthorizationDetailsProcessor} SPI，完成 RAR 参数的解析、校验、处理与令牌响应清理。</p>
 */
public class AuthorizationDetailsProcessorManager {

    /** 日志记录器 */
    private static final Logger logger = Logger.getLogger(AuthorizationDetailsProcessorManager.class);

    /** Keycloak 会话 */
    private final KeycloakSession session;

    /** @param session Keycloak 会话 */
    public AuthorizationDetailsProcessorManager(KeycloakSession session) {
        this.session = session;
    }

    /**
     * 处理授权请求中的 authorization_details 参数。
     * @param userSession 用户会话
     * @param clientSessionCtx 客户端会话上下文
     * @param authorizationDetailsParam JSON 字符串
     * @return 处理后的授权详情列表
     * @throws InvalidAuthorizationDetailsException 参数无效时
     */
    public List<AuthorizationDetailsJSONRepresentation> processAuthorizationDetails(UserSessionModel userSession, ClientSessionContext clientSessionCtx,
                                                                                    String authorizationDetailsParam) throws InvalidAuthorizationDetailsException {
        return processAuthorizationDetailsInternal(authorizationDetailsParam,
                (processor, authzDetail) -> processor.process(userSession, clientSessionCtx, authzDetail));
    }


    /** 处理已存储（会话中）的 authorization_details @return 处理后的授权详情列表 */
    public List<AuthorizationDetailsJSONRepresentation> processStoredAuthorizationDetails(UserSessionModel userSession,
                                                                                          ClientSessionContext clientSessionCtx,
                                                                                          String authorizationDetailsParam) throws InvalidAuthorizationDetailsException {
        return processAuthorizationDetailsInternal(authorizationDetailsParam,
                (processor, authzDetail) ->
                        processor.processStoredAuthorizationDetails(userSession, clientSessionCtx, authzDetail));
    }


    /**
     * 请求未携带 authorization_details 时，由各处理器生成默认响应（如预授权/凭证 offer 流程）。
     * @return 聚合后的授权详情列表
     */
    public List<AuthorizationDetailsJSONRepresentation> handleMissingAuthorizationDetails(UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        List<AuthorizationDetailsJSONRepresentation> allAuthzDetails = new ArrayList<>();
        session.getKeycloakSessionFactory()
                .getProviderFactoriesStream(AuthorizationDetailsProcessor.class)
                .sorted((f1, f2) -> f2.order() - f1.order())
                .map(f -> session.getProvider(AuthorizationDetailsProcessor.class, f.getId()))
                .map(processor -> processor.handleMissingAuthorizationDetails(userSession, clientSessionCtx))
                .filter(Objects::nonNull)
                .forEach(allAuthzDetails::addAll);
        return allAuthzDetails;
    }

    /** 校验 authorization_details 参数格式与各 type 处理器 @param authorizationDetailsParam JSON 字符串 */
    public void validateAuthorizationDetail(String authorizationDetailsParam) {
        processAuthorizationDetailsInternal(authorizationDetailsParam, AuthorizationDetailsProcessor::validateAuthorizationDetail);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    /** 授权详情处理完成后的后置回调 @param authorizationDetailsResponse 已处理的响应列表 */
    public void afterAuthorizationDetailsProcessed(UserSessionModel userSession,
                                                   ClientSessionContext clientSessionCtx,
                                                   List<AuthorizationDetailsJSONRepresentation> authorizationDetailsResponse) throws InvalidAuthorizationDetailsException {
        Map<String, AuthorizationDetailsProcessor<?>> processors = getAuthorizationDetailsProcessorMap();
        for (AuthorizationDetailsJSONRepresentation authzDetailResponse : authorizationDetailsResponse) {
            AuthorizationDetailsProcessor processor = findProcessorForAuthorizationDetails(processors, authzDetailResponse);
            processor.afterAuthorizationDetailsProcessed(userSession, clientSessionCtx, authzDetailResponse.asSubtype(processor.getSupportedResponseJavaType()));
        }
    }

    /**
     * 在令牌响应发出前清理 authorization_details（见 issue #50079）。
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    /** 清理令牌响应中的 authorization_details @param tokenResponse 访问令牌响应 */
    public void sanitizeBeforeSendingTokenResponse(AccessTokenResponse tokenResponse) {
        if (tokenResponse.getAuthorizationDetails() != null) {
            List<AuthorizationDetailsJSONRepresentation> outAuthzDetails = new ArrayList<>();
            Map<String, AuthorizationDetailsProcessor<?>> processors = getAuthorizationDetailsProcessorMap();
            for (AuthorizationDetailsJSONRepresentation authzDetail : tokenResponse.getAuthorizationDetails()) {
                AuthorizationDetailsProcessor processor = findProcessorForAuthorizationDetails(processors, authzDetail);
                AuthorizationDetailsJSONRepresentation subAuthzDetail = authzDetail.asSubtype(processor.getSupportedResponseJavaType());
                outAuthzDetails.add(processor.sanitizeBeforeSendingTokenResponse(subAuthzDetail));
            }
            tokenResponse.setAuthorizationDetails(outAuthzDetails);
        }
    }

    // 私有辅助方法

    /** @return 按 type/providerId 索引的处理器映射 */
    private Map<String, AuthorizationDetailsProcessor<?>> getAuthorizationDetailsProcessorMap() {
        return session.getKeycloakSessionFactory()
                .getProviderFactoriesStream(AuthorizationDetailsProcessor.class)
                .collect(Collectors.toMap(ProviderFactory::getId, factory -> (AuthorizationDetailsProcessor<?>) session.getProvider(AuthorizationDetailsProcessor.class, factory.getId())));
    }

    /** 内部通用处理流程：解析 JSON 并逐条分派给对应处理器 @return 非 null 响应列表 */
    private List<AuthorizationDetailsJSONRepresentation> processAuthorizationDetailsInternal(String authorizationDetailsParam,
                                                                                             BiFunction<AuthorizationDetailsProcessor<?>, AuthorizationDetailsJSONRepresentation, AuthorizationDetailsJSONRepresentation> function) throws InvalidAuthorizationDetailsException {

        List<AuthorizationDetailsJSONRepresentation> authzDetails = parseAuthorizationDetails(authorizationDetailsParam);
        if (authzDetails.isEmpty()) {
            throw new InvalidAuthorizationDetailsException("Authorization_Details parameter cannot be empty");
        }

        Map<String, AuthorizationDetailsProcessor<?>> processors = getAuthorizationDetailsProcessorMap();

        List<AuthorizationDetailsJSONRepresentation> authzResponses = new ArrayList<>();
        for (AuthorizationDetailsJSONRepresentation authzDetail : authzDetails) {
            AuthorizationDetailsProcessor<?> processor = findProcessorForAuthorizationDetails(processors, authzDetail);
            AuthorizationDetailsJSONRepresentation response = function.apply(processor, authzDetail);
            if (response != null) {
                authzResponses.add(response);
            } else {
                logger.debugf("Null response returned by authorization processor " + processor + " for given authorization details");
            }
        }

        return authzResponses;
    }

    /** 按 type 查找处理器，缺失或不支持时抛出 {@link InvalidAuthorizationDetailsException} */
    private AuthorizationDetailsProcessor<?> findProcessorForAuthorizationDetails(Map<String, AuthorizationDetailsProcessor<?>> processors, AuthorizationDetailsJSONRepresentation authzDetail) {
        if (authzDetail.getType() == null) {
            throw new InvalidAuthorizationDetailsException("Authorization_Details parameter provided without type: " + authzDetail);
        }
        AuthorizationDetailsProcessor<?> processor = processors.get(authzDetail.getType());
        if (processor == null) {
            String errorDetails = String.format("Unsupported type '%s' of authorization_details parameter supplied in the request. Supported values: %s",
                    authzDetail.getType(), processors.keySet());
            logger.warn(errorDetails);
            throw new InvalidAuthorizationDetailsException(errorDetails);
        }
        return processor;
    }

    /** 将 JSON 字符串解析为授权详情列表 @throws InvalidAuthorizationDetailsException 解析失败时 */
    private List<AuthorizationDetailsJSONRepresentation> parseAuthorizationDetails(String authorizationDetailsParam) {
        try {
            return JsonSerialization.readValue(authorizationDetailsParam, new TypeReference<>() {});
        } catch (Exception e) {
            logger.warnf(e, "Cannot parse authorization_details: %s", authorizationDetailsParam);
            throw new InvalidAuthorizationDetailsException("Invalid authorization_details: " + authorizationDetailsParam);
        }
    }
}
