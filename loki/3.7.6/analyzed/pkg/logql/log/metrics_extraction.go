package log

// metrics_extraction 实现 metric 查询中的样本提取器：从日志行或标签值转换为 float64 时间序列样本并应用分组。

import (
	"context"
	"sort"
	"strconv"
	"time"

	"github.com/pkg/errors"
	"github.com/prometheus/prometheus/model/labels"

	"github.com/dustin/go-humanize"
)

const (
	ConvertBytes    = "bytes"
	ConvertDuration = "duration"
	ConvertFloat    = "float"
)

// LineExtractor 从单行字节切片提取标量；Count/Bytes 为内置实现。
// LineExtractor extracts a float64 from a log line.
type LineExtractor func([]byte) float64

var (
	CountExtractor LineExtractor = func(_ []byte) float64 { return 1. }
	BytesExtractor LineExtractor = func(line []byte) float64 { return float64(len(line)) }
)

// SampleExtractor 按流标签哈希缓存 StreamSampleExtractor，避免重复构建。
// SampleExtractor creates StreamSampleExtractor that can extract samples for a given log stream.
type SampleExtractor interface {
	ForStream(labels labels.Labels) StreamSampleExtractor
}

// StreamSampleExtractor 处理单条日志；保证不修改入参 line 切片。
// StreamSampleExtractor extracts samples for a log line.
// A StreamSampleExtractor never mutates the received line.
type StreamSampleExtractor interface {
	BaseLabels() LabelsResult
	Process(ts int64, line []byte, structuredMetadata labels.Labels) ([]ExtractedSample, bool)
	ProcessString(ts int64, line string, structuredMetadata labels.Labels) ([]ExtractedSample, bool)
	ReferencedStructuredMetadata() bool
}

// ExtractedSample 含 float64 值与分组后的 LabelsResult。
// ExtractedSample represents a single sample extracted from a log line,
// including its value and associated labels.
type ExtractedSample struct {
	Value  float64
	Labels LabelsResult
}

// SampleExtractorWrapper takes an extractor, wraps it is some desired functionality
// and returns a new pipeline
type SampleExtractorWrapper interface {
	Wrap(ctx context.Context, extractor SampleExtractor, query, tenant string) SampleExtractor
}

type lineSampleExtractor struct {
	Stage
	LineExtractor

	baseBuilder      *BaseLabelsBuilder
	streamExtractors map[uint64]StreamSampleExtractor
}

// NewLineSampleExtractor 先运行 pipeline stages 再调用 LineExtractor。
// NewLineSampleExtractor creates a SampleExtractor from a LineExtractor.
// Multiple log stages are run before converting the log line.
func NewLineSampleExtractor(ex LineExtractor, stages []Stage, groups []string, without, noLabels bool) (SampleExtractor, error) {
	s := ReduceStages(stages)
	hints := NewParserHint(s.RequiredLabelNames(), groups, without, noLabels, "", stages)
	return &lineSampleExtractor{
		Stage:            s,
		LineExtractor:    ex,
		baseBuilder:      NewBaseLabelsBuilderWithGrouping(groups, hints, without, noLabels),
		streamExtractors: make(map[uint64]StreamSampleExtractor),
	}, nil
}

func (l *lineSampleExtractor) ForStream(labels labels.Labels) StreamSampleExtractor {
	hash := l.baseBuilder.Hash(labels)
	if res, ok := l.streamExtractors[hash]; ok {
		return res
	}

	res := &streamLineSampleExtractor{
		Stage:         l.Stage,
		LineExtractor: l.LineExtractor,
		builder:       l.baseBuilder.ForLabels(labels, hash),
	}
	l.streamExtractors[hash] = res
	return res
}

type streamLineSampleExtractor struct {
	Stage
	LineExtractor
	builder *LabelsBuilder
}

func (l *streamLineSampleExtractor) ReferencedStructuredMetadata() bool {
	return l.builder.referencedStructuredMetadata
}

