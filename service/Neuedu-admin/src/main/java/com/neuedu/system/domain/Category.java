package com.neuedu.system.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import com.neuedu.common.core.domain.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "类目对象")
@Data
@TableName("category")
public class Category extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @Schema(title = "主键")
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 创建人 */
    @Schema(title = "创建人")
    @TableField("create_user")
    private Long createUser;

    /** 更新人 */
    @Schema(title = "更新人")
    @TableField("update_user")
    private Long updateUser;

    /** 删除标志 - 使用@TableLogic注解指定逻辑删除字段 */
    @Schema(title = "删除标志")
    @TableField("delete_flag")
    @TableLogic(value = "0", delval = "1")  // 0表示未删除，1表示已删除
    private Integer deleteFlag;

    @TableField(exist = false)
    private Integer deleted;

    /** 父id */
    @Schema(title = "父id")
    @TableField("parent_id")
    private Long parentId;

    /** 名称 */
    @Schema(title = "名称")
    private String name;

    /** 启用禁用状态 1启用 0禁用 */
    @Schema(title = "启用禁用状态 1启用 0禁用")
    private Integer status;

    /** 排序 */
    @Schema(title = "排序")
    @TableField("sort_order")
    private Long sortOrder;

    /** 热门 */
    @Schema(title = "热门")
    @TableField("hot_flag")
    private Integer hotFlag;

    // ================ 排除BaseEntity中的字段 ================
    /** 创建者（不存在于数据库） */
    @TableField(exist = false)
    private String createBy;

    /** 更新者（不存在于数据库） */
    @TableField(exist = false)
    private String updateBy;

    /** 备注（不存在于数据库） */
    @TableField(exist = false)
    private String remark;

    // ================ 树形结构相关字段（非数据库字段） ================
    /** 父类目名称 */
    @TableField(exist = false)
    private String parentName;

    /** 子类目列表 */
    @TableField(exist = false)
    private List<Category> children = new ArrayList<>();


  
}