package org.keycloak.testframework.realm;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Keycloak 表示对象（Representation）的深拷贝工具。
 * <p>
 * 通过 Jackson 序列化/反序列化实现，避免修改副本时影响原对象。
 */
public class RepresentationUtils {

    /** 用于序列化与反序列化的 Jackson {@link ObjectMapper}。 */
    private static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 深拷贝表示对象。
     *
     * @param t 源对象
     * @return 类型相同的新实例
     */
    @SuppressWarnings("unchecked")
    public static <T> T clone(T t) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(t);
            return (T) objectMapper.readValue(bytes, t.getClass());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
