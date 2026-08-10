package logproto

// timeseries 通过 sync.Pool 预分配 WriteRequest/TimeSeries 切片，降低 gRPC 反序列化高频路径上的堆分配压力。

import (
	"flag"
	"sync"
)

var (
	expectedTimeseries       = 100
	expectedLabels           = 20
	expectedSamplesPerSeries = 10

	/*
		We cannot pool these as pointer-to-slice because the place we use them is in WriteRequest which is generated from Protobuf
		and we don't have an option to make it a pointer. There is overhead here 24 bytes of garbage every time a PreallocTimeseries
		is re-used. But since the slices are far far larger, we come out ahead.
	*/
	slicePool = sync.Pool{
		New: func() interface{} {
			return make([]PreallocTimeseries, 0, expectedTimeseries)
		},
	}

	timeSeriesPool = sync.Pool{
		New: func() interface{} {
			return &TimeSeries{
				Labels:  make([]LabelAdapter, 0, expectedLabels),
				Samples: make([]LegacySample, 0, expectedSamplesPerSeries),
			}
		},
	}
)

// PreallocConfig 暴露命令行 flag，配置预期的时序数、标签数与每序列样本数。
// PreallocConfig configures how structures will be preallocated to optimise
// proto unmarshalling.
type PreallocConfig struct{}

// RegisterFlags registers configuration settings.
func (PreallocConfig) RegisterFlags(f *flag.FlagSet) {
	f.IntVar(&expectedTimeseries, "ingester-client.expected-timeseries", expectedTimeseries, "Expected number of timeseries per request, used for preallocations.")
	f.IntVar(&expectedLabels, "ingester-client.expected-labels", expectedLabels, "Expected number of labels per timeseries, used for preallocations.")
	f.IntVar(&expectedSamplesPerSeries, "ingester-client.expected-samples-per-series", expectedSamplesPerSeries, "Expected number of samples per timeseries, used for preallocations.")
}

// PreallocWriteRequest 在 Unmarshal 时从池中获取 Timeseries 切片。
// PreallocWriteRequest is a WriteRequest which preallocs slices on Unmarshal.
type PreallocWriteRequest struct {
	WriteRequest
}

// Unmarshal implements proto.Message.
func (p *PreallocWriteRequest) Unmarshal(dAtA []byte) error {
	p.Timeseries = PreallocTimeseriesSliceFromPool()
	return p.WriteRequest.Unmarshal(dAtA)
}

// PreallocTimeseries 包装 *TimeSeries，反序列化前从 timeSeriesPool 取对象。
// PreallocTimeseries is a TimeSeries which preallocs slices on Unmarshal.
type PreallocTimeseries struct {
	*TimeSeries
}

// Unmarshal implements proto.Message.
func (p *PreallocTimeseries) Unmarshal(dAtA []byte) error {
	p.TimeSeries = TimeseriesFromPool()
	return p.TimeSeries.Unmarshal(dAtA)
}

// PreallocTimeseriesSliceFromPool 从 slicePool 获取 []PreallocTimeseries，用完须 ReuseSlice。
// PreallocTimeseriesSliceFromPool retrieves a slice of PreallocTimeseries from a sync.Pool.
// ReuseSlice should be called once done.
func PreallocTimeseriesSliceFromPool() []PreallocTimeseries {
	return slicePool.Get().([]PreallocTimeseries)
}

// ReuseSlice 逐条 ReuseTimeseries 后将切片截断为 [:0] 归还 slicePool。
// ReuseSlice puts the slice back into a sync.Pool for reuse.
func ReuseSlice(ts []PreallocTimeseries) {
	for i := range ts {
		ReuseTimeseries(ts[i].TimeSeries)
	}

	slicePool.Put(ts[:0]) //nolint:staticcheck //see comment on slicePool for more details
}

// TimeseriesFromPool 从 timeSeriesPool 借出 TimeSeries，Labels/Samples 已预扩容。
// TimeseriesFromPool retrieves a pointer to a TimeSeries from a sync.Pool.
// ReuseTimeseries should be called once done, unless ReuseSlice was called on the slice that contains this TimeSeries.
func TimeseriesFromPool() *TimeSeries {
	return timeSeriesPool.Get().(*TimeSeries)
}

// ReuseTimeseries 清空标签字符串引用（避免持有 gRPC 缓冲）后归还池。
// ReuseTimeseries puts the timeseries back into a sync.Pool for reuse.
func ReuseTimeseries(ts *TimeSeries) {
	// Name and Value may point into a large gRPC buffer, so clear the reference to allow GC
	for i := 0; i < len(ts.Labels); i++ {
		ts.Labels[i].Name = ""
		ts.Labels[i].Value = ""
	}
	ts.Labels = ts.Labels[:0]
	ts.Samples = ts.Samples[:0]
	timeSeriesPool.Put(ts)
}
// RegisterFlags 将 expected-timeseries 等 tuning 参数注册到 ingester-client flag 组。
