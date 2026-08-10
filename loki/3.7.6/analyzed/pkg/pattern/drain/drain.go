// MIT License
//
// Copyright (c) 2022 faceair
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

package drain

// drain 实现 Drain 日志模板挖掘算法：前缀树索引 token 序列，LRU 缓存 LogCluster，支持 logfmt/JSON/标点分词与相似度匹配。

import (
	"math"
	"strconv"
	"strings"
	"time"
	"unicode"
	"unsafe"

	"github.com/hashicorp/golang-lru/v2/simplelru"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/common/model"
)

// Config 控制树深度、相似度阈值 SimTh、最大子节点数、样本间隔与 chunk 寿命等。
type Config struct {
	maxNodeDepth         int
	LogClusterDepth      int
	SimTh                float64
	MaxChildren          int
	ExtraDelimiters      []string
	MaxClusters          int
	ParamString          string
	MaxEvictionRatio     float64
	MaxAllowedLineLength int
	MaxChunkAge          time.Duration
	SampleInterval       time.Duration
}

// Limits 供租户级 JSON 可 tokenize 字段列表等限制注入 Drain。
type Limits interface {
	PatternIngesterTokenizableJSONFields(userID string) []string
}

func createLogClusterCache(maxSize int, onEvict func(int, *LogCluster)) *LogClusterCache {
	if maxSize == 0 {
		maxSize = math.MaxInt
	}
	cache, _ := simplelru.NewLRU[int, *LogCluster](maxSize, onEvict)
	return &LogClusterCache{
		cache: cache,
	}
}

// LogClusterCache 包装 LRU，驱逐时触发指标与 limiter 回调。
type LogClusterCache struct {
	cache simplelru.LRUCache[int, *LogCluster]
}

func (c *LogClusterCache) Values() []*LogCluster {
	return c.cache.Values()
}

func (c *LogClusterCache) Set(key int, cluster *LogCluster) {
	c.cache.Add(key, cluster)
}

func (c *LogClusterCache) Get(key int) *LogCluster {
	cluster, ok := c.cache.Get(key)
	if !ok {
		return nil
	}
	return cluster
}

func createNode() *Node {
	return &Node{
		keyToChildNode: make(map[string]*Node),
		clusterIDs:     make([]int, 0),
	}
}

// Node 为 Drain 前缀树节点：子节点 map 与叶子上关联的 cluster ID 列表。
type Node struct {
	keyToChildNode map[string]*Node
	clusterIDs     []int
}

func DefaultConfig() *Config {
	// TODO(kolesnikovae):
	//
	// This is crucial for Drain to ensure that the first LogClusterDepth tokens
	// are constant (see https://jiemingzhu.github.io/pub/pjhe_icws2017.pdf).
	// We should remove any variables such as timestamps, IDs, IPs, counters, etc.
	// from these tokens.
	//
	// Moreover, Drain is not designed for structured logs. Therefore, we should
	// handle logfmt (and, probably, JSON) logs in a special way:
	//
	// The parse tree should have a fixed length, and the depth should be
	// determined by the number of fields in the logfmt message.
	// A parsing tree should be maintained for each unique field set.
	return &Config{
		// At training, if at the depth of LogClusterDepth there is a cluster with
		// similarity coefficient greater that SimTh, then the log message is added
		// to that cluster. Otherwise, a new cluster is created.
		//
		// LogClusterDepth should be equal to the number of constant tokens from
		// the beginning of the message that likely determine the message contents.
		//
		//  > In this step, Drain traverses from a 1-st layer node, which
		//  > is searched in step 2, to a leaf node. This step is based on
		//  > the assumption that tokens in the beginning positions of a log
		//  > message are more likely to be constants. Specifically, Drain
		//  > selects the next internal node by the tokens in the beginning
		//  > positions of the log message
		LogClusterDepth: 30,
		// SimTh is basically a ratio of matching/total in the cluster.
		// Cluster tokens: "foo <*> bar fred"
		//       Log line: "foo bar baz qux"
		//                  *   *   *   x
		// Similarity of these sequences is 0.75 (the distance)
		// Both SimTh and MaxClusterDepth impact branching factor: the greater
		// MaxClusterDepth and SimTh, the less the chance that there will be
		// "similar" clusters, but the greater the footprint.
		SimTh:                0.3,
		MaxChildren:          15,
		ParamString:          `<_>`,
		MaxClusters:          300,
		MaxEvictionRatio:     0.25,
		MaxAllowedLineLength: 3000,
		MaxChunkAge:          time.Hour,
		SampleInterval:       10 * time.Second,
	}
}

