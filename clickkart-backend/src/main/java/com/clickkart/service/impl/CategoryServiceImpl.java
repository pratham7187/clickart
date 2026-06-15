package com.clickkart.service.impl;

import com.clickkart.entity.Category;
import com.clickkart.exception.ResourceNotFoundException;
import com.clickkart.repository.CategoryRepository;
import com.clickkart.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of {@link CategoryService}.
 *
 * Categories are a stable, small lookup table. All reads are read-only transactions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * {@inheritDoc}
     *
     * Returns only active categories, ordered by sort_order ascending.
     * This is the data source for the home page category grid and
     * product creation dropdown in the admin panel.
     */
    @Override
    public List<Category> getAllCategories() {
        log.debug("Fetching all active categories ordered by sort_order");
        return categoryRepository.findByIsActiveTrueOrderBySortOrderAsc();
    }

    /**
     * {@inheritDoc}
     *
     * Returns all categories including inactive ones.
     * Admin-only: used in the category management panel.
     */
    @Override
    public List<Category> getAllCategoriesForAdmin() {
        log.debug("Fetching all categories (admin) ordered by sort_order");
        return categoryRepository.findAllByOrderBySortOrderAsc();
    }

    /**
     * {@inheritDoc}
     *
     * Category ID is INT (Integer), not BIGINT — matching the DB schema.
     */
    @Override
    public Category getCategoryById(Integer id) {
        log.debug("Fetching category by id: {}", id);
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }

    /**
     * {@inheritDoc}
     *
     * Looks up by the URL/API slug (e.g., "men", "women", "kids").
     * Used when the frontend passes ?category=men as a query parameter.
     */
    @Override
    public Category getCategoryByName(String name) {
        log.debug("Fetching category by name (slug): '{}'", name);
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "name", name));
    }
}
