package com.neuedu.system.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.neuedu.system.constants.OrderStatusConstants;
import com.neuedu.system.util.OrderStatusUtil;
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
 * 订单对象 order_info
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
@Schema(description = "订单对象")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_info")
public class OrderInfo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 乐观锁版本号 */
    @Version
    private Integer version;

    /** 主键 */
    @Schema(title = "主键")
    @TableId(value = "id", type = IdType.AUTO)
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

    /** 订单编号 */
    @Schema(title = "订单编号")
    @Excel(name = "订单编号")
    private String orderNo;

    /** 支付金额 */
    @Schema(title = "支付金额")
    @Excel(name = "支付金额")
    private BigDecimal payment;

    /** 支付类型 */
    @Schema(title = "支付类型")
    @Excel(name = "支付类型")
    private Integer paymentType;

    /** 支付类型描述 */
    @Schema(title = "支付类型描述")
    @Excel(name = "支付类型描述")
    private String paymentTypeDesc;

    /** 邮费 */
    @Schema(title = "邮费")
    @Excel(name = "邮费")
    private BigDecimal postage;

    /** 订单状态 */
    @Schema(title = "订单状态，10待付款，20待发货，30待收货，40待评价，50交易完成，60已取消")
    @Excel(name = "订单状态")
    private Integer status;

    /** 状态描述 */
    @Schema(title = "状态描述")
    @Excel(name = "状态描述")
    private String statusDesc;

    /** 支付时间 */
    @Schema(title = "支付时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "支付时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date paymentTime;

    /** 地址id */
    @Schema(title = "地址id")
    @Excel(name = "地址id")
    private Long addressId;

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

    /** 城市 */
    @Schema(title = "城市")
    @Excel(name = "城市")
    private String city;

    /** 区 */
    @Schema(title = "区")
    @Excel(name = "区")
    private String area;

    /** 详细地址 */
    @Schema(title = "详细地址")
    @Excel(name = "详细地址")
    private String street;

    /** 邮编 */
    @Schema(title = "邮编")
    @Excel(name = "邮编")
    private String postalCode;

    /** 购买人id */
    @Schema(title = "购买人id")
    @Excel(name = "购买人id")
    private Long userId;
    /** 订单超时时间（用于自动取消） */
    @Schema(title = "订单超时时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "订单超时时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;  // 确保这个字段存在

    /** 发货时间 */
    @Schema(title = "发货时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "发货时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date shipTime;

    /** 确认收货时间 */
    @Schema(title = "确认收货时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "确认收货时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date receiveTime;

    /** 取消时间 */
    @Schema(title = "取消时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "取消时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date cancelTime;

    /** 取消原因 */
    @Schema(title = "取消原因")
    @Excel(name = "取消原因")
    private String cancelReason;

    /** 退款金额 */
    @Schema(title = "退款金额")
    @Excel(name = "退款金额")
    private BigDecimal refundAmount;

    /** 退款时间 */
    @Schema(title = "退款时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "退款时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date refundTime;

    /** 退款原因 */
    @Schema(title = "退款原因")
    @Excel(name = "退款原因")
    private String refundReason;

    /** 是否为盲盒订单 */
    @Schema(title = "是否为盲盒订单")
    @Excel(name = "是否为盲盒订单")
    private Boolean isBlindBox;

    /** 关联用户信息（非数据库字段） */
    @Schema(title = "关联用户信息")
    @TableField(exist = false)
    private User user;

    /** 关联地址信息（非数据库字段） */
    @Schema(title = "关联地址信息")
    @TableField(exist = false)
    private UserAddress address;

    /** 关联订单明细列表（非数据库字段） */
    @Schema(title = "关联订单明细列表")
    @TableField(exist = false)
    private List<OrderDetail> orderDetails;

    /**
     * 设置状态并自动设置状态描述
     */
    public void setStatus(Integer status) {
        this.status = status;
        this.statusDesc = OrderStatusConstants.getOrderStatusDesc(status);
    }

    /**
     * 设置支付类型并自动设置描述
     */
    public void setPaymentType(Integer paymentType) {
        this.paymentType = paymentType;
        this.paymentTypeDesc = OrderStatusConstants.getPaymentTypeDesc(paymentType);
    }

}
