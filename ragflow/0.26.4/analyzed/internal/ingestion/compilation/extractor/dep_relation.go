// 基于依存树的关系抽取 Go 实现。
// 直接移植 Python DepRelationExtractor，与 semantica 对齐。
// 在依存树（head + dep 标签）上操作，与具体解析器解耦。

package extractor

import (
	"slices"
	"strings"
)


// 动词词形还原表（多语言）
var verbLemma = map[string]string{
	// English
	"founded": "found", "founding": "found",
	"works": "work", "working": "work",
	"based": "base", "basing": "base",
	"located": "locate", "locating": "locate",
	"situated": "situate", "situating": "situate",
	"acquired": "acquire", "acquiring": "acquire",
	"employed": "employ", "employing": "employ",
	"hired": "hire", "hiring": "hire",
	"born":   "bear",
	"joined": "join", "joining": "join",
	"merged": "merge", "merging": "merge",
	"bought": "buy", "buying": "buy",
	"created": "create", "creating": "create",
	"established": "establish", "establishing": "establish",
	"started": "start", "starting": "start",
	"led": "lead", "leading": "lead",
	"managed": "manage", "managing": "manage",
	"headed": "head", "heading": "head",
	"ran": "run", "running": "run",
	"owned": "own", "owning": "own",
	"developed": "develop", "developing": "develop",
	"wrote": "write", "written": "write", "writing": "write",
	"published": "publish", "publishing": "publish",
	"invested": "invest", "investing": "invest",
	"partnered": "partner", "partnering": "partner",
	"collaborated": "collaborate", "collaborating": "collaborate",
	"sets": "set",
	// German
	"gegründet": "gründen", "gründete": "gründen",
	"arbeitet": "arbeiten", "arbeitete": "arbeiten",
	"befindet": "befinden",
	"liegt":    "liegen", "lag": "liegen",
	"geboren":  "gebären",
	"erworben": "erwerben", "erwarb": "erwerben",
	"gekauft": "kaufen", "kaufte": "kaufen",
	"übernommen": "übernehmen", "übernahm": "übernehmen",
	// French
	"fondé": "fonder", "fondée": "fonder",
	"créé": "créer", "créée": "créer",
	"travaille": "travailler",
	"employé":   "employer", "employée": "employer",
	"situé": "situer", "située": "situer",
	"né": "naître", "née": "naître",
	"acquis": "acquérir",
	// Spanish + Portuguese (shared forms)
	"fundado": "fundar", "fundada": "fundar",
	"creado": "crear", "creada": "crear",
	"criado": "criar", "criada": "criar",
	"trabaja": "trabajar", "trabalha": "trabalhar",
	"ubicado": "ubicar", "ubicada": "ubicar",
	"situado": "situar", "situada": "situar",
	"localizado": "localizar", "localizada": "localizar",
	"sediado": "sediar", "sediada": "sediar",
	"nacido": "nacer", "nacida": "nacer",
	"nascido": "nascer", "nascida": "nascer",
}

func lemma(w string) string {
	if l, ok := verbLemma[w]; ok {
		return l
	}
	return w
}

