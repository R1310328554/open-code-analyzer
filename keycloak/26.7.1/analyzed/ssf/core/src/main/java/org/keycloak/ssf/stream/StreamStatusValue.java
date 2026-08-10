package org.keycloak.ssf.stream;

/**
 * SSF 流生命周期状态枚举，规范定义的三态：enabled、paused、disabled。
 * <p>发送方 MUST 按当前状态决定是否投递、缓存或丢弃事件。</p>
 */
public enum StreamStatusValue {

    /** 发送方 MUST 按流配置的投递方式正常投递事件。 */
    enabled,

    /**
     * 发送方 MUST NOT 投递事件，但 SHOULD 缓存暂停期间产生的事件，待恢复为 enabled 后补发。
     * 若同一主体_principal 有多条待补发事件，MUST 按生成时间顺序投递，或仅发送无需前置事件即可处理的最新事件。
     */
    paused,

    /** 发送方 MUST NOT 投递事件，且不会缓存待后续补发。 */
    disabled;

    /** @return 规范 wire 格式的小写状态码（与枚举名一致） */
    public String getStatusCode() {
        return name().toLowerCase();
    }
}
