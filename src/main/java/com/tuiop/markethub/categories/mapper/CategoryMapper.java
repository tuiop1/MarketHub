package com.tuiop.markethub.categories.mapper;

import com.tuiop.markethub.categories.Category;
import com.tuiop.markethub.categories.dto.CategoryResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponse toResponse(Category category);
}