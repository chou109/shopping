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
 * 【请填写功能名称】对象 banner_info
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
@Schema(description = "【请填写功能名称】对象")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("banner_info")
public class BannerInfo extends BaseEntity
{
    private static final long serialVersionUID = 1L;


    /** 主键ID */
    @Schema(title = "主键ID")
    private Long id;

    /** 图片存储地址 */
    @Schema(title = "图片存储地址")
    @Excel(name = "图片存储地址")
    private String image;

    /** 图片名称 */
    @Schema(title = "图片名称")
    @Excel(name = "图片名称")
    private String title;
}
