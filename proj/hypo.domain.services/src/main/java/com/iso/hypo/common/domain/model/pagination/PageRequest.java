package com.iso.hypo.common.domain.model.pagination;

public class PageRequest {

    private final int page;
    private final int size;

    public PageRequest(int page, int size) {
        this.page = page;
        this.size = size;
    }

    public static PageRequest of(int page, int size) {
        return new PageRequest(page, size);
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }
}
