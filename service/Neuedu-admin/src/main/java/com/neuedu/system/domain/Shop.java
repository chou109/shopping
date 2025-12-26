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
 * 店铺信息对象 shop
 *
 * @author ruoyi
 * @date 2025-12-21
 */
@Schema(description = "店铺信息对象")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("shop")
public class Shop extends BaseEntity
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

    /** 店铺所有者用户ID */
    @Schema(title = "店铺所有者用户ID")
    @Excel(name = "店铺所有者用户ID")
    private Long userId;

    /** 店铺名称 */
    @Schema(title = "店铺名称")
    @Excel(name = "店铺名称")
    private String shopName;

    /** 店铺logo */
    @Schema(title = "店铺logo")
    @Excel(name = "店铺logo")
    private String shopLogo;

    /** 店铺横幅 */
    @Schema(title = "店铺横幅")
    @Excel(name = "店铺横幅")
    private String shopBanner;

    /** 店铺描述 */
    @Schema(title = "店铺描述")
    @Excel(name = "店铺描述")
    private String description;

    /** 联系电话 */
    @Schema(title = "联系电话")
    @Excel(name = "联系电话")
    private String contactPhone;

    /** 联系地址 */
    @Schema(title = "联系地址")
    @Excel(name = "联系地址")
    private String contactAddress;

    /** 店铺评分 */
    @Schema(title = "店铺评分")
    @Excel(name = "店铺评分")
    private BigDecimal shopScore;

    /** 销售数量 */
    @Schema(title = "销售数量")
    @Excel(name = "销售数量")
    private Long salesCount;

    /** 商品数量 */
    @Schema(title = "商品数量")
    @Excel(name = "商品数量")
    private Long productCount;

    /** 粉丝数量 */
    @Schema(title = "粉丝数量")
    @Excel(name = "粉丝数量")
    private Long followerCount;

    /** 店铺状态 1:正常 2:关闭 3:审核中 */
    @Schema(title = "店铺状态 1:正常 2:关闭 3:审核中")
    @Excel(name = "店铺状态 1:正常 2:关闭 3:审核中")
    private Integer status;


    // ============ 新增的便捷方法 ============

    /**
     * 判断店铺是否正常营业
     */
    public boolean isNormal() {
        return status != null && status == 1;
    }

    /**
     * 判断店铺是否关闭
     */
    public boolean isClosed() {
        return status != null && status == 2;
    }

    /**
     * 判断店铺是否在审核中
     */
    public boolean isPending() {
        return status != null && status == 3;
    }

    /**
     * 判断店铺是否已删除
     */
//    public boolean isDeleted() {
//        return deleteFlag != null && deleteFlag == 1;
//    }

    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        if (status == null) return "未知";
        switch (status) {
            case 1: return "正常营业";
            case 2: return "已关闭";
            case 3: return "审核中";
            default: return "未知状态";
        }
    }
}