func (l *streamLineSampleExtractor) Process(ts int64, line []byte, structuredMetadata labels.Labels) ([]ExtractedSample, bool) {
	l.builder.Reset()
	l.builder.Add(StructuredMetadataLabel, structuredMetadata)

	// short circuit.
	if l.Stage == NoopStage {
		value := l.LineExtractor(line)
		labels := l.builder.GroupedLabels()
		return []ExtractedSample{{Value: value, Labels: labels}}, true
	}

	line, ok := l.Stage.Process(ts, line, l.builder)
	if !ok {
		return nil, false
	}

	value := l.LineExtractor(line)
	labels := l.builder.GroupedLabels()
	return []ExtractedSample{{Value: value, Labels: labels}}, true
}

func (l *streamLineSampleExtractor) ProcessString(ts int64, line string, structuredMetadata labels.Labels) ([]ExtractedSample, bool) {
	// unsafe get bytes since we have the guarantee that the line won't be mutated.
	return l.Process(ts, unsafeGetBytes(line), structuredMetadata)
}

func (l *streamLineSampleExtractor) BaseLabels() LabelsResult { return l.builder.currentResult }

type convertionFn func(value string) (float64, error)

type labelSampleExtractor struct {
	preStage     Stage
	postFilter   Stage
	labelName    string
	conversionFn convertionFn

	baseBuilder      *BaseLabelsBuilder
	streamExtractors map[uint64]StreamSampleExtractor
}

// LabelExtractorWithStages 从指定标签名读取字符串并经 bytes/duration/float 转换。
// LabelExtractorWithStages creates a SampleExtractor that will extract metrics from a labels.
// A set of log stage is executed before the conversion. A Filtering stage is executed after the conversion allowing
// to remove sample containing the __error__ label.
func LabelExtractorWithStages(
	labelName, conversion string,
	groups []string, without, noLabels bool,
	preStages []Stage,
	postFilter Stage,
) (SampleExtractor, error) {
	var convFn convertionFn
	switch conversion {
	case ConvertBytes:
		convFn = convertBytes
	case ConvertDuration:
		convFn = convertDuration
	case ConvertFloat:
		convFn = convertFloat
	default:
		return nil, errors.Errorf("unsupported conversion operation %s", conversion)
	}
	if len(groups) == 0 || without {
		without = true
		groups = append(groups, labelName)
		sort.Strings(groups)
	}
	preStage := ReduceStages(preStages)
	hints := NewParserHint(append(preStage.RequiredLabelNames(), postFilter.RequiredLabelNames()...), groups, without, noLabels, labelName, append(preStages, postFilter))
	return &labelSampleExtractor{
		preStage:         preStage,
		conversionFn:     convFn,
		labelName:        labelName,
		postFilter:       postFilter,
		baseBuilder:      NewBaseLabelsBuilderWithGrouping(groups, hints, without, noLabels),
		streamExtractors: make(map[uint64]StreamSampleExtractor),
	}, nil
}

type streamLabelSampleExtractor struct {
	*labelSampleExtractor
	builder *LabelsBuilder
}

func (l *labelSampleExtractor) ReferencedStructuredMetadata() bool {
	return l.baseBuilder.referencedStructuredMetadata
}

func (l *labelSampleExtractor) ForStream(labels labels.Labels) StreamSampleExtractor {
	hash := l.baseBuilder.Hash(labels)
	if res, ok := l.streamExtractors[hash]; ok {
		return res
	}

	res := &streamLabelSampleExtractor{
		labelSampleExtractor: l,
		builder:              l.baseBuilder.ForLabels(labels, hash),
	}
	l.streamExtractors[hash] = res
	return res
}

func (l *streamLabelSampleExtractor) Process(ts int64, line []byte, structuredMetadata labels.Labels) ([]ExtractedSample, bool) {
	// Apply the pipeline first.
	l.builder.Reset()
	l.builder.Add(StructuredMetadataLabel, structuredMetadata)
	line, ok := l.preStage.Process(ts, line, l.builder)
	if !ok {
		return nil, false
	}
	// convert the label value.
	var v float64
	stringValue, _ := l.builder.Get(l.labelName)
	if stringValue == "" {
		// NOTE: It's totally fine for log line to not have this particular label.
		// See Issue: https://github.com/grafana/loki/issues/6713
		return nil, false
	}

	var err error
	v, err = l.conversionFn(stringValue)
	if err != nil {
		l.builder.SetErr(errSampleExtraction)
		l.builder.SetErrorDetails(err.Error())
	}

	// post filters
	if _, ok = l.postFilter.Process(ts, line, l.builder); !ok {
		return nil, false
	}
	return []ExtractedSample{{Value: v, Labels: l.builder.GroupedLabels()}}, true
}