// New 按日志格式选择 tokenizer，创建 LRU 集群缓存与 eviction limiter。
func New(tenantID string, config *Config, limits Limits, format string, metrics *Metrics) *Drain {
	if config.LogClusterDepth < 3 {
		panic("depth argument must be at least 3")
	}
	config.maxNodeDepth = config.LogClusterDepth - 2

	d := &Drain{
		config:   config,
		rootNode: createNode(),
		metrics:  metrics,
		format:   format,
	}

	limiter := newLimiter(config.MaxEvictionRatio)

	var tokenizer LineTokenizer
	switch format {
	case FormatJSON:
		fieldsToTokenize := limits.PatternIngesterTokenizableJSONFields(tenantID)
		tokenizer = newJSONTokenizer(config.ParamString, config.MaxAllowedLineLength, fieldsToTokenize)
	case FormatLogfmt:
		tokenizer = newLogfmtTokenizer(config.ParamString, config.MaxAllowedLineLength)
	default:
		tokenizer = newPunctuationTokenizer(config.MaxAllowedLineLength)
	}

	d.idToCluster = createLogClusterCache(config.MaxClusters, func(int, *LogCluster) {
		if metrics != nil {
			if d.pruning {
				metrics.PatternsPrunedTotal.Inc()
			} else {
				metrics.PatternsEvictedTotal.Inc()
			}
		}
		if !d.pruning {
			limiter.Evict()
		}
	})
	d.tokenizer = &DedupingTokenizer{
		LineTokenizer: tokenizer,
		dedupParam:    config.ParamString,
	}
	d.limiter = limiter
	return d
}

// Drain 维护 rootNode、idToCluster、tokenizer 与训练状态 tokens/state。
type Drain struct {
	config          *Config
	rootNode        *Node
	idToCluster     *LogClusterCache
	clustersCounter int
	metrics         *Metrics
	tokenizer       LineTokenizer
	format          string
	tokens          []string
	state           interface{}
	limiter         *limiter
	pruning         bool
}

func (d *Drain) Clusters() []*LogCluster {
	return d.idToCluster.Values()
}

// Train 在 limiter 允许时分词日志行，匹配或新建 LogCluster 并更新时序样本。
func (d *Drain) Train(content string, ts int64) *LogCluster {
	if !d.limiter.Allow() {
		return nil
	}
	var linesSkipped *prometheus.CounterVec
	if d.metrics != nil {
		linesSkipped = d.metrics.LinesSkipped
	}
	d.tokens, d.state = d.tokenizer.Tokenize(content, d.tokens, d.state, linesSkipped)
	if d.tokens == nil && d.state == nil {
		return nil
	}

	return d.train(d.tokens, d.state, ts, int64(len(content)))
}

func (d *Drain) train(tokens []string, state any, ts int64, contentSize int64) *LogCluster {
	if len(tokens) < 4 {
		if d.metrics != nil && d.metrics.LinesSkipped != nil {
			d.metrics.LinesSkipped.WithLabelValues(TooFewTokens).Inc()
		}
		return nil
	}
	if len(tokens) > 80 {
		if d.metrics != nil && d.metrics.LinesSkipped != nil {
			d.metrics.LinesSkipped.WithLabelValues(TooManyTokens).Inc()
		}
		return nil
	}
	if d.metrics != nil {
		d.metrics.TokensPerLine.Observe(float64(len(tokens)))
		if stateInts, ok := state.([]int); ok {
			d.metrics.StatePerLine.Observe(float64(len(stateInts)))
		}
	}
	matchCluster := d.treeSearch(d.rootNode, tokens, d.config.SimTh, false)
	// Match no existing log cluster
	if matchCluster == nil {
		d.clustersCounter++
		clusterID := d.clustersCounter
		tokens, state = d.tokenizer.Clone(tokens, state)
		matchCluster = &LogCluster{
			Tokens:      tokens,
			TokenState:  state,
			id:          clusterID,
			Size:        1,
			Stringer:    d.tokenizer.Join,
			Chunks:      Chunks{},
			Volume:      contentSize,
			SampleCount: 1,
		}
		modeTs := model.TimeFromUnixNano(ts)
		matchCluster.append(modeTs, d.config.MaxChunkAge, d.config.SampleInterval)
		d.idToCluster.Set(clusterID, matchCluster)
		d.addSeqToPrefixTree(d.rootNode, matchCluster)
		if d.metrics != nil {
			d.metrics.PatternsDetectedTotal.Inc()
		}
	} else {
		matchCluster.Tokens = d.createTemplate(tokens, matchCluster.Tokens)
		matchCluster.append(model.TimeFromUnixNano(ts), d.config.MaxChunkAge, d.config.SampleInterval)
		matchCluster.Volume += contentSize
		matchCluster.SampleCount++
		// Touch cluster to update its state in the cache.
		d.idToCluster.Get(matchCluster.id)
	}
	return matchCluster
}

