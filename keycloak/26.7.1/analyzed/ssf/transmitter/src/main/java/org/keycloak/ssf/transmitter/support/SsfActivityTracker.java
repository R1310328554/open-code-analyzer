package org.keycloak.ssf.transmitter.support;

import org.keycloak.common.util.Time;
import org.keycloak.models.ClientModel;
import org.keycloak.ssf.transmitter.stream.storage.client.ClientStreamStore;

/**
 * 当接收方以规范定义的「合格 Receiver 活动」（SSF 1.0 §8.1.1 inactivity_timeout）方式
 * 访问 SSF 发送方时，在接收方客户端上写入
 * {@link ClientStreamStore#SSF_LAST_ACTIVITY_TIMESLOT_KEY}：
 * PUSH 或 POLL 流的任意流管理 API 调用，以及 POLL 流的 poll 本身。
 *
 * <p>写入合并——仅当存储值早于 {@link #STAMP_GRANULARITY_SECONDS} 时才持久化。
 * 否则繁忙的 POLL 接收方每隔数秒拉取会 hammer 客户端属性表并在每次请求触发集群级
 * Infinispan 失效；不活动超时检查可容忍数分钟陈旧，因真实超时为分钟至天级。
 */
public final class SsfActivityTracker {

    /**
     * 仅当存储值早于该秒数时才持久化 timeslot。300 秒（5 分钟）= 每接收方至多每 5 分钟写一次。
     * 不活动超时精度同等降级（至多晚 5 分钟）——相对 UI 以分/时/天展示的超时可忽略。
     */
    public static final long STAMP_GRANULARITY_SECONDS = 300L;

    private SsfActivityTracker() {
    }

    /**
     * 记录给定接收方客户端的活动。{@code client} 为 {@code null} 时无操作
     * （例如未认证且未解析调用方的请求），调用方无需空检查。
     * 按 {@link #STAMP_GRANULARITY_SECONDS} 合并写入。
     */
    public static void stamp(ClientModel client) {
        if (client == null) {
            return;
        }
        long now = Time.currentTime();
        String existing = client.getAttribute(ClientStreamStore.SSF_LAST_ACTIVITY_TIMESLOT_KEY);
        if (existing != null && !existing.isBlank()) {
            try {
                long stored = Long.parseLong(existing.trim());
                if (now - stored < STAMP_GRANULARITY_SECONDS) {
                    return;
                }
            } catch (NumberFormatException ignored) {
                // 属性格式错误——继续执行并覆盖。
            }
        }
        client.setAttribute(ClientStreamStore.SSF_LAST_ACTIVITY_TIMESLOT_KEY, String.valueOf(now));
    }
}
