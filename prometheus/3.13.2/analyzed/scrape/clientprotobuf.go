// Copyright The Prometheus Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// 测试辅助：将 client_model MetricFamily 序列化为 exposition protobuf 流（length-delimited），供 scrape 路径集成测试使用。

package scrape

import (
	"bytes"
	"encoding/binary"

	"github.com/gogo/protobuf/proto"
	// Intentionally using client model to simulate client in tests.
	dto "github.com/prometheus/client_model/go"
)

// MetricFamilyToProtobuf 返回完整 protobuf 字节切片。
// MetricFamilyToProtobuf writes a MetricFamily into a protobuf.
// This function is intended for testing scraping by providing protobuf serialized input.
func MetricFamilyToProtobuf(metricFamily *dto.MetricFamily) ([]byte, error) {
	buffer := &bytes.Buffer{}
	err := AddMetricFamilyToProtobuf(buffer, metricFamily)
	if err != nil {
		return nil, err
	}
	return buffer.Bytes(), nil
}

// AddMetricFamilyToProtobuf 写入 uvarint 长度前缀 + proto.Marshal 载荷。
// AddMetricFamilyToProtobuf appends a MetricFamily protobuf representation to a buffer.
// This function is intended for testing scraping by providing protobuf serialized input.
func AddMetricFamilyToProtobuf(buffer *bytes.Buffer, metricFamily *dto.MetricFamily) error {
	protoBuf, err := proto.Marshal(metricFamily)
	if err != nil {
		return err
	}

	varintBuf := make([]byte, binary.MaxVarintLen32)
	varintLength := binary.PutUvarint(varintBuf, uint64(len(protoBuf)))

	_, err = buffer.Write(varintBuf[:varintLength])
	if err != nil {
		return err
	}
	_, err = buffer.Write(protoBuf)
	return err
}
