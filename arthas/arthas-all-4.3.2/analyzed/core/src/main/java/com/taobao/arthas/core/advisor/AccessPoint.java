package com.taobao.arthas.core.advisor;

/**
 * 字节码增强的切入位点枚举，用位掩码标识 watch/trace/line 等通知场景。
 * <p>
 * 各常量对应 {@link com.alibaba.bytekit.asm.interceptor.annotation} 中的
 * AtEnter、AtExit、AtExceptionExit、AtLine 等拦截点。
 */
public enum AccessPoint {
    /** 方法进入前 */
    ACCESS_BEFORE(1, "AtEnter"),
    /** 正常返回后 */
    ACCESS_AFTER_RETUNING(1 << 1, "AtExit"),
    /** 抛异常退出后 */
    ACCESS_AFTER_THROWING(1 << 2, "AtExceptionExit"),
    /** 指定源码行 */
    ACCESS_LINE(1 << 3, "AtLine");

    /** 位掩码数值，可与 access 参数按位与判断通知类型 */
    private int value;

    /** ByteKit 拦截点名称，与注解 key 对应 */
    private String key;

    /** 返回位掩码值 */
    public int getValue() {
        return value;
    }

    /** 返回拦截点标识字符串 */
    public String getKey() {
        return key;
    }

    AccessPoint(int value, String key) {
        this.value = value;
        this.key = key;
    }
}
