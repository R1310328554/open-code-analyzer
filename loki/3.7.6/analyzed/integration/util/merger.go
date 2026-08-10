package util //nolint:revive

// 集成测试 YAML 片段合并器：按添加顺序将多段 YAML 深度合并为单一文档，
// 后段覆盖前段同键值，slice 追加，供 Cluster 动态拼装 Loki 配置。

import (
	"fmt"

	"dario.cat/mergo"
	"gopkg.in/yaml.v2"
)

// YAMLMerger 收集 []byte 片段，Merge 时经 mergo 合并 map 再 Marshal 输出。
// YAMLMerger takes a set of given YAML fragments and merges them into a single YAML document.
// The order in which these fragments is supplied is maintained, so subsequent fragments will override preceding ones.
type YAMLMerger struct {
	fragments [][]byte
}

func NewYAMLMerger() *YAMLMerger {
	return &YAMLMerger{}
}

func (m *YAMLMerger) AddFragment(fragment []byte) {
	m.fragments = append(m.fragments, fragment)
}

// Merge 逐片段 Unmarshal 为 map，WithOverride 与 WithAppendSlice 控制合并语义。
func (m *YAMLMerger) Merge() ([]byte, error) {
	merged := make(map[interface{}]interface{})
	for _, fragment := range m.fragments {
		fragmentMap, err := yamlToMap(fragment)
		if err != nil {
			return nil, fmt.Errorf("failed to unmarshal given fragment %q to map: %w", fragment, err)
		}

		if err = mergo.Merge(&merged, fragmentMap, mergo.WithOverride, mergo.WithTypeCheck, mergo.WithAppendSlice); err != nil {
			return nil, fmt.Errorf("failed to merge fragment %q with base: %w", fragment, err)
		}
	}

	mergedYAML, err := yaml.Marshal(merged)
	if err != nil {
		return nil, err
	}

	return mergedYAML, nil
}

// yamlToMap 将单段 YAML 反序列化为 map[interface{}]interface{} 供 mergo 使用。
func yamlToMap(fragment []byte) (interface{}, error) {
	var fragmentMap map[interface{}]interface{}

	err := yaml.Unmarshal(fragment, &fragmentMap)
	if err != nil {
		return nil, err
	}

	return fragmentMap, nil
}
