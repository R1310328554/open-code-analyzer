package org.keycloak.admin.api;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.representations.admin.v2.BaseClientRepresentation;

/**
 * 客户端 Admin API v2 列表查询中可排序的字段（对应 {@code sort} 参数）。
 * <p>
 * API 名称映射到 {@code CLIENT} 表中的标量列，并提供按升序/降序构建 {@link Comparator} 的能力。
 */
public enum ClientField {
    /** 客户端 ID。 */
    CLIENT_ID("clientId", stringKey(BaseClientRepresentation::getClientId)),
    /** 显示名称。 */
    DISPLAY_NAME("displayName", stringKey(BaseClientRepresentation::getDisplayName)),
    /** 描述。 */
    DESCRIPTION("description", stringKey(BaseClientRepresentation::getDescription)),
    /** 协议类型（如 openid-connect、saml）。 */
    PROTOCOL("protocol", stringKey(BaseClientRepresentation::getProtocol)),
    /** 是否启用。 */
    ENABLED("enabled", booleanKey(BaseClientRepresentation::getEnabled)),
    /** 应用主页 URL。 */
    APP_URL("appUrl", stringKey(BaseClientRepresentation::getAppUrl)),
    /** 创建时间戳。 */
    CREATED_TIMESTAMP("createdTimestamp", longKey(BaseClientRepresentation::getCreatedTimestamp)),
    /** 最后更新时间戳。 */
    UPDATED_TIMESTAMP("updatedTimestamp", longKey(BaseClientRepresentation::getUpdatedTimestamp));

    /** 在 API 查询参数中使用的字段名。 */
    private final String apiName;
    /** 根据升序/降序生成比较器的工厂。 */
    private final ComparatorFactory comparatorFactory;

    ClientField(String apiName, ComparatorFactory comparatorFactory) {
        this.apiName = apiName;
        this.comparatorFactory = comparatorFactory;
    }

    public String getApiName() {
        return apiName;
    }

    /** 返回用于 sort 查询参数的字段名。 */
    public String toQueryValue() {
        return apiName;
    }

    /**
     * 构建针对 {@link BaseClientRepresentation} 的比较器。
     *
     * @param ascending 是否升序
     */
    public Comparator<BaseClientRepresentation> comparator(boolean ascending) {
        return comparatorFactory.comparator(ascending);
    }

    /** 默认排序字段（clientId）。 */
    public static ClientField defaultField() {
        return CLIENT_ID;
    }

    /** 按 API 名称解析排序字段，未知名称返回空。 */
    public static Optional<ClientField> fromApiName(String apiName) {
        return Stream.of(values()).filter(field -> field.apiName.equals(apiName)).findFirst();
    }

    /** 返回所有允许的 API 字段名，逗号分隔。 */
    public static String allowedApiNames() {
        return Stream.of(values()).map(ClientField::getApiName).collect(Collectors.joining(", "));
    }

    private static ComparatorFactory longKey(Function<BaseClientRepresentation, Long> getter) {
        return ascending -> Comparator.comparing(getter, Comparator.nullsLast(
                ascending ? Long::compareTo : Comparator.<Long>reverseOrder()));
    }

    private static ComparatorFactory stringKey(Function<BaseClientRepresentation, String> getter) {
        return ascending -> Comparator.comparing(getter, Comparator.nullsLast(
                ascending ? String.CASE_INSENSITIVE_ORDER : String.CASE_INSENSITIVE_ORDER.reversed()));
    }

    private static ComparatorFactory booleanKey(Function<BaseClientRepresentation, Boolean> getter) {
        return ascending -> Comparator.comparing(getter, Comparator.nullsLast(
                ascending ? Boolean::compareTo : Comparator.<Boolean>reverseOrder()));
    }

    /** 根据升序标志生成比较器的函数式接口。 */
    @FunctionalInterface
    private interface ComparatorFactory {
        Comparator<BaseClientRepresentation> comparator(boolean ascending);
    }
}
