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
 * 订单明细对象 order_detail
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
@Schema(description = "订单明细对象")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_detail")
public class OrderDetail extends BaseEntity
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

    /** 订单编号 */
    @Schema(title = "订单编号")
    @Excel(name = "订单编号")
    private String orderNo;

    /** 订单明细编号 */
    @Schema(title = "订单明细编号")
    @Excel(name = "订单明细编号")
    private String orderDetailNo;

    /** 活动id */
    @Schema(title = "活动id")
    @Excel(name = "活动id")
    private Long activityId;

    /** 活动名称 */
    @Schema(title = "活动名称")
    @Excel(name = "活动名称")
    private String activityName;

    /** 活动图片地址 */
    @Schema(title = "活动图片地址")
    @Excel(name = "活动图片地址")
    private String activityMainImage;

    /** 商品id */
    @Schema(title = "商品id")
    @Excel(name = "商品id")
    private Long productId;

    /** 商品名称 */
    @Schema(title = "商品名称")
    @Excel(name = "商品名称")
    private String productName;

    /** 商品图片地址 */
    @Schema(title = "商品图片地址")
    @Excel(name = "商品图片地址")
    private String productMainImage;

    /** 单价 */
    @Schema(title = "单价")
    @Excel(name = "单价")
    private BigDecimal currentUnitPrice;

    /** 数量 */
    @Schema(title = "数量")
    @Excel(name = "数量")
    private Long quantity;

    /** 总价 */
    @Schema(title = "总价")
    @Excel(name = "总价")
    private BigDecimal totalPrice;

    /** 购买人id */
    @Schema(title = "购买人id")
    @Excel(name = "购买人id")
    private Long userId;

    /** 订单状态 */
    @Schema(title = "订单状态")
    @Excel(name = "订单状态")
    private Integer status;

    /** 状态描述 */
    @Schema(title = "状态描述")
    @Excel(name = "状态描述")
    private String statusDesc;

    /** 取消时间 */
    @Schema(title = "取消时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "取消时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date cancelTime;

    /** 取消原因 */
    @Schema(title = "取消原因")
    @Excel(name = "取消原因")
    private Long cancelReason;

    /** 发货时间 */
    @Schema(title = "发货时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "发货时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date sendTime;

    /** 签收时间 */
    @Schema(title = "签收时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "签收时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date receiveTime;
}
