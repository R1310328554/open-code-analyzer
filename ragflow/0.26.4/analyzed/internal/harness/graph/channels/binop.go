package channels

import (
	"fmt"
	"reflect"

	"ragflow/internal/harness/graph/constants"
	"ragflow/internal/harness/graph/errors"
)

// binop.go — 二元算子聚合通道：用自定义二元函数逐步合并多个更新。

// BinaryOperator 将两个值合并为一个的二元函数。
type BinaryOperator func(a, b interface{}) interface{}

// BinaryOperatorAggregate 用二元算子将当前值与每个新值逐步聚合。
type BinaryOperatorAggregate struct {
	BaseChannel
	value    interface{}
	operator BinaryOperator
}

// NewBinaryOperatorAggregate 创建二元算子聚合通道。
func NewBinaryOperatorAggregate(typ interface{}, operator BinaryOperator) *BinaryOperatorAggregate {
	c := &BinaryOperatorAggregate{
		BaseChannel: BaseChannel{Typ: typ},
		operator:    operator,
	}

	c.value = createZeroValue(typ)

	return c
}

// createZeroValue 为给定类型创建零值初始状态。
func createZeroValue(typ interface{}) (result interface{}) {
	result = Missing
	if typ == nil {
		return
	}

	rt := reflect.TypeOf(typ)

	switch rt.String() {
	case "[]interface {}":
		return make([]interface{}, 0)
	case "map[string]interface {}":
		return make(map[string]interface{})
	}

	if rt.Kind() == reflect.Ptr {
		rt = rt.Elem()
	}

	if rt.Kind() == reflect.Slice {
		return reflect.MakeSlice(rt, 0, 0).Interface()
	}

	if rt.Kind() == reflect.Map {
		return reflect.MakeMap(rt).Interface()
	}

	defer func() {
		if r := recover(); r != nil {
			result = Missing
		}
	}()

	zero := reflect.Zero(rt)
	result = zero.Interface()
	return
}

// Get 返回聚合后的当前值。
func (c *BinaryOperatorAggregate) Get() (interface{}, error) {
	if IsMissing(c.value) {
		return nil, &errors.EmptyChannelError{}
	}
	return c.value, nil
}

// IsAvailable 通道是否已有聚合结果。
func (c *BinaryOperatorAggregate) IsAvailable() bool {
	return !IsMissing(c.value)
}

// isOverwrite 检测值是否为覆盖（Overwrite）包装。
func isOverwrite(value interface{}) (bool, interface{}) {
	if value == nil {
		return false, nil
	}

	type overwriter interface {
		GetValue() interface{}
	}
	if ow, ok := value.(overwriter); ok {
		return true, ow.GetValue()
	}

	if m, ok := value.(map[string]interface{}); ok {
		if len(m) == 1 {
			if v, exists := m[constants.Overwrite]; exists {
				return true, v
			}
		}
	}

	return false, nil
}

// Update 用二元算子逐步聚合多个更新；支持 Overwrite 强制覆盖。
func (c *BinaryOperatorAggregate) Update(values []interface{}) (bool, error) {
	if len(values) == 0 {
		return false, nil
	}

	if IsMissing(c.value) {
		c.value = values[0]
		values = values[1:]
	}

	seenOverwrite := false
	for _, value := range values {
		isOver, overwriteValue := isOverwrite(value)
		if isOver {
			if seenOverwrite {
				return false, &errors.InvalidUpdateError{
					Message: "Can receive only one Overwrite value per super-step.",
				}
			}
			c.value = overwriteValue
			seenOverwrite = true
			continue
		}

		if !seenOverwrite {
			c.value = c.operator(c.value, value)
		}
	}

	return true, nil
}

// Copy 返回通道拷贝。
func (c *BinaryOperatorAggregate) Copy() Channel {
	newCh := NewBinaryOperatorAggregate(c.Typ, c.operator)
	newCh.Key = c.Key
	newCh.value = c.value
	return newCh
}

// Checkpoint 返回当前聚合值。
func (c *BinaryOperatorAggregate) Checkpoint() interface{} {
	return c.value
}

// FromCheckpoint 从检查点恢复。
func (c *BinaryOperatorAggregate) FromCheckpoint(checkpoint interface{}) Channel {
	newCh := NewBinaryOperatorAggregate(c.Typ, c.operator)
	newCh.Key = c.Key
	if !IsMissing(checkpoint) {
		newCh.value = checkpoint
	}
	return newCh
}

// StringConcat 字符串拼接二元算子。
func StringConcat(a, b interface{}) interface{} {
	sa, ok1 := a.(string)
	sb, ok2 := b.(string)
	if !ok1 || !ok2 {
		return fmt.Sprint(a) + fmt.Sprint(b)
	}
	return sa + sb
}

// IntAdd 整数/浮点加法二元算子；类型不匹配时优雅降级返回 a。
func IntAdd(a, b interface{}) interface{} {
	if ai, ok := a.(int); ok {
		if bi, ok := b.(int); ok {
			return ai + bi
		}
	}
	if af, ok := a.(float64); ok {
		if bf, ok := b.(float64); ok {
			return af + bf
		}
	}
	return a
}

// ListAppend 列表追加二元算子。
func ListAppend(a, b interface{}) interface{} {
	if al, ok := a.([]interface{}); ok {
		if bl, ok := b.([]interface{}); ok {
			result := make([]interface{}, len(al)+len(bl))
			copy(result, al)
			copy(result[len(al):], bl)
			return result
		}
		result := make([]interface{}, len(al)+1)
		copy(result, al)
		result[len(al)] = b
		return result
	}
	return []interface{}{a, b}
}
