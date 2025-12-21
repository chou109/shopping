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
 * 用户信息对象 user
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
@Schema(description = "用户信息对象")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity
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

    /** 启用标志 */
    @Schema(title = "启用标志")
    @Excel(name = "启用标志")
    private Integer status;

    /** 用户名 */
    @Schema(title = "用户名")
    @Excel(name = "用户名")
    private String userName;

    /** 邮箱 */
    @Schema(title = "邮箱")
    @Excel(name = "邮箱")
    private String email;

    /** 手机号 */
    @Schema(title = "手机号")
    @Excel(name = "手机号")
    private String phone;

    /** 密码 */
    @Schema(title = "密码")
    @Excel(name = "密码")
    private String password;

    /** 角色 */
    @Schema(title = "角色")
    @Excel(name = "角色")
    private String type;

    /** 昵称 */
    @Schema(title = "昵称")
    @Excel(name = "昵称")
    private String nickname;

    /** 余额 */
    @Schema(title = "余额")
    @Excel(name = "余额")
    private BigDecimal balance;

    /**
     * 设置默认值
     */
    public void setDefaultValues() {
        if (this.status == null) {
            this.status = 1;
        }
        if (this.balance == null) {
            this.balance = BigDecimal.ZERO;
        }
        if (this.type == null) {
            this.type = "user";
        }
    }
}
