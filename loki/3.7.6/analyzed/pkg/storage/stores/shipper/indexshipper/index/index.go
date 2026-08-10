package index

// index 定义 shipper 索引文件抽象 Index 与 OpenIndexFileFunc、ForEachIndexCallback 类型，供 boltdb/tsdb 等后端统一上传与查询接口。

import "io"

type Index interface {
	Name() string
	Path() string
	Close() error
	Reader() (io.ReadSeeker, error)
}

// OpenIndexFileFunc 须优雅处理 abrupt shutdown 导致的损坏文件并返回可读 Index。
// OpenIndexFileFunc opens an index file stored at the given path.
// There is a possibility of files being corrupted due to abrupt shutdown so
// the implementation should take care of gracefully handling failures in opening corrupted files.
type OpenIndexFileFunc func(string) (Index, error)
// ForEachIndexCallback 的 isMultiTenantIndex 表示当前文件是否为多租户公共索引。
type ForEachIndexCallback func(isMultiTenantIndex bool, idx Index) error
// 各存储后端通过实现 Index 与注入 OpenIndexFileFunc 接入 indexshipper 生命周期管理。
