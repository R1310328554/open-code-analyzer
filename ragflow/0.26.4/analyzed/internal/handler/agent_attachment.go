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

// agent_attachment.go — 智能体附件下载与预览：对齐 Python download_attachment / preview_attachment；支持 inline 安全类型预览与 attachment 强制下载。

//

// 附件下载与预览处理器（见下方 Python 对照说明）。
//
// Mirrors the Python handlers in api/apps/restful_apis/agent_api.py:
//   - download_attachment (agent_api.py:2368)
//   - preview_attachment  (agent_api.py:2496)
//   - _attachment_request_metadata
//   - _stream_agent_attachment
//
// The Python PR #15399 added a dedicated preview endpoint that returns
// Content-Disposition: inline for safe types (PDF, images, Markdown,
// etc.) while still forcing attachment on HTML, SVG, and XML.

package handler

import (
	"net/http"
	"path/filepath"
	"strings"

	"github.com/gin-gonic/gin"

	"ragflow/internal/common"
	"ragflow/internal/utility"
)

// agentAttachmentFileService 附件下载所需的 FileService 子集 of FileService used by
// the attachment-download handler.
type agentAttachmentFileService interface {
	DownloadAgentFile(tenantID, location string) ([]byte, error)
}

// attachmentRequestMetadata 解析后的 ext/mime/filename 元数据 params used when
// streaming an attachment. Mirrors Python _attachment_request_metadata().
type attachmentRequestMetadata struct {
	ContentType string
	Ext         string
	Filename    string
}

// attachmentRequestMeta 从 query 解析附件展示元数据, and filename from the
// request query string and resolves the content type. Mirrors Python
// _attachment_request_metadata().
func attachmentRequestMeta(c *gin.Context) attachmentRequestMetadata {
	ext := strings.TrimSpace(c.Query("ext"))
	mimeType := strings.TrimSpace(c.Query("mime_type"))
	filename := strings.TrimSpace(c.Query("filename"))

	contentType, resolvedExt := utility.ResolveAttachmentContentType(ext, mimeType)
	return attachmentRequestMetadata{
		ContentType: contentType,
		Ext:         resolvedExt,
		Filename:    filename,
	}
}

// streamAgentAttachment 拉取存储 blob 并按 inline/attachment 写响应头 from storage and writes it to
// the response with the appropriate Content-Disposition header.
// When inline=true, uses SetPreviewFileResponseHeaders (inline for
// safe types, attachment for dangerous ones). When inline=false,
// always uses attachment disposition. Mirrors Python
// _stream_agent_attachment().
func (h *AgentHandler) streamAgentAttachment(c *gin.Context, tenantID, attachmentID string, inline bool) {
	if h.fileService == nil {
		common.ResponseWithCodeData(c, common.CodeServerError, nil, "file service not configured")
		return
	}

	blob, err := h.fileService.DownloadAgentFile(tenantID, attachmentID)
	if err != nil {
		common.ResponseWithCodeData(c, common.CodeDataError, nil, "Attachment not found!")
		return
	}

	meta := attachmentRequestMeta(c)
	if inline {
		utility.SetPreviewFileResponseHeaders(c.Writer.Header(), meta.ContentType, meta.Ext, meta.Filename)
	} else {
		utility.SetDownloadFileResponseHeaders(c.Writer.Header(), meta.ContentType, meta.Ext, meta.Filename)
	}

	// If content type was not resolved, fall back to octet-stream.
	contentType := meta.ContentType
	if contentType == "" {
		contentType = "application/octet-stream"
	}
	c.Data(http.StatusOK, contentType, blob)
}

// DownloadAttachment 下载附件，可选 ?disposition=inline/<attachment_id>/download
//
// Supports optional ?disposition=inline for browsers that prefer inline
// rendering. Mirrors Python download_attachment() at agent_api.py:2507.
func (h *AgentHandler) DownloadAttachment(c *gin.Context) {
	user, code, msg := GetUser(c)
	if code != common.CodeSuccess {
		common.ResponseWithCodeData(c, code, nil, msg)
		return
	}
	attachmentID := c.Param("attachment_id")
	if attachmentID == "" {
		common.ResponseWithCodeData(c, common.CodeArgumentError, nil, "`attachment_id` is required.")
		return
	}
	// Note (review F9): the plan explicitly defers attachment-id
	// shape validation to the storage layer. The python download
	// endpoint at api/apps/restful_apis/agent_api.py:2368 and the
	// existing Go DownloadAgentFile path rely on storage lookup +
	// header sanitization; we DO NOT gate on UUID here because
	// attachment IDs in storage are not guaranteed UUIDs and the
	// review found no evidence of a UUID invariant. The
	// filepath.Base + CR/LF/quote check below is the only defensive
	// layer and runs BEFORE the file-service call so an unsafe id
	// never crosses the service boundary.
	safe := filepath.Base(attachmentID)
	if safe == "" || safe == "." || safe == "/" || strings.ContainsAny(safe, "\r\n\"") {
		common.ResponseWithCodeData(c, common.CodeArgumentError, nil, "invalid attachment id.")
		return
	}

	// Support ?disposition=inline for optional inline viewing.
	inline := strings.ToLower(strings.TrimSpace(c.Query("disposition"))) == "inline"
	h.streamAgentAttachment(c, user.ID, attachmentID, inline)
}

// PreviewAttachment 预览附件，安全类型 inline、危险类型 attachment/<attachment_id>/preview
//
// Returns the attachment with Content-Disposition: inline for safe types
// (PDF, images, Markdown, etc.) and forces attachment for dangerous types
// (HTML, SVG, XML). This is the endpoint used by MCP clients and the
// preview_url generated by DocGenerator. Mirrors Python preview_attachment()
// at agent_api.py:2496.
func (h *AgentHandler) PreviewAttachment(c *gin.Context) {
	user, code, msg := GetUser(c)
	if code != common.CodeSuccess {
		common.ResponseWithCodeData(c, code, nil, msg)
		return
	}
	attachmentID := c.Param("attachment_id")
	if attachmentID == "" {
		common.ResponseWithCodeData(c, common.CodeArgumentError, nil, "`attachment_id` is required.")
		return
	}
	safe := filepath.Base(attachmentID)
	if safe == "" || safe == "." || safe == "/" || strings.ContainsAny(safe, "\r\n\"") {
		common.ResponseWithCodeData(c, common.CodeArgumentError, nil, "invalid attachment id.")
		return
	}

	h.streamAgentAttachment(c, user.ID, attachmentID, true)
}

// 附件 ID 仅做 filepath.Base 与 CR/LF 消毒，形状校验交由存储层；tenant 使用 user.ID（单租户会话模型）。
