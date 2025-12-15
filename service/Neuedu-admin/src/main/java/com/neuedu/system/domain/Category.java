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
 * 类目对象 category
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
@Schema(description = "类目对象")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("category")
public class Category extends BaseEntity
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

    /** 父id */
    @Schema(title = "父id")
    @Excel(name = "父id")
    private Long parentId;

    /** 名称 */
    @Schema(title = "名称")
    @Excel(name = "名称")
    private String name;

    /** 启用禁用状态 1启用 0禁用 */
    @Schema(title = "启用禁用状态 1启用 0禁用")
    @Excel(name = "启用禁用状态 1启用 0禁用")
    private Integer status;

    /** 排序 */
    @Schema(title = "排序")
    @Excel(name = "排序")
    private Long sortOrder;

    /** 热门 */
    @Schema(title = "热门")
    @Excel(name = "热门")
    private Integer hotFlag;
}
