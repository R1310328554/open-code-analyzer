package types //nolint:revive

// types 集中定义 Loki 支持的索引与 chunk 对象存储类型常量，并区分当前推荐、已弃用与仅测试用途的后端名称。

var SupportedIndexTypes = []string{
	BoltDBShipperType,
	TSDBType,
}

var DeprecatedIndexTypes = []string{
	StorageTypeAWS,
	StorageTypeAWSDynamo,
	StorageTypeBigTable,
	StorageTypeBigTableHashed,
	StorageTypeBoltDB,
	StorageTypeCassandra,
	StorageTypeGCP,
	StorageTypeGCPColumnKey,
	StorageTypeGrpc,
}

// SupportedStorageTypes 覆盖 filesystem 与各云厂商 object storage 驱动名。
var SupportedStorageTypes = []string{
	// local file system
	StorageTypeFileSystem,
	// remote object storages
	StorageTypeAWS,
	StorageTypeAlibabaCloud,
	StorageTypeAzure,
	StorageTypeBOS,
	StorageTypeCOS,
	StorageTypeGCS,
	StorageTypeS3,
	StorageTypeSwift,
	StorageTypeNoop,
}

var DeprecatedStorageTypes = []string{
	StorageTypeAWSDynamo,
	StorageTypeBigTable,
	StorageTypeBigTableHashed,
	StorageTypeCassandra,
	StorageTypeGCP,
	StorageTypeGCPColumnKey,
	StorageTypeGrpc,
}

var TestingStorageTypes = []string{
	StorageTypeInMemory,
}

// StorageType* 与 BoltDBShipperType/TSDBType 常量供 schema 与 CLI 校验引用。
const (
	StorageTypeAlibabaCloud   = "alibabacloud"
	StorageTypeAWS            = "aws"
	StorageTypeAWSDynamo      = "aws-dynamo"
	StorageTypeAzure          = "azure"
	StorageTypeBOS            = "bos"
	StorageTypeBoltDB         = "boltdb"
	StorageTypeCassandra      = "cassandra"
	StorageTypeInMemory       = "inmemory"
	StorageTypeBigTable       = "bigtable"
	StorageTypeBigTableHashed = "bigtable-hashed"
	StorageTypeFileSystem     = "filesystem"
	StorageTypeGCP            = "gcp"
	StorageTypeGCPColumnKey   = "gcp-columnkey"
	StorageTypeGCS            = "gcs"
	StorageTypeGrpc           = "grpc-store"
	StorageTypeLocal          = "local"
	StorageTypeS3             = "s3"
	StorageTypeSwift          = "swift"
	StorageTypeCOS            = "cos"
	StorageTypeNoop           = "noop"

	BoltDBShipperType = "boltdb-shipper"
	TSDBType          = "tsdb"
)
// TestingStorageTypes 仅含 inmemory，供单元测试快速构造 storage 栈。
