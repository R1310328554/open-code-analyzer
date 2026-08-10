package org.keycloak.ssf.transmitter.subject;

/** 主题增删改操作的结果码。 */
public enum SubjectManagementResult {
    /** 操作成功。 */
    OK,
    /** 未找到对应流或调用方无权访问。 */
    STREAM_NOT_FOUND,
    /** 不支持的主体格式或类型。 */
    FORMAT_UNSUPPORTED,
    /** 无法解析到目标主体。 */
    SUBJECT_NOT_FOUND,
    /**
     * 解析到的主体由只读用户存储支撑（例如 LDAP 联合且编辑模式为
     * {@code READ_ONLY}，或未启用导入），因此无法持久化
     * {@code ssf.notify.<clientId>} 订阅状态。作为明确的发送方能力限制返回，
     * 而非未处理的 500。要持久化按用户订阅状态，需可写用户存储（例如
     * LDAP 编辑模式 {@code UNSYNCED} 且启用导入，或通过 LDAP 属性映射器
     * 返回正确的 {@code ssf.notify.<client_id>=true} 属性）。
     */
    SUBJECT_READ_ONLY
}
