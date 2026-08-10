package org.keycloak.db.compatibility.verifier;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Keycloak 运行时数据迁移类的 JSON 表示，字段 {@code class} 存储迁移实现的全限定类名。
 *
 * @param clazz 迁移类 FQCN（JSON 键名为 {@code class}）
 */
record Migration(@JsonProperty("class") String clazz) {
}
