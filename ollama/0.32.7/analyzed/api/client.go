// Package api 为需要与 Ollama 服务交互的客户端代码提供 API 封装。
// 本包 [Client] 的方法与 Ollama REST API 一一对应；命令行客户端亦通过本包访问后端。
// Package api implements the client-side API for code wishing to interact
// with the ollama service. The methods of the [Client] type correspond to
// the ollama REST API as described in [the API documentation].
// The ollama command-line client itself uses this package to interact with
// the backend service.
//
// # Examples
//
// Several examples of using this package are available [in the GitHub
// repository].
//
// [the API documentation]: https://github.com/ollama/ollama/blob/main/docs/api.md
// [in the GitHub repository]: https://github.com/ollama/ollama/tree/main/api/examples
package api

import (
	"bufio"
	"bytes"
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"runtime"
	"strconv"
	"time"

	"github.com/ollama/ollama/auth"
	"github.com/ollama/ollama/envconfig"
	"github.com/ollama/ollama/format"
	"github.com/ollama/ollama/version"
)

// Client 封装与 Ollama 服务通信所需的客户端状态；可通过 [ClientFromEnvironment] 创建实例。
// Client encapsulates client state for interacting with the ollama
// service. Use [ClientFromEnvironment] to create new Clients.
type Client struct {
	base *url.URL
	http *http.Client
}

// checkError 解析 HTTP 响应，将 4xx/5xx 状态转为 StatusError 或 AuthorizationError。
func checkError(resp *http.Response, body []byte) error {
	if resp.StatusCode < http.StatusBadRequest {
		return nil
	}

	if resp.StatusCode == http.StatusUnauthorized {
		authError := AuthorizationError{StatusCode: resp.StatusCode}
		json.Unmarshal(body, &authError)
		return authError
	}

	apiError := StatusError{StatusCode: resp.StatusCode}

	err := json.Unmarshal(body, &apiError)
	if err != nil {
		// Use the full body as the message if we fail to decode a response.
		apiError.ErrorMessage = string(body)
	}

	return apiError
}

// ClientFromEnvironment 读取环境变量 OLLAMA_HOST（格式 <scheme>://<host>:<port>）并创建 [Client]；未设置时使用默认地址。
// ClientFromEnvironment creates a new [Client] using configuration from the
// environment variable OLLAMA_HOST, which points to the network host and
// port on which the ollama service is listening. The format of this variable
// is:
//
//	<scheme>://<host>:<port>
//
// If the variable is not specified, a default ollama host and port will be
// used.
func ClientFromEnvironment() (*Client, error) {
	return &Client{
		base: envconfig.Host(),
		http: http.DefaultClient,
	}, nil
}

// NewClient 使用指定 base URL 与 HTTP 客户端构造 Client。
func NewClient(base *url.URL, http *http.Client) *Client {
	return &Client{
		base: base,
		http: http,
	}
}

// getAuthorizationToken 对认证质询字符串签名，返回 Authorization 头令牌。
func getAuthorizationToken(ctx context.Context, challenge string) (string, error) {
	token, err := auth.Sign(ctx, []byte(challenge))
	if err != nil {
		return "", err
	}
	return token, nil
}

// do 执行单次 JSON 请求/响应往返；启用认证时自动附加签名令牌。
func (c *Client) do(ctx context.Context, method, path string, reqData, respData any) error {
	var reqBody io.Reader
	var data []byte
	var err error

	switch reqData := reqData.(type) {
	case io.Reader:
		// reqData is already an io.Reader
		reqBody = reqData
	case nil:
		// noop
	default:
		data, err = json.Marshal(reqData)
		if err != nil {
			return err
		}

		reqBody = bytes.NewReader(data)
	}

	requestURL := c.base.JoinPath(path)

	var token string
	if envconfig.UseAuth() || c.base.Hostname() == "ollama.com" {
		now := strconv.FormatInt(time.Now().Unix(), 10)
		chal := fmt.Sprintf("%s,%s?ts=%s", method, path, now)
		token, err = getAuthorizationToken(ctx, chal)
		if err != nil {
			return err
		}

		q := requestURL.Query()
		q.Set("ts", now)
		requestURL.RawQuery = q.Encode()
	}

	request, err := http.NewRequestWithContext(ctx, method, requestURL.String(), reqBody)
	if err != nil {
		return err
	}

	request.Header.Set("Content-Type", "application/json")
	request.Header.Set("Accept", "application/json")
	request.Header.Set("User-Agent", fmt.Sprintf("ollama/%s (%s %s) Go/%s", version.Version, runtime.GOARCH, runtime.GOOS, runtime.Version()))

	if token != "" {
		request.Header.Set("Authorization", token)
	}

	respObj, err := c.http.Do(request)
	if err != nil {
		return err
	}
	defer respObj.Body.Close()

	respBody, err := io.ReadAll(respObj.Body)
	if err != nil {
		return err
	}

	if err := checkError(respObj, respBody); err != nil {
		return err
	}

	if len(respBody) > 0 && respData != nil {
		if err := json.Unmarshal(respBody, respData); err != nil {
			return err
		}
	}
	return nil
}

