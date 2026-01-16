package com.fuint.framework.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "分页结果")
@Data
public final class PageResult<T> implements Serializable {

    @ApiModelProperty(value = "总量", required = true)
    private Long total;

    @ApiModelProperty(value = "数据", required = true)
    private List<T> list;

    @ApiModelProperty(value = "总页数", required = true)
    private Long totalPages;

    @ApiModelProperty(value = "当前页", required = true)
    private Long currentPage;

    @ApiModelProperty(value = "每页数量", required = true)
    private Long pageSize;

    public PageResult() {
    }

    public PageResult(List<T> list, Long total) {
        this.list = list;
        this.total = total;
        this.totalPages = 0L;
        this.currentPage = 0L;
        this.pageSize = 0L;
    }

    public PageResult(List<T> list, Long total, Long totalPages, Long currentPage, Long pageSize) {
        this.list = list;
        this.total = total;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
    }

    public PageResult(Long total) {
        this.list = new ArrayList<>();
        this.total = total;
        this.totalPages = 0L;
        this.currentPage = 0L;
        this.pageSize = 0L;
    }

    public static <T> PageResult<T> empty() {
        return new PageResult<>(0L);
    }

    public static <T> PageResult<T> empty(Long total) {
        return new PageResult<>(total);
    }

}
