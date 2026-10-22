package com.neuedu.system.demoai.repository;

import com.neuedu.system.demoai.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    // 根据删除标志和状态查询商品
    List<Product> findByDeleteFlagAndStatus(Integer deleteFlag, Integer status);

    // 根据名称模糊查询、删除标志和状态查询商品
    List<Product> findByNameContainingAndDeleteFlagAndStatus(String name, Integer deleteFlag, Integer status);

    // 根据价格大于等于、删除标志和状态查询商品
    List<Product> findByPriceGreaterThanEqualAndDeleteFlagAndStatus(BigDecimal price, Integer deleteFlag, Integer status);

    // 根据类别ID、删除标志和状态查询商品
    List<Product> findByCategoryIdAndDeleteFlagAndStatus(Long categoryId, Integer deleteFlag, Integer status);

    // 根据价格小于等于、删除标志和状态查询商品
    List<Product> findByPriceLessThanEqualAndDeleteFlagAndStatus(BigDecimal price, Integer deleteFlag, Integer status);

    // 根据价格在区间内、删除标志和状态查询商品
    List<Product> findByPriceBetweenAndDeleteFlagAndStatus(BigDecimal minPrice, BigDecimal maxPrice, Integer deleteFlag, Integer status);



    // 根据品牌名称模糊查询、删除标志和状态查询商品
    List<Product> findByNameContainingAndDeleteFlagAndStatusOrderByPriceAsc(String name, Integer deleteFlag, Integer status);

    // 根据类别ID和价格大于等于、删除标志和状态查询商品
    List<Product> findByCategoryIdAndPriceGreaterThanEqualAndDeleteFlagAndStatus(
            Long categoryId, BigDecimal price, Integer deleteFlag, Integer status);

    // 根据类别ID和价格小于等于、删除标志和状态查询商品
    List<Product> findByCategoryIdAndPriceLessThanEqualAndDeleteFlagAndStatus(
            Long categoryId, BigDecimal price, Integer deleteFlag, Integer status);

    // 根据类别ID和价格在区间内、删除标志和状态查询商品
    List<Product> findByCategoryIdAndPriceBetweenAndDeleteFlagAndStatus(
            Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Integer deleteFlag, Integer status);

    // 根据名称模糊查询、类别ID、删除标志和状态查询商品
    List<Product> findByNameContainingAndCategoryIdAndDeleteFlagAndStatus(
            String name, Long categoryId, Integer deleteFlag, Integer status);

    // 根据名称模糊查询、价格大于等于、删除标志和状态查询商品
    List<Product> findByNameContainingAndPriceGreaterThanEqualAndDeleteFlagAndStatus(
            String name, BigDecimal price, Integer deleteFlag, Integer status);

    // 根据名称模糊查询、价格小于等于、删除标志和状态查询商品
    List<Product> findByNameContainingAndPriceLessThanEqualAndDeleteFlagAndStatus(
            String name, BigDecimal price, Integer deleteFlag, Integer status);

    // 根据名称模糊查询、价格在区间内、删除标志和状态查询商品
    List<Product> findByNameContainingAndPriceBetweenAndDeleteFlagAndStatus(
            String name, BigDecimal minPrice, BigDecimal maxPrice, Integer deleteFlag, Integer status);

    // 根据名称模糊查询、类别ID、价格大于等于、删除标志和状态查询商品
    List<Product> findByNameContainingAndCategoryIdAndPriceGreaterThanEqualAndDeleteFlagAndStatus(
            String name, Long categoryId, BigDecimal price, Integer deleteFlag, Integer status);

    // 根据名称模糊查询、类别ID、价格小于等于、删除标志和状态查询商品
    List<Product> findByNameContainingAndCategoryIdAndPriceLessThanEqualAndDeleteFlagAndStatus(
            String name, Long categoryId, BigDecimal price, Integer deleteFlag, Integer status);

    // 根据名称模糊查询、类别ID、价格在区间内、删除标志和状态查询商品
    List<Product> findByNameContainingAndCategoryIdAndPriceBetweenAndDeleteFlagAndStatus(
            String name, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Integer deleteFlag, Integer status);

    // 新增：根据精确价格、删除标志和状态查询商品（你缺少的方法）
    List<Product> findByPriceAndDeleteFlagAndStatus(BigDecimal price, Integer deleteFlag, Integer status);

    // 新增：精确查询
    Product findByNameAndDeleteFlagAndStatus(String name, Integer deleteFlag, Integer status);

    // 新增：名称或副标题查询
    @Query("SELECT p FROM Product p WHERE (p.name LIKE CONCAT('%', :keyword, '%') OR p.subTitle LIKE CONCAT('%', :keyword, '%')) AND p.deleteFlag = 0 AND p.status = 1")
    List<Product> searchActiveByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE (p.name LIKE CONCAT('%', :name, '%') OR p.subTitle LIKE CONCAT('%', :subTitle, '%')) AND p.deleteFlag = :deleteFlag AND p.status = :status")
    List<Product> findByNameContainingOrSubTitleContainingAndDeleteFlagAndStatus(
            @Param("name") String name, @Param("subTitle") String subTitle,
            @Param("deleteFlag") Integer deleteFlag, @Param("status") Integer status
    );

    // 新增：根据ID查询
    Product findByIdAndDeleteFlagAndStatus(Long id, Integer deleteFlag, Integer status);
}