func deduplicatePlaceholders(line string, placeholder string) string {
	first := strings.Index(line, "<_><_>")
	if first == -1 {
		return line
	}
	builder := make([]byte, 0, len(line))
	low := 0
	for i := first; i < len(line)-5; i++ {
		if line[i:i+len(placeholder)] == placeholder {
			high := i + 3
			for ; high < len(line)-2; high += 3 {
				if line[high:high+len(placeholder)] != placeholder {
					break
				}
			}
			builder = append(builder, line[low:i+len(placeholder)]...)
			low = high
			i = high
		}
	}
	builder = append(builder, line[low:]...)

	return unsafeString(builder)
}

func (d *Drain) Prune() {
	d.pruneTree(d.rootNode)
}

func (d *Drain) pruneTree(node *Node) int {
	for key, child := range node.keyToChildNode {
		if d.pruneTree(child) == 0 {
			delete(node.keyToChildNode, key)
		}
	}

	validClusterIDs := 0
	for _, clusterID := range node.clusterIDs {
		cluster := d.idToCluster.Get(clusterID)
		if cluster != nil {
			validClusterIDs++
		}
	}
	return len(node.keyToChildNode) + validClusterIDs
}

func (d *Drain) Delete(cluster *LogCluster) {
	d.pruning = true
	d.idToCluster.cache.Remove(cluster.id)
	d.pruning = false
}

// treeSearch 按 token 数量进入首层，再沿前缀向下直至叶，调用 fastMatch 选最佳集群。
func (d *Drain) treeSearch(rootNode *Node, tokens []string, simTh float64, includeParams bool) *LogCluster {
	tokenCount := len(tokens)

	// at first level, children are grouped by token (word) count
	curNode, ok := rootNode.keyToChildNode[strconv.Itoa(tokenCount)]

	// no template with same token count yet
	if !ok {
		return nil
	}

	// handle case of empty log string - return the single cluster in that group
	if tokenCount < 2 {
		return d.idToCluster.Get(curNode.clusterIDs[0])
	}

	// find the leaf node for this log - a path of nodes matching the first N tokens (N=tree depth)
	curNodeDepth := 1
	for _, token := range tokens {
		// at max depth
		if curNodeDepth >= d.config.maxNodeDepth {
			break
		}

		// this is last token
		if curNodeDepth == tokenCount {
			break
		}

		keyToChildNode := curNode.keyToChildNode
		curNode, ok = keyToChildNode[token]
		if !ok { // no exact next token exist, try wildcard node
			curNode, ok = keyToChildNode[d.config.ParamString]
		}
		if !ok { // no wildcard node exist
			return nil
		}
		curNodeDepth++
	}

	// get best match among all clusters with same prefix, or None if no match is above sim_th
	cluster := d.fastMatch(curNode.clusterIDs, tokens, simTh, includeParams)
	return cluster
}

// fastMatch 在候选 clusterIDs 中计算序列相似度，取 simTh 以上且参数位最多的集群。
// fastMatch Find the best match for a log message (represented as tokens) versus a list of clusters
func (d *Drain) fastMatch(clusterIDs []int, tokens []string, simTh float64, includeParams bool) *LogCluster {
	var matchCluster, maxCluster *LogCluster

	maxSim := -1.0
	maxParamCount := -1
	for _, clusterID := range clusterIDs {
		// Try to retrieve cluster from cache with bypassing eviction
		// algorithm as we are only testing candidates for a match.
		cluster := d.idToCluster.Get(clusterID)
		if cluster == nil {
			continue
		}
		curSim, paramCount := d.getSeqDistance(cluster.Tokens, tokens, includeParams)
		if paramCount < 0 {
			continue
		}
		if curSim > maxSim || (curSim == maxSim && paramCount > maxParamCount) {
			maxSim = curSim
			maxParamCount = paramCount
			maxCluster = cluster
		}
	}
	if maxSim >= simTh {
		matchCluster = maxCluster
	}
	return matchCluster
}