// 动词+介词 → 关系类型（多语言）；键为 verbLemma+prep 或单独 verbLemma。
var depVerbRelations = map[string]string{
	// English
	"found+by": "founded_by", "co-found+by": "founded_by",
	"establish+by": "founded_by", "create+by": "founded_by",
	"set+up": "founded_by", "start+by": "founded_by",
	"work+for": "works_for", "employ+by": "works_for",
	"hire+by": "works_for", "join": "works_for",
	"lead+by": "works_for", "manage+by": "works_for",
	"head+by": "works_for", "run+by": "works_for",
	"own+by": "owns", "develop+by": "develops",
	"write+by": "wrote", "publish+by": "published",
	"invest+in": "invests_in", "partner+with": "partners_with",
	"collaborate+with": "collaborates_with",
	"merge+with":       "merged_with", "subsidiar+y": "is_subsidiary_of",
	"base+in": "located_in", "locate+in": "located_in",
	"situate+in": "located_in", "headquarter+in": "located_in",
	"bear+in": "born_in", "bear+on": "born_in",
	"acquire+by": "acquired", "buy+by": "acquired",
	// German (de)
	"gründen+von": "founded_by", "errichten+von": "founded_by",
	"arbeiten+für": "works_for", "beschäftigen+bei": "works_for",
	"anstellen+bei": "works_for",
	"sich+befinden": "located_in", "liegen+in": "located_in",
	"sitzen+in": "located_in", "gebären+in": "born_in",
	"gebären+am":     "born_in",
	"erwerben+durch": "acquired", "kaufen+durch": "acquired",
	"übernehmen+durch": "acquired",
	// French (fr)
	"fonder+par": "founded_by", "créer+par": "founded_by",
	"établir+par":     "founded_by",
	"travailler+pour": "works_for", "employer+par": "works_for",
	"embaucher+par": "works_for",
	"situer+à":      "located_in", "baser+à": "located_in",
	"implanter+à":  "located_in",
	"naître+à":     "born_in",
	"acquérir+par": "acquired", "racheter+par": "acquired",
	// Spanish (es)
	"fundar+por": "founded_by", "crear+por": "founded_by",
	"establecer+por": "founded_by",
	"trabajar+para":  "works_for", "emplear+por": "works_for",
	"contratar+por": "works_for",
	"ubicar+en":     "located_in", "situar+en": "located_in",
	"tener+sede":   "located_in",
	"nacer+en":     "born_in",
	"adquirir+por": "acquired", "comprar+por": "acquired",
	// Portuguese (pt)
	"criar+por":       "founded_by",
	"estabelecer+por": "founded_by",
	"trabalhar+para":  "works_for", "empregar+por": "works_for",
	"localizar+em": "located_in", "situar+em": "located_in",
	"sediar+em": "located_in",
	"nascer+em": "born_in",
	// Chinese (zh)
	"创立+由": "founded_by", "创建+由": "founded_by",
	"成立+由": "founded_by", "创办+由": "founded_by",
	"设立+由": "founded_by",
	"任职+于": "works_for", "就职+于": "works_for",
	"工作+在": "works_for", "位于+在": "located_in",
	"坐落+在": "located_in", "总部设+在": "located_in",
	"出生+在": "born_in", "出生+于": "born_in",
	"收购+由": "acquired", "并购+由": "acquired",
	// Japanese (ja)
	"設立+によって": "founded_by", "創立+によって": "founded_by",
	"勤務+で": "works_for", "在籍+で": "works_for",
	"位置+に": "located_in", "所在+に": "located_in",
	"本社+を":    "located_in",
	"出生+に":    "born_in",
	"買収+によって": "acquired",
}

// 系词头衔模式：X is [title] of Y → 类型化关系
var depCopulaTitles = map[string][]string{
	"ceo": {"ceo_of", "works_for"}, "cto": {"works_for"},
	"cfo": {"works_for"}, "coo": {"works_for"},
	"vp": {"works_for"}, "director": {"works_for"},
	"manager": {"works_for"}, "engineer": {"works_for"},
	"employee": {"works_for"},
	"founder":  {"founded_by"}, "co-founder": {"founded_by"},
}

// 多跳推理规则：A rel1 B + B rel2 C ⇒ A rel3 C
var multiHopRules = map[string]map[string]string{
	"ceo_of":     {"is_subsidiary_of": "works_for", "located_in": "works_for"},
	"works_for":  {"is_subsidiary_of": "works_for"},
	"founded_by": {"is_subsidiary_of": "founded_by"},
}

// ---------------------------------------------------------------------------
// 各语言依存角色映射（与 Python _LANG_DEP_RULES 一致）
// ---------------------------------------------------------------------------
// 覆盖 en/zh/de/fr/es/pt/ja 七种语言。

type roleSpec struct {
	dep        string // 主依存标签（如 nsubj、obl:agent）
	childDep   string // 复合规则的子依存标签
	caseMarker string // 可选格标记文本（中文「由」、日文「によって」）
}

