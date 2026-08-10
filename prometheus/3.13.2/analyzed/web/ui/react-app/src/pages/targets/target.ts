// Targets 页数据模型与工具：描述 scrape 目标结构、按 pool 分组及健康状态过滤/着色。

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

// DroppedTarget 仅保留 relabel 丢弃前的 discoveredLabels，供 dropped targets 面板展示。
export interface DroppedTarget {
  discoveredLabels: Labels;
}

export interface ScrapePool {
  upCount: number;
  targets: Target[];
}

export interface ScrapePools {
  [scrapePool: string]: ScrapePool;
}

// groupTargets 将 flat activeTargets 按 scrapePool 聚合，并累计 health=up 的数量。
export const groupTargets = (targets: Target[]): ScrapePools =>
  targets.reduce((pools: ScrapePools, target: Target) => {
    const { health, scrapePool } = target;
    const up = health.toLowerCase() === 'up' ? 1 : 0;
    if (!pools[scrapePool]) {
      pools[scrapePool] = {
        upCount: 0,
        targets: [],
      };
    }
    pools[scrapePool].targets.push(target);
    pools[scrapePool].upCount += up;
    return pools;
  }, {});

// getColor 将 health 映射为 success/danger/warning，供 Reactstrap Badge 着色。
export const getColor = (health: string): string => {
  switch (health.toLowerCase()) {
    case 'up':
      return 'success';
    case 'down':
      return 'danger';
    default:
      return 'warning';
  }
};

export interface TargetHealthFilters {
  healthy: boolean;
  unhealthy: boolean;
  unknown: boolean;
}

// filterTargetsByHealth 根据 Targets 页健康状态复选框决定是否显示该目标。
export const filterTargetsByHealth = (health: string, filters: TargetHealthFilters): boolean => {
  switch (health.toLowerCase()) {
    case 'up':
      return filters.healthy;
    case 'down':
      return filters.unhealthy;
    default:
      return filters.unknown;
  }
};
// Targets 模块类型与纯函数供旧版 React UI targets 页面复用。
