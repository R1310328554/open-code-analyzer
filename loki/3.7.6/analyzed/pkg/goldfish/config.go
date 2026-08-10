package goldfish

// goldfish config 定义查询采样对比结果的持久化后端：支持 Cloud SQL 代理、RDS、直连 MySQL 或空类型禁用存储。

// StorageConfig 通过 yaml type 选择后端；各后端密码统一由 GOLDFISH_DB_PASSWORD 环境变量注入。
// StorageConfig defines storage backend configuration
type StorageConfig struct {
	Type string `yaml:"type"` // "cloudsql", "rds", "mysql", or empty string for no storage

	// Direct MySQL connection
// MySQL* 字段配置直连 MySQL 主机、端口、库名与用户。
	MySQLHost     string `yaml:"mysql_host"`
	MySQLPort     int    `yaml:"mysql_port"`
	MySQLDatabase string `yaml:"mysql_database"`
	MySQLUser     string `yaml:"mysql_user"`
	// MySQLPassword provided via GOLDFISH_DB_PASSWORD environment variable

	// CloudSQL specific (via proxy)
// CloudSQL* 字段经 Cloud SQL Auth Proxy 连接托管实例。
	CloudSQLHost     string `yaml:"cloudsql_host"`
	CloudSQLPort     int    `yaml:"cloudsql_port"`
	CloudSQLDatabase string `yaml:"cloudsql_database"`
	CloudSQLUser     string `yaml:"cloudsql_user"`
	// CloudSQLPassword provided via GOLDFISH_DB_PASSWORD environment variable

	// RDS specific
// RDS* 字段指向 AWS RDS 端点、数据库与用户。
	RDSEndpoint string `yaml:"rds_endpoint"` // e.g., "mydb.123456789012.us-east-1.rds.amazonaws.com:3306"
	RDSDatabase string `yaml:"rds_database"`
	RDSUser     string `yaml:"rds_user"`
	// RDSPassword provided via GOLDFISH_DB_PASSWORD environment variable

	// Common settings
// MaxConnections 与 MaxIdleTime 控制连接池大小与空闲连接最大存活秒数。
	MaxConnections int `yaml:"max_connections"`
	MaxIdleTime    int `yaml:"max_idle_time_seconds"`
}
// type 为空字符串时表示 Goldfish 不持久化采样数据，仅内存或 noop。
