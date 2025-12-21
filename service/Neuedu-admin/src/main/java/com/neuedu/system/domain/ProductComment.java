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
 * 商品评论对象 product_comment
 * 
 * @author ruoyi
 * @date 2025-12-21
 */
@Schema(description = "商品评论对象")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_comment")
public class ProductComment extends BaseEntity
{
    private static final long serialVersionUID = 1L;


    /** 评论ID */
    @Schema(title = "评论ID")
    private Long commentId;

    /** 商品ID */
    @Schema(title = "商品ID")
    @Excel(name = "商品ID")
    private Long productId;

    /** 用户ID */
    @Schema(title = "用户ID")
    @Excel(name = "用户ID")
    private Long userId;

    /** 订单ID（可选） */
    @Schema(title = "订单ID（可选）")
    @Excel(name = "订单ID", readConverterExp = "可=选")
    private Long orderId;

    /** 父评论ID（用于回复） */
    @Schema(title = "父评论ID（用于回复）")
    @Excel(name = "父评论ID", readConverterExp = "用=于回复")
    private Long parentId;

    /** 评论内容 */
    @Schema(title = "评论内容")
    @Excel(name = "评论内容")
    private String content;

    /** 评论图片 */
    @Schema(title = "评论图片")
    @Excel(name = "评论图片")
    private String images;

    /** 评分（1-5星） */
    @Schema(title = "评分（1-5星）")
    @Excel(name = "评分", readConverterExp = "1=-5星")
    private Integer score;

    /** 是否匿名（0-否，1-是） */
    @Schema(title = "是否匿名（0-否，1-是）")
    @Excel(name = "是否匿名", readConverterExp = "0=-否，1-是")
    private Integer isAnonymous;

    /** 点赞数 */
    @Schema(title = "点赞数")
    @Excel(name = "点赞数")
    private Long likeCount;

    /** 回复数 */
    @Schema(title = "回复数")
    @Excel(name = "回复数")
    private Long replyCount;

    /** 状态（1-正常，2-审核中，3-已删除） */
    @Schema(title = "状态（1-正常，2-审核中，3-已删除）")
    @Excel(name = "状态", readConverterExp = "1=-正常，2-审核中，3-已删除")
    private Integer status;
}