var langRolesMap = map[string]map[string]roleSpec{
	"en": {
		"pass_subj": {dep: "nsubjpass"},
		"subj":      {dep: "nsubj"},
		"agent":     {dep: "agent", childDep: "pobj"},
		"dobj":      {dep: "dobj"},
		"prep_obj":  {dep: "prep", childDep: "pobj"},
	},
	"de": {
		"subj":     {dep: "sb"},
		"agent":    {dep: "sbp", childDep: "nk"},
		"prep_obj": {dep: "mo", childDep: "nk"},
		// 德语 ROOT 为助动词，真实动词 dep 为 oc
		"root_verb_child": {dep: "oc"},
	},
	"fr": {
		"pass_subj": {dep: "nsubj:pass"},
		"subj":      {dep: "nsubj"},
		"agent":     {dep: "obl:agent"},
		"dobj":      {dep: "obj"},
		"prep_obj":  {dep: "case", childDep: "obl"},
	},
	"es": {
		"subj":     {dep: "nsubj"},
		"agent":    {dep: "obj"},
		"prep_obj": {dep: "case", childDep: "obl"},
	},
	"pt": {
		"pass_subj": {dep: "nsubj:pass"},
		"subj":      {dep: "nsubj"},
		"agent":     {dep: "obl:agent"},
		"dobj":      {dep: "obj"},
		"prep_obj":  {dep: "case", childDep: "obl"},
	},
	"zh": {
		"subj":     {dep: "nsubj"},
		"agent":    {dep: "nmod:prep", caseMarker: "由"},
		"dobj":     {dep: "dobj"},
		"prep_obj": {dep: "case", childDep: "nmod"},
	},
	"ja": {
		"subj":     {dep: "nsubj"},
		"agent":    {dep: "obl", caseMarker: "によって"},
		"dobj":     {dep: "dobj"},
		"prep_obj": {dep: "case", childDep: "obl"},
	},
}

// 各语言系词依存标签集（attr/prep/obj），与 Python _extract_copula 一致。
var copulaDeps = map[string]struct {
	attrDeps []string
	prepDeps []string
	objDeps  []string
}{
	"en": {attrDeps: []string{"attr"}, prepDeps: []string{"prep"}, objDeps: []string{"pobj"}},
	"de": {attrDeps: []string{"pred"}, prepDeps: []string{"mo"}, objDeps: []string{"nk"}},
	"fr": {attrDeps: []string{"attr"}, prepDeps: []string{"case"}, objDeps: []string{"obl"}},
	"es": {attrDeps: []string{"attr"}, prepDeps: []string{"case"}, objDeps: []string{"obl"}},
	"pt": {attrDeps: []string{"attr"}, prepDeps: []string{"case"}, objDeps: []string{"obl"}},
	"zh": {attrDeps: []string{"attr"}, prepDeps: []string{"case"}, objDeps: []string{"nmod"}},
	"ja": {attrDeps: []string{"attr"}, prepDeps: []string{"case"}, objDeps: []string{"obl"}},
}

// 七种语言的 be 动词表面形式（系词检测用）。
var beVerbs = map[string]bool{
	// English
	"is": true, "are": true, "was": true, "were": true, "be": true, "been": true, "being": true,
	// German
	"ist": true, "sind": true, "bin": true, "bist": true, "seid": true,
	"war": true, "waren": true, "gewesen": true, "sein": true,
	// French
	"est": true, "suis": true, "sommes": true, "êtes": true, "sont": true,
	"était": true, "étant": true, "être": true,
	// Spanish + Portuguese (shared forms; french "es" omitted to avoid key collision)
	"es": true, "é": true, "son": true, "são": true,
	"está": true, "están": true, "estão": true,
	"era": true, "eran": true, "eram": true,
	"ser": true, "sido": true, "siendo": true, "sendo": true,
}

// DepToken C++ 解析器返回的 token 依存信息。
type DepToken struct {
	Text  string `json:"text"`
	Head  int    `json:"head"`
	Dep   string `json:"dep"`
	Index int    `json:"index"`
	POS   string `json:"pos,omitempty"`
}

// roleResult getByRole 返回值（实体 + 可选介词）。
type roleResult struct {
	entity Entity
	prep   string // prep lemma for prep_obj role; empty otherwise
}

