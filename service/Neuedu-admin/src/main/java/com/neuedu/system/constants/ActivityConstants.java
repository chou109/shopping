package com.neuedu.system.constants;

import java.util.Date;

/**
 * 活动相关常量
 */
public class ActivityConstants {

    /**
     * 活动状态
     */
    public static final int STATUS_NOT_STARTED = 0;  // 未开始
    public static final int STATUS_ONGOING = 1;      // 进行中
    public static final int STATUS_ENDED = 2;        // 已结束
    public static final int STATUS_CANCELLED = 3;    // 已取消

    /**
     * 删除标志
     */
    public static final int DELETE_FLAG_NO = 0;      // 未删除
    public static final int DELETE_FLAG_YES = 1;     // 已删除

    /**
     * 根据时间判断活动状态
     */
    public static int calculateStatus(Date startTime, Date endTime, Integer originalStatus) {
        if (originalStatus == null) {
            originalStatus = STATUS_NOT_STARTED;
        }

        // 如果是已取消状态，保持不变
        if (originalStatus == STATUS_CANCELLED) {
            return STATUS_CANCELLED;
        }

        Date now = new Date();

        if (startTime == null || endTime == null) {
            return originalStatus;
        }

        if (now.before(startTime)) {
            return STATUS_NOT_STARTED;
        } else if (now.after(endTime)) {
            return STATUS_ENDED;
        } else {
            return STATUS_ONGOING;
        }
    }

    /**
     * 获取状态名称
     */
    public static String getStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case STATUS_NOT_STARTED: return "未开始";
            case STATUS_ONGOING: return "进行中";
            case STATUS_ENDED: return "已结束";
            case STATUS_CANCELLED: return "已取消";
            default: return "未知";
        }
    }

    private ActivityConstants() {
        // 私有构造器
    }
}