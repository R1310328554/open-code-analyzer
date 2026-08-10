package org.keycloak.ssf.metadata;

/**
 * SSF Transmitter {@code default_subjects} 元数据字段的合法取值（SSF 1.0 §7.1）。
 * <p>控制 Transmitter 在 subject 作用域内投递事件的默认行为：</p>
 * <ul>
 *     <li>{@link #ALL} — 除非流显式收窄范围，否则为每个匹配 subject 投递事件；
 *         保留 subject 管理引入前的 Transmitter 行为。</li>
 *     <li>{@link #NONE} — 仅向已显式订阅的 subject 投递
 *         （通过 Receiver add-subject 调用或管理员配置的 {@code ssf.notify.<clientId>} 属性）。</li>
 * </ul>
 * <p>规范还允许以具体 Subject 声明作为第三种选项；Keycloak 当前不支持该变体。</p>
 */
public enum DefaultSubjects {
    /** 为所有匹配 subject 投递事件（默认行为）。 */
    ALL,
    /** 仅向显式订阅的 subject 投递事件。 */
    NONE;

    /**
     * 将大小写不敏感的字符串解析为 {@link DefaultSubjects} 值。
     * <p>输入为 {@code null}、空白或非法值时返回 {@code fallback}。
     * 用于 SPI / 配置入口，对用户输入的大小写具有容错性。</p>
     * @param value 待解析字符串
     * @param fallback 解析失败时的回退值
     * @return 解析结果或回退值
     */
    public static DefaultSubjects parseOrDefault(String value, DefaultSubjects fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return DefaultSubjects.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}
