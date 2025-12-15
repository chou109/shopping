package com.neuedu.system.domain;

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
 * 活动对象 activity
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
@Schema(description = "活动对象")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("activity")
public class Activity extends BaseEntity
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

    /** 活动id */
    @Schema(title = "活动id")
    @Excel(name = "活动id")
    private Long activityId;

    /** 活动名称 */
    @Schema(title = "活动名称")
    @Excel(name = "活动名称")
    private String name;

    /** 活动状态 */
    @Schema(title = "活动状态")
    @Excel(name = "活动状态")
    private Integer status;

    /** 活动图片地址 */
    @Schema(title = "活动图片地址")
    @Excel(name = "活动图片地址")
    private String mainImage;

    /** 开始时间 */
    @Schema(title = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "开始时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date startTime;

    /** 结束时间 */
    @Schema(title = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "结束时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date endTime;
}
