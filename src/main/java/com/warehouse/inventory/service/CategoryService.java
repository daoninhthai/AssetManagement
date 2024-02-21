package com.warehouse.inventory.service;

import com.warehouse.inventory.entity.Category;
import com.warehouse.inventory.exception.ResourceNotFoundException;
import com.warehouse.inventory.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<Category> findAll() {
        log.debug("Finding all categories");
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Category findById(Long id) {
        log.debug("Finding category by id: {}", id);
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", id));
    }

    public Category save(Category category) {
        log.info("Saving category: {}", category.getName());
        return categoryRepository.save(category);
    }

    public void delete(Long id) {
        log.info("Deleting category with id: {}", id);
        Category category = findById(id);
        categoryRepository.delete(category);
    }

    @Transactional(readOnly = true)
    public List<Category> findRootCategories() {
        log.debug("Finding root categories");
        return categoryRepository.findRootCategories();
    }

    @Transactional(readOnly = true)
    public long count() {
        return categoryRepository.count();
    }
}
