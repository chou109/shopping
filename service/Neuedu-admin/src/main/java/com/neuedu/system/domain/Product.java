package com.neuedu.system.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 商品对象 product
 *
 * @author ruoyi
 * @date 2025-12-13
 */
@Schema(description = "商品对象")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product")
public class Product extends BaseEntity
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

    /** 商品id */
    @Schema(title = "商品id")
    @Excel(name = "商品id")
    private Long productId;

    /** 品类id */
    @Schema(title = "品类id")
    @Excel(name = "品类id")
    private Long categoryId;

    /** 商品名称 */
    @Schema(title = "商品名称")
    @Excel(name = "商品名称")
    private String name;

    /** 简要描述 */
    @Schema(title = "简要描述")
    @Excel(name = "简要描述")
    private String subTitle;

    /** 商品图片地址 */
    @Schema(title = "商品图片地址")
    @Excel(name = "商品图片地址")
    private String mainImage;

    /** 子图片列表 */
    @Schema(title = "子图片列表")
    @Excel(name = "子图片列表")
    private String subImages;

    /** 活动id */
    @Schema(title = "活动id")
    @Excel(name = "活动id")
    private Long activityId;

    /** 商品状态 */
    @Schema(title = "商品状态")
    @Excel(name = "商品状态")
    private Integer status;

    /** 商品单价 */
    @Schema(title = "商品单价")
    @Excel(name = "商品单价")
    private BigDecimal price;

    /** 库存数 */
    @Schema(title = "库存数")
    @Excel(name = "库存数")
    private Long stock;

    /** 热门 */
    @Schema(title = "热门")
    @Excel(name = "热门")
    private Integer hotFlag;

    // ============ 关联字段（非数据库字段） ============

    /** 关联分类信息（非数据库字段） */
    @Schema(title = "关联分类信息")
    @TableField(exist = false)
    private Category category;

    /** 关联活动信息（非数据库字段） */
    @Schema(title = "关联活动信息")
    @TableField(exist = false)
    private Activity activity;

    /** 店铺id */
    @Schema(title = "店铺id")
    @Excel(name = "店铺id")
    @TableField(exist = false)
    private Long shopId;

    // ============ 查询参数字段（非数据库字段） ============

    /** 最小价格（查询参数） */
    @Schema(title = "最小价格")
    @TableField(exist = false)
    private BigDecimal minPrice;

    /** 最大价格（查询参数） */
    @Schema(title = "最大价格")
    @TableField(exist = false)
    private BigDecimal maxPrice;

    /** 最小库存（查询参数） */
    @Schema(title = "最小库存")
    @TableField(exist = false)
    private Long minStock;

    /** 最大库存（查询参数） */
    @Schema(title = "最大库存")
    @TableField(exist = false)
    private Long maxStock;

    /** 开始创建时间（查询参数） */
    @Schema(title = "开始创建时间")
    @TableField(exist = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate beginCreateTime;

    /** 结束创建时间（查询参数） */
    @Schema(title = "结束创建时间")
    @TableField(exist = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endCreateTime;
}
