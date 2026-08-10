package loghttp

// Entry 表示单条日志：时间戳、正文、结构化元数据与 parser 解析出的标签。

import (
	"fmt"
	"strconv"
	"time"
	"unsafe"

	"github.com/grafana/jsonparser"
	jsoniter "github.com/json-iterator/go"
	"github.com/modern-go/reflect2"
	"github.com/prometheus/prometheus/model/labels"
)

// init 注册 jsoniter 扩展，使 []Entry 与 Entry 使用紧凑数组 JSON 格式。
func init() {
	jsoniter.RegisterExtension(&jsonExtension{})
}

// Entry 是 loghttp 流式结果中最小日志单元，支持自定义 JSON 编解码。
// Entry represents a log entry.  It includes a log message and the time it occurred at.
type Entry struct {
	Timestamp          time.Time
	Line               string
	StructuredMetadata labels.Labels
	Parsed             labels.Labels
}

func (e *Entry) UnmarshalJSON(data []byte) error {
	var (
		i          int
		parseError error
	)
	_, err := jsonparser.ArrayEach(data, func(value []byte, t jsonparser.ValueType, _ int, _ error) {
		// assert that both items in array are of type string
		switch i {
		case 0: // timestamp
			if t != jsonparser.String {
				parseError = jsonparser.MalformedStringError
				return
			}
			ts, err := jsonparser.ParseInt(value)
			if err != nil {
				parseError = err
				return
			}
			e.Timestamp = time.Unix(0, ts)
		case 1: // value
			if t != jsonparser.String {
				parseError = jsonparser.MalformedStringError
				return
			}
			v, err := jsonparser.ParseString(value)
			if err != nil {
				parseError = err
				return
			}
			e.Line = v
		case 2: // structured metadata
			if t != jsonparser.Object {
				parseError = jsonparser.MalformedObjectError
				return
			}

// UnmarshalJSON 兼容 push 三元素数组与 query 响应中带 structuredMetadata/parsed 的对象。
			// Here we deserialize entries for both query responses and push requests.
			//
			// For push requests, we accept structured metadata as the third object in the entry array. E.g.:
			// [ "<ts>", "<log line>", {"trace_id": "0242ac120002", "user_id": "superUser123"}]
			//
			// For query responses, we accept structured metadata and parsed labels in the third object in the entry array. E.g.:
			// [ "<ts>", "<log line>", { "structuredMetadata": {"trace_id": "0242ac120002", "user_id": "superUser123"}, "parsed": {"msg": "text"}}]
			//
// 第三元素若为嵌套对象则分别解析 structuredMetadata 与 parsed 子对象。
			// Therefore, we need to check if the third object contains the "structuredMetadata" or "parsed" fields. If it does,
			// we deserialize the inner objects into the structured metadata and parsed labels respectively.
			// If it doesn't, we deserialize the object into the structured metadata labels.
			structuredMetadataBuilder := labels.NewScratchBuilder(0)
			var parsed labels.Labels

			if err := jsonparser.ObjectEach(value, func(key []byte, value []byte, dataType jsonparser.ValueType, _ int) error {
				if dataType == jsonparser.Object {
					if string(key) == "structuredMetadata" {
						lbls, err := parseLabels(value)
						if err != nil {
							return err
						}

						structuredMetadataBuilder.Reset()
						lbls.Range(func(l labels.Label) {
							structuredMetadataBuilder.Add(l.Name, l.Value)
						})
					}
					if string(key) == "parsed" {
						lbls, err := parseLabels(value)
						if err != nil {
							return err
						}
						parsed = lbls
					}
					return nil
				}
				if dataType == jsonparser.String || t != jsonparser.Number {
					// Use ParseString to properly decode escape sequences like \n
					val, err := jsonparser.ParseString(value)
					if err != nil {
						return err
					}
					structuredMetadataBuilder.Add(string(key), val)
					return nil
				}
				return fmt.Errorf("could not parse structured metadata or parsed fileds")
			}); err != nil {
				parseError = err
				return
			}

			structuredMetadataBuilder.Sort()
			e.StructuredMetadata = structuredMetadataBuilder.Labels()
			e.Parsed = parsed
		}
		i++
	})
	if parseError != nil {
		return parseError
	}
	return err
}

