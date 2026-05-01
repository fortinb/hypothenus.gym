package com.iso.hypo.common.infrastructure.persistence.adapter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;

public abstract class BaseAdapter {

    protected org.springframework.data.domain.PageRequest toSpringPageable(PageRequest pageRequest) {
        return org.springframework.data.domain.PageRequest.of(pageRequest.getPage(), pageRequest.getSize());
    }

    protected org.springframework.data.domain.PageRequest toSpringPageable(PageRequest pageRequest, Sort sort) {
        if (sort == null) {
            return toSpringPageable(pageRequest);
        }
  //      org.springframework.data.domain.PageRequest validatedPageRequest = org.springframework.data.domain.PageRequest.of(pageRequest.getPage(), pageRequest.getSize(), sort);
        return org.springframework.data.domain.PageRequest.of(pageRequest.getPage(), pageRequest.getSize(), sort);
    }

    protected <T> PageResult<T> toPageResult(Page<T> page, PageRequest pageRequest) {
        return new PageResult<>(
            page.getContent(),
            pageRequest.getPage(),
            pageRequest.getSize(),
            page.getTotalElements()
        );
    }
}