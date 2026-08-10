package cluster

// 集成测试 schema 配置模板：提供 boltdb-shipper 与 tsdb shipper 的 YAML 片段，
// 支持单/双 period 组合，供 Cluster 渲染不同索引后端与 object store 场景。

var (
	boltDBShipperSchemaConfigTemplate = `
schema_config:
  configs:
    - from: {{.curPeriodStart}}
      store: boltdb-shipper
      object_store: filesystem
      schema: {{.schemaVer}}
      index:
        prefix: index_
        period: 24h
`
	additionalBoltDBShipperSchemaConfigTemplate = `
schema_config:
  configs:
    - from: {{.additionalPeriodStart}}
      store: boltdb-shipper
      object_store: store-1
      schema: {{.schemaVer}}
      index:
        prefix: index_
        period: 24h
`

	tsdbShipperSchemaConfigTemplate = `
schema_config:
  configs:
    - from: {{.curPeriodStart}}
      store: tsdb
      object_store: filesystem
      schema: {{.schemaVer}}
      index:
        prefix: index_
        period: 24h
`
	additionalTSDBShipperSchemaConfigTemplate = `
schema_config:
  configs:
    - from: {{.additionalPeriodStart}}
      store: tsdb
      object_store: store-1
      schema: {{.schemaVer}}
      index:
        prefix: index_tsdb_
        period: 24h
`
)

// SchemaWithTSDB 追加额外 TSDB period 配置，filesystem 与 store-1 双 object store。
func SchemaWithTSDB(c *Cluster) {
	c.periodCfgs = append(c.periodCfgs, additionalTSDBShipperSchemaConfigTemplate)
}

func SchemaWithBoltDBAndBoltDB(c *Cluster) {
	c.periodCfgs = append(c.periodCfgs, additionalBoltDBShipperSchemaConfigTemplate, boltDBShipperSchemaConfigTemplate)
}

// SchemaWithTSDBAndTSDB 使用双 TSDB period，第二段 index 前缀为 index_tsdb_。
func SchemaWithTSDBAndTSDB(c *Cluster) {
	c.periodCfgs = append(c.periodCfgs, additionalTSDBShipperSchemaConfigTemplate, tsdbShipperSchemaConfigTemplate)
}

func SchemaWithBoltDBAndTSDB(c *Cluster) {
	c.periodCfgs = append(c.periodCfgs, additionalBoltDBShipperSchemaConfigTemplate, tsdbShipperSchemaConfigTemplate)
}