// maxBufferSize 为流式响应扫描缓冲区的最大容量。
const maxBufferSize = 8 * format.MegaByte

// stream 发送 NDJSON 流式请求，逐行回调 fn 处理每个 JSON 块。
func (c *Client) stream(ctx context.Context, method, path string, data any, fn func([]byte) error) error {
	var buf io.Reader
	if data != nil {
		bts, err := json.Marshal(data)
		if err != nil {
			return err
		}

		buf = bytes.NewBuffer(bts)
	}

	requestURL := c.base.JoinPath(path)

	var token string
	if envconfig.UseAuth() || c.base.Hostname() == "ollama.com" {
		var err error
		now := strconv.FormatInt(time.Now().Unix(), 10)
		chal := fmt.Sprintf("%s,%s?ts=%s", method, path, now)
		token, err = getAuthorizationToken(ctx, chal)
		if err != nil {
			return err
		}

		q := requestURL.Query()
		q.Set("ts", now)
		requestURL.RawQuery = q.Encode()
	}

	request, err := http.NewRequestWithContext(ctx, method, requestURL.String(), buf)
	if err != nil {
		return err
	}

	request.Header.Set("Content-Type", "application/json")
	request.Header.Set("Accept", "application/x-ndjson")
	request.Header.Set("User-Agent", fmt.Sprintf("ollama/%s (%s %s) Go/%s", version.Version, runtime.GOARCH, runtime.GOOS, runtime.Version()))

	if token != "" {
		request.Header.Set("Authorization", token)
	}

	response, err := c.http.Do(request)
	if err != nil {
		return err
	}
	defer response.Body.Close()

	scanner := bufio.NewScanner(response.Body)
	// increase the buffer size to avoid running out of space
	scanBuf := make([]byte, 0, maxBufferSize)
	scanner.Buffer(scanBuf, maxBufferSize)
	for scanner.Scan() {
		var errorResponse struct {
			Error     string `json:"error,omitempty"`
			SigninURL string `json:"signin_url,omitempty"`
		}

		bts := scanner.Bytes()
		if err := json.Unmarshal(bts, &errorResponse); err != nil {
			if response.StatusCode >= http.StatusBadRequest {
				return StatusError{
					StatusCode:   response.StatusCode,
					Status:       response.Status,
					ErrorMessage: string(bts),
				}
			}
			return errors.New(string(bts))
		}

		if response.StatusCode == http.StatusUnauthorized {
			return AuthorizationError{
				StatusCode: response.StatusCode,
				Status:     response.Status,
				SigninURL:  errorResponse.SigninURL,
			}
		} else if response.StatusCode >= http.StatusBadRequest {
			return StatusError{
				StatusCode:   response.StatusCode,
				Status:       response.Status,
				ErrorMessage: errorResponse.Error,
			}
		}

		if errorResponse.Error != "" {
			return errors.New(errorResponse.Error)
		}

		if err := fn(bts); err != nil {
			return err
		}
	}

	if err := scanner.Err(); err != nil {
		return err
	}

	return nil
}

// GenerateResponseFunc 为 [Client.Generate] 的流式回调；返回 error 时停止生成。
// GenerateResponseFunc is a function that [Client.Generate] invokes every time
// a response is received from the service. If this function returns an error,
// [Client.Generate] will stop generating and return this error.
type GenerateResponseFunc func(GenerateResponse) error

// Generate 根据 GenerateRequest 向模型发送提示并流式接收响应。
// Generate generates a response for a given prompt. The req parameter should
// be populated with prompt details. fn is called for each response (there may
// be multiple responses, e.g. in case streaming is enabled).
func (c *Client) Generate(ctx context.Context, req *GenerateRequest, fn GenerateResponseFunc) error {
	return c.stream(ctx, http.MethodPost, "/api/generate", req, func(bts []byte) error {
		var resp GenerateResponse
		if err := json.Unmarshal(bts, &resp); err != nil {
			return err
		}

		return fn(resp)
	})
}

// ChatResponseFunc 为 [Client.Chat] 的流式回调；返回 error 时停止生成。
// ChatResponseFunc is a function that [Client.Chat] invokes every time
// a response is received from the service. If this function returns an error,
// [Client.Chat] will stop generating and return this error.
type ChatResponseFunc func(ChatResponse) error

