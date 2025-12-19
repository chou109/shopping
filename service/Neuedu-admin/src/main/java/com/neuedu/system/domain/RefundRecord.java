package com.neuedu.system.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.neuedu.common.annotation.Excel;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.neuedu.common.core.domain.BaseEntity;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 退款记录对象 refund_record
 * 
 * @author ruoyi
 * @date 2025-12-16
 */
@Schema(description = "退款记录对象")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("refund_record")
public class RefundRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;


    /** 主键 */
    @Schema(title = "主键")
    private Long id;

    /** 创建人 */
    @Schema(title = "创建人")
    @Excel(name = "创建人")
    private Long createUser;

    /** 更新人 */
    @Schema(title = "更新人")
    @Excel(name = "更新人")
    private Long updateUser;

    /** 删除标志 */
    @Schema(title = "删除标志")
    @Excel(name = "删除标志")
    private Integer deleteFlag;

    /** 退款单号 */
    @Schema(title = "退款单号")
    @Excel(name = "退款单号")
    private String refundNo;

    /** 订单编号 */
    @Schema(title = "订单编号")
    @Excel(name = "订单编号")
    private String orderNo;

    /** 用户ID */
    @Schema(title = "用户ID")
    @Excel(name = "用户ID")
    private Long userId;

    /** 退款金额 */
    @Schema(title = "退款金额")
    @Excel(name = "退款金额")
    private BigDecimal refundAmount;

    /** 退款状态：1-申请中 2-处理中 3-退款成功 4-退款失败 5-已拒绝 */
    @Schema(title = "退款状态：1-申请中 2-处理中 3-退款成功 4-退款失败 5-已拒绝")
    @Excel(name = "退款状态：1-申请中 2-处理中 3-退款成功 4-退款失败 5-已拒绝")
    private Integer refundStatus;

    /** 退款状态描述 */
    @Schema(title = "退款状态描述")
    @Excel(name = "退款状态描述")
    private String refundStatusDesc;

    /** 退款原因 */
    @Schema(title = "退款原因")
    @Excel(name = "退款原因")
    private String refundReason;

    /** 审核意见 */
    @Schema(title = "审核意见")
    @Excel(name = "审核意见")
    private String auditOpinion;

    /** 退款方式：1-原路退回 2-余额退回 */
    @Schema(title = "退款方式：1-原路退回 2-余额退回")
    @Excel(name = "退款方式：1-原路退回 2-余额退回")
    private Integer refundType;

    /** 退款时间 */
    @Schema(title = "退款时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "退款时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date refundTime;
}
