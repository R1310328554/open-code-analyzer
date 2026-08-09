package com.taobao.arthas.core.command.model;

/**
 * cat 命令的结构化结果：文件名与文件内容文本。
 * <p>
 * 实现 {@link Countable}，size 按内容长度粗估行数供分页/统计使用。
 *
 * @author gongdewei 2020/5/11
 */
public class CatModel extends ResultModel implements Countable {

    /** 被读取的文件路径 */
    private String file;
    /** 文件内容（可能截断） */
    private String content;

    public CatModel() {
    }

    public CatModel(String file, String content) {
        this.file = file;
        this.content = content;
    }

    @Override
    public String getType() {
        return "cat";
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    /** 按内容长度/100+1 估算条目数，用于结果计数 */
    @Override
    public int size() {
        if (content != null) {
            //粗略计算行数作为item size
            return content.length()/100 + 1;
        }
        return 0;
    }
}
