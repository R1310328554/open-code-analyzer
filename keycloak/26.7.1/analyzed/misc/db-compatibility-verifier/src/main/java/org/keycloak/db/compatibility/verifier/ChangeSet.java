package org.keycloak.db.compatibility.verifier;

/**
 * Liquibase 变更集标识：对应 {@code jpa-changelog*.xml} 中单个 {@code changeSet} 的元数据。
 *
 * @param id       变更集 id 属性
 * @param author   变更集 author 属性
 * @param filename 声明该变更集的 XML 资源路径
 */
record ChangeSet(String id, String author, String filename) {
}
