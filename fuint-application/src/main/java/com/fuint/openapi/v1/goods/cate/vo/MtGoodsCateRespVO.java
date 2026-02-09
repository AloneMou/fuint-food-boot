package com.fuint.openapi.v1.goods.cate.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 商品分类响应VO
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "商品分类响应VO")
public class MtGoodsCateRespVO {

    @ApiModelProperty(value = "分类ID", example = "1")
    private Integer id;

    @ApiModelProperty(value = "商户ID", example = "1")
    private Integer merchantId;

    @ApiModelProperty(value = "商户名称", example = "总店")
    private String merchantName;

    @ApiModelProperty(value = "店铺ID", example = "1")
    private Integer storeId;

    @ApiModelProperty(value = "店铺名称", example = "总店")
    private String storeName;

    @ApiModelProperty(value = "分类名称", example = "饮品")
    private String name;

    @ApiModelProperty(value = "LOGO地址", example = "/uploads/logo.jpg")
    private String logo;

    @ApiModelProperty(value = "分类描述", example = "各类饮品")
    private String description;

    @ApiModelProperty(value = "排序", example = "100")
    private Integer sort;

    @ApiModelProperty(value = "状态：A-正常；D-删除", example = "A")
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间", example = "2024-01-01 12:00:00")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "更新时间", example = "2024-01-01 12:00:00")
    private Date updateTime;

    @ApiModelProperty(value = "操作人", example = "admin")
    private String operator;
}
