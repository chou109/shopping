package com.neuedu.system.domain;

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
 * 订单记录对象 order_status_record
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
@Schema(description = "订单记录对象")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_status_record")
public class OrderStatusRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;


    /** 主键 */
    @Schema(title = "主键")
    private Long id;

    /** 订单编号 */
    @Schema(title = "订单编号")
    @Excel(name = "订单编号")
    private String orderNo;

    /** 订单明细编号 */
    @Schema(title = "订单明细编号")
    @Excel(name = "订单明细编号")
    private String orderDetailNo;

    /** 商品id */
    @Schema(title = "商品id")
    @Excel(name = "商品id")
    private Long productId;

    /** 商品名称 */
    @Schema(title = "商品名称")
    @Excel(name = "商品名称")
    private String productName;

    /** 订单状态 */
    @Schema(title = "订单状态")
    @Excel(name = "订单状态")
    private Integer status;

    /** 状态描述 */
    @Schema(title = "状态描述")
    @Excel(name = "状态描述")
    private String statusDesc;

    /** 备注 */
    @Schema(title = "备注")
    private String remark;
}
