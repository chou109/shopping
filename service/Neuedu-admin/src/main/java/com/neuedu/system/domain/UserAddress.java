package com.neuedu.system.domain;

import com.neuedu.system.constants.AddressConstants;
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
 * 用户地址对象 user_address
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
@Schema(description = "用户地址对象")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_address")
public class UserAddress extends BaseEntity
{
    private static final long serialVersionUID = 1L;


    /** 主键 */
    @Schema(title = "主键")
    private Long id;

    /** 地址id */
    @Schema(title = "地址id")
    @Excel(name = "地址id")
    private Long addressId;

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

    /** 默认标志 */
    @Schema(title = "默认标志")
    @Excel(name = "默认标志")
    private Integer defaultFlag;

    /** 收货人 */
    @Schema(title = "收货人")
    @Excel(name = "收货人")
    private String receiveName;

    /** 联系号码 */
    @Schema(title = "联系号码")
    @Excel(name = "联系号码")
    private String receivePhone;

    /** 省份 */
    @Schema(title = "省份")
    @Excel(name = "省份")
    private String province;

    /** 省份编码 */
    @Schema(title = "省份编码")
    @Excel(name = "省份编码")
    private String provinceCode;

    /** 城市 */
    @Schema(title = "城市")
    @Excel(name = "城市")
    private String city;

    /** 城市编码 */
    @Schema(title = "城市编码")
    @Excel(name = "城市编码")
    private String cityCode;

    /** 区 */
    @Schema(title = "区")
    @Excel(name = "区")
    private String area;

    /** 区编码 */
    @Schema(title = "区编码")
    @Excel(name = "区编码")
    private String areaCode;

    /** 详细地址 */
    @Schema(title = "详细地址")
    @Excel(name = "详细地址")
    private String street;

    /** 邮编 */
    @Schema(title = "邮编")
    @Excel(name = "邮编")
    private String postalCode;

    /** 地址标签 */
    @Schema(title = "地址标签")
    @Excel(name = "地址标签")
    private Integer addressLabel;

    /** 用户ID */
    @Schema(title = "用户ID")
    private Long userId;
    // ================ 扩展字段 ================
    /** 地址标签名称 */
    @TableField(exist = false)
    private String addressLabelName;

    /** 完整地址 */
    @TableField(exist = false)
    private String fullAddress;

    /**
     * 获取地址标签名称
     */
    public String getAddressLabelName() {
        return AddressConstants.getLabelName(this.addressLabel);
    }

    /**
     * 获取完整地址
     */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (province != null) sb.append(province);
        if (city != null) sb.append(city);
        if (area != null) sb.append(area);
        if (street != null) sb.append(street);
        return sb.toString();
    }
    /**
     * 初始化扩展字段
     */
    public void initExtraFields() {
        this.addressLabelName = getAddressLabelName();
        this.fullAddress = getFullAddress();
    }

}
