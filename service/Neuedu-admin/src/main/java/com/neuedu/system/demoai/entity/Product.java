// Product.java - 简化版本，只保留数据库实际存在的字段
package com.neuedu.system.demoai.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    @Column(name = "update_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    @Column(name = "create_user")
    private Long createUser;

    @Column(name = "update_user")
    private Long updateUser;

    @Column(name = "delete_flag")
    private Integer deleteFlag;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "name")
    private String name;

    @Column(name = "sub_title")
    private String subTitle;

    @Column(name = "main_image")
    private String mainImage;

    @Column(name = "sub_images")
    private String subImages;

    @Column(name = "activity_id")
    private Long activityId;

    @Column(name = "status")
    private Integer status;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "stock")
    private Integer stock;

    @Column(name = "hot_flag")
    private Integer hotFlag;

    // 如果数据库中没有这些字段，删除或标记为 @Transient
    @Transient
    private String description;  // 可能不存在

    @Transient
    private String afterSalePolicy;  // 可能不存在

    @Transient
    private Boolean hasFreightInsurance;  // 可能不存在

    @Transient
    private String shippingInfo;  // 可能不存在

    @Transient
    private String shippingPolicy;  // 可能不存在

    @Transient
    private String category;  // 可能不存在

    @Transient
    private String brand;  // 可能不存在

    @Transient
    private String details;  // 可能不存在

    // Getter and Setter methods
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }

    public Long getCreateUser() { return createUser; }
    public void setCreateUser(Long createUser) { this.createUser = createUser; }

    public Long getUpdateUser() { return updateUser; }
    public void setUpdateUser(Long updateUser) { this.updateUser = updateUser; }

    public Integer getDeleteFlag() { return deleteFlag; }
    public void setDeleteFlag(Integer deleteFlag) { this.deleteFlag = deleteFlag; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getName() { return name != null ? name : ""; }
    public void setName(String name) { this.name = name; }

    public String getSubTitle() { return subTitle != null ? subTitle : ""; }
    public void setSubTitle(String subTitle) { this.subTitle = subTitle; }

    public String getMainImage() { return mainImage; }
    public void setMainImage(String mainImage) { this.mainImage = mainImage; }

    public String getSubImages() { return subImages; }
    public void setSubImages(String subImages) { this.subImages = subImages; }

    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public BigDecimal getPrice() { return price != null ? price : BigDecimal.ZERO; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Integer getHotFlag() { return hotFlag; }
    public void setHotFlag(Integer hotFlag) { this.hotFlag = hotFlag; }

    // 为 @Transient 字段提供getter/setter
    public String getDescription() { return description != null ? description : ""; }
    public void setDescription(String description) { this.description = description; }

    public String getAfterSalePolicy() { return afterSalePolicy != null ? afterSalePolicy : ""; }
    public void setAfterSalePolicy(String afterSalePolicy) { this.afterSalePolicy = afterSalePolicy; }

    public Boolean getHasFreightInsurance() { return hasFreightInsurance != null ? hasFreightInsurance : false; }
    public void setHasFreightInsurance(Boolean hasFreightInsurance) { this.hasFreightInsurance = hasFreightInsurance; }

    public String getShippingInfo() { return shippingInfo != null ? shippingInfo : ""; }
    public void setShippingInfo(String shippingInfo) { this.shippingInfo = shippingInfo; }

    public String getShippingPolicy() { return shippingPolicy != null ? shippingPolicy : ""; }
    public void setShippingPolicy(String shippingPolicy) { this.shippingPolicy = shippingPolicy; }

    public String getCategory() { return category != null ? category : ""; }
    public void setCategory(String category) { this.category = category; }

    public String getBrand() { return brand != null ? brand : ""; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getDetails() { return details != null ? details : ""; }
    public void setDetails(String details) { this.details = details; }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", subTitle='" + subTitle + '\'' +
                ", price=" + price +
                ", categoryId=" + categoryId +
                '}';
    }
}