func (l *streamLabelSampleExtractor) ProcessString(ts int64, line string, structuredMetadata labels.Labels) ([]ExtractedSample, bool) {
	// unsafe get bytes since we have the guarantee that the line won't be mutated.
	return l.Process(ts, unsafeGetBytes(line), structuredMetadata)
}

func (l *streamLabelSampleExtractor) BaseLabels() LabelsResult { return l.builder.currentResult }

// NewFilteringSampleExtractor 在提取前按 PipelineFilter 时间窗过滤日志行。
// NewFilteringSampleExtractor creates a sample extractor where entries from
// the underlying log stream are filtered by pipeline filters before being
// passed to extract samples. Filters are always upstream of the extractor.
func NewFilteringSampleExtractor(f []PipelineFilter, e SampleExtractor) SampleExtractor {
	return &filteringSampleExtractor{
		filters:   f,
		extractor: e,
	}
}

type filteringSampleExtractor struct {
	filters   []PipelineFilter
	extractor SampleExtractor
}

func (p *filteringSampleExtractor) ForStream(labels labels.Labels) StreamSampleExtractor {
	var streamFilters []streamFilter
	for _, f := range p.filters {
		if allMatch(f.Matchers, labels) {
			streamFilters = append(streamFilters, streamFilter{
				start:    f.Start,
				end:      f.End,
				pipeline: f.Pipeline.ForStream(labels),
			})
		}
	}

	return &filteringStreamExtractor{
		filters:   streamFilters,
		extractor: p.extractor.ForStream(labels),
	}
}

type filteringStreamExtractor struct {
	filters   []streamFilter
	extractor StreamSampleExtractor
}

func (sp *filteringStreamExtractor) ReferencedStructuredMetadata() bool {
	return false
}

func (sp *filteringStreamExtractor) BaseLabels() LabelsResult {
	return sp.extractor.BaseLabels()
}

func (sp *filteringStreamExtractor) Process(ts int64, line []byte, structuredMetadata labels.Labels) ([]ExtractedSample, bool) {
	for _, filter := range sp.filters {
		if ts < filter.start || ts > filter.end {
			continue
		}

		_, _, matches := filter.pipeline.Process(ts, line, structuredMetadata)
		if matches { // When the filter matches, don't run the next step
			return nil, false
		}
	}

	return sp.extractor.Process(ts, line, structuredMetadata)
}

func (sp *filteringStreamExtractor) ProcessString(ts int64, line string, structuredMetadata labels.Labels) ([]ExtractedSample, bool) {
	for _, filter := range sp.filters {
		if ts < filter.start || ts > filter.end {
			continue
		}

		_, _, matches := filter.pipeline.ProcessString(ts, line, structuredMetadata)
		if matches { // When the filter matches, don't run the next step
			return nil, false
		}
	}

	return sp.extractor.ProcessString(ts, line, structuredMetadata)
}

// convertFloat 使用 strconv.ParseFloat 解析标签字符串。
func convertFloat(v string) (float64, error) {
	return strconv.ParseFloat(v, 64)
}

func convertDuration(v string) (float64, error) {
	d, err := time.ParseDuration(v)
	if err != nil {
		return 0, err
	}
	return d.Seconds(), nil
}

// convertBytes 经 humanize.ParseBytes 解析如 1KiB 的人类可读字节量。
func convertBytes(v string) (float64, error) {
	b, err := humanize.ParseBytes(v)
	if err != nil {
		return 0, err
	}
	return float64(b), nil
}
// 标签缺失或转换失败时跳过样本；__error__ 由 postFilter stage 过滤。
