package com.example.payment.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public class PageResponse<T> {

    @JsonProperty("traceId")
    private String traceId;
    @JsonProperty("statusCode")
    private int statusCode;
    private String message;
    private List<T> data;
    private Metadata metadata;

    public PageResponse() {
        this.traceId = UUID.randomUUID().toString();
    }

    public PageResponse(Page<T> pageData) {
        this.traceId = UUID.randomUUID().toString();
        this.statusCode = 200;
        this.message = "success";
        this.data = pageData.getContent();
        this.metadata = new Metadata(pageData);
    }

    public static <T> PageResponse<T> of(Page<T> pageData) {
        return new PageResponse<>(pageData);
    }

    public static class Metadata {
        @JsonProperty("hasNext") private boolean hasNext;
        @JsonProperty("hasPrev") private boolean hasPrev;
        @JsonProperty("totalPage") private int totalPage;
        @JsonProperty("currentPage") private int currentPage;
        private int limit;
        @JsonProperty("totalCount") private long totalCount;

        public Metadata() {}

        public Metadata(Page<?> page) {
            this.hasNext = page.hasNext();
            this.hasPrev = page.hasPrevious();
            this.totalPage = page.getTotalPages();
            this.currentPage = page.getNumber() + 1;
            this.limit = page.getSize();
            this.totalCount = page.getTotalElements();
        }

        public boolean isHasNext() { return hasNext; }
        public void setHasNext(boolean v) { this.hasNext = v; }
        public boolean isHasPrev() { return hasPrev; }
        public void setHasPrev(boolean v) { this.hasPrev = v; }
        public int getTotalPage() { return totalPage; }
        public void setTotalPage(int v) { this.totalPage = v; }
        public int getCurrentPage() { return currentPage; }
        public void setCurrentPage(int v) { this.currentPage = v; }
        public int getLimit() { return limit; }
        public void setLimit(int v) { this.limit = v; }
        public long getTotalCount() { return totalCount; }
        public void setTotalCount(long v) { this.totalCount = v; }
    }

    public String getTraceId() { return traceId; }
    public void setTraceId(String v) { this.traceId = v; }
    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int v) { this.statusCode = v; }
    public String getMessage() { return message; }
    public void setMessage(String v) { this.message = v; }
    public List<T> getData() { return data; }
    public void setData(List<T> v) { this.data = v; }
    public Metadata getMetadata() { return metadata; }
    public void setMetadata(Metadata v) { this.metadata = v; }
}
