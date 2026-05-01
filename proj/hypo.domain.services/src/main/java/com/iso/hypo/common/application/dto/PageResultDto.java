package com.iso.hypo.common.application.dto;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.iso.hypo.common.domain.model.pagination.PageResult;

import lombok.Getter;

@Getter
public class PageResultDto<T> {

    private final List<T> content;
    private final int pageNumber;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean hasNext;
    private final boolean hasPrevious;

    public PageResultDto() {
		this.content = null;
		this.pageNumber = 0;
		this.size = 0;
		this.totalElements = 0;
		this.totalPages = 0;
		this.hasNext = false;
		this.hasPrevious = false;
    }
    
    public PageResultDto(List<T> content, int page, int size, long totalElements) {
        this.content = content;
        this.pageNumber = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        this.hasNext = page < totalPages - 1;
        this.hasPrevious = page > 0;
    }

    public static <T> PageResultDto<T> from(PageResult<T> pageResult) {
        return new PageResultDto<>(
            pageResult.getContent(),
            pageResult.getPage(),
            pageResult.getSize(),
            pageResult.getTotalElements()
        );
    }

    public <R> PageResultDto<R> map(Function<T, R> mapper) {
        return new PageResultDto<>(
            content.stream().map(mapper).collect(Collectors.toList()),
            pageNumber,
            size,
            totalElements
        );
    }
}