// Chat 在多轮对话中生成下一条消息，Messages 可携带历史记录。
// Chat generates the next message in a chat. [ChatRequest] may contain a
// sequence of messages which can be used to maintain chat history with a model.
// fn is called for each response (there may be multiple responses, e.g. if case
// streaming is enabled).
func (c *Client) Chat(ctx context.Context, req *ChatRequest, fn ChatResponseFunc) error {
	return c.stream(ctx, http.MethodPost, "/api/chat", req, func(bts []byte) error {
		var resp ChatResponse
		if err := json.Unmarshal(bts, &resp); err != nil {
			return err
		}

		return fn(resp)
	})
}

// PullProgressFunc 为 [Client.Pull] 的进度回调；返回 error 时中止拉取。
// PullProgressFunc is a function that [Client.Pull] invokes every time there
// is progress with a "pull" request sent to the service. If this function
// returns an error, [Client.Pull] will stop the process and return this error.
type PullProgressFunc func(ProgressResponse) error

// Pull 从 Ollama 库下载模型，fn 用于展示进度条等 UI。
// Pull downloads a model from the ollama library. fn is called each time
// progress is made on the request and can be used to display a progress bar,
// etc.
func (c *Client) Pull(ctx context.Context, req *PullRequest, fn PullProgressFunc) error {
	return c.stream(ctx, http.MethodPost, "/api/pull", req, func(bts []byte) error {
		var resp ProgressResponse
		if err := json.Unmarshal(bts, &resp); err != nil {
			return err
		}

		return fn(resp)
	})
}

// PushProgressFunc 为 [Client.Push] 的进度回调。
// PushProgressFunc is a function that [Client.Push] invokes when progress is
// made.
// It's similar to other progress function types like [PullProgressFunc].
type PushProgressFunc func(ProgressResponse) error

// Push 将本地模型上传至模型库；需先在 ollama.ai 注册并添加公钥。
// Push uploads a model to the model library; requires registering for ollama.ai
// and adding a public key first. fn is called each time progress is made on
// the request and can be used to display a progress bar, etc.
func (c *Client) Push(ctx context.Context, req *PushRequest, fn PushProgressFunc) error {
	return c.stream(ctx, http.MethodPost, "/api/push", req, func(bts []byte) error {
		var resp ProgressResponse
		if err := json.Unmarshal(bts, &resp); err != nil {
			return err
		}

		return fn(resp)
	})
}

// CreateProgressFunc 为 [Client.Create] 的进度回调。
// CreateProgressFunc is a function that [Client.Create] invokes when progress
// is made.
// It's similar to other progress function types like [PullProgressFunc].
type CreateProgressFunc func(ProgressResponse) error

// Create 根据 Modelfile 创建模型，fn 行为类似 Pull 的进度回调。
// Create creates a model from a [Modelfile]. fn is a progress function that
// behaves similarly to other methods (see [Client.Pull]).
//
// [Modelfile]: https://github.com/ollama/ollama/blob/main/docs/modelfile.mdx
func (c *Client) Create(ctx context.Context, req *CreateRequest, fn CreateProgressFunc) error {
	return c.stream(ctx, http.MethodPost, "/api/create", req, func(bts []byte) error {
		var resp ProgressResponse
		if err := json.Unmarshal(bts, &resp); err != nil {
			return err
		}

		return fn(resp)
	})
}

// List 列出本地已安装的全部模型。
// List lists models that are available locally.
func (c *Client) List(ctx context.Context) (*ListResponse, error) {
	var lr ListResponse
	if err := c.do(ctx, http.MethodGet, "/api/tags", nil, &lr); err != nil {
		return nil, err
	}
	return &lr, nil
}

// ModelRecommendationsExperimental 查询本地实验性模型推荐端点。
// ModelRecommendationsExperimental lists model recommendations from the local
// server's experimental recommendations endpoint.
func (c *Client) ModelRecommendationsExperimental(ctx context.Context) (*ModelRecommendationsResponse, error) {
	var resp ModelRecommendationsResponse
	if err := c.do(ctx, http.MethodGet, "/api/experimental/model-recommendations", nil, &resp); err != nil {
		return nil, err
	}
	return &resp, nil
}

// ListRunning 列出当前正在内存中运行的模型进程。
// ListRunning lists running models.
func (c *Client) ListRunning(ctx context.Context) (*ProcessResponse, error) {
	var lr ProcessResponse
	if err := c.do(ctx, http.MethodGet, "/api/ps", nil, &lr); err != nil {
		return nil, err
	}
	return &lr, nil
}

// Copy 复制已有模型并以新名称保存副本。
// Copy copies a model - creating a model with another name from an existing
// model.
func (c *Client) Copy(ctx context.Context, req *CopyRequest) error {
	if err := c.do(ctx, http.MethodPost, "/api/copy", req, nil); err != nil {
		return err
	}
	return nil
}

// Delete 删除指定模型及其全部数据。
// Delete deletes a model and its data.
func (c *Client) Delete(ctx context.Context, req *DeleteRequest) error {
	if err := c.do(ctx, http.MethodDelete, "/api/delete", req, nil); err != nil {
		return err
	}
	return nil
}

