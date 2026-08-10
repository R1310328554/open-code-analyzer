package log

// consolidated_variant_extractor 将公共流水线与多个变体采样提取器合并，为每条日志行附加 variant 标签区分来源分支。

import (
	"strconv"

	"github.com/prometheus/prometheus/model/labels"

	"github.com/grafana/loki/v3/pkg/util/constants"
)

func NewConsolidatedMultiVariantExtractor(commonPipeline Pipeline, variants []SampleExtractor) SampleExtractor {
	return &consolidatedMultiVariantExtractor{
		commonPipeline: commonPipeline,
		variants:       variants,
	}
}

// consolidatedMultiVariantExtractor 持有公共 Pipeline 与各变体 SampleExtractor 列表。
type consolidatedMultiVariantExtractor struct {
	commonPipeline Pipeline
	variants       []SampleExtractor
}

func (c *consolidatedMultiVariantExtractor) ForStream(labels labels.Labels) StreamSampleExtractor {
	return &consolidatedMultiVariantStreamExtractor{
		commonPipeline: c.commonPipeline.ForStream(labels),
		variants:       c.variants,
	}
}

type consolidatedMultiVariantStreamExtractor struct {
	commonPipeline               StreamPipeline
	variants                     []SampleExtractor
	referencedStructuredMetadata bool
}

func (c *consolidatedMultiVariantStreamExtractor) BaseLabels() LabelsResult {
	return c.commonPipeline.BaseLabels()
}

// Process 先经公共流水线过滤/解析，再逐变体提取样本并打上 variant 索引标签。
func (c *consolidatedMultiVariantStreamExtractor) Process(ts int64, line []byte, structuredMetadata labels.Labels) ([]ExtractedSample, bool) {
	// Process the line through the common pipeline
	processedLine, commonLabels, ok := c.commonPipeline.Process(ts, line, structuredMetadata)
	if !ok {
		// If the common pipeline filters out the line, no need to process further
		return nil, false
	}

	var commonStructuredMetadata labels.Labels
	if commonLabels != nil {
		commonStructuredMetadata = commonLabels.StructuredMetadata()
	}

	var allSamples []ExtractedSample
	anyExtracted := false

	// Pass the processed line to each variant-specific extractor
	lbls := commonLabels.Labels()
	for i, variant := range c.variants {
		streamVariantExtractor := variant.ForStream(lbls)
		samples, ok := streamVariantExtractor.Process(ts, processedLine, commonStructuredMetadata)
		if ok {
			for _, sample := range samples {
				sample.Labels = appendVariantLabel(sample.Labels, i)
				allSamples = append(allSamples, sample)
			}

			anyExtracted = true
			c.referencedStructuredMetadata = c.referencedStructuredMetadata || streamVariantExtractor.ReferencedStructuredMetadata()
		}
	}

	// Return all the collected samples
	return allSamples, anyExtracted
}

// appendVariantLabel 在流标签上追加 __variant__ 标签，值为变体在列表中的索引。
func appendVariantLabel(lbls LabelsResult, variantIndex int) LabelsResult {
	newLblsBuilder := labels.NewScratchBuilder(lbls.Stream().Len() + 1)

	lbls.Stream().Range(func(l labels.Label) {
		newLblsBuilder.Add(l.Name, l.Value)
	})

	newLblsBuilder.Add(constants.VariantLabel, strconv.Itoa(variantIndex))
	newLblsBuilder.Sort()
	newLbls := newLblsBuilder.Labels()

	builder := NewBaseLabelsBuilder().ForLabels(newLbls, labels.StableHash(newLbls))
	builder.Add(StructuredMetadataLabel, lbls.StructuredMetadata())
	builder.Add(ParsedLabel, lbls.Parsed())
	return builder.LabelsResult()
}

func (c *consolidatedMultiVariantStreamExtractor) ProcessString(ts int64, line string, structuredMetadata labels.Labels) ([]ExtractedSample, bool) {
	return c.Process(ts, unsafeGetBytes(line), structuredMetadata)
}

func (c *consolidatedMultiVariantStreamExtractor) ReferencedStructuredMetadata() bool {
	// Check if common pipeline references structured metadata
	if c.commonPipeline.ReferencedStructuredMetadata() {
		return true
	}

	return c.referencedStructuredMetadata
}
// ReferencedStructuredMetadata 聚合公共与各变体提取器是否引用结构化元数据的标志。
