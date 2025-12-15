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
 * 购物车对象 cart
 * 
 * @author ruoyi
 * @date 2025-12-10
 */
@Schema(description = "购物车对象")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cart")
public class Cart extends BaseEntity
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

    /** 用户id */
    @Schema(title = "用户id")
    @Excel(name = "用户id")
    private Long userId;

    /** 活动id */
    @Schema(title = "活动id")
    @Excel(name = "活动id")
    private Long activityId;

    /** 活动名称 */
    @Schema(title = "活动名称")
    @Excel(name = "活动名称")
    private String activityName;

    /** 商品id */
    @Schema(title = "商品id")
    @Excel(name = "商品id")
    private Long productId;

    /** 商品名称 */
    @Schema(title = "商品名称")
    @Excel(name = "商品名称")
    private String productName;

    /** 商品简要描述 */
    @Schema(title = "商品简要描述")
    @Excel(name = "商品简要描述")
    private String productSubtitle;

    /** 商品图片地址 */
    @Schema(title = "商品图片地址")
    @Excel(name = "商品图片地址")
    private String productMainImage;

    /** 数量 */
    @Schema(title = "数量")
    @Excel(name = "数量")
    private Integer quantity;

    /** 单价 */
    @Schema(title = "单价")
    @Excel(name = "单价")
    private BigDecimal productUnitPrice;

    /** 是否已选择 1是 0 否 */
    @Schema(title = "是否已选择 1是 0 否")
    @Excel(name = "是否已选择 1是 0 否")
    private Integer selected;

    /** 总价格 */
    @Schema(title = "总价格")
    @Excel(name = "总价格")
    private BigDecimal productTotalPrice;


    /** 关联用户信息（非数据库字段） */
    @Schema(title = "关联用户信息")
    @TableField(exist = false)
    private User user;

    /** 关联商品信息（非数据库字段） */
    @Schema(title = "关联商品信息")
    @TableField(exist = false)
    private Product product;

    /** 关联活动信息（非数据库字段） */
    @Schema(title = "关联活动信息")
    @TableField(exist = false)
    private Activity activity;
}
