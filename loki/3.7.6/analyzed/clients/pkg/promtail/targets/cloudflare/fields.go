package cloudflare

// Cloudflare Logpull 字段预设：default/minimal/extended/all/custom 五档，
// Fields() 合并附加字段后排序去重，供 target 构造 API 客户端时指定拉取列。

import (
	"fmt"
	"slices"
)

type FieldsType string

const (
	FieldsTypeDefault  FieldsType = "default"
	FieldsTypeMinimal  FieldsType = "minimal"
	FieldsTypeExtended FieldsType = "extended"
	FieldsTypeAll      FieldsType = "all"
	FieldsTypeCustom   FieldsType = "custom"
)

var (
	defaultFields = []string{
		"ClientIP", "ClientRequestHost", "ClientRequestMethod", "ClientRequestURI", "EdgeEndTimestamp", "EdgeResponseBytes",
		"EdgeRequestHost", "EdgeResponseStatus", "EdgeStartTimestamp", "RayID",
	}
	minimalFields = append(defaultFields, []string{
		"ZoneID", "ClientSSLProtocol", "ClientRequestProtocol", "ClientRequestPath", "ClientRequestUserAgent", "ClientRequestReferer",
		"EdgeColoCode", "ClientCountry", "CacheCacheStatus", "CacheResponseStatus", "EdgeResponseContentType", "SecurityLevel",
		"WAFAction", "WAFProfile", "WAFRuleID", "WAFRuleMessage", "EdgeRateLimitID", "EdgeRateLimitAction",
	}...)
	extendedFields = append(minimalFields, []string{
		"ClientSSLCipher", "ClientASN", "ClientIPClass", "CacheResponseBytes", "EdgePathingOp", "EdgePathingSrc", "EdgePathingStatus", "ParentRayID",
		"WorkerCPUTime", "WorkerStatus", "WorkerSubrequest", "WorkerSubrequestCount", "OriginIP", "OriginResponseStatus", "OriginSSLProtocol",
		"OriginResponseHTTPExpires", "OriginResponseHTTPLastModified",
	}...)
	allFields = append(extendedFields, []string{
		"BotScore", "BotScoreSrc", "BotTags", "ClientRequestBytes", "ClientSrcPort", "ClientXRequestedWith", "CacheTieredFill", "EdgeResponseCompressionRatio", "EdgeServerIP", "FirewallMatchesSources",
		"FirewallMatchesActions", "FirewallMatchesRuleIDs", "OriginResponseBytes", "OriginResponseTime", "ClientDeviceType", "WAFFlags", "WAFMatchedVar", "EdgeColoID",
		"RequestHeaders", "ResponseHeaders", "ClientRequestSource",
	}...)
)

// 按 FieldsType 展开预设字段列表，与 additionalFields 合并后排序并 Compact 去重。
// Fields returns the union of a set of fields represented by the Fieldtype and the given additional fields. The returned slice will contain no duplicates.
func Fields(t FieldsType, additionalFields []string) ([]string, error) {
	var fields []string
	switch t {
	case FieldsTypeDefault:
		fields = append(defaultFields, additionalFields...)
	case FieldsTypeMinimal:
		fields = append(minimalFields, additionalFields...)
	case FieldsTypeExtended:
		fields = append(extendedFields, additionalFields...)
	case FieldsTypeAll:
		fields = append(allFields, additionalFields...)
// custom 模式不附加任何预设，仅保留 scrape 配置中的 additional_fields。
	case FieldsTypeCustom:
		fields = append(fields, additionalFields...)
	default:
		return nil, fmt.Errorf("unknown fields type: %s", t)
	}
	// remove duplicates
	slices.Sort(fields)
	return slices.Compact(fields), nil
}
