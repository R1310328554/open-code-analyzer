package channels

import (
	"reflect"

	"ragflow/internal/harness/graph/types"
)

// reducer.go — 归约器通道：用自定义归约函数合并多值更新。

// ReducerChannel 在底层通道外包装归约函数。
type ReducerChannel struct {
	Channel
	reducer types.ReducerFunc // 二元归约函数
}

// NewReducerChannel 创建 ReducerChannel。
func NewReducerChannel(channel Channel, reducer types.ReducerFunc) *ReducerChannel {
	return &ReducerChannel{
		Channel: channel,
		reducer: reducer,
	}
}

// Update 用归约函数将新值与当前值合并后写入底层通道。
func (rc *ReducerChannel) Update(values []interface{}) (bool, error) {
	if len(values) == 0 {
		return false, nil
	}

	// 读取底层通道当前值
	current, err := rc.Channel.Get()

	// 用归约函数合并：通道为空时从 values[0] 起步
	combined := values[0]
	if err == nil {
		combined = rc.reducer(current, combined)
	}
	for i := 1; i < len(values); i++ {
		combined = rc.reducer(combined, values[i])
	}

	return rc.Channel.Update([]interface{}{combined})
}

// Copy 返回 ReducerChannel 拷贝。
func (rc *ReducerChannel) Copy() Channel {
	return &ReducerChannel{
		Channel: rc.Channel.Copy(),
		reducer: rc.reducer,
	}
}

// Checkpoint 返回底层通道的检查点。
func (rc *ReducerChannel) Checkpoint() interface{} {
	return rc.Channel.Checkpoint()
}

// FromCheckpoint 从检查点恢复底层通道。
func (rc *ReducerChannel) FromCheckpoint(checkpoint interface{}) Channel {
	return &ReducerChannel{
		Channel: rc.Channel.FromCheckpoint(checkpoint),
		reducer: rc.reducer,
	}
}

// CreateReducerChannel 根据字段类型与注解自动选择底层通道并可选包装归约器。
func CreateReducerChannel(fieldName string, fieldType reflect.Type, reducer types.ReducerFunc) (Channel, error) {
	var channel Channel

	switch fieldType.Kind() {
	case reflect.Slice, reflect.Array:
		// 切片类型：BinaryOperatorAggregate + ListAppend
		channel = NewBinaryOperatorAggregate(fieldType, ListAppend)
	case reflect.Map:
		// 映射类型：BinaryOperatorAggregate + 合并算子
		channel = NewBinaryOperatorAggregate(fieldType, func(a, b interface{}) interface{} {
			if aMap, ok := a.(map[string]interface{}); ok {
				if bMap, ok := b.(map[string]interface{}); ok {
					result := make(map[string]interface{}, len(aMap)+len(bMap))
					for k, v := range aMap {
						result[k] = v
					}
					for k, v := range bMap {
						result[k] = v
					}
					return result
				}
			}
			return b
		})
	case reflect.Int, reflect.Int8, reflect.Int16, reflect.Int32, reflect.Int64,
		reflect.Uint, reflect.Uint8, reflect.Uint16, reflect.Uint32, reflect.Uint64,
		reflect.Float32, reflect.Float64:
		// 数值类型：BinaryOperatorAggregate + IntAdd
		channel = NewBinaryOperatorAggregate(reflect.Zero(fieldType).Interface(), IntAdd)
	default:
		// 默认 LastValue 通道
		channel = NewLastValue(fieldType)
	}

	channel.SetKey(fieldName)

	if reducer != nil {
		return NewReducerChannel(channel, reducer), nil
	}

	return channel, nil
}

// 内置归约函数，适用于常见类型。
var (
	// AddReducer 数值相加。
	AddReducer = func(current, update interface{}) interface{} {
		if current == nil {
			return update
		}
		if ci, ok := current.(int); ok {
			if ui, ok := update.(int); ok {
				return ci + ui
			}
		}
		if cf, ok := current.(float64); ok {
			if uf, ok := update.(float64); ok {
				return cf + uf
			}
		}
		return update
	}

	// AppendReducer 向切片追加元素。
	AppendReducer = func(current, update interface{}) interface{} {
		if current == nil {
			return []interface{}{update}
		}
		if slice, ok := current.([]interface{}); ok {
			return append(slice, update)
		}
		return []interface{}{current, update}
	}

	// MergeReducer 合并映射。
	MergeReducer = func(current, update interface{}) interface{} {
		if current == nil {
			return update
		}
		if currentMap, ok := current.(map[string]interface{}); ok {
			if updateMap, ok := update.(map[string]interface{}); ok {
				result := make(map[string]interface{}, len(currentMap)+len(updateMap))
				for k, v := range currentMap {
					result[k] = v
				}
				for k, v := range updateMap {
					result[k] = v
				}
				return result
			}
		}
		return update
	}
)
