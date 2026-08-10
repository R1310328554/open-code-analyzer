// Targets API 响应类型，对应 /api/v1/targets，展示 active 与 dropped scrape 目标。

export interface Labels {
  [key: string]: string;
}

export type Target = {
  discoveredLabels: Labels;
  labels: Labels;
  scrapePool: string;
  scrapeUrl: string;
  globalUrl: string;
  lastError: string;
  lastScrape: string;
  lastScrapeDuration: number;
  health: string;
  scrapeInterval: string;
  scrapeTimeout: string;
};

// DroppedTarget 仅保留 relabel drop 前的 discoveredLabels 与所属 pool。
export interface DroppedTarget {
  discoveredLabels: Labels;
  scrapePool: string;
}

// TargetsResult 区分 activeTargets、droppedTargets 及各 pool 的 dropped 计数。
// Result type for /api/v1/targets endpoint.
// See: https://prometheus.io/docs/prometheus/latest/querying/api/#targets
export type TargetsResult = {
  activeTargets: Target[];
  droppedTargets: DroppedTarget[];
  droppedTargetCounts: Record<string, number>;
};
