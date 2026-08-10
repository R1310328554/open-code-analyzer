package org.keycloak.testframework.events;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.common.util.reflections.Reflections;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.representations.idm.AdminEventRepresentation;
import org.keycloak.representations.idm.AuthDetailsRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.core.type.TypeReference;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;

/**
 * 管理事件（{@link AdminEventRepresentation}）的流式断言工具。
 * <p>
 * 支持校验操作类型、资源路径、认证详情及 JSON 表示内容，推荐链式调用。
 */
public class AdminEventAssertion {

    /** 被断言的管理事件。 */
    private final AdminEventRepresentation event;
    /** 是否期望为成功事件（非 {@code *_ERROR}）。 */
    private final boolean expectSuccess;

    /**
     * @param event 管理事件
     * @param expectSuccess 是否期望成功
     */
    protected AdminEventAssertion(AdminEventRepresentation event, boolean expectSuccess) {
        Assertions.assertNotNull(event, "Event was null");
        Assertions.assertNotNull(event.getId(), "Event id was null");
        this.event = event;
        this.expectSuccess = expectSuccess;
    }

    /**
     * 断言事件为成功的管理操作（操作类型不以 {@code _ERROR} 结尾）。
     *
     * @param event 待断言事件
     * @return 断言器实例，可继续链式校验
     */
    public static AdminEventAssertion assertSuccess(AdminEventRepresentation event) {
        Assertions.assertFalse(event.getOperationType().endsWith("_ERROR"), "Expected successful event");
        return new AdminEventAssertion(event, true)
                .assertEventId()
                .assertValidOperationType();
    }

    /**
     * 断言事件为失败的管理操作（操作类型以 {@code _ERROR} 结尾）。
     *
     * @param event 待断言事件
     * @return 断言器实例，可继续链式校验
     */
    public static AdminEventAssertion assertError(AdminEventRepresentation event) {
        Assertions.assertTrue(event.getOperationType().endsWith("_ERROR"), "Expected error event");
        return new AdminEventAssertion(event, false)
                .assertEventId()
                .assertValidOperationType();
    }

    /**
     * 一次性断言成功事件的多项字段；建议使用链式方法代替。
     *
     * @param event 待断言事件
     * @param operationType 期望操作类型
     * @param resourcePath 期望资源路径
     * @param representation 期望 JSON 表示
     * @param resourceType 期望资源类型
     * @return 断言器实例
     */
    public static AdminEventAssertion assertEvent(AdminEventRepresentation event, OperationType operationType, String resourcePath, Object representation, ResourceType resourceType) {
        return assertSuccess(event)
                .operationType(operationType)
                .resourcePath(resourcePath)
                .representation(representation)
                .resourceType(resourceType);
    }

    /**
     * 一次性断言成功事件（不含 representation）；建议使用链式方法代替。
     *
     * @param event 待断言事件
     * @param operationType 期望操作类型
     * @param resourcePath 期望资源路径
     * @param resourceType 期望资源类型
     * @return 断言器实例
     */
    public static AdminEventAssertion assertEvent(AdminEventRepresentation event, OperationType operationType, String resourcePath, ResourceType resourceType) {
        return assertSuccess(event)
                .operationType(operationType)
                .resourcePath(resourcePath)
                .resourceType(resourceType);
    }

    /**
     * 断言事件操作类型。
     * @param operationType 期望的 {@link OperationType}
     * @return 当前断言器
     */
    public AdminEventAssertion operationType(OperationType operationType) {
        Assertions.assertEquals(operationType.name(), getOperationType());
        return this;
    }

    /**
     * 断言事件的认证详情（realm、客户端、用户）。
     *
     * @param expectedRealmId 期望认证 realm ID
     * @param expectedClientId 期望客户端 ID
     * @param expectedUserId 期望用户 ID
     * @return 当前断言器
     */
    public AdminEventAssertion auth(String expectedRealmId, String expectedClientId, String expectedUserId) {
        AuthDetailsRepresentation authDetails = event.getAuthDetails();
        Assertions.assertEquals(expectedRealmId, authDetails.getRealmId());
        Assertions.assertEquals(expectedClientId, authDetails.getClientId());
        Assertions.assertEquals(expectedUserId, authDetails.getUserId());
        return this;
    }

