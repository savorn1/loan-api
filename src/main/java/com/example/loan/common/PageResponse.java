package com.example.loan.common;

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

        @JsonProperty("hasNext")
        private boolean hasNext;

        @JsonProperty("hasPrev")
        private boolean hasPrev;

        @JsonProperty("totalPage")
        private int totalPage;

        @JsonProperty("currentPage")
        private int currentPage;

        private int limit;

        @JsonProperty("totalCount")
        private long totalCount;

        public Metadata() {}

        public Metadata(Page<?> page) {
            this.hasNext = page.hasNext();
            this.hasPrev = page.hasPrevious();
            this.totalPage = page.getTotalPages();
            this.currentPage = page.getNumber() + 1; // 1-based
            this.limit = page.getSize();
            this.totalCount = page.getTotalElements();
        }

        public boolean isHasNext() { return hasNext; }
        public void setHasNext(boolean hasNext) { this.hasNext = hasNext; }

        public boolean isHasPrev() { return hasPrev; }
        public void setHasPrev(boolean hasPrev) { this.hasPrev = hasPrev; }

        public int getTotalPage() { return totalPage; }
        public void setTotalPage(int totalPage) { this.totalPage = totalPage; }

        public int getCurrentPage() { return currentPage; }
        public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }

        public int getLimit() { return limit; }
        public void setLimit(int limit) { this.limit = limit; }

        public long getTotalCount() { return totalCount; }
        public void setTotalCount(long totalCount) { this.totalCount = totalCount; }
    }

    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }

    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<T> getData() { return data; }
    public void setData(List<T> data) { this.data = data; }

    public Metadata getMetadata() { return metadata; }
    public void setMetadata(Metadata metadata) { this.metadata = metadata; }
}
