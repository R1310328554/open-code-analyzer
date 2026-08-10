//
//  Copyright 2026 The InfiniFlow Authors. All Rights Reserved.
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
// dla.go — 文档版面分析（DLA）远程调用：multipart 上传 JPEG、解析 bboxes  wire 格式并映射为 DLAResult。

//

package deepdoc

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"mime/multipart"
	"net/url"
	"strings"
)

// BBox 图像像素坐标四元组 [left, top, right, bottom]；float 保留子像素精度。
type BBox [4]float64

// DLAResult 单块版面区域；Type 为规范化类名，TypeIdx 为 DLAClasses 原始索引（含重复槽位）。
type DLAResult struct {
	Type    string  `json:"type"`
	Score   float64 `json:"score"`
	BBox    BBox    `json:"bbox"`
	TypeIdx int     `json:"type_idx"`
}

// DLAClasses 10 类版面 taxonomy，顺序与 wire type_idx 对应；索引 4/6/7/9 重复项保留以兼容旧推理服务。
var DLAClasses = []string{
	"title",          // 0
	"text",           // 1
	"reference",      // 2
	"figure",         // 3
	"figure caption", // 4
	"table",          // 5
	"table caption",  // 6
	"table caption",  // 7  duplicate
	"equation",       // 8
	"figure caption", // 9  duplicate
}

// rawDLA DLA 服务 JSON 响应结构（§2.3）。
type rawDLA struct {
	BBoxes [][]float64 `json:"bboxes"`
}

// DLA 对多张 JPEG 调用远程版面分析，每张独立 POST+重试；失败图返回空 DLAResult 不中断批次（对齐 Python layout_recognizer）。未配置 URL 返回 ErrNoURL。
func (c *Client) DLA(ctx context.Context, images [][]byte) ([]DLAResult, error) {
	if !c.Enabled() {
		return nil, ErrNoURL
	}
	if len(images) == 0 {
		return []DLAResult{}, nil
	}
	predictURL, err := c.predictURL()
	if err != nil {
		return nil, err
	}
	out := make([]DLAResult, 0, len(images))
	for _, img := range images {
		res := c.predictOne(ctx, predictURL, img)
		// Per Python: a failed image yields an empty slot rather
		// than aborting the whole batch. Surface the first hard
		// error at the end if the user wants it.
		if len(res) == 0 {
			out = append(out, DLAResult{})
		} else {
			out = append(out, res...)
		}
	}
	return out, nil
}

// predictURL 拼接 predict 端点 URL，去除 base 尾部斜杠。
func (c *Client) predictURL() (string, error) {
	base := strings.TrimRight(c.baseURL, "/")
	u, err := url.Parse(base + predictPath)
	if err != nil {
		return "", fmt.Errorf("deepdoc: parse predict url: %w", err)
	}
	return u.String(), nil
}

// predictOne 单张图片的重试预测；耗尽重试返回空切片（非 error）；4xx/坏 URL 立即失败。
func (c *Client) predictOne(ctx context.Context, predictURL string, image []byte) []DLAResult {
	buildBody := func() (io.Reader, string) {
		// Each retry needs a fresh multipart body — multipart.Writer
		// consumes its underlying buffer on Close. CreatePart lets
		// us set both a filename (so Go's net/http server-side
		// parser routes the part to MultipartForm.File) and the
		// image/jpeg Content-Type the DLA server expects (matches
		// the Python `files={'request': ('image.jpg', ...)}`
		// contract from dla_cli.py:35).
		buf := &bytes.Buffer{}
		w := multipart.NewWriter(buf)
		fw, _ := w.CreatePart(map[string][]string{
			"Content-Disposition": {`form-data; name="request"; filename="image.jpg"`},
			"Content-Type":        {"image/jpeg"},
		})
		_, _ = fw.Write(image)
		_ = w.Close()
		return buf, w.FormDataContentType()
	}
	validate := func(data []byte) error {
		var r rawDLA
		if err := json.Unmarshal(data, &r); err != nil {
			return fmt.Errorf("%w: %v", ErrInvalidResponse, err)
		}
		if r.BBoxes == nil {
			return fmt.Errorf("%w: missing bboxes key", ErrInvalidResponse)
		}
		return nil
	}
	data, err := c.doPost(ctx, predictURL, buildBody, validate)
	if err != nil {
		return nil
	}
	var r rawDLA
	_ = json.Unmarshal(data, &r) // already validated above
	results := make([]DLAResult, 0, len(r.BBoxes))
	for _, b := range r.BBoxes {
		if len(b) < 6 {
			continue
		}
		// bbox 六元组 [l,t,r,b,score,type_idx]，见 deepdoc-endpoints §2.3。
		bbox := BBox{b[0], b[1], b[2], b[3]}
		idx := int(b[5])
		cls := ""
		if idx >= 0 && idx < len(DLAClasses) {
			cls = DLAClasses[idx]
		}
		results = append(results, DLAResult{
			Type:    cls,
			Score:   b[4],
			BBox:    bbox,
			TypeIdx: idx,
		})
	}
	return results
}
