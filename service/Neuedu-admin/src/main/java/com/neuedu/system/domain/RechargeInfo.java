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
}
