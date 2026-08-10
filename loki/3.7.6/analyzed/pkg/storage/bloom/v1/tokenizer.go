package v1

// tokenizer 将结构化元数据标签拆成 Bloom 可索引的 token 序列：对 name、name=value 及带 chunk 前缀的变体各生成条目供过滤器测试。

import (
	"fmt"

	iter "github.com/grafana/loki/v3/pkg/iter/v2"

	"github.com/grafana/loki/pkg/push"
)

type StructuredMetadataTokenizer struct {
	// prefix to add to tokens, typically the encoded chunkref
	prefix string
	tokens []string
}

// NewStructuredMetadataTokenizer 预分配容量 6 的 tokens 切片以减少 append 分配。
func NewStructuredMetadataTokenizer(prefix string) *StructuredMetadataTokenizer {
	return &StructuredMetadataTokenizer{
		prefix: prefix,
		tokens: make([]string, 6),
	}
}

// Tokens 为单个 kv 生成四条裸 token 与四条 prefix+token，返回 SliceIter。
func (t *StructuredMetadataTokenizer) Tokens(kv push.LabelAdapter) iter.Iterator[string] {
	combined := fmt.Sprintf("%s=%s", kv.Name, kv.Value)
	t.tokens = append(t.tokens[:0],
		kv.Name, t.prefix+kv.Name,
		combined, t.prefix+combined,
	)
	return iter.NewSliceIter(t.tokens)
}
// combined 格式为 name=value，prefix 前缀使同一 chunk 内 token 在 bloom 中可区分。
