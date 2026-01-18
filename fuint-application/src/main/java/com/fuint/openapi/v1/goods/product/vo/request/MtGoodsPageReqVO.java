package com.fuint.openapi.v1.goods.product.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;

/**
 * 商品分页查询请求VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "商品分页查询请求VO")
public class MtGoodsPageReqVO {

    @ApiModelProperty(value = "当前页码", example = "1")
    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @ApiModelProperty(value = "每页数量", example = "10")
    @Min(value = 1, message = "每页数量最小为1")
    private Integer pageSize = 10;

    @ApiModelProperty(value = "商品名称（模糊搜索）", example = "咖啡")
    private String name;

    @ApiModelProperty(value = "商品编码", example = "G001")
    private String goodsNo;

    @ApiModelProperty(value = "商品类型", example = "goods")
    private String type;

    @ApiModelProperty(value = "分类ID", example = "1")
    private Integer cateId;

    @ApiModelProperty(value = "状态：A-正常；D-删除", example = "A")
    private String status;

    @ApiModelProperty(value = "店铺ID", example = "1")
    private Integer storeId;

    @ApiModelProperty(value = "商户ID", example = "1")
    private Integer merchantId;

    @ApiModelProperty(value = "是否单规格：Y-是；N-否", example = "Y")
    private String isSingleSpec;

    @ApiModelProperty(value = "库存筛选：low-库存紧张", example = "low")
    private String stock;
}
