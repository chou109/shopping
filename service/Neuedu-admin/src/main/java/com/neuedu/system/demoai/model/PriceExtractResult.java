package com.neuedu.system.demoai.model;

import lombok.Data;

@Data
public class PriceExtractResult {
    private Double price;           // 单个价格
    private Double minPrice;        // 区间最小值
    private Double maxPrice;        // 区间最大值
    private PriceCondition condition;

    // 构造方法
    public PriceExtractResult() {}

    public PriceExtractResult(Double price) {
        this.price = price;
    }

    public PriceExtractResult(Double minPrice, Double maxPrice) {
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.condition = PriceCondition.RANGE;
    }

    // 获取价格条件文本
    public String getConditionText() {
        if (condition == null) return "";

        switch (condition) {
            case ABOVE:
                return price + "元以上";
            case BELOW:
                return price + "元以下";
            case AROUND:
                return price + "元左右";
            case EXACT:
                return price + "元";
            case RANGE:
                return minPrice + "到" + maxPrice + "元";
            default:
                return "";
        }
    }
}