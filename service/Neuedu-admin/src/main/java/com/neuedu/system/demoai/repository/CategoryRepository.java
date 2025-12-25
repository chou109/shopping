package com.neuedu.system.demoai.repository;

import com.neuedu.system.demoai.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 根据名称模糊查询
    List<Category> findByNameContainingAndDeleteFlag(String name, Integer deleteFlag);

    // 根据父ID查询
    List<Category> findByParentIdAndDeleteFlag(Long parentId, Integer deleteFlag);

    // 根据ID和删除标志查询
    Category findByIdAndDeleteFlag(Long id, Integer deleteFlag);

    // 查询所有启用的类别
    List<Category> findByStatusAndDeleteFlag(Integer status, Integer deleteFlag);

    // 查询指定父ID下所有启用的子类别
    @Query("SELECT c FROM Category c WHERE c.parentId = :parentId AND c.status = 1 AND c.deleteFlag = 0 ORDER BY c.sortOrder")
    List<Category> findEnabledChildrenByParentId(@Param("parentId") Long parentId);

    // 根据名称精确查询
    Category findByNameAndDeleteFlag(String name, Integer deleteFlag);
}
