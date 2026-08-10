package org.keycloak.storage.ldap.idm.query.internal;

import org.keycloak.storage.ldap.idm.query.EscapeStrategy;

/**
 * LDAP 过滤器值 Octet-String 编码器，按二进制或默认策略转义属性比较值。
 */
class OctetStringEncoder {

  /** 非二进制模式下的回退转义策略。 */
  private final EscapeStrategy fallback;

  /**
   * @param fallback 字符串转义回退策略
   */
  OctetStringEncoder(EscapeStrategy fallback) {
    this.fallback = fallback;
  }

  /**
   * 将参数值编码为 LDAP 过滤器安全字符串。
   *
   * @param parameterValue 原始比较值
   * @param isBinary 是否按 Octet-String 十六进制转义
   */
  public String encode(Object parameterValue, boolean isBinary) {
    String escaped;
    if (parameterValue instanceof byte[]) {
      escaped = EscapeStrategy.escapeHex((byte[]) parameterValue);
    } else {
      escaped = escapeAsString(parameterValue, isBinary);
    }
    return escaped;
  }

  /** 将非字节数组值按字符串或二进制语义转义。 */
  private String escapeAsString(Object parameterValue, boolean isBinary) {
    String escaped;
    String stringValue = parameterValue.toString();
    if (isBinary) {
      escaped = EscapeStrategy.OCTET_STRING.escape(stringValue);
    } else {
      escaped = fallback.escape(stringValue);
    }
    return escaped;
  }

}
