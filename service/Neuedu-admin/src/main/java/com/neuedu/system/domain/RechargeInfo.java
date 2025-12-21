package com.neuedu.system.domain;

import java.math.BigDecimal;
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
 * 【请填写功能名称】对象 recharge_info
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
@Schema(description = "【请填写功能名称】对象")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("recharge_info")
public class RechargeInfo extends BaseEntity
{
    private static final long serialVersionUID = 1L;


    /** 主键ID */
    @Schema(title = "主键ID")
    private Long id;


    /** 用户ID */
    @Schema(title = "用户ID")
    @Excel(name = "用户ID")
    private Long userId;

    /** 用户名（非数据库字段） */
    @Schema(title = "用户名")
    @Excel(name = "用户名")
    @TableField(exist = false)
    private String userName;

    /** 用户昵称（非数据库字段） */
    @Schema(title = "用户昵称")
    @Excel(name = "用户昵称")
    @TableField(exist = false)
    private String nickName;


    /** 金额 */
    @Schema(title = "金额")
    @Excel(name = "金额")
    private BigDecimal balance;

    /** 订单id */
    @Schema(title = "订单id")
    @Excel(name = "订单id")
    private Long orderId;

    /** 订单编号 */
    @Schema(title = "订单编号")
    @Excel(name = "订单编号")
    private String orderNo;


    /** 充值状态（1-待支付 2-支付成功 3-支付失败 4-已退款） */
    @Schema(title = "充值状态")
    @Excel(name = "充值状态", readConverterExp = "1=待支付,2=支付成功,3=支付失败,4=已退款")
    private Integer status;

    /** 支付方式（1-支付宝 2-微信 3-余额 4-银行卡） */
    @Schema(title = "支付方式")
    @Excel(name = "支付方式", readConverterExp = "1=支付宝,2=微信,3=余额,4=银行卡")
    private Integer paymentType;

    /** 支付时间 */
    @Schema(title = "支付时间")
    @Excel(name = "支付时间")
    private java.util.Date paymentTime;

    /** 支付流水号 */
    @Schema(title = "支付流水号")
    @Excel(name = "支付流水号")
    private String paymentNo;

    /** 备注 */
    @Schema(title = "备注")
    @Excel(name = "备注")
    private String remark;
}
