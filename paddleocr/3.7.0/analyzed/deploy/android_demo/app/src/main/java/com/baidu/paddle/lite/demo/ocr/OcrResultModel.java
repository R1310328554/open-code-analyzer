package com.baidu.paddle.lite.demo.ocr;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

// 单条 OCR 检测结果：四边形顶点、识别文本与方向分类
public class OcrResultModel {
    private List<Point> points;
    private List<Integer> wordIndex;
    private String label;
    private float confidence;
    private float cls_idx;
    private String cls_label;
    private float cls_confidence;

    // 初始化空顶点与字索引列表
    public OcrResultModel() {
        super();
        points = new ArrayList<>();
        wordIndex = new ArrayList<>();
    }

    // 追加检测框顶点坐标
    public void addPoints(int x, int y) {
        Point point = new Point(x, y);
        points.add(point);
    }

    // 追加识别字符在词典中的索引
    public void addWordIndex(int index) {
        wordIndex.add(index);
    }

    // 获取检测框顶点列表
    public List<Point> getPoints() {
        return points;
    }

    public List<Integer> getWordIndex() {
        return wordIndex;
    }

    // 获取识别文本标签
    public String getLabel() {
        return label;
    }

    // 设置识别文本
    public void setLabel(String label) {
        this.label = label;
    }

    // 识别置信度
    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    // 文本行方向分类索引
    public float getClsIdx() {
        return cls_idx;
    }

    public void setClsIdx(float idx) {
        this.cls_idx = idx;
    }

    // 方向分类标签（如 0/180 度）
    public String getClsLabel() {
        return cls_label;
    }

    public void setClsLabel(String label) {
        this.cls_label = label;
    }

    public float getClsConfidence() {
        return cls_confidence;
    }

    public void setClsConfidence(float confidence) {
        this.cls_confidence = confidence;
    }
}
