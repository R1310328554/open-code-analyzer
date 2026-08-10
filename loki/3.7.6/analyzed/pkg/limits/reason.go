package limits

// reason 定义 ExceedsLimits 拒绝流时返回的原因码，供 frontend 与 distributor 解析。

type Reason int

const (
	// ReasonUnknown 表示未知或未设置的拒绝原因。
// ReasonUnknown is the zero value.
	ReasonUnknown Reason = iota
	// ReasonFailed 表示检查过程出错，流无法接受也无法明确判定超限。
// ReasonFailed is the reason returned when a stream cannot be checked
	// against limits due to an error.
	ReasonFailed
	// ReasonMaxStreams 表示租户已达或超过 MaxGlobalStreamsPerUser 配额。
// ReasonMaxStreams is returned when a stream cannot be accepted because
	// the tenant has either reached or exceeded their maximum stream limit.
	ReasonMaxStreams
)

func (r Reason) String() string {
	switch r {
	case ReasonFailed:
		return "failed"
	case ReasonMaxStreams:
		return "max streams"
	default:
		return "unknown reason"
	}
}
// Reason 值以 uint32 写入 protobuf，与 proto.ExceedsLimitsResult 对应。
