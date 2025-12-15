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
 * 商品明细对象 product_detail
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
@Schema(description = "商品明细对象")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_detail")
public class ProductDetail extends BaseEntity
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

    /** 商品详情 */
    @Schema(title = "商品详情")
    @Excel(name = "商品详情")
    private String detail;

    /** 商品参数 */
    @Schema(title = "商品参数")
    @Excel(name = "商品参数")
    private String param;

    /** 轮播图片 */
    @Schema(title = "轮播图片")
    @Excel(name = "轮播图片")
    private String rotation;
}
