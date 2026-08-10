// Package arrowagg 提供 Apache Arrow 数据结构聚合工具。
// 包含 schema 哈希、字段映射以及多 RecordBatch 合并为统一 schema 的能力。
package arrowagg
// Records 与 Mapper 被 executor 批处理与 schema 对齐路径复用。
