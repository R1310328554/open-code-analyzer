package org.keycloak.ssf.transmitter.subject;

/**
 * 管理端主体订阅操作的结果元组。
 *
 * @param result     操作结果枚举（纳入/忽略/已通过组织等）
 * @param entityType 实体类型（如 user、organization）
 * @param entityId   实体标识
 */
public record AdminSubjectResult(SubjectManagementResult result, String entityType, String entityId) {
}
