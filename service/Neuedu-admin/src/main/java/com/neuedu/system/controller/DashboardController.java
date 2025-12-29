package com.neuedu.system.controller;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import com.neuedu.common.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.neuedu.common.core.controller.BaseController;
import com.neuedu.common.core.domain.AjaxResult;
import com.neuedu.system.domain.*;
import com.neuedu.system.service.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * 首页统计Dashboard Controller
 * 根据用户角色返回不同的统计数据：
 * - 超级管理员(user_id=1)：查看所有店铺的统计信息
 * - 商家(user_id=2)：只查看自己店铺的统计信息
 * 
 * @author system
 * @date 2025-12-28
 */
@RestController
@RequestMapping("/system/dashboard")
@Tag(name = "【首页统计】Dashboard")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DashboardController extends BaseController
{
    private final IProductService productService;
    private final IOrderInfoService orderInfoService;
    private final IShopService shopService;
    private final IUserService userService;
    private final ICategoryService categoryService;

    /**
     * 获取首页统计数据
     * 根据当前登录用户角色返回不同的数据
     */
    @Operation(summary = "获取首页统计数据")
    @GetMapping("/statistics")
    public AjaxResult getStatistics()
    {
        Long userId = SecurityUtils.getUserId();
        Map<String, Object> result = new HashMap<>();
        
        // 判断用户角色
        boolean isAdmin = (userId != null && userId == 1L);
        
        if (isAdmin) {
            // 超级管理员：获取所有数据
            result = getAdminStatistics();
        } else {
            // 商家：只获取自己店铺的数据
            result = getMerchantStatistics(userId);
        }
        
        return success(result);
    }

    /**
     * 获取统计卡片数据
     */
    @Operation(summary = "获取统计卡片数据")
    @GetMapping("/stats-cards")
    public AjaxResult getStatsCards()
    {
        Long userId = SecurityUtils.getUserId();
        boolean isAdmin = (userId != null && userId == 1L);
        
        List<Map<String, Object>> statsCards = new ArrayList<>();
        
        if (isAdmin) {
            // 管理员视角
            // 1. 总商品数
            Product productQuery = new Product();
            List<Product> allProducts = productService.selectProductList(productQuery);
            int totalProducts = allProducts != null ? allProducts.size() : 0;
            
            // 2. 总订单数
            OrderInfo orderQuery = new OrderInfo();
            List<OrderInfo> allOrders = orderInfoService.selectOrderInfoList(orderQuery);
            int totalOrders = allOrders != null ? allOrders.size() : 0;
            
            // 3. 总销售额
            BigDecimal totalSales = BigDecimal.ZERO;
            if (allOrders != null) {
                for (OrderInfo order : allOrders) {
                    if (order.getPayment() != null && order.getStatus() != null && order.getStatus() >= 20) {
                        totalSales = totalSales.add(order.getPayment());
                    }
                }
            }
            
            // 4. 总店铺数
            Shop shopQuery = new Shop();
            List<Shop> allShops = shopService.selectShopList(shopQuery);
            int totalShops = allShops != null ? allShops.size() : 0;
            
            // 5. 总用户数
            User userQuery = new User();
            List<User> allUsers = userService.selectUserList(userQuery);
            int totalUsers = allUsers != null ? allUsers.size() : 0;
            
            statsCards.add(createStatsCard("总商品数", String.valueOf(totalProducts), "ShoppingCart", 
                    "linear-gradient(135deg, #1890ff, #36cfc9)", 8.5));
            statsCards.add(createStatsCard("订单数量", String.valueOf(totalOrders), "List", 
                    "linear-gradient(135deg, #52c41a, #95de64)", 12.3));
            statsCards.add(createStatsCard("销售总额", "¥" + totalSales.setScale(2, BigDecimal.ROUND_HALF_UP), "Money", 
                    "linear-gradient(135deg, #faad14, #ffc53d)", 15.7));
            statsCards.add(createStatsCard("店铺数量", String.valueOf(totalShops), "Shop", 
                    "linear-gradient(135deg, #722ed1, #b37feb)", 5.2));
            
        } else {
            // 商家视角 - 根据店铺ID筛选
            Shop myShop = shopService.selectShopByUserId(userId);
            
            if (myShop != null) {
                Long myShopId = myShop.getId();
                
                // 1. 我的商品数 - 通过shopId筛选
                Product productQuery = new Product();
                productQuery.setShopId(myShopId);
                List<Product> myProducts = productService.selectProductList(productQuery);
                int myProductCount = myProducts != null ? myProducts.size() : 0;
                
                // 2. 我的订单数（待发货 status=20）
                int pendingOrders = 0;
                OrderInfo orderQuery = new OrderInfo();
                List<OrderInfo> allOrders = orderInfoService.selectOrderInfoList(orderQuery);
                if (allOrders != null && myProducts != null) {
                    Set<Long> myProductIds = new HashSet<>();
                    for (Product p : myProducts) {
                        myProductIds.add(p.getId());
                    }
                    for (OrderInfo order : allOrders) {
                        if (order.getStatus() != null && order.getStatus() == 20) { // 待发货
                            if (order.getOrderDetails() != null) {
                                for (OrderDetail detail : order.getOrderDetails()) {
                                    if (myProductIds.contains(detail.getProductId())) {
                                        pendingOrders++;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                
                // 3. 店铺销量
                Long salesCount = myShop.getSalesCount() != null ? myShop.getSalesCount() : 0L;
                
                // 4. 店铺评分
                BigDecimal shopScore = myShop.getShopScore() != null ? myShop.getShopScore() : BigDecimal.ZERO;
                
                statsCards.add(createStatsCard("我的商品", String.valueOf(myProductCount), "ShoppingCart", 
                        "linear-gradient(135deg, #1890ff, #36cfc9)", 3.2));
                statsCards.add(createStatsCard("待发货订单", String.valueOf(pendingOrders), "List", 
                        "linear-gradient(135deg, #f5222d, #ff7875)", 0.0));
                statsCards.add(createStatsCard("店铺销量", String.valueOf(salesCount), "TrendCharts", 
                        "linear-gradient(135deg, #52c41a, #95de64)", 8.6));
                statsCards.add(createStatsCard("店铺评分", shopScore.setScale(1, BigDecimal.ROUND_HALF_UP) + "分", "Star", 
                        "linear-gradient(135deg, #faad14, #ffc53d)", 2.1));
            } else {
                // 没有店铺
                statsCards.add(createStatsCard("我的商品", "0", "ShoppingCart", 
                        "linear-gradient(135deg, #1890ff, #36cfc9)", 0.0));
                statsCards.add(createStatsCard("待发货订单", "0", "List", 
                        "linear-gradient(135deg, #f5222d, #ff7875)", 0.0));
                statsCards.add(createStatsCard("店铺销量", "0", "TrendCharts", 
                        "linear-gradient(135deg, #52c41a, #95de64)", 0.0));
                statsCards.add(createStatsCard("店铺评分", "0分", "Star", 
                        "linear-gradient(135deg, #faad14, #ffc53d)", 0.0));
            }
        }
        
        return success(statsCards);
    }

    /**
     * 获取销售趋势数据
     */
    @Operation(summary = "获取销售趋势数据")
    @GetMapping("/sales-trend")
    public AjaxResult getSalesTrend(@RequestParam(defaultValue = "week") String timeRange)
    {
        Long userId = SecurityUtils.getUserId();
        boolean isAdmin = (userId != null && userId == 1L);
        
        // 根据时间范围计算日期
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        List<String> dates = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();
        List<Integer> orderCounts = new ArrayList<>();
        
        int days = 7; // 默认一周
        if ("month".equals(timeRange)) {
            days = 30;
        } else if ("quarter".equals(timeRange)) {
            days = 90;
        } else if ("year".equals(timeRange)) {
            days = 365;
        }
        
        // 获取订单列表
        OrderInfo orderQuery = new OrderInfo();
        List<OrderInfo> allOrders = orderInfoService.selectOrderInfoList(orderQuery);
        
        // 如果是商家，过滤自己店铺的商品订单
        Set<Long> myProductIds = new HashSet<>();
        if (!isAdmin) {
            Shop myShop = shopService.selectShopByUserId(userId);
            if (myShop != null) {
                Product productQuery = new Product();
                productQuery.setShopId(myShop.getId());
                List<Product> myProducts = productService.selectProductList(productQuery);
                if (myProducts != null) {
                    for (Product p : myProducts) {
                        myProductIds.add(p.getId());
                    }
                }
            }
        }
        
        // 按日期统计
        Map<String, BigDecimal> dailyAmounts = new HashMap<>();
        Map<String, Integer> dailyOrders = new HashMap<>();
        
        for (int i = days - 1; i >= 0; i--) {
            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_MONTH, -i);
            String dateStr = sdf.format(calendar.getTime());
            dates.add(dateStr);
            dailyAmounts.put(dateStr, BigDecimal.ZERO);
            dailyOrders.put(dateStr, 0);
        }
        
        if (allOrders != null) {
            for (OrderInfo order : allOrders) {
                if (order.getCreateTime() == null || order.getStatus() == null || order.getStatus() < 20) {
                    continue; // 跳过未支付订单
                }
                
                String orderDate = sdf.format(order.getCreateTime());
                if (!dailyAmounts.containsKey(orderDate)) {
                    continue;
                }
                
                // 商家只统计自己商品的订单
                if (!isAdmin) {
                    boolean isMyOrder = false;
                    if (order.getOrderDetails() != null) {
                        for (OrderDetail detail : order.getOrderDetails()) {
                            if (myProductIds.contains(detail.getProductId())) {
                                isMyOrder = true;
                                break;
                            }
                        }
                    }
                    if (!isMyOrder) continue;
                }
                
                BigDecimal currentAmount = dailyAmounts.get(orderDate);
                if (order.getPayment() != null) {
                    dailyAmounts.put(orderDate, currentAmount.add(order.getPayment()));
                }
                dailyOrders.put(orderDate, dailyOrders.get(orderDate) + 1);
            }
        }
        
        for (String date : dates) {
            amounts.add(dailyAmounts.get(date));
            orderCounts.add(dailyOrders.get(date));
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("dates", dates);
        result.put("amounts", amounts);
        result.put("orderCounts", orderCounts);
        
        return success(result);
    }

    /**
     * 获取热门商品
     */
    @Operation(summary = "获取热门商品")
    @GetMapping("/hot-products")
    public AjaxResult getHotProducts(@RequestParam(defaultValue = "5") Integer limit)
    {
        Long userId = SecurityUtils.getUserId();
        boolean isAdmin = (userId != null && userId == 1L);
        
        List<Product> hotProducts;
        if (isAdmin) {
            hotProducts = productService.selectHotProductList(limit);
        } else {
            // 商家只看自己店铺的热门商品
            Shop myShop = shopService.selectShopByUserId(userId);
            if (myShop != null) {
                Product query = new Product();
                query.setShopId(myShop.getId());
                query.setHotFlag(1);
                List<Product> myHotProducts = productService.selectProductList(query);
                hotProducts = myHotProducts != null ? myHotProducts.subList(0, Math.min(limit, myHotProducts.size())) : new ArrayList<>();
            } else {
                hotProducts = new ArrayList<>();
            }
        }
        
        return success(hotProducts);
    }

    /**
     * 获取最新订单
     */
    @Operation(summary = "获取最新订单")
    @GetMapping("/recent-orders")
    public AjaxResult getRecentOrders(@RequestParam(defaultValue = "5") Integer limit)
    {
        Long userId = SecurityUtils.getUserId();
        boolean isAdmin = (userId != null && userId == 1L);
        
        OrderInfo orderQuery = new OrderInfo();
        List<OrderInfo> allOrders = orderInfoService.selectOrderInfoList(orderQuery);
        List<OrderInfo> recentOrders = new ArrayList<>();
        
        if (allOrders != null) {
            // 按时间倒序排序
            allOrders.sort((o1, o2) -> {
                if (o1.getCreateTime() == null) return 1;
                if (o2.getCreateTime() == null) return -1;
                return o2.getCreateTime().compareTo(o1.getCreateTime());
            });
            
            if (isAdmin) {
                recentOrders = allOrders.subList(0, Math.min(limit, allOrders.size()));
            } else {
                // 商家只看自己店铺商品的订单
                Shop myShop = shopService.selectShopByUserId(userId);
                Set<Long> myProductIds = new HashSet<>();
                if (myShop != null) {
                    Product productQuery = new Product();
                    productQuery.setShopId(myShop.getId());
                    List<Product> myProducts = productService.selectProductList(productQuery);
                    if (myProducts != null) {
                        for (Product p : myProducts) {
                            myProductIds.add(p.getId());
                        }
                    }
                }
                
                for (OrderInfo order : allOrders) {
                    if (recentOrders.size() >= limit) break;
                    if (order.getOrderDetails() != null) {
                        for (OrderDetail detail : order.getOrderDetails()) {
                            if (myProductIds.contains(detail.getProductId())) {
                                recentOrders.add(order);
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        return success(recentOrders);
    }

    /**
     * 获取分类统计
     */
    @Operation(summary = "获取分类统计")
    @GetMapping("/category-stats")
    public AjaxResult getCategoryStats()
    {
        Long userId = SecurityUtils.getUserId();
        boolean isAdmin = (userId != null && userId == 1L);
        
        // 获取分类列表
        List<Category> categories = categoryService.selectCategoryList(new Category());
        
        // 获取商品列表
        Product productQuery = new Product();
        if (!isAdmin) {
            Shop myShop = shopService.selectShopByUserId(userId);
            if (myShop != null) {
                productQuery.setShopId(myShop.getId());
            }
        }
        List<Product> products = productService.selectProductList(productQuery);
        
        // 统计每个分类的商品数量
        Map<Long, Integer> categoryCount = new HashMap<>();
        Map<Long, String> categoryNames = new HashMap<>();
        
        if (categories != null) {
            for (Category cat : categories) {
                categoryCount.put(cat.getId(), 0);
                categoryNames.put(cat.getId(), cat.getName());
            }
        }
        
        if (products != null) {
            for (Product product : products) {
                if (product.getCategoryId() != null && categoryCount.containsKey(product.getCategoryId())) {
                    categoryCount.put(product.getCategoryId(), categoryCount.get(product.getCategoryId()) + 1);
                }
            }
        }
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : categoryCount.entrySet()) {
            if (entry.getValue() > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("categoryId", entry.getKey());
                item.put("categoryName", categoryNames.get(entry.getKey()));
                item.put("productCount", entry.getValue());
                result.add(item);
            }
        }
        
        // 按数量排序
        result.sort((a, b) -> ((Integer) b.get("productCount")).compareTo((Integer) a.get("productCount")));
        
        // 只返回前10个
        if (result.size() > 10) {
            result = result.subList(0, 10);
        }
        
        return success(result);
    }

    /**
     * 获取订单状态分布
     */
    @Operation(summary = "获取订单状态分布")
    @GetMapping("/order-status-distribution")
    public AjaxResult getOrderStatusDistribution()
    {
        Long userId = SecurityUtils.getUserId();
        boolean isAdmin = (userId != null && userId == 1L);
        
        OrderInfo orderQuery = new OrderInfo();
        List<OrderInfo> allOrders = orderInfoService.selectOrderInfoList(orderQuery);
        
        // 如果是商家，过滤自己店铺的商品订单
        Set<Long> myProductIds = new HashSet<>();
        if (!isAdmin) {
            Shop myShop = shopService.selectShopByUserId(userId);
            if (myShop != null) {
                Product productQuery = new Product();
                productQuery.setShopId(myShop.getId());
                List<Product> myProducts = productService.selectProductList(productQuery);
                if (myProducts != null) {
                    for (Product p : myProducts) {
                        myProductIds.add(p.getId());
                    }
                }
            }
        }
        
        // 统计各状态订单数量
        Map<Integer, Integer> statusCount = new HashMap<>();
        statusCount.put(10, 0); // 待付款
        statusCount.put(20, 0); // 待发货
        statusCount.put(30, 0); // 待收货
        statusCount.put(40, 0); // 待评价
        statusCount.put(50, 0); // 交易完成
        statusCount.put(60, 0); // 已取消
        
        if (allOrders != null) {
            for (OrderInfo order : allOrders) {
                // 商家只统计自己商品的订单
                if (!isAdmin) {
                    boolean isMyOrder = false;
                    if (order.getOrderDetails() != null) {
                        for (OrderDetail detail : order.getOrderDetails()) {
                            if (myProductIds.contains(detail.getProductId())) {
                                isMyOrder = true;
                                break;
                            }
                        }
                    }
                    if (!isMyOrder) continue;
                }
                
                Integer status = order.getStatus();
                if (status != null && statusCount.containsKey(status)) {
                    statusCount.put(status, statusCount.get(status) + 1);
                }
            }
        }
        
        List<Map<String, Object>> result = new ArrayList<>();
        result.add(createStatusItem("待付款", statusCount.get(10), "#faad14"));
        result.add(createStatusItem("待发货", statusCount.get(20), "#1890ff"));
        result.add(createStatusItem("待收货", statusCount.get(30), "#722ed1"));
        result.add(createStatusItem("待评价", statusCount.get(40), "#13c2c2"));
        result.add(createStatusItem("交易完成", statusCount.get(50), "#52c41a"));
        result.add(createStatusItem("已取消", statusCount.get(60), "#8c8c8c"));
        
        return success(result);
    }

    /**
     * 获取店铺信息（商家端）
     */
    @Operation(summary = "获取我的店铺信息")
    @GetMapping("/my-shop")
    public AjaxResult getMyShop()
    {
        Long userId = SecurityUtils.getUserId();
        Shop myShop = shopService.selectShopByUserId(userId);
        return success(myShop);
    }

    // ========== 私有辅助方法 ==========

    private Map<String, Object> getAdminStatistics() {
        Map<String, Object> result = new HashMap<>();
        
        // 获取总商品数
        Product productQuery = new Product();
        List<Product> allProducts = productService.selectProductList(productQuery);
        result.put("totalProducts", allProducts != null ? allProducts.size() : 0);
        
        // 获取总订单数
        OrderInfo orderQuery = new OrderInfo();
        List<OrderInfo> allOrders = orderInfoService.selectOrderInfoList(orderQuery);
        result.put("totalOrders", allOrders != null ? allOrders.size() : 0);
        
        // 获取总销售额
        BigDecimal totalSales = BigDecimal.ZERO;
        if (allOrders != null) {
            for (OrderInfo order : allOrders) {
                if (order.getPayment() != null && order.getStatus() != null && order.getStatus() >= 20) {
                    totalSales = totalSales.add(order.getPayment());
                }
            }
        }
        result.put("totalSales", totalSales);
        
        // 获取总店铺数
        Shop shopQuery = new Shop();
        List<Shop> allShops = shopService.selectShopList(shopQuery);
        result.put("totalShops", allShops != null ? allShops.size() : 0);
        
        // 获取总用户数
        User userQuery = new User();
        List<User> allUsers = userService.selectUserList(userQuery);
        result.put("totalUsers", allUsers != null ? allUsers.size() : 0);
        
        result.put("role", "admin");
        return result;
    }

    private Map<String, Object> getMerchantStatistics(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取我的店铺
        Shop myShop = shopService.selectShopByUserId(userId);
        if (myShop != null) {
            result.put("shopName", myShop.getShopName());
            result.put("shopScore", myShop.getShopScore());
            result.put("salesCount", myShop.getSalesCount());
            result.put("followerCount", myShop.getFollowerCount());
        }
        
        // 获取我的商品数 - 通过shopId筛选
        Product productQuery = new Product();
        if (myShop != null) {
            productQuery.setShopId(myShop.getId());
        }
        List<Product> myProducts = productService.selectProductList(productQuery);
        result.put("totalProducts", myProducts != null ? myProducts.size() : 0);
        
        // 获取相关订单统计
        int pendingOrders = 0; // 待发货(status=20)
        int todayOrders = 0; // 今日订单
        BigDecimal todaySales = BigDecimal.ZERO;
        
        OrderInfo orderQuery = new OrderInfo();
        List<OrderInfo> allOrders = orderInfoService.selectOrderInfoList(orderQuery);
        
        if (allOrders != null && myProducts != null) {
            Set<Long> myProductIds = new HashSet<>();
            for (Product p : myProducts) {
                myProductIds.add(p.getId());
            }
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String today = sdf.format(new Date());
            
            for (OrderInfo order : allOrders) {
                boolean isMyOrder = false;
                if (order.getOrderDetails() != null) {
                    for (OrderDetail detail : order.getOrderDetails()) {
                        if (myProductIds.contains(detail.getProductId())) {
                            isMyOrder = true;
                            break;
                        }
                    }
                }
                
                if (isMyOrder) {
                    // 待发货订单 (status=20)
                    if (order.getStatus() != null && order.getStatus() == 20) {
                        pendingOrders++;
                    }
                    
                    // 今日订单
                    if (order.getCreateTime() != null && today.equals(sdf.format(order.getCreateTime()))) {
                        todayOrders++;
                        if (order.getPayment() != null && order.getStatus() != null && order.getStatus() >= 20) {
                            todaySales = todaySales.add(order.getPayment());
                        }
                    }
                }
            }
        }
        
        result.put("pendingOrders", pendingOrders);
        result.put("todayOrders", todayOrders);
        result.put("todaySales", todaySales);
        result.put("role", "merchant");
        
        return result;
    }

    private Map<String, Object> createStatsCard(String title, String value, String icon, String color, double trend) {
        Map<String, Object> card = new HashMap<>();
        card.put("title", title);
        card.put("value", value);
        card.put("icon", icon);
        card.put("color", color);
        card.put("trend", trend);
        return card;
    }

    private Map<String, Object> createStatusItem(String name, Integer value, String color) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("value", value != null ? value : 0);
        item.put("color", color);
        return item;
    }
}
