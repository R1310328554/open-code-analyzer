// client.go — PDF 解析用 DeepDoc HTTP 客户端：封装 DLA/TSR/OCR 预测、批量识别与健康检查，带指数退避重试。

package inference

import (
	"bytes"
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"image"
	"io"
	"log/slog"
	"mime/multipart"
	"net"
	"net/http"
	"sync"
	"time"

	pdf "ragflow/internal/deepdoc/parser/pdf/type"
	util "ragflow/internal/deepdoc/parser/pdf/util"

	"github.com/cenkalti/backoff/v5"
)

// Client 封装 DeepDoc HTTP API（DLA/TSR/OCR）。
type Client struct {
	baseURL    string
	httpClient *http.Client

	// Label tables for class_id → label string mapping.
	// Set by the service layer (model-specific) to reflect the model's taxonomy.
	DLALabels []string
	TSRLabels []string
}

// BaseURL 返回配置的 DeepDoc 服务根 URL。
func (c *Client) BaseURL() string { return c.baseURL }

// NewClient 创建客户端；baseURL 必填（通常来自 DEEPDOC_URL），空则报错。
func NewClient(baseURL string) (*Client, error) {
	if baseURL == "" {
		return nil, fmt.Errorf("deepdoc client: baseURL is required (set DEEPDOC_URL)")
	}
	return &Client{
		baseURL: baseURL,
		httpClient: &http.Client{
			Timeout: 120 * time.Second,
		},
		DLALabels: DefaultDLALabels(),
		TSRLabels: DefaultTSRLabels(),
	}, nil
}

// DefaultDLALabels 返回 10 类 DLA 标签表，对齐 Python dla_cli；重复索引保留兼容。
func DefaultDLALabels() []string {
	return []string{
		pdf.LayoutTypeTitle, pdf.LayoutTypeText, pdf.LayoutTypeReference,
		pdf.LayoutTypeFigure, pdf.DLALabelFigureCaption,
		pdf.LayoutTypeTable, pdf.DLALabelTableCaption, pdf.DLALabelTableCaption,
		pdf.LayoutTypeEquation, pdf.DLALabelFigureCaption,
	}
}

// DefaultTSRLabels 返回 6 类 TSR 标签表，对齐 Python tsr_adapter。
func DefaultTSRLabels() []string {
	return []string{
		"table", "table column", "table row",
		"table column header", "table projected row header",
		"table spanning cell",
	}
}

// bboxesResponse DeepDoc 预测接口通用 bboxes JSON 信封。
type bboxesResponse struct {
	BBoxes [][]float64 `json:"bboxes"`
}

// DLA 分析整页图像，返回带标签的版面区域列表。
func (c *Client) DLA(ctx context.Context, pageImage image.Image) ([]pdf.DLARegion, error) {
	data, err := util.EncodeJPEG(pageImage)
	if err != nil {
		return nil, fmt.Errorf("dla: encode: %w", err)
	}
	var resp bboxesResponse
	if err := c.post(ctx, "/predict/dla", data, "dla.jpeg", &resp); err != nil {
		return nil, fmt.Errorf("dla: %w", err)
	}
	regions := make([]pdf.DLARegion, 0, len(resp.BBoxes))
	for _, b := range resp.BBoxes {
		if len(b) < 6 {
			continue
		}
		labels := c.DLALabels
		label := ""
		if clsID := int(b[5]); clsID >= 0 && clsID < len(labels) {
			label = labels[clsID]
		}
		regions = append(regions, pdf.DLARegion{
			X0: b[0], Y0: b[1], X1: b[2], Y1: b[3],
			Confidence: b[4],
			Label:      label,
		})
	}
	return regions, nil
}