// ---------------------------------------------------------------------------
// 主入口
// ---------------------------------------------------------------------------

// DepExtractRelations 从依存 parse 树抽取类型化关系。
// lang 为语言代码；maxDistance 为共现最大字符距离（0 默认 100）。
func DepExtractRelations(text string, tokens []DepToken, entities []Entity, lang string, maxDistance int) []Relation {
	entityMap := buildEntityMapMulti(entities)
	var relations []Relation

	// 检测德语 oc 特殊处理
	_, hasRootVerbChild := langRolesMap[lang]["root_verb_child"]

	for _, tok := range tokens {
		if tok.Head != tok.Index {
			continue
		}
		// 德语：ROOT 为助动词，真实动词为 oc 子节点
		if hasRootVerbChild {
			for _, c := range childrenOf(tok.Index, tokens) {
				if c.Dep == langRolesMap[lang]["root_verb_child"].dep {
					if hasNegation(c.Index, tokens) {
						continue
					}
					rels := extractFromRoot(text, c.Index, tokens, entityMap, lang)
					relations = append(relations, rels...)
					if isBeVerb(tok) {
						rels := extractCopula(text, c.Index, tokens, entityMap, lang)
						relations = append(relations, rels...)
					}
				}
			}
			continue
		}
		// 标准语言：ROOT 即主动词
		if hasNegation(tok.Index, tokens) {
			continue
		}
		rels := extractFromRoot(text, tok.Index, tokens, entityMap, lang)
		relations = append(relations, rels...)
		if isBeVerb(tok) {
			rels := extractCopula(text, tok.Index, tokens, entityMap, lang)
			relations = append(relations, rels...)
		}
	}

	// 共现关系（始终执行，与 Python extract 一致）
	if maxDistance <= 0 {
		maxDistance = 100
	}
	relations = append(relations, extractCooccurrence(text, entities, maxDistance)...)

	relations = inferMultiHop(relations)
	relations = dedupRelations(relations)
	return relations
}

// ---------------------------------------------------------------------------
// 否定检测
// ---------------------------------------------------------------------------

func hasNegation(idx int, tokens []DepToken) bool {
	for _, t := range tokens {
		if t.Head == idx && (t.Dep == "neg" || t.Dep == "advmod:neg") {
			return true
		}
	}
	return false
}

func isBeVerb(tok DepToken) bool {
	return beVerbs[strings.ToLower(tok.Text)]
}

// ---------------------------------------------------------------------------
// 多跳推理
// ---------------------------------------------------------------------------

func inferMultiHop(rels []Relation) []Relation {
	bySubj := make(map[string][]Relation)
	for _, r := range rels {
		if r.Predicate == "related_to" {
			continue
		}
		key := strings.ToLower(r.Subject.Text)
		bySubj[key] = append(bySubj[key], r)
	}

	var inferred []Relation
	for _, r := range rels {
		if r.Predicate == "related_to" {
			continue
		}
		objKey := strings.ToLower(r.Object.Text)
		if chain, ok := bySubj[objKey]; ok {
			for _, r2 := range chain {
				if hopRules, ok := multiHopRules[r.Predicate]; ok {
					if inferredPred, ok := hopRules[r2.Predicate]; ok {
						conf := r.Confidence
						if r2.Confidence < conf {
							conf = r2.Confidence
						}
						conf *= 0.9
						r := Relation{
							Subject:    r.Subject,
							Predicate:  inferredPred,
							Object:     r2.Object,
							Confidence: conf,
							Metadata: map[string]interface{}{
								"method": "multi_hop",
								"via":    r.Predicate + "→" + r2.Predicate,
							},
						}
						inferred = append(inferred, r)
					}
				}
			}
		}
	}
	return append(rels, inferred...)
}

// ---------------------------------------------------------------------------
// 实体映射
// ---------------------------------------------------------------------------

