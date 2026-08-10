package stages

// structured_metadata 阶段：将 extracted/labels 字段写入结构化元数据。
// 写入后若标签与元数据重复则从 Labels 中移除以降低索引基数。

import (
	"github.com/go-kit/log"
	"github.com/mitchellh/mapstructure"
	"github.com/prometheus/common/model"

	"github.com/grafana/loki/v3/pkg/logproto"
)

func newStructuredMetadataStage(params StageCreationParams) (Stage, error) {
	cfgs := &LabelsConfig{}
	err := mapstructure.Decode(params.config, cfgs)
	if err != nil {
		return nil, err
	}
	err = validateLabelsConfig(*cfgs)
	if err != nil {
		return nil, err
	}
	return &structuredMetadataStage{
		cfgs:   *cfgs,
		logger: params.logger,
	}, nil
}

// structured_metadata 阶段：LabelsConfig 与 logger。
type structuredMetadataStage struct {
	cfgs   LabelsConfig
	logger log.Logger
}

func (s *structuredMetadataStage) Name() string {
	return StageTypeStructuredMetadata
}

// Cleanup implements Stage.
func (*structuredMetadataStage) Cleanup() {
	// no-op
}

// 从 extracted 与 labels 填充 StructuredMetadata 并清理重复标签。
func (s *structuredMetadataStage) Run(in chan Entry) chan Entry {
	return RunWith(in, func(e Entry) Entry {
		structuredMetadata := make(map[model.LabelName]model.LabelValue)

		processLabelsConfigs(s.logger, e.Extracted, s.cfgs, func(source, labelName model.LabelName, labelValue model.LabelValue) {
			e.StructuredMetadata = append(e.StructuredMetadata, logproto.LabelAdapter{Name: string(labelName), Value: string(labelValue)})
			structuredMetadata[source] = labelValue
		})

		for lName, lSrc := range s.cfgs {
			source := model.LabelName(*lSrc)
			if _, ok := structuredMetadata[source]; ok {
				continue
			}
			if lValue, ok := e.Labels[source]; ok {
				e.StructuredMetadata = append(e.StructuredMetadata, logproto.LabelAdapter{Name: lName, Value: string(lValue)})
				structuredMetadata[source] = lValue
			}
		}

		// Remove labels which are already added as structure metadata
		for lName, lValue := range structuredMetadata {
			if e.Labels[lName] != lValue {
				continue
			}
			delete(e.Labels, lName)
		}
		return e
	})
}
