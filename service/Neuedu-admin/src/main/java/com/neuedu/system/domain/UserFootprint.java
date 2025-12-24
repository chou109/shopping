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
 * 用户足迹对象 user_footprint
 * 
 * @author ruoyi
 * @date 2025-12-22
 */
@Schema(description = "用户足迹对象")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_footprint")
public class UserFootprint extends BaseEntity
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

    /** 用户ID（关联sys_user.user_id） */
    @Schema(title = "用户ID（关联sys_user.user_id）")
    @Excel(name = "用户ID", readConverterExp = "关=联sys_user.user_id")
    private Long userId;

    /** 浏览类型(1-商品 2-店铺 3-活动) */
    @Schema(title = "浏览类型(1-商品 2-店铺 3-活动)")
    @Excel(name = "浏览类型(1-商品 2-店铺 3-活动)")
    private Integer targetType;

    /** 目标ID（商品ID/店铺ID/活动ID） */
    @Schema(title = "目标ID（商品ID/店铺ID/活动ID）")
    @Excel(name = "目标ID", readConverterExp = "商=品ID/店铺ID/活动ID")
    private Long targetId;

    /** 目标名称 */
    @Schema(title = "目标名称")
    @Excel(name = "目标名称")
    private String targetName;

    /** 目标图片 */
    @Schema(title = "目标图片")
    @Excel(name = "目标图片")
    private String targetImage;

    /** 价格（如果是商品） */
    @Schema(title = "价格（如果是商品）")
    @Excel(name = "价格", readConverterExp = "如=果是商品")
    private BigDecimal price;

    /** 分类ID */
    @Schema(title = "分类ID")
    @Excel(name = "分类ID")
    private Long categoryId;

    /** 分类名称 */
    @Schema(title = "分类名称")
    @Excel(name = "分类名称")
    private String categoryName;

    /** 浏览次数 */
    @Schema(title = "浏览次数")
    @Excel(name = "浏览次数")
    private Long browseCount;

    /** 最后浏览时间 */
    @Schema(title = "最后浏览时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")  // 包含时间
    @Excel(name = "最后浏览时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date lastBrowseTime;
}
