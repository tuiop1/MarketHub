package com.tuiop.markethub.categories;


import com.tuiop.markethub.categories.dto.CategoryResponse;
import com.tuiop.markethub.categories.dto.CreateCategoryRequest;
import com.tuiop.markethub.categories.dto.UpdateCategoryRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CategoryAdminController {
    private final CategoryService categoryService;

    @GetMapping
    public Page<CategoryResponse> getAllCategories(Pageable pageable) {
        return categoryService.getAllCategories(pageable);
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(request));
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable UUID categoryId, @Valid @RequestBody UpdateCategoryRequest request) {
        return ResponseEntity.ok( categoryService.updateCategory(categoryId, request));
    }

    @PatchMapping("/{categoryId}/enable")
    public ResponseEntity<CategoryResponse> enableCategory(@PathVariable UUID categoryId) {
        return ResponseEntity.ok( categoryService.enableCategory(categoryId));
    }

    @PatchMapping("/{categoryId}/disable")
    public ResponseEntity<CategoryResponse> disableCategory(@PathVariable UUID categoryId) {

        return ResponseEntity.ok( categoryService.disableCategory(categoryId));

    }
}