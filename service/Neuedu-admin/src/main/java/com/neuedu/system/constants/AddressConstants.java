package com.neuedu.system.constants;

/**
 * 地址相关常量,地址常量类
 */
public class AddressConstants {

    /**
     * 默认地址标志
     */
    public static final int DEFAULT_FLAG_YES = 1;    // 是默认地址
    public static final int DEFAULT_FLAG_NO = 0;     // 不是默认地址

    /**
     * 删除标志
     */
    public static final int DELETE_FLAG_NO = 0;      // 未删除
    public static final int DELETE_FLAG_YES = 1;     // 已删除

    /**
     * 地址标签
     */
    public static final int LABEL_NONE = 0;          // 无标签
    public static final int LABEL_HOME = 1;          // 家
    public static final int LABEL_COMPANY = 2;       // 公司
    public static final int LABEL_SCHOOL = 3;        // 学校
    public static final int LABEL_OTHER = 4;         // 其他

    /**
     * 地址标签名称映射
     */
    public static String getLabelName(Integer label) {
        if (label == null) return "无标签";
        switch (label) {
            case LABEL_HOME: return "家";
            case LABEL_COMPANY: return "公司";
            case LABEL_SCHOOL: return "学校";
            case LABEL_OTHER: return "其他";
            default: return "无标签";
        }
    }

    private AddressConstants() {
        // 私有构造器
    }
}