func buildEntityMapMulti(entities []Entity) map[string][]Entity {
	m := make(map[string][]Entity)
	for _, e := range entities {
		key := strings.ToLower(e.Text)
		m[key] = append(m[key], e)
		cleaned := strings.TrimRight(e.Text, ".,;:!?")
		cleaned = strings.TrimSpace(cleaned)
		if cleaned != e.Text {
			ckey := strings.ToLower(cleaned)
			m[ckey] = append(m[ckey], e)
		}
	}
	return m
}

func findBestEntity(key string, entityMap map[string][]Entity) *Entity {
	entries := entityMap[strings.ToLower(strings.TrimSpace(key))]
	if len(entries) == 0 {
		return nil
	}
	return &entries[0]
}

// ---------------------------------------------------------------------------
// 语言感知角色查找（替代旧 getChildEntity 等）
// ---------------------------------------------------------------------------

// getByRole 返回语义角色的 (entity, prep) 对，与 Python _get_by_role 一致。
func getByRole(lang string, role string, rootIdx int, tokens []DepToken, entityMap map[string][]Entity) []roleResult {
	roles, ok := langRolesMap[lang]
	if !ok {
		roles = langRolesMap["en"]
	}
	spec, ok := roles[role]
	if !ok {
		return nil
	}

	var results []roleResult
	for _, c := range childrenOf(rootIdx, tokens) {
		if spec.caseMarker != "" {
			// 中/日 agent：检查子树中的格标记
			if c.Dep == spec.dep && hasCaseMarkerInSubtree(c.Index, tokens, spec.caseMarker) {
				if ent := findEntityInSubtree(c.Index, tokens, entityMap); ent != nil {
					results = append(results, roleResult{entity: *ent})
				}
			}
		} else if spec.childDep != "" {
			// 复合规则：父 dep + 子 dep
			if c.Dep == spec.dep {
				prepLemma := strings.ToLower(c.Text)
				for _, gc := range childrenOf(c.Index, tokens) {
					if gc.Dep == spec.childDep {
						if ent := findEntityInSubtree(gc.Index, tokens, entityMap); ent != nil {
							if role == "prep_obj" {
								results = append(results, roleResult{entity: *ent, prep: prepLemma})
							} else {
								results = append(results, roleResult{entity: *ent})
							}
						}
					}
				}
			}
		} else {
			// 简单规则：单一 dep 标签
			if c.Dep == spec.dep {
				if ent := findEntityInSubtree(c.Index, tokens, entityMap); ent != nil {
					results = append(results, roleResult{entity: *ent})
				}
			}
		}
	}
	return results
}

// hasCaseMarkerInSubtree 检查子树是否含格标记。
func hasCaseMarkerInSubtree(idx int, tokens []DepToken, marker string) bool {
	visited := map[int]bool{}
	subtree := collectSubtree(idx, tokens, visited)
	for _, t := range subtree {
		if t.Text == marker {
			return true
		}
	}
	return false
}

func collectSubtree(idx int, tokens []DepToken, visited map[int]bool) []DepToken {
	if visited[idx] {
		return nil
	}
	visited[idx] = true
	result := []DepToken{tokens[idx]}
	for _, c := range childrenOf(idx, tokens) {
		result = append(result, collectSubtree(c.Index, tokens, visited)...)
	}
	return result
}

// ---------------------------------------------------------------------------
// extractFromRoot — 被动/主动/介词模式（语言感知）
// ---------------------------------------------------------------------------

