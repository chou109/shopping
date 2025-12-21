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
 * 用户收藏对象 user_favorite
 * 
 * @author ruoyi
 * @date 2025-12-21
 */
@Schema(description = "用户收藏对象")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_favorite")
public class UserFavorite extends BaseEntity
{
    private static final long serialVersionUID = 1L;


    /** 收藏ID */
    @Schema(title = "收藏ID")
    private Long favoriteId;

    /** 用户ID */
    @Schema(title = "用户ID")
    @Excel(name = "用户ID")
    private Long userId;

    /** 收藏类型(1-商品 2-店铺) */
    @Schema(title = "收藏类型(1-商品 2-店铺)")
    @Excel(name = "收藏类型(1-商品 2-店铺)")
    private Integer targetType;

    /** 目标ID(商品ID或店铺ID) */
    @Schema(title = "目标ID(商品ID或店铺ID)")
    @Excel(name = "目标ID(商品ID或店铺ID)")
    private Long targetId;
}
