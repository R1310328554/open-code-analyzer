package org.keycloak.ssf.transmitter.admin;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * realm 或单个接收方客户端的 SSF 事件状态精简摘要——各状态的计数与最早 {@code createdAt}。
 * 驱动 {@code GET /admin/realms/{realm}/ssf/events/stats} 与
 * {@code GET /admin/realms/{realm}/ssf/clients/{clientId}/events/stats} 端点，
 * 使操作员无需抓取 Prometheus 或直接查库即可判断发件箱是在消化还是积压。
 *
 * <p>零行状态不会出现在 {@code statuses} 映射中——底层 SQL {@code GROUP BY} 不合成零行，
 * 也无必要膨胀 wire 结构。</p>
 */
public class SsfEventStatsRepresentation {

    /** 各状态的快照。键为 {@link org.keycloak.models.jpa.entities.OutboxEntryStatus} 的 wire 形式——
     * {@code PENDING}、{@code DELIVERED}、{@code DEAD_LETTER}、{@code HELD}。 */
    private Map<String, StatusEntry> statuses = new LinkedHashMap<>();

    public Map<String, StatusEntry> getStatuses() {
        return statuses;
    }

    public void setStatuses(Map<String, StatusEntry> statuses) {
        this.statuses = statuses;
    }

    public static class StatusEntry {

        private long count;

        /** 该状态下各行的最早 {@code createdAt}；无行时为 {@code null}（通常整个条目不会出现在父映射中）。 */
        private Instant oldestCreatedAt;

        public StatusEntry() {
        }

        public StatusEntry(long count, Instant oldestCreatedAt) {
            this.count = count;
            this.oldestCreatedAt = oldestCreatedAt;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }

        public Instant getOldestCreatedAt() {
            return oldestCreatedAt;
        }

        public void setOldestCreatedAt(Instant oldestCreatedAt) {
            this.oldestCreatedAt = oldestCreatedAt;
        }
    }
}