// TSR 对裁剪表格图识别单元格结构。
func (c *Client) TSR(ctx context.Context, cropped image.Image) ([]pdf.TSRCell, error) {
	data, err := util.EncodeJPEG(cropped)
	if err != nil {
		return nil, fmt.Errorf("tsr: encode: %w", err)
	}
	var resp bboxesResponse
	if err := c.post(ctx, "/predict/tsr", data, "tsr.jpeg", &resp); err != nil {
		return nil, fmt.Errorf("tsr: %w", err)
	}
	cells := make([]pdf.TSRCell, 0, len(resp.BBoxes))
	for _, b := range resp.BBoxes {
		if len(b) < 5 {
			continue
		}
		tlabels := c.TSRLabels
		label := ""
		if len(b) >= 6 {
			if cls := int(b[5]); cls >= 0 && cls < len(tlabels) {
				label = tlabels[cls]
			}
		}
		cells = append(cells, pdf.TSRCell{
			X0: b[0], Y0: b[1], X1: b[2], Y1: b[3],
			Label: label,
		})
	}
	return cells, nil
}

// ocrDetectResponse 匹配 /predict/ocr?operator=det 的四边形检测输出。
type ocrDetectResponse struct {
	Output [][][][][]float64 `json:"output"`
}

// ocrRecognizeResponse 匹配 /predict/ocr?operator=rec 的文本识别输出。
type ocrRecognizeResponse struct {
	Output [][][][]any `json:"output"`
}

// OCRDetect 检测文本区域四边形框；operator=det。
func (c *Client) OCRDetect(ctx context.Context, cropped image.Image) ([]pdf.OCRBox, error) {
	data, err := util.EncodeJPEG(cropped)
	if err != nil {
		return nil, fmt.Errorf("ocr detect: encode: %w", err)
	}

	// 先解析外层 RawMessage，格式不符时可打日志。
	var rawEnvelope struct {
		Output json.RawMessage `json:"output"`
	}
	if err := c.post(ctx, "/predict/ocr", data, "ocr_detect.jpeg", &rawEnvelope, "operator", "det"); err != nil {
		return nil, fmt.Errorf("ocr detect: %w", err)
	}

	var result ocrDetectResponse
	if err := json.Unmarshal(rawEnvelope.Output, &result.Output); err != nil {
		rawStr := string(rawEnvelope.Output)
		if len(rawStr) > 1000 {
			rawStr = rawStr[:1000]
		}
		slog.Warn("ocr detect: output format mismatch", "err", err, "raw_output", rawStr)
		return nil, fmt.Errorf("ocr detect: %w", err)
	}

	var boxes []pdf.OCRBox
	for _, outer := range result.Output {
		for _, page := range outer {
			for _, box := range page {
				if len(box) < 4 {
					continue
				}
				boxes = append(boxes, pdf.OCRBox{
					X0: box[0][0], Y0: box[0][1],
					X1: box[1][0], Y1: box[1][1],
					X2: box[2][0], Y2: box[2][1],
					X3: box[3][0], Y3: box[3][1],
				})
			}
		}
	}
	return boxes, nil
}

// OCRRecognize 识别裁剪区域内文字；operator=rec。
func (c *Client) OCRRecognize(ctx context.Context, cropped image.Image) ([]pdf.OCRText, error) {
	data, err := util.EncodeJPEG(cropped)
	if err != nil {
		return nil, fmt.Errorf("ocr rec: encode: %w", err)
	}
	var result ocrRecognizeResponse
	if err := c.post(ctx, "/predict/ocr", data, "ocr_rec.jpeg", &result, "operator", "rec"); err != nil {
		return nil, fmt.Errorf("ocr rec: %w", err)
	}
	var texts []pdf.OCRText
	for _, page := range result.Output {
		for _, item := range page {
			for _, pair := range item {
				if len(pair) >= 2 {
					text, _ := pair[0].(string)
					conf, _ := pair[1].(float64)
					texts = append(texts, pdf.OCRText{Text: text, Confidence: conf})
				}
			}
		}
	}
	return texts, nil
}

