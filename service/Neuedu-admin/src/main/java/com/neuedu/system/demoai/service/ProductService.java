package com.neuedu.system.demoai.service;

import com.neuedu.system.demoai.entity.Product;
import com.neuedu.system.demoai.model.PriceCondition;
import com.neuedu.system.demoai.model.PriceExtractResult;
import com.neuedu.system.demoai.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    // 专门查询手机（category_id=7），排除配件（category_id=8）
    public List<Product> getPhoneProductsByBrandAndPriceRange(String brand, PriceExtractResult priceResult) {
        // 构建查询条件
        Specification<Product> spec = Specification.where((root, query, cb) ->
                cb.equal(root.get("categoryId"), 7) // 只查询手机
        );

        // 如果有品牌条件
        if (brand != null && !brand.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(root.get("name"), "%" + brand + "%")
            );
        }

        // 如果有价格条件
        if (priceResult != null) {
            spec = spec.and(createPriceSpecification(priceResult));
        }

        return productRepository.findAll(spec);
    }

    // 通用的价格条件构建方法
    private Specification<Product> createPriceSpecification(PriceExtractResult priceResult) {
        return (root, query, cb) -> {
            if (priceResult == null) return cb.conjunction();

            Double price = priceResult.getPrice();
            PriceCondition condition = priceResult.getCondition();

            if (price == null) return cb.conjunction();

            switch (condition) {
                case ABOVE:
                    return cb.greaterThanOrEqualTo(root.get("price"), price);
                case BELOW:
                    return cb.lessThanOrEqualTo(root.get("price"), price);
                case AROUND:
                    // 左右20%的范围
                    return cb.between(root.get("price"), price * 0.8, price * 1.2);
                case EXACT:
                    return cb.equal(root.get("price"), price);
                case RANGE:
                    if (priceResult.getMinPrice() != null && priceResult.getMaxPrice() != null) {
                        return cb.between(root.get("price"),
                                priceResult.getMinPrice(), priceResult.getMaxPrice());
                    }
                    return cb.conjunction();
                default:
                    return cb.conjunction();
            }
        };
    }

    // 如果同时需要类别和品牌筛选
    public List<Product> getProductsByCategoryAndBrand(Long categoryId, String brand, PriceExtractResult priceResult) {
        Specification<Product> spec = Specification.where((root, query, cb) ->
                cb.equal(root.get("categoryId"), categoryId)
        );

        if (brand != null && !brand.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(root.get("name"), "%" + brand + "%")
            );
        }

        if (priceResult != null) {
            spec = spec.and(createPriceSpecification(priceResult));
        }

        return productRepository.findAll(spec);
    }



    // 根据品牌、类别和价格范围查询商品
    public List<Product> getProductsByBrandAndCategoryIdsAndPriceRange(
            String brand, List<Long> categoryIds, PriceExtractResult priceResult) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> query = cb.createQuery(Product.class);
        Root<Product> product = query.from(Product.class);

        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.hasText(brand)) {
            predicates.add(cb.like(product.get("name"), "%" + brand + "%"));
        }

        if (categoryIds != null && !categoryIds.isEmpty()) {
            predicates.add(product.get("categoryId").in(categoryIds));
        }

        // 应用价格筛选条件
        addPricePredicate(cb, product, predicates, priceResult);

        predicates.add(cb.equal(product.get("deleteFlag"), 0));
        predicates.add(cb.equal(product.get("status"), 1));

        query.where(predicates.toArray(new Predicate[0]));

        // 设置排序
        setPriceOrder(cb, query, product, priceResult);

        return entityManager.createQuery(query).getResultList();
    }

    // 根据类别和价格范围查询商品
    public List<Product> getProductsByCategoryIdsAndPriceRange(
            List<Long> categoryIds, PriceExtractResult priceResult) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> query = cb.createQuery(Product.class);
        Root<Product> product = query.from(Product.class);

        List<Predicate> predicates = new ArrayList<>();

        if (categoryIds != null && !categoryIds.isEmpty()) {
            predicates.add(product.get("categoryId").in(categoryIds));
        }

        // 应用价格筛选条件
        addPricePredicate(cb, product, predicates, priceResult);

        predicates.add(cb.equal(product.get("deleteFlag"), 0));
        predicates.add(cb.equal(product.get("status"), 1));

        query.where(predicates.toArray(new Predicate[0]));

        // 设置排序
        setPriceOrder(cb, query, product, priceResult);

        return entityManager.createQuery(query).getResultList();
    }

    // 根据品牌和价格范围查询商品
    public List<Product> getProductsByBrandAndPriceRange(
            String brand, PriceExtractResult priceResult) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> query = cb.createQuery(Product.class);
        Root<Product> product = query.from(Product.class);

        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.hasText(brand)) {
            predicates.add(cb.like(product.get("name"), "%" + brand + "%"));
        }

        // 应用价格筛选条件
        addPricePredicate(cb, product, predicates, priceResult);

        predicates.add(cb.equal(product.get("deleteFlag"), 0));
        predicates.add(cb.equal(product.get("status"), 1));

        query.where(predicates.toArray(new Predicate[0]));

        // 设置排序
        setPriceOrder(cb, query, product, priceResult);

        return entityManager.createQuery(query).getResultList();
    }

    // 根据价格范围查询商品
    public List<Product> getProductsByPriceRange(PriceExtractResult priceResult) {
        if (priceResult == null) {
            return getAllProducts();
        }

        // 根据价格条件类型进行筛选
        switch (priceResult.getCondition()) {
            case ABOVE:
                BigDecimal minPrice = BigDecimal.valueOf(priceResult.getPrice());
                return productRepository.findByPriceGreaterThanEqualAndDeleteFlagAndStatus(
                        minPrice, 0, 1);
            case BELOW:
                BigDecimal maxPrice = BigDecimal.valueOf(priceResult.getPrice());
                return productRepository.findByPriceLessThanEqualAndDeleteFlagAndStatus(
                        maxPrice, 0, 1);
            case AROUND:
                double targetPrice = priceResult.getPrice();
                double min = targetPrice * 0.8; // 价格上下浮动20%
                double max = targetPrice * 1.2;
                return productRepository.findByPriceBetweenAndDeleteFlagAndStatus(
                        BigDecimal.valueOf(min), BigDecimal.valueOf(max), 0, 1);
            case RANGE:
                // 价格区间筛选
                BigDecimal rangeMin = priceResult.getMinPrice() != null ?
                        BigDecimal.valueOf(priceResult.getMinPrice()) : BigDecimal.ZERO;
                BigDecimal rangeMax = priceResult.getMaxPrice() != null ?
                        BigDecimal.valueOf(priceResult.getMaxPrice()) : BigDecimal.valueOf(Double.MAX_VALUE);
                return productRepository.findByPriceBetweenAndDeleteFlagAndStatus(
                        rangeMin, rangeMax, 0, 1);
            case EXACT:
                BigDecimal exactPrice = BigDecimal.valueOf(priceResult.getPrice());
                return productRepository.findByPriceAndDeleteFlagAndStatus(
                        exactPrice, 0, 1);
            default:
                return getAllProducts();
        }
    }

    // 辅助方法：添加价格筛选条件
    private void addPricePredicate(CriteriaBuilder cb, Root<Product> product,
                                   List<Predicate> predicates, PriceExtractResult priceResult) {
        if (priceResult == null) {
            return;
        }

        BigDecimal price = priceResult.getPrice() != null ?
                BigDecimal.valueOf(priceResult.getPrice()) : null;
        BigDecimal minPrice = priceResult.getMinPrice() != null ?
                BigDecimal.valueOf(priceResult.getMinPrice()) : null;
        BigDecimal maxPrice = priceResult.getMaxPrice() != null ?
                BigDecimal.valueOf(priceResult.getMaxPrice()) : null;

        switch (priceResult.getCondition()) {
            case ABOVE:
                if (price != null) {
                    predicates.add(cb.greaterThanOrEqualTo(product.get("price"), price));
                }
                break;
            case BELOW:
                if (price != null) {
                    predicates.add(cb.lessThanOrEqualTo(product.get("price"), price));
                }
                break;
            case AROUND:
                if (price != null) {
                    // 左右浮动10%
                    BigDecimal lowerBound = price.multiply(BigDecimal.valueOf(0.9));
                    BigDecimal upperBound = price.multiply(BigDecimal.valueOf(1.1));
                    predicates.add(cb.between(product.get("price"), lowerBound, upperBound));
                }
                break;
            case RANGE:
                if (minPrice != null && maxPrice != null) {
                    predicates.add(cb.between(product.get("price"), minPrice, maxPrice));
                }
                break;
            case EXACT:
                if (price != null) {
                    predicates.add(cb.equal(product.get("price"), price));
                }
                break;
            default:
                // 不添加任何条件
                break;
        }
    }

    // 辅助方法：设置排序
    private void setPriceOrder(CriteriaBuilder cb, CriteriaQuery<Product> query,
                               Root<Product> product, PriceExtractResult priceResult) {
        if (priceResult == null || priceResult.getCondition() != PriceCondition.AROUND) {
            // 默认按价格升序排序
            query.orderBy(cb.asc(product.get("price")));
        } else if (priceResult.getCondition() == PriceCondition.AROUND && priceResult.getPrice() != null) {
            // AROUND条件按价格与目标值的差值排序
            BigDecimal targetPrice = BigDecimal.valueOf(priceResult.getPrice());
            Expression<BigDecimal> priceDiff = cb.abs(cb.diff(product.get("price"), targetPrice));
            query.orderBy(cb.asc(priceDiff));
        }
    }

    // 获取所有商品
    public List<Product> getAllProducts() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> query = cb.createQuery(Product.class);
        Root<Product> product = query.from(Product.class);

        query.where(
                cb.equal(product.get("deleteFlag"), 0),
                cb.equal(product.get("status"), 1)
        );
        query.orderBy(cb.asc(product.get("price")));

        return entityManager.createQuery(query).getResultList();
    }

    // 根据类别ID查询商品
    public List<Product> getProductsByCategoryId(Long categoryId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> query = cb.createQuery(Product.class);
        Root<Product> product = query.from(Product.class);

        query.where(
                cb.equal(product.get("categoryId"), categoryId),
                cb.equal(product.get("deleteFlag"), 0),
                cb.equal(product.get("status"), 1)
        );
        query.orderBy(cb.asc(product.get("price")));

        return entityManager.createQuery(query).getResultList();
    }

    // 简单的Repository查询方法
    public List<Product> findAllProducts() {
        return productRepository.findByDeleteFlagAndStatus(0, 1);
    }

    public List<Product> findProductsByNameContaining(String name) {
        return productRepository.findByNameContainingAndDeleteFlagAndStatus(name, 0, 1);
    }

    // 新增：精确查询方法
    public Product findProductByExactName(String name) {
        return productRepository.findByNameAndDeleteFlagAndStatus(name, 0, 1);
    }

    // 新增：搜索商品名称或副标题
    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContainingOrSubTitleContainingAndDeleteFlagAndStatus(
                keyword, keyword, 0, 1
        );
    }

    // 新增：根据多个关键词搜索
    public List<Product> searchProductsByKeywords(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return new ArrayList<>();
        }

        List<Product> allProducts = new ArrayList<>();
        for (String keyword : keywords) {
            List<Product> products = findProductsByNameContaining(keyword);
            for (Product product : products) {
                if (!allProducts.contains(product)) {
                    allProducts.add(product);
                }
            }
        }
        return allProducts;
    }

    // 新增：根据ID获取商品（如果需要）
    public Product getProductById(Long id) {
        return productRepository.findByIdAndDeleteFlagAndStatus(id, 0, 1);
    }

}