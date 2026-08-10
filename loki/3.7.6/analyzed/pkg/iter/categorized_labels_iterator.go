package iter

// categorizeLabelsIterator 从流标签中剥离 structured metadata 与 parsed 标签，使 StreamHash/Labels 反映基础流身份而非解析后的衍生标签。

import (
	"fmt"

	"github.com/prometheus/prometheus/model/labels"

	"github.com/grafana/loki/v3/pkg/logql/syntax"
)

type categorizeLabelsIterator struct {
	EntryIterator

	currStreamLabels string
	currHash         uint64
	currErr          error
}

// NewCategorizeLabelsIterator 构造装饰器，无额外元数据时直接透传底层标签。
func NewCategorizeLabelsIterator(wrap EntryIterator) EntryIterator {
	return &categorizeLabelsIterator{
		EntryIterator: wrap,
	}
}

func (c *categorizeLabelsIterator) Next() bool {
	if !c.EntryIterator.Next() {
		return false
	}

	currEntry := c.At()
	if len(currEntry.StructuredMetadata) == 0 && len(currEntry.Parsed) == 0 {
		c.currStreamLabels = c.EntryIterator.Labels()
		c.currHash = c.EntryIterator.StreamHash()
		return true
	}

// 从 builder 删除 structured metadata 与 parsed 标签名，再 StableHash 得到分类后流 hash。
	// We need to remove the structured metadata labels and parsed labels from the stream labels.
	streamLabels := c.EntryIterator.Labels()
	lbls, err := syntax.ParseLabels(streamLabels)
	if err != nil {
		c.currErr = fmt.Errorf("failed to parse series labels to categorize labels: %w", err)
		return false
	}

	builder := labels.NewBuilder(lbls)
	for _, label := range currEntry.StructuredMetadata {
		builder.Del(label.Name)
	}
	for _, label := range currEntry.Parsed {
		builder.Del(label.Name)
	}

	newLabels := builder.Labels()
	c.currStreamLabels = newLabels.String()
	c.currHash = labels.StableHash(newLabels)

	return true
}

func (c *categorizeLabelsIterator) Err() error {
	return c.currErr
}

func (c *categorizeLabelsIterator) Labels() string {
	return c.currStreamLabels
}

func (c *categorizeLabelsIterator) StreamHash() uint64 {
	return c.currHash
}
// 分类标签迭代器保证 dedupe 与 merge 按原始流维度而非解析标签维度聚合。
