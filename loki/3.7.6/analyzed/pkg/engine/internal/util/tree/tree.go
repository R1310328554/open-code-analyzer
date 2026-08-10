package tree

// tree 包定义可打印/可视化的通用树节点 Property 与 Node，供执行计划等结构复用。

// Property 支持单值与多值两种展示形式，由 IsMultiValue 控制括号语法。
// Property represents a property of a [Node]. It is a key-value-pair, where
// the value is either a single value or a list of values.
// When the value is a multi-value, the field IsMultiValue needs to be set to
// `true`.
// A single-value property is represented as `key=value` and a multi-value
// property as `key=(value1, value2, ...)`.
type Property struct {
	// Key is the name of the property.
	Key string
	// Values holds the value(s) of the property.
	Values []any
	// IsMultiValue marks whether the property is a multi-value property.
	IsMultiValue bool
}

// NewProperty 便捷构造属性，values 为可变参数列表。
// NewProperty creates a new Property with the specified key, multi-value flag, and values.
// The multi parameter determines if the property should be treated as a multi-value property.
func NewProperty(key string, multi bool, values ...any) Property {
	return Property{
		Key:          key,
		Values:       values,
		IsMultiValue: multi,
	}
}

// Node 含 Children 与 Comments 两类子节点，Comments 在 Printer 中多缩进一级。
// Node represents a node in a tree structure that can be traversed and printed
// by the [Printer].
// It allows for building hierarchical representations of data where each node
// can have multiple properties and multiple children.
type Node struct {
	// ID is a unique identifier for the node.
	ID string
	// Name is the display name of the node.
	Name string
	// Properties contains a list of key-value properties associated with the node.
	Properties []Property
	// Children are child nodes of the node.
	Children []*Node
	// Comments, like Children, are child nodes of the node, with the difference
	// that comments are indented a level deeper than children. A common use-case
	// for comments are tree-style properties of a node, such as expressions of a
	// physical plan node.
	Comments []*Node
	// Context is an optional value to associate with the node.
	Context any
}

// NewNode 创建无子节点的计划节点，Properties 可选传入。
// NewNode creates a new node with the given name, unique identifier and
// properties.
func NewNode(name, id string, properties ...Property) *Node {
	return &Node{
		ID:         id,
		Name:       name,
		Properties: properties,
	}
}

// AddChild 追加正式子节点并返回新节点指针以便继续链式构建。
// AddChild creates a new node with the given name, unique identifier, and properties
// and adds it to the parent node.
func (n *Node) AddChild(name, id string, properties []Property) *Node {
	child := NewNode(name, id, properties...)
	n.Children = append(n.Children, child)
	return child
}

// AddComment 添加注释性子节点，常用于挂载表达式等附属树。
func (n *Node) AddComment(name, id string, properties []Property) *Node {
	node := NewNode(name, id, properties...)
	n.Comments = append(n.Comments, node)
	return node
}
// Context 可挂载任意上下文对象供遍历方使用。
