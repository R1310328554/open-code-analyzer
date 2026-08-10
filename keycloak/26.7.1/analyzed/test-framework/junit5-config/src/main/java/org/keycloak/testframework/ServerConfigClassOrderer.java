package org.keycloak.testframework;

import java.util.Optional;

import org.keycloak.testframework.annotations.KeycloakIntegrationTest;

import org.infinispan.util.function.SerializableComparator;
import org.junit.jupiter.api.ClassDescriptor;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.ClassOrdererContext;

/**
 * JUnit 5 测试类排序器，按 {@link KeycloakIntegrationTest#config()} 名称排序。
 * <p>
 * 带 {@link KeycloakIntegrationTest} 注解的类优先于无注解类，且同类之间按配置名升序排列。
 */
public class ServerConfigClassOrderer implements ClassOrderer {

    /** {@inheritDoc} 使用 {@link ServerConfigComparator} 对测试类描述符排序。 */
    @Override
    public void orderClasses(ClassOrdererContext classOrdererContext) {
        classOrdererContext.getClassDescriptors().sort(new ServerConfigComparator());
    }

    /** 比较两个测试类上 {@link KeycloakIntegrationTest} 配置名称的序列化比较器。 */
    static class ServerConfigComparator implements SerializableComparator<ClassDescriptor> {

        /**
         * 按集成测试配置名排序；仅一侧有注解时该侧靠后。
         *
         * @param o1 第一个类描述符
         * @param o2 第二个类描述符
         * @return 比较结果，语义同 {@link Comparable#compareTo}
         */
        @Override
        public int compare(ClassDescriptor o1, ClassDescriptor o2) {
            Optional<KeycloakIntegrationTest> a1 = o1.findAnnotation(KeycloakIntegrationTest.class);
            Optional<KeycloakIntegrationTest> a2 = o2.findAnnotation(KeycloakIntegrationTest.class);

            if (a1.isPresent() && a2.isPresent()) {
                return a1.get().config().getName().compareTo(a2.get().config().getName());
            } else if (a1.isPresent()) {
                return 1;
            } else if (a2.isPresent()) {
                return 2;
            } else {
                return 0;
            }
        }

    }

}
