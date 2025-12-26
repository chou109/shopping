package com.neuedu.system.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.neuedu.common.annotation.Excel;
import com.baomidou.mybatisplus.annotation.*;
import lombok.EqualsAndHashCode;
import com.neuedu.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 客服消息对象 customer_message
 * 
 * @author neuedu
 * @date 2025-12-26
 */
@Schema(description = "客服消息对象")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("customer_message")
public class CustomerMessage extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 消息ID */
    @Schema(title = "消息ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话ID - 用于标识一个会话 */
    @Schema(title = "会话ID")
    @Excel(name = "会话ID")
    private String sessionId;

    /** 发送者ID（用户或商家） */
    @Schema(title = "发送者ID")
    @Excel(name = "发送者ID")
    private Long senderId;

    /** 发送者类型（1-用户，2-商家） */
    @Schema(title = "发送者类型")
    @Excel(name = "发送者类型", readConverterExp = "1=用户,2=商家")
    private Integer senderType;

    /** 发送者名称 */
    @Schema(title = "发送者名称")
    @Excel(name = "发送者名称")
    private String senderName;

    /** 发送者头像 */
    @Schema(title = "发送者头像")
    private String senderAvatar;

    /** 接收者ID（用户或商家） */
    @Schema(title = "接收者ID")
    @Excel(name = "接收者ID")
    private Long receiverId;

    /** 接收者类型（1-用户，2-商家） */
    @Schema(title = "接收者类型")
    @Excel(name = "接收者类型", readConverterExp = "1=用户,2=商家")
    private Integer receiverType;

    /** 消息内容 */
    @Schema(title = "消息内容")
    @Excel(name = "消息内容")
    private String content;

    /** 消息类型（1-文本，2-图片，3-订单卡片） */
    @Schema(title = "消息类型")
    @Excel(name = "消息类型", readConverterExp = "1=文本,2=图片,3=订单卡片")
    private Integer messageType;

    /** 关联订单ID（可选） */
    @Schema(title = "关联订单ID")
    private Long orderId;

    /** 关联商品ID（可选） */
    @Schema(title = "关联商品ID")
    private Long productId;

    /** 是否已读（0-未读，1-已读） */
    @Schema(title = "是否已读")
    @Excel(name = "是否已读", readConverterExp = "0=未读,1=已读")
    private Integer isRead;

    /** 已读时间 */
    @Schema(title = "已读时间")
    private Date readTime;

    /** 状态（1-正常，2-已撤回，3-已删除） */
    @Schema(title = "状态")
    @Excel(name = "状态", readConverterExp = "1=正常,2=已撤回,3=已删除")
    private Integer status;

    // ============ 非数据库字段 ============

    @TableField(exist = false)
    private String createBy;

    @TableField(exist = false)
    private String updateBy;

    @TableField(exist = false)
    private String remark;

    @TableField(exist = false)
    private Integer deleted;

    // ============ 常量定义 ============

    /** 发送者类型：用户 */
    public static final Integer SENDER_TYPE_USER = 1;
    /** 发送者类型：商家 */
    public static final Integer SENDER_TYPE_ADMIN = 2;

    /** 消息类型：文本 */
    public static final Integer MESSAGE_TYPE_TEXT = 1;
    /** 消息类型：图片 */
    public static final Integer MESSAGE_TYPE_IMAGE = 2;
    /** 消息类型：订单卡片 */
    public static final Integer MESSAGE_TYPE_ORDER = 3;

    /** 状态：正常 */
    public static final Integer STATUS_NORMAL = 1;
    /** 状态：已撤回 */
    public static final Integer STATUS_RECALLED = 2;
    /** 状态：已删除 */
    public static final Integer STATUS_DELETED = 3;

    // ============ 便捷方法 ============

    /**
     * 判断消息是否来自用户
     */
    public boolean isFromUser() {
        return SENDER_TYPE_USER.equals(senderType);
    }

    /**
     * 判断消息是否来自商家
     */
    public boolean isFromAdmin() {
        return SENDER_TYPE_ADMIN.equals(senderType);
    }

    /**
     * 判断消息是否已读
     */
    public boolean hasRead() {
        return isRead != null && isRead == 1;
    }

    /**
     * 获取发送者类型描述
     */
    public String getSenderTypeDesc() {
        if (senderType == null) return "未知";
        switch (senderType) {
            case 1: return "用户";
            case 2: return "商家客服";
            default: return "未知";
        }
    }

    /**
     * 获取消息类型描述
     */
    public String getMessageTypeDesc() {
        if (messageType == null) return "文本";
        switch (messageType) {
            case 1: return "文本";
            case 2: return "图片";
            case 3: return "订单卡片";
            default: return "文本";
        }
    }

    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        if (status == null) return "正常";
        switch (status) {
            case 1: return "正常";
            case 2: return "已撤回";
            case 3: return "已删除";
            default: return "正常";
        }
    }
}