func (d *Drain) getSeqDistance(clusterTokens, tokens []string, includeParams bool) (float64, int) {
	if len(clusterTokens) != len(tokens) {
		panic("seq1 seq2 be of same length")
	}

	simTokens := 0
	paramCount := 0
	for i := range clusterTokens {
		token1 := clusterTokens[i]
		token2 := tokens[i]
		// Require exact match for marked tokens
		if len(token1) > 0 && token1[0] == 0 && token1 != token2 {
			return 0, -1
		}
		switch token1 {
		case d.config.ParamString:
			paramCount++
		case token2:
			simTokens++
		}
	}
	if includeParams {
		simTokens += paramCount
	}
	retVal := float64(simTokens) / float64(len(clusterTokens))
	return retVal, paramCount
}

// addSeqToPrefixTree 将新集群 token 序列插入前缀树，数字 token 优先走 ParamString 通配路径。
func (d *Drain) addSeqToPrefixTree(rootNode *Node, cluster *LogCluster) {
	tokenCount := len(cluster.Tokens)
	tokenCountStr := strconv.Itoa(tokenCount)

	firstLayerNode, ok := rootNode.keyToChildNode[tokenCountStr]
	if !ok {
		firstLayerNode = createNode()
		rootNode.keyToChildNode[tokenCountStr] = firstLayerNode
	}
	curNode := firstLayerNode

	// handle case of empty log string
	if tokenCount == 0 {
		curNode.clusterIDs = append(curNode.clusterIDs, cluster.id)
		return
	}

	currentDepth := 1
	for _, token := range cluster.Tokens {
		// if at max depth or this is last token in template - add current log cluster to the leaf node
		if (currentDepth >= d.config.maxNodeDepth) || currentDepth >= tokenCount {
			// clean up stale clusters before adding a new one.
			newClusterIDs := make([]int, 0, len(curNode.clusterIDs))
			for _, clusterID := range curNode.clusterIDs {
				if d.idToCluster.Get(clusterID) != nil {
					newClusterIDs = append(newClusterIDs, clusterID)
				}
			}
			newClusterIDs = append(newClusterIDs, cluster.id)
			curNode.clusterIDs = newClusterIDs
			break
		}

		// if token not matched in this layer of existing tree.
		if _, ok = curNode.keyToChildNode[token]; !ok {
			if !d.hasNumbers(token) {
				// Numbers in token: Prioritize the param string path
				if _, ok = curNode.keyToChildNode[d.config.ParamString]; ok {
					if len(curNode.keyToChildNode) < d.config.MaxChildren {
						newNode := createNode()
						curNode.keyToChildNode[token] = newNode
						curNode = newNode
					} else {
						curNode = curNode.keyToChildNode[d.config.ParamString]
					}
				} else {
					if len(curNode.keyToChildNode)+1 < d.config.MaxChildren {
						newNode := createNode()
						curNode.keyToChildNode[token] = newNode
						curNode = newNode
					} else if len(curNode.keyToChildNode)+1 == d.config.MaxChildren {
						newNode := createNode()
						curNode.keyToChildNode[d.config.ParamString] = newNode
						curNode = newNode
					} else {
						curNode = curNode.keyToChildNode[d.config.ParamString]
					}
				}
			} else {
				// No numbers, use the key as-is to traverse
				if _, ok = curNode.keyToChildNode[d.config.ParamString]; !ok {
					newNode := createNode()
					curNode.keyToChildNode[d.config.ParamString] = newNode
					curNode = newNode
				} else {
					curNode = curNode.keyToChildNode[d.config.ParamString]
				}
			}
		} else {
			// if the token is matched
			curNode = curNode.keyToChildNode[token]
		}

		currentDepth++
	}
}

func (d *Drain) hasNumbers(s string) bool {
	for _, c := range s {
		if unicode.IsNumber(c) {
			return true
		}
	}
	return false
}

// createTemplate 将不匹配位置替换为 ParamString，泛化集群模板。
func (d *Drain) createTemplate(tokens, matchClusterTokens []string) []string {
	if len(tokens) != len(matchClusterTokens) {
		panic("seq1 seq2 be of same length")
	}
	for i := range tokens {
		if tokens[i] != matchClusterTokens[i] {
			matchClusterTokens[i] = d.config.ParamString
		}
	}
	return matchClusterTokens
}

func unsafeString(s []byte) string {
	return unsafe.String(unsafe.SliceData(s), len(s)) // #nosec G103 -- we know the string is not mutated -- nosemgrep: use-of-unsafe-block
}

func unsafeBytes(s string) []byte {
	return unsafe.Slice(unsafe.StringData(s), len(s)) // #nosec G103 -- we know the string is not mutated -- nosemgrep: use-of-unsafe-block
}
// Prune/pruneTree 递归清理树中已驱逐的 cluster 引用，保持前缀树与 LRU 一致。
