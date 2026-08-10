package org.keycloak.services.error;

import java.util.Set;

/** Bean 校验失败时的 JSON 错误体：总述与逐字段违规列表。 */
public record ViolationExceptionResponse(String error, Set<String> violations) {
}
