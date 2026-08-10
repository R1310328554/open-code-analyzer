package org.keycloak.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.keycloak.representations.AuthorizationDetailsJSONRepresentation;

/**
 * 授权详情（authorization_details）解析器，可高效地将通用表示转换为特定子类型。
 *
 * <p>
 * 通过 {@link #registerParser(String, AuthorizationDetailsParser)} 按 type 注册解析器，
 * 再调用 {@link #asSubtype} 或 {@link #parseToSubtype} 完成类型安全的转换。
 * </p>
 */
public interface AuthorizationDetailsParser {

    /**
     * 将授权详情对象转换为指定的子类型。
     *
     * @param authzDetail 待转换的授权详情对象
     * @param clazz 目标子类型
     * @return 转换后的子类型实例
     */
    <T extends AuthorizationDetailsJSONRepresentation> T asSubtype(AuthorizationDetailsJSONRepresentation authzDetail, Class<T> clazz);


    /** 按 type 声明注册的解析器映射表。 */
    Map<String, AuthorizationDetailsParser> PARSERS = new ConcurrentHashMap<>();

    /**
     * 为指定 type 注册解析器，供后续 {@link #asSubtype} 调用使用。
     * 解析器应在应用首次使用 authorization_details 之前、
     * 首次调用 {@link #asSubtype} 之前完成注册，通常于应用启动时完成。
     * 若实现 Keycloak 提供者 <em>AuthorizationDetailsProcessor</em>，
     * 建议在 <em>AuthorizationDetailsProcessorFactory.init</em> 中注册对应解析器。
     *
     * @param type authorization_details 条目中 "type" 声明的值
     * @param parser 该 type 对应的解析器
     */
    static void registerParser(String type, AuthorizationDetailsParser parser) {
        PARSERS.put(type, parser);
    }

    /**
     * 不应直接调用；请先通过 {@link #registerParser(String, AuthorizationDetailsParser)} 注册，
     * 再在应用中调用 {@link #asSubtype(AuthorizationDetailsJSONRepresentation, Class)}。
     *
     * @param authzDetail 待转换的授权详情对象
     * @param clazz 期望返回的 {@link AuthorizationDetailsJSONRepresentation} 子类型
     * @return 若存在与 {@link AuthorizationDetailsJSONRepresentation#getType} 对应的解析器，
     *         则将其转换并强转为 clazz 指定的子类型；否则抛出异常
     */
    static <T extends AuthorizationDetailsJSONRepresentation> T parseToSubtype(AuthorizationDetailsJSONRepresentation authzDetail, Class<T> clazz) {
        if (authzDetail.getType() == null) {
            throw new IllegalArgumentException("Used authzDetail entry does not have 'type' set. The used authzDetail entry was: " + authzDetail);
        }
        AuthorizationDetailsParser parser = PARSERS.get(authzDetail.getType());
        if (parser == null) {
            throw new IllegalArgumentException("Unsupported to parse response of type '" + authzDetail.getType() + "' to the class '" + clazz +
                    "'. Please make sure that corresponding parser is registered.");
        }
        return parser.asSubtype(authzDetail, clazz);
    }
}