// OCRRecognizeBatch 批量 OCR 识别；结果与 errs 并行切片；nil 图像对应非 nil error。
func (c *Client) OCRRecognizeBatch(ctx context.Context, cropped []image.Image) ([][]pdf.OCRText, []error) {
	results := make([][]pdf.OCRText, len(cropped))
	errs := make([]error, len(cropped))

	// Process images concurrently with a bounded worker pool to avoid
	// overwhelming the DeepDoc service.
	const maxConcurrent = 4
	sem := make(chan struct{}, maxConcurrent)
	var wg sync.WaitGroup

	for i, img := range cropped {
		if img == nil {
			errs[i] = fmt.Errorf("ocr rec batch: image[%d] is nil", i)
			continue
		}
		wg.Add(1)
		go func(idx int, im image.Image) {
			defer wg.Done()
			sem <- struct{}{}
			defer func() { <-sem }()

			texts, err := c.OCRRecognize(ctx, im)
			results[idx] = texts
			errs[idx] = err
		}(i, img)
	}
	wg.Wait()
	return results, errs
}

// Health 探测 /health 是否可达。
func (c *Client) Health() bool {
	resp, err := c.httpClient.Get(c.baseURL + "/health")
	if err != nil {
		return false
	}
	resp.Body.Close()
	return resp.StatusCode == 200
}

// post multipart POST 通用实现：构建表单、指数退避重试、解析 JSON。
func (c *Client) post(ctx context.Context, endpoint string, imgData []byte, filename string, result interface{}, extraFields ...string) error {
	// 请求体只构建一次（图像数据幂等，可复用于重试）。
	var body bytes.Buffer
	w := multipart.NewWriter(&body)
	fw, err := w.CreateFormFile("request", filename)
	if err != nil {
		return err
	}
	if _, err := fw.Write(imgData); err != nil {
		return err
	}
	for i := 0; i+1 < len(extraFields); i += 2 {
		w.WriteField(extraFields[i], extraFields[i+1])
	}
	w.Close()
	contentType := w.FormDataContentType()
	bodyBytes := body.Bytes()

	_, err = backoff.Retry(ctx, func() (struct{}, error) {
		req, err := http.NewRequestWithContext(ctx, "POST", c.baseURL+endpoint, bytes.NewReader(bodyBytes))
		if err != nil {
			return struct{}{}, backoff.Permanent(err)
		}
		req.Header.Set("Content-Type", contentType)

		resp, err := c.httpClient.Do(req)
		if err != nil {
			if errors.Is(err, context.Canceled) || errors.Is(err, context.DeadlineExceeded) {
				return struct{}{}, backoff.Permanent(err)
			}
			var netErr net.Error
			if errors.As(err, &netErr) {
				slog.Warn("deepdoc: network error, will retry", "endpoint", endpoint, "err", err)
				return struct{}{}, err
			}
			return struct{}{}, backoff.Permanent(err)
		}

		if resp.StatusCode == 200 {
			defer resp.Body.Close()
			return struct{}{}, json.NewDecoder(io.LimitReader(resp.Body, 64<<20)).Decode(result)
		}

		errBody, _ := io.ReadAll(io.LimitReader(resp.Body, 1<<20))
		resp.Body.Close()
		respErr := fmt.Errorf("http %d: %s", resp.StatusCode, string(errBody[:min(200, len(errBody))]))

		if resp.StatusCode >= 500 {
			slog.Warn("deepdoc: server error, will retry", "endpoint", endpoint, "status", resp.StatusCode)
			return struct{}{}, respErr
		}
		// 4xx 等客户端错误不可重试。
		return struct{}{}, backoff.Permanent(respErr)
	}, backoff.WithMaxTries(4), backoff.WithNotify(func(err error, d time.Duration) {
		slog.Info("deepdoc: retrying", "endpoint", endpoint, "backoff", d.Round(time.Millisecond), "err", err)
	}))
	return err
}
