package com.neuedu.system.constants;

/**
 * 分类相关常量
 */
public class CategoryConstants {

    /**
     * 启用状态
     */
    public static final Integer STATUS_ENABLED = 1;  // 启用
    public static final Integer STATUS_DISABLED = 0; // 禁用

    /**
     * 热门标志
     */
    public static final Integer HOT_FLAG_YES = 1;    // 热门
    public static final Integer HOT_FLAG_NO = 0;     // 非热门

    /**
     * 删除标志
     */
    public static final Integer DELETE_FLAG_NO = 0;  // 未删除
    public static final Integer DELETE_FLAG_YES = 1; // 已删除

    /**
     * 根分类的父ID
     */
    public static final Long ROOT_PARENT_ID = 0L;

    private CategoryConstants() {
        // 私有构造器
    }
}