// parseLabels 将 JSON 对象解码为 Prometheus labels.Labels 并排序。
func parseLabels(data []byte) (labels.Labels, error) {
	labelsBuilder := labels.NewScratchBuilder(0)

	err := jsonparser.ObjectEach(data, func(key []byte, value []byte, t jsonparser.ValueType, _ int) error {
		if t != jsonparser.String && t != jsonparser.Number {
			return fmt.Errorf("could not parse label value. Expected string or number, got %s", t)
		}

		val, err := jsonparser.ParseString(value)
		if err != nil {
			return err
		}

		labelsBuilder.Add(string(key), val)
		return nil
	})

	labelsBuilder.Sort()
	return labelsBuilder.Labels(), err
}

type jsonExtension struct {
	jsoniter.DummyExtension
}

// sliceEntryDecoder 为 jsoniter 提供 []Entry 的流式数组解码，避免中间 DOM。
type sliceEntryDecoder struct{}

func (sliceEntryDecoder) Decode(ptr unsafe.Pointer, iter *jsoniter.Iterator) {
	*((*[]Entry)(ptr)) = (*((*[]Entry)(ptr)))[:0]
	iter.ReadArrayCB(func(iter *jsoniter.Iterator) bool {
		i := 0
		var ts time.Time
		var line string
		structuredMetadataBuilder := labels.NewScratchBuilder(0)
		ok := iter.ReadArrayCB(func(iter *jsoniter.Iterator) bool {
			var ok bool
			switch i {
			case 0:
				ts, ok = readTimestamp(iter)
				i++
				return ok
			case 1:
				line = iter.ReadString()
				i++
				if iter.Error != nil {
					return false
				}
				return true
			case 2:
				iter.ReadMapCB(func(iter *jsoniter.Iterator, labelName string) bool {
					labelValue := iter.ReadString()
					structuredMetadataBuilder.Add(labelName, labelValue)
					return true
				})
				i++
				if iter.Error != nil {
					return false
				}
				return true
			default:
				iter.ReportError("error reading entry", "array must have at least 2 and up to 3 values")
				return false
			}
		})
		if ok {
			structuredMetadataBuilder.Sort()

			*((*[]Entry)(ptr)) = append(*((*[]Entry)(ptr)), Entry{
				Timestamp:          ts,
				Line:               line,
				StructuredMetadata: structuredMetadataBuilder.Labels(),
			})
			return true
		}
		return false
	})
}

func readTimestamp(iter *jsoniter.Iterator) (time.Time, bool) {
	s := iter.ReadString()
	if iter.Error != nil {
		return time.Time{}, false
	}
	t, err := strconv.ParseInt(s, 10, 64)
	if err != nil {
		iter.ReportError("error reading entry timestamp", err.Error())
		return time.Time{}, false

	}
	return time.Unix(0, t), true
}

// EntryEncoder 将 Entry 编码为 ["纳秒时间戳","日志行",{元数据}] 数组形式。
type EntryEncoder struct{}

func (EntryEncoder) IsEmpty(_ unsafe.Pointer) bool {
	// we don't omit-empty with log entries.
	return false
}

func (EntryEncoder) Encode(ptr unsafe.Pointer, stream *jsoniter.Stream) {
	e := *((*Entry)(ptr))
	stream.WriteArrayStart()
	stream.WriteRaw(`"`)
	stream.WriteRaw(strconv.FormatInt(e.Timestamp.UnixNano(), 10))
	stream.WriteRaw(`"`)
	stream.WriteMore()
	stream.WriteStringWithHTMLEscaped(e.Line)
	if !e.StructuredMetadata.IsEmpty() {
		stream.WriteMore()
		stream.WriteObjectStart()

		var i int
		e.StructuredMetadata.Range(func(lbl labels.Label) {
			if i > 0 {
				stream.WriteMore()
			}
			stream.WriteObjectField(lbl.Name)
			stream.WriteStringWithHTMLEscaped(lbl.Value)

			i++
		})
		stream.WriteObjectEnd()
	}
	stream.WriteArrayEnd()
}

// CreateDecoder 仅为 []Entry 类型注册 sliceEntryDecoder。
func (e *jsonExtension) CreateDecoder(typ reflect2.Type) jsoniter.ValDecoder {
	if typ == reflect2.TypeOf([]Entry{}) {
		return sliceEntryDecoder{}
	}
	return nil
}

func (e *jsonExtension) CreateEncoder(typ reflect2.Type) jsoniter.ValEncoder {
	if typ == reflect2.TypeOf(Entry{}) {
		return EntryEncoder{}
	}
	return nil
}
// readTimestamp 从 JSON 字符串解析纳秒 Unix 时间戳供 jsoniter 解码路径使用。