    /**
     * 断言事件资源类型。
     *
     * @param expectedResourceType 期望的 {@link ResourceType}
     * @return 当前断言器
     */
    public AdminEventAssertion resourceType(ResourceType expectedResourceType) {
        Assertions.assertEquals(expectedResourceType.name(), event.getResourceType());
        return this;
    }

    /**
     * 断言事件资源路径（多段以 {@code /} 拼接）。
     *
     * @param expectedResourcePath 期望路径段
     * @return 当前断言器
     */
    public AdminEventAssertion resourcePath(String... expectedResourcePath) {
        Assertions.assertEquals(String.join("/", expectedResourcePath), event.getResourcePath());
        return this;
    }

    /**
     * 断言事件附带的 JSON 表示内容与期望值一致。
     *
     * @param expectedRep 期望表示对象（支持 List、Map 及 POJO 反射比较）
     * @return 当前断言器
     */
    public AdminEventAssertion representation(Object expectedRep) {
        String actualRepresentation = event.getRepresentation();
        if (expectedRep == null) {
            Assertions.assertNull(event.getRepresentation());
        } else {
            try {
                if (expectedRep instanceof List) {
                    // 角色列表：实际表示中须包含全部期望角色
                    List<RoleRepresentation> expectedRoles = (List<RoleRepresentation>) expectedRep;
                    List<RoleRepresentation> actualRoles = JsonSerialization.readValue(new ByteArrayInputStream(actualRepresentation.getBytes()), new TypeReference<>() {});

                    Map<String, String> expectedRolesMap = new HashMap<>();
                    for (RoleRepresentation role : expectedRoles) {
                        expectedRolesMap.put(role.getId(), role.getName());
                    }

                    Map<String, String> actualRolesMap = new HashMap<>();
                    for (RoleRepresentation role : actualRoles) {
                        actualRolesMap.put(role.getId(), role.getName());
                    }
                    Assertions.assertEquals(expectedRolesMap, actualRolesMap);

                } else if (expectedRep instanceof Map<?, ?> expectedRepMap) {
                    Map<?, ?> actualRepMap = JsonSerialization.readValue(actualRepresentation, Map.class);
                    for (Map.Entry<?, ?> entry : expectedRepMap.entrySet()) {
                        Object expectedValue = entry.getValue();
                        if (expectedValue != null) {
                            Object actualValue = actualRepMap.get(entry.getKey());
                            Assertions.assertEquals(expectedValue, actualValue, "Map item with key '" + entry.getKey() + "' not equal.");
                        }
                    }
                } else {
                    Object actualRep = JsonSerialization.readValue(actualRepresentation, expectedRep.getClass());

                    // 其他类型：反射比较 expected 中非空 getter 与事件中的实际表示
                    for (Method method : Reflections.getAllDeclaredMethods(expectedRep.getClass())) {
                        if (method.getParameterCount() == 0 && (method.getName().startsWith("get") || method.getName().startsWith("is"))) {
                            Object expectedValue = Reflections.invokeMethod(method, expectedRep);
                            if (expectedValue != null) {
                                Object actualValue = Reflections.invokeMethod(method, actualRep);
                                Assertions.assertEquals(expectedValue, actualValue, "Property method '" + method.getName() + "' of representation not equal.");
                            }
                        }
                    }
                }
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }
        return this;
    }

    /** 断言事件 ID 为合法 UUID。 */
    private AdminEventAssertion assertEventId() {
        MatcherAssert.assertThat(event.getId(), EventMatchers.isUUID());
        return this;
    }

    /** 断言操作类型为已知 {@link OperationType} 枚举值。 */
    private AdminEventAssertion assertValidOperationType() {
        String actualOperationType = getOperationType();
        try {
            OperationType.valueOf(actualOperationType);
        } catch (IllegalArgumentException e) {
            Assertions.fail("Unknown operation type: " + actualOperationType);
        }
        return this;
    }

    /** 返回用于校验的操作类型（错误事件会去掉 {@code _ERROR} 后缀）。 */
    private String getOperationType() {
        return expectSuccess ? event.getOperationType() : event.getOperationType().substring(0, "_ERROR".length());
    }

}
