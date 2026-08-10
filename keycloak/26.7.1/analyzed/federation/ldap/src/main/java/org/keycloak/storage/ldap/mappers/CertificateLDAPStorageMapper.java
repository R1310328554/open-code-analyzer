package org.keycloak.storage.ldap.mappers;

import org.keycloak.common.util.PemUtils;
import org.keycloak.component.ComponentModel;
import org.keycloak.storage.ldap.LDAPStorageProvider;
import org.keycloak.storage.ldap.idm.query.Condition;
import org.keycloak.storage.ldap.idm.query.internal.EqualCondition;
import org.keycloak.storage.ldap.idm.query.internal.LDAPQuery;

/**
 * 证书 LDAP 映射器：将 LDAP 中的证书属性映射到 Keycloak 用户属性，支持 DER 格式查询转换。
 */
public class CertificateLDAPStorageMapper extends UserAttributeLDAPStorageMapper {

  /** 配置项：LDAP 中证书是否为 DER 格式（非 PEM）。 */
  public static final String IS_DER_FORMATTED = "is.der.formatted";

  public CertificateLDAPStorageMapper(ComponentModel mapperModel, LDAPStorageProvider ldapProvider) {
    super(mapperModel, ldapProvider);
  }

  /** {@inheritDoc} DER 格式时将等值条件中的 PEM 转为 DER 字节。 */
  @Override
  public void beforeLDAPQuery(LDAPQuery query) {
    super.beforeLDAPQuery(query);

    String ldapAttrName = getLdapAttributeName();

    if (isDerFormatted()) {
      for (Condition condition : query.getConditions()) {
        if (condition instanceof EqualCondition &&
            condition.getParameterName().equalsIgnoreCase(ldapAttrName)) {
          EqualCondition equalCondition = ((EqualCondition) condition);
          equalCondition.setValue(PemUtils.pemToDer(equalCondition.getValue().toString()));
        }
      }
    }
  }

  /** 是否启用 DER 格式证书。 */
  private boolean isDerFormatted() {
    return mapperModel.get(IS_DER_FORMATTED, false);
  }
}