func extractFromRoot(text string, rootIdx int, tokens []DepToken, entityMap map[string][]Entity, lang string) []Relation {
	var relations []Relation
	root := tokens[rootIdx]
	verbLemma := lemma(strings.ToLower(root.Text))

	// 用语言感知映射获取各语义角色
	first := func(lst []roleResult) *Entity {
		if len(lst) > 0 {
			return &lst[0].entity
		}
		return nil
	}

	nsubj := first(getByRole(lang, "subj", rootIdx, tokens, entityMap))
	nsubjpass := first(getByRole(lang, "pass_subj", rootIdx, tokens, entityMap))
	dobj := first(getByRole(lang, "dobj", rootIdx, tokens, entityMap))
	agentList := getByRole(lang, "agent", rootIdx, tokens, entityMap)
	var agentEntity *Entity
	if len(agentList) > 0 {
		agentEntity = &agentList[0].entity
	}
	prepList := getByRole(lang, "prep_obj", rootIdx, tokens, entityMap)
	hasExplicitAgent := agentEntity != nil

	// Detect passive:
	// - explicit pass_subj (en, fr, pt)
	// - subj + agent (zh/ja with agent marker, es-style)
	isPassiveCandidate := hasExplicitAgent

	effectiveNsubjpass := nsubjpass
	effectiveNsubj := nsubj
	if isPassiveCandidate {
		if nsubjpass != nil {
			effectiveNsubjpass = nsubjpass
		} else if nsubj != nil {
			effectiveNsubjpass = nsubj
			effectiveNsubj = nil
		}
	}

	// 被动：X was founded/acquired by Y
	if effectiveNsubjpass != nil && agentEntity != nil {
		candidates := []string{"by", "von", "par", "por", "durch", "由", "によって"}
		for _, candidate := range candidates {
			if relType := lookupVerb(verbLemma, candidate); relType != "" {
				var subj, obj Entity
				if relType == "founded_by" || relType == "acquired" {
					subj, obj = *effectiveNsubjpass, *agentEntity
				} else {
					subj, obj = *agentEntity, *effectiveNsubjpass
				}
				r := makeRelation(subj, relType, obj, 0.90)
				r.Metadata = map[string]interface{}{"method": "passive", "verb": verbLemma}
				relations = append(relations, r)
				break
			}
		}
	}

	// 主动：X VERB Y 或 X VERB prep Y
	if effectiveNsubj != nil {
		if dobj != nil {
			if relType := lookupVerb(verbLemma, ""); relType != "" {
				r := makeRelation(*effectiveNsubj, relType, *dobj, 0.85)
				r.Metadata = map[string]interface{}{"method": "active", "verb": verbLemma}
				relations = append(relations, r)
			}
		}
		for _, pe := range prepList {
			if relType := lookupVerb(verbLemma, pe.prep); relType != "" {
				r := makeRelation(*effectiveNsubj, relType, pe.entity, 0.85)
				r.Metadata = map[string]interface{}{"method": "active_prep", "verb": verbLemma, "prep": pe.prep}
				relations = append(relations, r)
			}
		}
	}

	// 带介词的被动（如 is based in）
	if effectiveNsubjpass != nil && len(prepList) > 0 && agentEntity == nil {
		for _, pe := range prepList {
			relType := lookupVerb(verbLemma, pe.prep)
			if relType == "" {
				relType = lookupVerb("be+"+verbLemma, pe.prep)
			}
			if relType != "" {
				r := makeRelation(*effectiveNsubjpass, relType, pe.entity, 0.85)
				r.Metadata = map[string]interface{}{"method": "passive_prep", "verb": verbLemma, "prep": pe.prep}
				relations = append(relations, r)
			}
		}
	}

	return relations
}

// ---------------------------------------------------------------------------
// 系词抽取（语言感知）
// ---------------------------------------------------------------------------

func extractCopula(text string, rootIdx int, tokens []DepToken, entityMap map[string][]Entity, lang string) []Relation {
	subjList := getByRole(lang, "subj", rootIdx, tokens, entityMap)
	if len(subjList) == 0 {
		return nil
	}
	subj := subjList[0].entity

	cd, ok := copulaDeps[lang]
	if !ok {
		cd = copulaDeps["en"]
	}

	var titleLemma string
	var prepObj *Entity

	for _, c := range childrenOf(rootIdx, tokens) {
		if !slices.Contains(cd.attrDeps, c.Dep) {
			continue
		}
		for _, cc := range childrenOf(c.Index, tokens) {
			if !slices.Contains(cd.prepDeps, cc.Dep) {
				continue
			}
			for _, gc := range childrenOf(cc.Index, tokens) {
				if slices.Contains(cd.objDeps, gc.Dep) {
					if ent := findEntityInSubtree(gc.Index, tokens, entityMap); ent != nil {
						prepObj = ent
						titleLemma = strings.ToLower(c.Text)
					}
					break
				}
			}
		}
	}

	if titleLemma == "" || prepObj == nil {
		return nil
	}

	var relations []Relation
	for keyword, relTypes := range depCopulaTitles {
		if strings.Contains(titleLemma, keyword) {
			for _, rt := range relTypes {
				r := makeRelation(subj, rt, *prepObj, 0.88)
				r.Metadata = map[string]interface{}{"method": "copula", "title": titleLemma}
				relations = append(relations, r)
			}
			break
		}
	}
	return relations
}

