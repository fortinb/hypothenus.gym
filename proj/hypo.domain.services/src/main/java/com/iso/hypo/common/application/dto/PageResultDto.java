package com.iso.hypo.common.application.dto;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    
    @JsonCreator
    public PageResultDto(
            @JsonProperty("content") List<T> content,
            @JsonProperty("pageNumber") int pageNumber,
            @JsonProperty("size") int size,
            @JsonProperty("totalElements") long totalElements) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        this.hasNext = pageNumber < totalPages - 1;
        this.hasPrevious = pageNumber > 0;
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