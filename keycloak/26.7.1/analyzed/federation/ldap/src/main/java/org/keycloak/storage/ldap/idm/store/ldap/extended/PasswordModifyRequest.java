/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.storage.ldap.idm.store.ldap.extended;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.naming.ldap.ExtendedRequest;
import javax.naming.ldap.ExtendedResponse;

/**
 * RFC 3062 LDAP 密码修改扩展操作（Password Modify Extended Operation）的客户端请求实现。
 * <p>
 * 可用于支持该扩展操作的任意 LDAP 服务器。
 *
 * @author Josh Cummings
 * @since 4.2.9
 */
public final class PasswordModifyRequest implements ExtendedRequest {

  /** 密码修改扩展操作 OID。 */
  public static final String PASSWORD_MODIFY_OID = "1.3.6.1.4.1.4203.1.11.1";

  private static final byte SEQUENCE_TYPE = 48;
  private static final byte USER_IDENTITY_OCTET_TYPE = -128;
  private static final byte OLD_PASSWORD_OCTET_TYPE = -127;
  private static final byte NEW_PASSWORD_OCTET_TYPE = -126;

  private final ByteArrayOutputStream value = new ByteArrayOutputStream();

  /**
   * 构造密码修改请求。
   *
   * @param userIdentity 用户标识（DN 或空表示当前绑定用户）
   * @param oldPassword 旧密码，可为 null
   * @param newPassword 新密码，可为 null
   */
  public PasswordModifyRequest(String userIdentity, String oldPassword, String newPassword) {
    ByteArrayOutputStream elements = new ByteArrayOutputStream();

    if (userIdentity != null) {
      berEncode(USER_IDENTITY_OCTET_TYPE, userIdentity.getBytes(), elements);
    }

    if (oldPassword != null) {
      berEncode(OLD_PASSWORD_OCTET_TYPE, oldPassword.getBytes(), elements);
    }

    if (newPassword != null) {
      berEncode(NEW_PASSWORD_OCTET_TYPE, newPassword.getBytes(), elements);
    }

    berEncode(SEQUENCE_TYPE, elements.toByteArray(), this.value);
  }

  /** {@inheritDoc} */
  @Override
  public String getID() {
    return PASSWORD_MODIFY_OID;
  }

  /** {@inheritDoc} */
  @Override
  public byte[] getEncodedValue() {
    return this.value.toByteArray();
  }

  /** {@inheritDoc} 本实现不解析扩展响应。 */
  @Override
  public ExtendedResponse createExtendedResponse(String id, byte[] berValue, int offset, int length) {
    return null;
  }

  /**
   * 最小 BER 编码实现，仅满足密码修改请求所需。
   * 参见 <a target="_blank" href="https://www.itu.int/ITU-T/studygroups/com17/languages/X.690-0207.pdf">X.690</a>。
   */
  private void berEncode(byte type, byte[] src, ByteArrayOutputStream dest) {
    int length = src.length;

    dest.write(type);

    if (length < 128) {
      dest.write(length);
    } else if ((length & 0x0000_00FF) == length) {
      dest.write((byte) 0x81);
      dest.write((byte) (length & 0xFF));
    } else if ((length & 0x0000_FFFF) == length) {
      dest.write((byte) 0x82);
      dest.write((byte) ((length >> 8) & 0xFF));
      dest.write((byte) (length & 0xFF));
    } else if ((length & 0x00FF_FFFF) == length) {
      dest.write((byte) 0x83);
      dest.write((byte) ((length >> 16) & 0xFF));
      dest.write((byte) ((length >> 8) & 0xFF));
      dest.write((byte) (length & 0xFF));
    } else {
      dest.write((byte) 0x84);
      dest.write((byte) ((length >> 24) & 0xFF));
      dest.write((byte) ((length >> 16) & 0xFF));
      dest.write((byte) ((length >> 8) & 0xFF));
      dest.write((byte) (length & 0xFF));
    }

    try {
      dest.write(src);
    } catch (IOException e) {
      throw new IllegalArgumentException("Failed to BER encode provided value of type: " + type);
    }
  }
}
