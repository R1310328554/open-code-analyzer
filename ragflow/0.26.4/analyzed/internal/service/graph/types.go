package graph

// types.go 定义知识图谱检索流水线使用的核心数据结构。

// KGEntity 表示知识图谱中的一个实体节点。
type KGEntity struct {
	Name        string       // entity_kwd
	Type        string       // entity_type_kwd
	PageRank    float64      // rank_flt
	Similarity  float64      // _score
	Description string       // content_with_weight
	NhopEnts    []NhopEntity // n_hop_with_weight (parsed JSON)
}

// NhopEntity 表示 N 跳邻居路径及每跳 PageRank 权重。
type NhopEntity struct {
	Path    []string  // entity names along the path
	Weights []float64 // pagerank weights per hop
}

// KGRelation 表示两实体间的有向关系及描述。
type KGRelation struct {
	From        string  // from_entity_kwd
	To          string  // to_entity_kwd
	Description string  // content_with_weight
	Sim         float64 // score accumulated during pipeline scoring
	PageRank    float64 // rank_flt or weight_int as float64
}

// Edge 有向边：(from_entity, to_entity)。
type Edge struct {
	From, To string
}

// EdgeScore N 跳分析为边累积的相似度与 PageRank。
type EdgeScore struct {
	Sim      float64
	PageRank float64
}

// ScoredEntity 排序输出用的带分实体。
type ScoredEntity struct {
	Entity      string
	Score       float64
	Description string
}

// ScoredRelation 排序输出用的带分关系。
type ScoredRelation struct {
	From        string
	To          string
	Score       float64
	Description string
}

// KGCommunityReport 社区报告摘要（标题、正文、权重、关联实体）。
type KGCommunityReport struct {
	Title    string  // docnm_kwd
	Content  string  // content_with_weight
	Weight   float64 // weight_flt
	Entities string  // entities_kwd
}
// graph/types.go — 知识图谱领域类型：实体、关系、边、社区报告及评分结构体。
