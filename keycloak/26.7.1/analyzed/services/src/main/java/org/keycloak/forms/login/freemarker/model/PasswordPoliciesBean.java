package org.keycloak.forms.login.freemarker.model;

import org.keycloak.models.PasswordPolicy;

/**
 * 密码策略 FreeMarker Bean：将 Realm {@link PasswordPolicy} 转为模板可读的最小/最大长度、字符类要求与历史策略等数值与布尔标志。
 * <p>供注册、重置密码等页面展示密码复杂度提示。</p>
 */
public class PasswordPoliciesBean {
  private final Integer length;
  private final Integer maxLength;
  private final Integer lowerCase;
  private final Integer upperCase;
  private final Integer specialChars;
  private final Integer digits;
  private final Integer passwordHistory;
  private final Integer forceExpiredPasswordChange;
  private final boolean notUsername;
  private final boolean notEmail;

  /** @param policy Realm 密码策略 */
  public PasswordPoliciesBean(PasswordPolicy policy) {
    this.length = policy.getPolicyConfig("length");
    this.maxLength = policy.getPolicyConfig("maxLength");
    this.lowerCase = policy.getPolicyConfig("lowerCase");
    this.upperCase = policy.getPolicyConfig("upperCase");
    this.specialChars = policy.getPolicyConfig("specialChars");
    this.digits = policy.getPolicyConfig("digits");
    this.passwordHistory = policy.getPolicyConfig("passwordHistory");
    this.forceExpiredPasswordChange = policy.getPolicyConfig("forceExpiredPasswordChange");
    this.notUsername = policy.getPolicies().contains("notUsername");
    this.notEmail = policy.getPolicies().contains("notEmail");
  }

  /** @return 最小长度要求（未配置时为 null） */
  public Integer getLength() {
    return length;
  }

  /** @return 最大长度要求 */
  public Integer getMaxLength() {
    return maxLength;
  }

  /** @return 所需小写字母最少个数 */
  public Integer getLowerCase() {
    return lowerCase;
  }

  /** @return 所需大写字母最少个数 */
  public Integer getUpperCase() {
    return upperCase;
  }

  /** @return 所需特殊字符最少个数 */
  public Integer getSpecialChars() {
    return specialChars;
  }

  /** @return 所需数字最少个数 */
  public Integer getDigits() {
    return digits;
  }

  /** @return 密码历史记录条数限制 */
  public Integer getPasswordHistory() {
    return passwordHistory;
  }

  /** @return 强制过期改密天数 */
  public Integer getForceExpiredPasswordChange() {
    return forceExpiredPasswordChange;
  }

  /** @return 是否禁止密码与用户名相同 */
  public boolean isNotUsername() {
    return notUsername;
  }

  /** @return 是否禁止密码与邮箱相同 */
  public boolean isNotEmail() {
    return notEmail;
  }
}
