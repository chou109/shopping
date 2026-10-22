package com.neuedu.system.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neuedu.common.core.domain.entity.SysUser;
import com.neuedu.common.utils.SecurityUtils;
import com.neuedu.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.neuedu.common.annotation.Log;
import com.neuedu.common.core.controller.BaseController;
import com.neuedu.common.core.domain.AjaxResult;
import com.neuedu.common.enums.BusinessType;
import com.neuedu.system.domain.ProductComment;
import com.neuedu.system.service.IProductCommentService;
import com.neuedu.common.utils.poi.ExcelUtil;
import com.neuedu.common.core.page.TableDataInfo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * 商品评论Controller
 * 
 * @author ruoyi
 * @date 2025-12-21
 */
@RestController
@RequestMapping("/system/comment")
@Tag(name = "【商品评论】管理")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProductCommentController extends BaseController
{
    private final IProductCommentService productCommentService;

    /**
     * 查询商品评论列表
     */
    @Operation(summary = "查询商品评论列表")
    @PreAuthorize("@ss.hasPermi('system:comment:list')")
    @GetMapping("/list")
    public TableDataInfo list(ProductComment productComment)
    {
        startPage();
        List<ProductComment> list = productCommentService.selectProductCommentList(productComment);
        return getDataTable(list);
    }

    /**
     * 导出商品评论列表
     */
    @Operation(summary = "导出商品评论列表")
    @PreAuthorize("@ss.hasPermi('system:comment:export')")
    @Log(title = "商品评论", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ProductComment productComment)
    {
        List<ProductComment> list = productCommentService.selectProductCommentList(productComment);
        ExcelUtil<ProductComment> util = new ExcelUtil<ProductComment>(ProductComment.class);
        util.exportExcel(response, list, "商品评论数据");
    }

    /**
     * 获取商品评论详细信息
     */
    @Operation(summary = "获取商品评论详细信息")
    @PreAuthorize("@ss.hasPermi('system:comment:query')")
    @GetMapping(value = "/{commentId}")
    public AjaxResult getInfo(@PathVariable("commentId") Long commentId)
    {
        return success(productCommentService.selectProductCommentByCommentId(commentId));
    }

    /**
     * 新增商品评论
     */
    @Operation(summary = "新增商品评论")
    @PreAuthorize("@ss.hasPermi('system:comment:add')")
    @Log(title = "商品评论", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ProductComment productComment)
    {
        return toAjax(productCommentService.insertProductComment(productComment));
    }

    /**
     * 修改商品评论
     */
    @Operation(summary = "修改商品评论")
    @PreAuthorize("@ss.hasPermi('system:comment:edit')")
    @Log(title = "商品评论", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ProductComment productComment)
    {
        return toAjax(productCommentService.updateProductComment(productComment));
    }

    /**
     * 删除商品评论
     */
    @Operation(summary = "删除商品评论")
    @PreAuthorize("@ss.hasPermi('system:comment:remove')")
    @Log(title = "商品评论", businessType = BusinessType.DELETE)
	@DeleteMapping("/{commentIds}")
    public AjaxResult remove(@PathVariable( name = "commentIds" ) Long[] commentIds) 
    {
        return toAjax(productCommentService.deleteProductCommentByCommentIds(commentIds));
    }


    // ============ 新增的接口 ============

    /**
     * 发表评论
     */
    @Operation(summary = "发表评论")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/publish")
    public AjaxResult publish(@RequestBody ProductComment comment) {
        boolean result = productCommentService.publishComment(comment);
        return success(result);
    }

    /**
     * 回复评论
     */
    @Operation(summary = "回复评论")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/reply/{parentId}")
    public AjaxResult reply(@PathVariable Long parentId, @RequestBody Map<String, String> params) {
        String content = params.get("content");
        Long userId = getUserId();
        boolean result = productCommentService.replyComment(parentId, content, userId);
        return success(result);
    }

    /**
     * 获取商品评论列表（前台）
     */
    @Operation(summary = "获取商品评论列表（前台）")
    @GetMapping("/product/{productId}")
    public TableDataInfo getProductComments(@PathVariable Long productId,
                                            @RequestParam(required = false) Integer score,
                                            @RequestParam(required = false) Boolean hasImages) {
        startPage();
        List<ProductComment> list = productCommentService.getProductComments(productId, score, hasImages);
        return getDataTable(list);
    }

    /**
     * 获取商品的根评论列表
     */
    @Operation(summary = "获取商品的根评论列表")
    @GetMapping("/product/{productId}/roots")
    public AjaxResult getRootComments(@PathVariable Long productId) {
        List<ProductComment> list = productCommentService.getRootComments(productId);
        return success(list);
    }

    /**
     * 获取评论的回复列表
     */
    @Operation(summary = "获取评论的回复列表")
    @GetMapping("/{commentId}/replies")
    public AjaxResult getCommentReplies(@PathVariable Long commentId) {
        List<ProductComment> list = productCommentService.getCommentReplies(commentId);
        return success(list);
    }

    /**
     * 点赞评论
     */
    @Operation(summary = "点赞评论")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{commentId}/like")
    public AjaxResult likeComment(@PathVariable Long commentId) {
        boolean result = productCommentService.likeComment(commentId, getUserId());
        return success(result);
    }

    /**
     * 取消点赞评论
     */
    @Operation(summary = "取消点赞评论")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{commentId}/unlike")
    public AjaxResult unlikeComment(@PathVariable Long commentId) {
        boolean result = productCommentService.unlikeComment(commentId, getUserId());
        return success(result);
    }

    /**
     * 获取商品评论统计
     */
    @Operation(summary = "获取商品评论统计")
    @GetMapping("/product/{productId}/statistics")
    public AjaxResult getCommentStatistics(@PathVariable Long productId) {
        Map<String, Object> stats = productCommentService.getCommentStatistics(productId);
        return success(stats);
    }

    /**
     * 获取商品评分分布
     */
    @Operation(summary = "获取商品评分分布")
    @GetMapping("/product/{productId}/scoreDistribution")
    public AjaxResult getScoreDistribution(@PathVariable Long productId) {
        Map<Integer, Long> distribution = productCommentService.getScoreDistribution(productId);
        return success(distribution);
    }

    /**
     * 获取商品平均评分
     */
    @Operation(summary = "获取商品平均评分")
    @GetMapping("/product/{productId}/averageScore")
    public AjaxResult getAverageScore(@PathVariable Long productId) {
        Double score = productCommentService.getAverageScore(productId);
        return success(score != null ? String.format("%.1f", score) : "0.0");
    }

    /**
     * 审核评论
     */
    @Operation(summary = "审核评论")
    @PreAuthorize("@ss.hasPermi('system:comment:audit')")
    @Log(title = "商品评论", businessType = BusinessType.UPDATE)
    @PostMapping("/audit/{commentId}")
    public AjaxResult auditComment(@PathVariable Long commentId,
                                   @RequestBody Map<String, Object> params) {
        Integer status = Integer.valueOf(params.get("status").toString());
        String reason = (String) params.get("reason");
        boolean result = productCommentService.auditComment(commentId, status, reason);
        return success(result);
    }

    /**
     * 我的评论列表
     */
    @Operation(summary = "我的评论列表")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my")
    public TableDataInfo myComments() {
        startPage();
        List<ProductComment> list = productCommentService.getMyComments();
        return getDataTable(list);
    }

    /**
     * 获取带图片的评论
     */
    @Operation(summary = "获取带图片的评论")
    @GetMapping("/product/{productId}/withImages")
    public AjaxResult getCommentsWithImages(@PathVariable Long productId,
                                            @RequestParam(defaultValue = "6") Integer limit) {
        List<ProductComment> list = productCommentService.getCommentsWithImages(productId, limit);
        return success(list);
    }

    /**
     * 检查用户是否评论过商品
     */
    @Operation(summary = "检查用户是否评论过商品")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/check")
    public AjaxResult checkUserComment(@RequestParam Long productId,
                                       @RequestParam(required = false) Long orderId) {
        Long userId = getUserId();
        ProductComment comment = productCommentService.getUserProductComment(userId, productId, orderId);
        Map<String, Object> result = new HashMap<>();
        result.put("hasCommented", comment != null);
        result.put("comment", comment);
        return success(result);
    }

    /**
     * 批量审核评论
     */
    @Operation(summary = "批量审核评论")
    @PreAuthorize("@ss.hasPermi('system:comment:audit')")
    @Log(title = "商品评论", businessType = BusinessType.UPDATE)
    @PostMapping("/batchAudit")
    public AjaxResult batchAudit(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Long> commentIds = (List<Long>) params.get("commentIds");
        Integer status = Integer.valueOf(params.get("status").toString());
        String reason = (String) params.get("reason");

        int successCount = 0;
        for (Long commentId : commentIds) {
            try {
                if (productCommentService.auditComment(commentId, status, reason)) {
                    successCount++;
                }
            } catch (Exception e) {
                // 记录错误但继续处理其他评论
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", commentIds.size());
        result.put("success", successCount);
        result.put("failed", commentIds.size() - successCount);

        return success(result);
    }

    /**
     * 获取评论详情（包含回复）
     */
    @Operation(summary = "获取评论详情（包含回复）")
    @GetMapping("/detail/{commentId}/withReplies")
    public AjaxResult getCommentWithReplies(@PathVariable Long commentId) {
        ProductComment comment = productCommentService.selectProductCommentByCommentId(commentId);
        if (comment == null) {
            return error("评论不存在");
        }
        comment.setOrderId(null);
        maskAnonymousIdentity(comment);

        Map<String, Object> result = new HashMap<>();
        result.put("comment", comment);
        comment.setLikedByCurrentUser(productCommentService.getLikedCommentIds(
            SecurityUtils.getOptionalUserId(),
            java.util.Collections.singletonList(commentId)).contains(commentId));

        if (comment.isRootComment()) {
            List<ProductComment> replies = productCommentService.getCommentReplies(commentId);
            result.put("replies", replies);
        }

        return success(result);
    }

    /**
     * 获取商品评论树（最简单版本）
     */
    @Operation(summary = "获取商品评论树")
    @GetMapping("/product/{productId}/tree")
    public AjaxResult getCommentTree(@PathVariable Long productId) {
        List<Map<String, Object>> tree = productCommentService.getCommentTreeSimple(productId);
        return success(tree);
    }

    /**
     * 获取商品评论树（分页版）
     */
    @Operation(summary = "获取商品评论树（分页）")
    @GetMapping("/product/{productId}/tree/page")
    public AjaxResult getCommentTreePage(@PathVariable Long productId,
                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        // 先获取所有根评论
        LambdaQueryWrapper<ProductComment> query = new LambdaQueryWrapper<>();
        query.eq(ProductComment::getProductId, productId)
                .eq(ProductComment::getStatus, 1)
                .and(w -> w.isNull(ProductComment::getParentId).or().eq(ProductComment::getParentId, 0))
                .orderByDesc(ProductComment::getCreateTime);

        Page<ProductComment> page = new Page<>(pageNum, pageSize);
        Page<ProductComment> result = productCommentService.page(page, query);

        if (result.getRecords().isEmpty()) {
            return success(new ArrayList<>());
        }

        // 获取这些根评论的直接回复
        List<Long> rootIds = result.getRecords().stream()
                .map(ProductComment::getCommentId)
                .collect(Collectors.toList());

        LambdaQueryWrapper<ProductComment> childQuery = new LambdaQueryWrapper<>();
        childQuery.in(ProductComment::getParentId, rootIds)
                .eq(ProductComment::getStatus, 1)
                .orderByAsc(ProductComment::getCreateTime);

        List<ProductComment> children = productCommentService.list(childQuery);

        List<Long> allCommentIds = new ArrayList<>(rootIds);
        allCommentIds.addAll(children.stream().map(ProductComment::getCommentId).collect(Collectors.toList()));
        Set<Long> likedCommentIds = new HashSet<>(productCommentService.getLikedCommentIds(
            SecurityUtils.getOptionalUserId(), allCommentIds));

        // 构建树
        Map<Long, Map<String, Object>> rootMap = new HashMap<>();
        List<Map<String, Object>> resultTree = new ArrayList<>();

        // 先处理根评论
        for (ProductComment root : result.getRecords()) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", root.getCommentId());
            node.put("content", root.getContent());
            node.put("score", root.getScore());
            node.put("likeCount", root.getLikeCount());
            node.put("likedByCurrentUser", likedCommentIds.contains(root.getCommentId()));
            node.put("createTime", root.getCreateTime());
            node.put("images", root.getImages());
            node.put("children", new ArrayList<Map<String, Object>>());
            addCommentIdentity(node, root);

            rootMap.put(root.getCommentId(), node);
            resultTree.add(node);
        }

        // 再处理子评论
        for (ProductComment child : children) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", child.getCommentId());
            node.put("content", child.getContent());
            node.put("score", child.getScore());
            node.put("likeCount", child.getLikeCount());
            node.put("likedByCurrentUser", likedCommentIds.contains(child.getCommentId()));
            node.put("createTime", child.getCreateTime());
            node.put("images", child.getImages());
            node.put("children", new ArrayList<Map<String, Object>>()); // 子评论不再有children
            addCommentIdentity(node, child);

            // 找到父节点
            Map<String, Object> parent = rootMap.get(child.getParentId());
            if (parent != null) {
                List<Map<String, Object>> parentChildren = (List<Map<String, Object>>) parent.get("children");
                parentChildren.add(node);
            }
        }

        // 返回分页结果
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("list", resultTree);
        resultData.put("total", result.getTotal());
        resultData.put("pageNum", pageNum);
        resultData.put("pageSize", pageSize);

        return success(resultData);
    }

    private void addCommentIdentity(Map<String, Object> node, ProductComment comment) {
        boolean anonymous = Integer.valueOf(1).equals(comment.getIsAnonymous());
        node.put("isAnonymous", comment.getIsAnonymous());
        node.put("userId", anonymous ? null : comment.getUserId());
        if (anonymous) {
            node.put("userName", "匿名用户");
            node.put("userAvatar", null);
            return;
        }
        SysUser user = sysUserService.selectUserById(comment.getUserId());
        if (user != null) {
            node.put("userName", user.getNickName());
            node.put("userAvatar", user.getAvatar());
        }
    }

    private void maskAnonymousIdentity(ProductComment comment) {
        if (comment != null && Integer.valueOf(1).equals(comment.getIsAnonymous())) {
            comment.setUserId(null);
            comment.setNickName("匿名用户");
            comment.setUserAvatar(null);
        }
    }

    @Autowired
    private ISysUserService sysUserService;
}
