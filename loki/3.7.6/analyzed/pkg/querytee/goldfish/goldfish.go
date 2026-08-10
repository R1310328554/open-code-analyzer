// goldfish 包为 querytee 提供查询采样与双后端 A/B 比较能力。
// Package goldfish provides query sampling and comparison functionality for the querytee tool.
// 通过对采样查询比较响应哈希、性能指标并持久化结果支持跨 cell 验证。
// It enables A/B testing between two query backends (cells) by sampling queries, comparing
// their responses (including performance metrics and content hashes), and persisting results
// for analysis.
package goldfish
// 与 pkg/goldfish 共享 QuerySample/ComparisonResult 等核心类型定义。