// Show 获取模型详情，含 modelfile、license、参数等。
// Show obtains model information, including details, modelfile, license etc.
func (c *Client) Show(ctx context.Context, req *ShowRequest) (*ShowResponse, error) {
	var resp ShowResponse
	if err := c.do(ctx, http.MethodPost, "/api/show", req, &resp); err != nil {
		return nil, err
	}
	return &resp, nil
}

// Heartbeat 检测服务是否已启动且可响应；正常时返回 nil。
// Heartbeat checks if the server has started and is responsive; if yes, it
// returns nil, otherwise an error.
func (c *Client) Heartbeat(ctx context.Context) error {
	if err := c.do(ctx, http.MethodHead, "/", nil, nil); err != nil {
		return err
	}
	return nil
}

// Embed 调用 /api/embed 接口生成向量嵌入。
// Embed generates embeddings from a model.
func (c *Client) Embed(ctx context.Context, req *EmbedRequest) (*EmbedResponse, error) {
	var resp EmbedResponse
	if err := c.do(ctx, http.MethodPost, "/api/embed", req, &resp); err != nil {
		return nil, err
	}
	return &resp, nil
}

// Embeddings 调用旧版 /api/embeddings 接口生成单条嵌入向量。
// Embeddings generates an embedding from a model.
func (c *Client) Embeddings(ctx context.Context, req *EmbeddingRequest) (*EmbeddingResponse, error) {
	var resp EmbeddingResponse
	if err := c.do(ctx, http.MethodPost, "/api/embeddings", req, &resp); err != nil {
		return nil, err
	}
	return &resp, nil
}

// CreateBlob 上传 blob 文件至服务器；digest 为期望的 SHA256 摘要。
// CreateBlob creates a blob from a file on the server. digest is the
// expected SHA256 digest of the file, and r represents the file.
func (c *Client) CreateBlob(ctx context.Context, digest string, r io.Reader) error {
	return c.do(ctx, http.MethodPost, fmt.Sprintf("/api/blobs/%s", digest), r, nil)
}

// Version 返回 Ollama 服务器版本字符串。
// Version returns the Ollama server version as a string.
func (c *Client) Version(ctx context.Context) (string, error) {
	var version struct {
		Version string `json:"version"`
	}

	if err := c.do(ctx, http.MethodGet, "/api/version", nil, &version); err != nil {
		return "", err
	}

	return version.Version, nil
}

// CloudStatusExperimental 查询服务器是否禁用云端功能。
// CloudStatusExperimental returns whether cloud features are disabled on the server.
func (c *Client) CloudStatusExperimental(ctx context.Context) (*StatusResponse, error) {
	var status StatusResponse
	if err := c.do(ctx, http.MethodGet, "/api/status", nil, &status); err != nil {
		return nil, err
	}

	return &status, nil
}

// WebSearchExperimental 通过本地实验性端点执行网页搜索。
// WebSearchExperimental searches the web through the local server's
// experimental web search endpoint.
func (c *Client) WebSearchExperimental(ctx context.Context, req *WebSearchRequest) (*WebSearchResponse, error) {
	var resp WebSearchResponse
	if err := c.do(ctx, http.MethodPost, "/api/experimental/web_search", req, &resp); err != nil {
		return nil, err
	}
	return &resp, nil
}

// WebFetchExperimental 通过本地实验性端点抓取网页正文。
// WebFetchExperimental fetches web page content through the local server's
// experimental web fetch endpoint.
func (c *Client) WebFetchExperimental(ctx context.Context, req *WebFetchRequest) (*WebFetchResponse, error) {
	var resp WebFetchResponse
	if err := c.do(ctx, http.MethodPost, "/api/experimental/web_fetch", req, &resp); err != nil {
		return nil, err
	}
	return &resp, nil
}

// Signout 使本地 Ollama 客户端登出。
// Signout will signout a client for a local ollama server.
func (c *Client) Signout(ctx context.Context) error {
	return c.do(ctx, http.MethodPost, "/api/signout", nil, nil)
}

// Disconnect 将 Ollama 实例与 ollama.com 断开连接。
// Disconnect will disconnect an ollama instance from ollama.com.
func (c *Client) Disconnect(ctx context.Context, encodedKey string) error {
	return c.do(ctx, http.MethodDelete, fmt.Sprintf("/api/user/keys/%s", encodedKey), nil, nil)
}

// Whoami 返回当前已登录用户的信息。
func (c *Client) Whoami(ctx context.Context) (*UserResponse, error) {
	var resp UserResponse
	if err := c.do(ctx, http.MethodPost, "/api/me", nil, &resp); err != nil {
		return nil, err
	}
	return &resp, nil
}
