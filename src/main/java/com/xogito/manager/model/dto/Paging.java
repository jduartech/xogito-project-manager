package com.xogito.manager.model.dto;

import lombok.Data;

@Data
public class Paging {
    private int currentPage;
    private int totalElements;
    private int totalPages;

    public Paging(int currentPage, int totalElements, int totalPages) {
        this.currentPage = currentPage;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }
}
