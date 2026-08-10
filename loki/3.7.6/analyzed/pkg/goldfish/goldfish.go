// Package goldfish 为 querytee 与 Goldfish UI 提供 A/B 查询采样、响应对比（含性能与内容哈希）及 SQL 持久化/检索。
// Package goldfish provides query sampling and comparison functionality for the querytee and Goldfish UI.
// It enables A/B testing between two query backends (cells) by sampling queries, comparing
// their responses (including performance metrics and content hashes), and persisting results
// for analysis. This package provides the data structures and SQL queries to persist and retrieve the results
// of this analysis.
package goldfish
// 双 cell 对比结果写入 Storage，供 UI 分页浏览与统计分析。
