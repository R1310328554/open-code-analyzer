package chunk

// json_helpers 为 jsoniter 注册 labels.Labels 与 model.Time 的高效编解码，绕过 Prometheus 默认经 map 的慢路径。

import (
	"unsafe"

	jsoniter "github.com/json-iterator/go"
	"github.com/prometheus/common/model"
	"github.com/prometheus/prometheus/model/labels"

	"github.com/grafana/loki/v3/pkg/util/labelpool"
)

// init 注册自定义 TypeDecoder/Encoder，覆盖 Prometheus 默认 labels 与 model.Time 处理。
func init() {
	jsoniter.RegisterTypeDecoderFunc("labels.Labels", decodeLabels)
	jsoniter.RegisterTypeEncoderFunc("labels.Labels", encodeLabels, labelsIsEmpty)
	jsoniter.RegisterTypeDecoderFunc("model.Time", decodeModelTime)
	jsoniter.RegisterTypeEncoderFunc("model.Time", encodeModelTime, modelTimeIsEmpty)
}

// decodeLabels 用 labelpool 构建器逐键值读取 JSON map，读完后 Sort 保证标签有序。
// Override Prometheus' labels.Labels decoder which goes via a map
func decodeLabels(ptr unsafe.Pointer, iter *jsoniter.Iterator) {
	builder := labelpool.Get()
	defer labelpool.Put(builder)

	iter.ReadMapCB(func(iter *jsoniter.Iterator, key string) bool {
		value := iter.ReadString()
		builder.Add(key, value)
		return true
	})

	// Labels are always sorted, but earlier Cortex using a map would
	// output in any order so we have to sort on read in
	builder.Sort()

	// Store result in the pointer.
	labelsPtr := (*labels.Labels)(ptr)
	*labelsPtr = builder.Labels()
}

// encodeLabels 直接遍历 labels.Range 写 JSON 对象，避免中间 map 分配。
// Override Prometheus' labels.Labels encoder which goes via a map
func encodeLabels(ptr unsafe.Pointer, stream *jsoniter.Stream) {
	labelsPtr := (*labels.Labels)(ptr)
	stream.WriteObjectStart()

	var i int
	labelsPtr.Range(func(v labels.Label) {
		if i != 0 {
			stream.WriteMore()
		}
		stream.WriteString(v.Name)
		stream.WriteRaw(`:`)
		stream.WriteString(v.Value)

		i++
	})

	stream.WriteObjectEnd()
}

func labelsIsEmpty(ptr unsafe.Pointer) bool {
	labelsPtr := (*labels.Labels)(ptr)
	return labelsPtr.IsEmpty()
}

// decodeModelTime 读 float64 毫秒时间戳并转为 model.Time（int64 纳秒精度）。
// Decode via jsoniter's float64 routine is faster than getting the string data and decoding as two integers
func decodeModelTime(ptr unsafe.Pointer, iter *jsoniter.Iterator) {
	pt := (*model.Time)(ptr)
	f := iter.ReadFloat64()
	*pt = model.Time(int64(f * 1000))
}

// encodeModelTime 将纳秒时间戳格式化为秒.毫秒字符串，比 float 编码约快 3 倍。
// Write out the timestamp as an int divided by 1000. This is ~3x faster than converting to a float.
// Adapted from https://github.com/prometheus/prometheus/blob/cc39021b2bb6f829c7a626e4bdce2f338d1b76db/web/api/v1/api.go#L829
func encodeModelTime(ptr unsafe.Pointer, stream *jsoniter.Stream) {
	pt := (*model.Time)(ptr)
	t := int64(*pt)
	if t < 0 {
		stream.WriteRaw(`-`)
		t = -t
	}
	stream.WriteInt64(t / 1000)
	fraction := t % 1000
	if fraction != 0 {
		stream.WriteRaw(`.`)
		if fraction < 100 {
			stream.WriteRaw(`0`)
		}
		if fraction < 10 {
			stream.WriteRaw(`0`)
		}
		stream.WriteInt64(fraction)
	}
}

func modelTimeIsEmpty(_ unsafe.Pointer) bool {
	return false
}
// labelsIsEmpty 与 modelTimeIsEmpty 供 jsoniter 判断零值；兼容旧 Cortex map 输出的乱序标签。