// ---------------------------------------------------------------------------
// 辅助函数
// ---------------------------------------------------------------------------

func makeRelation(subj Entity, pred string, obj Entity, conf float64) Relation {
	return Relation{
		Subject:    subj,
		Predicate:  pred,
		Object:     obj,
		Confidence: conf,
		Metadata:   map[string]interface{}{},
	}
}

func lookupVerb(verb, prep string) string {
	if prep != "" {
		if v, ok := depVerbRelations[verb+"+"+prep]; ok {
			return v
		}
		return ""
	}
	return depVerbRelations[verb]
}

func childrenOf(idx int, tokens []DepToken) []DepToken {
	var kids []DepToken
	for _, t := range tokens {
		if t.Head == idx && t.Index != idx {
			kids = append(kids, t)
		}
	}
	return kids
}

func findEntityInSubtree(idx int, tokens []DepToken, emap map[string][]Entity) *Entity {
	words := collectWords(idx, tokens, map[int]bool{})
	if len(words) == 0 {
		return nil
	}
	text := strings.Join(words, " ")
	key := strings.ToLower(strings.TrimSpace(text))
	if ent := findBestEntity(key, emap); ent != nil {
		return ent
	}
	// 中日韩无空格文本，尝试无空格拼接匹配
	noSpace := strings.ToLower(strings.TrimSpace(strings.Join(words, "")))
	if noSpace != key {
		if ent := findBestEntity(noSpace, emap); ent != nil {
			return ent
		}
	}
	for _, sep := range []string{" and ", " or ", ", "} {
		if strings.Contains(key, sep) {
			candidate := strings.TrimSpace(strings.SplitN(key, sep, 2)[0])
			if ent := findBestEntity(candidate, emap); ent != nil {
				return ent
			}
		}
	}
	// 模糊：尝试子串匹配
	for ek, ev := range emap {
		if strings.Contains(ek, key) || strings.Contains(key, ek) {
			e := ev[0]
			return &e
		}
	}
	return nil
}

func collectWords(idx int, tokens []DepToken, visited map[int]bool) []string {
	if visited[idx] {
		return nil
	}
	visited[idx] = true
	tok := tokens[idx]
	if tok.Dep == "prep" || tok.Dep == "punct" || tok.Dep == "det" ||
		tok.Dep == "aux" || tok.Dep == "auxpass" || tok.Dep == "cc" ||
		tok.Dep == "conj" || tok.Dep == "neg" {
		return nil
	}
	var childWords []string
	kids := childrenOf(idx, tokens)
	sortByIndex(kids)
	for _, c := range kids {
		childWords = append(childWords, collectWords(c.Index, tokens, visited)...)
	}
	hasChildBefore := false
	for _, k := range kids {
		if k.Index < idx {
			hasChildBefore = true
			break
		}
	}
	if hasChildBefore {
		return append(childWords, tok.Text)
	}
	return append([]string{tok.Text}, childWords...)
}

func sortByIndex(ts []DepToken) {
	for i := 0; i < len(ts); i++ {
		for j := i + 1; j < len(ts); j++ {
			if ts[j].Index < ts[i].Index {
				ts[i], ts[j] = ts[j], ts[i]
			}
		}
	}
}

func dedupRelations(rels []Relation) []Relation {
	seen := make(map[string]bool)
	var result []Relation
	for _, r := range rels {
		key := strings.ToLower(r.Subject.Text + "|" + r.Predicate + "|" + r.Object.Text)
		rev := strings.ToLower(r.Object.Text + "|" + r.Predicate + "|" + r.Subject.Text)
		if seen[key] || seen[rev] {
			continue
		}
		seen[key] = true
		result = append(result, r)
	}
	return result
}
