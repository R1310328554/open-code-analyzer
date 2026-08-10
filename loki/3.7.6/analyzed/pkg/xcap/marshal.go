package xcap

// xcap 包 marshal 实现 Capture/Region 到 internal proto 的转换：序列化前幂等 End 全部 Region 以防并发写入。

import (
	"fmt"

	"github.com/grafana/loki/v3/pkg/xcap/internal/proto"
)

// toProtoCapture 构建 statistics 索引并递归 toProtoRegion，nil Capture 返回 nil。
// toProtoCapture converts a Capture to its protobuf representation.
//
// Marshalling is intended to happen after the capture lifecycle is
// complete. toProtoCapture marks the capture and all its regions
// as ended to prevent further recording while marshalling is in progress.
func toProtoCapture(c *Capture) (*proto.Capture, error) {
	if c == nil {
		return nil, nil
	}

	// end the capture and its regions. This operation is idempotent.
	c.End()
	for _, region := range c.regions {
		region.End()
	}

	// Build the statistics index from deduplicated statistics.
	statistics := c.getAllStatistics()
	statsIndex := make(map[StatisticKey]uint32, len(statistics))
	protoStats := make([]*proto.Statistic, 0, len(statistics))

	for _, stat := range statistics {
		statsIndex[stat.Key()] = uint32(len(protoStats))
		protoStats = append(protoStats, &proto.Statistic{
			Name:            stat.Name(),
			DataType:        marshalDataType(stat.DataType()),
			AggregationType: marshalAggregationType(stat.Aggregation()),
		})
	}

	// Convert regions to proto regions
	protoRegions := make([]*proto.Region, 0, len(c.regions))
	for _, region := range c.regions {
		protoRegion, err := toProtoRegion(region, statsIndex)
		if err != nil {
			return nil, fmt.Errorf("failed to marshal region: %w", err)
		}

		if protoRegion != nil {
			protoRegions = append(protoRegions, protoRegion)
		}
	}

	return &proto.Capture{
		Regions:    protoRegions,
		Statistics: protoStats,
	}, nil
}

// toProtoRegion 将 observations map 转为 proto 切片，缺失 statistic 索引则报错。
// toProtoRegion converts a Region to its protobuf representation.
func toProtoRegion(region *Region, statsIndex map[StatisticKey]uint32) (*proto.Region, error) {
	protoObservations := make([]*proto.Observation, 0, len(region.observations))
	for key, observation := range region.observations {
		statIndex, exists := statsIndex[key]
		if !exists {
			return nil, fmt.Errorf("statistic not found in index: %v", key)
		}

		protoValue, err := marshalObservationValue(observation.Value)
		if err != nil {
			return nil, fmt.Errorf("failed to marshal observation: %w", err)
		}

		protoObservations = append(protoObservations, &proto.Observation{
			StatisticId: statIndex,
			Value:       protoValue,
			Count:       uint32(observation.Count),
		})
	}

	return &proto.Region{
		Name:         region.name,
		Observations: protoObservations,
		Id:           region.id[:],
		ParentId:     region.parentID[:],
	}, nil
}

// marshalObservationValue 用 oneof 包装 int64/float64/bool，不支持类型返回 fmt 错误。
// marshalObservationValue converts an observation value to proto ObservationValue.
func marshalObservationValue(value any) (*proto.ObservationValue, error) {
	switch v := value.(type) {
	case int64:
		return &proto.ObservationValue{
			Kind: &proto.ObservationValue_IntValue{IntValue: v},
		}, nil
	case float64:
		return &proto.ObservationValue{
			Kind: &proto.ObservationValue_FloatValue{FloatValue: v},
		}, nil
	case bool:
		return &proto.ObservationValue{
			Kind: &proto.ObservationValue_BoolValue{BoolValue: v},
		}, nil
	default:
		return nil, fmt.Errorf("unsupported observation value type: %T", value)
	}
}

// marshalDataType converts a DataType to proto DataType.
func marshalDataType(dt DataType) proto.DataType {
	switch dt {
	case DataTypeInvalid:
		return proto.DATA_TYPE_INVALID
	case DataTypeInt64:
		return proto.DATA_TYPE_INT64
	case DataTypeFloat64:
		return proto.DATA_TYPE_FLOAT64
	case DataTypeBool:
		return proto.DATA_TYPE_BOOL
	default:
		return proto.DATA_TYPE_INVALID
	}
}

// marshalAggregationType converts an AggregationType to proto AggregationType.
func marshalAggregationType(agg AggregationType) proto.AggregationType {
	switch agg {
	case AggregationTypeInvalid:
		return proto.AGGREGATION_TYPE_INVALID
	case AggregationTypeSum:
		return proto.AGGREGATION_TYPE_SUM
	case AggregationTypeMin:
		return proto.AGGREGATION_TYPE_MIN
	case AggregationTypeMax:
		return proto.AGGREGATION_TYPE_MAX
	case AggregationTypeLast:
		return proto.AGGREGATION_TYPE_LAST
	case AggregationTypeFirst:
		return proto.AGGREGATION_TYPE_FIRST
	default:
		return proto.AGGREGATION_TYPE_INVALID
	}
}
// marshalDataType/marshalAggregationType 将 Go 枚举映射为 proto 常量，未知值落 INVALID。
