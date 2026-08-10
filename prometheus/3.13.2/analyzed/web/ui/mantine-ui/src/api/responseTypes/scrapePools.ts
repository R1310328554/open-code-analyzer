// ScrapePoolsResult 列出配置中全部 scrape job 名称，供 targets 页筛选。
// Result type for /api/v1/scrape_pools endpoint.
export type ScrapePoolsResult = { scrapePools: string[] };
