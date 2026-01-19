package com.fuint.openapi.v1.goods.cate.vo;

import com.fuint.framework.pojo.PageParams;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品分类分页查询请求VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value = "商品分类分页查询请求VO")
public class MtGoodsCatePageReqVO extends PageParams {

    @ApiModelProperty(value = "分类名称（模糊搜索）", example = "饮品")
    private String name;

    @ApiModelProperty(value = "状态：A-正常；D-删除", example = "A")
    private String status;

    @ApiModelProperty(value = "店铺ID", example = "1")
    private Integer storeId;

    @ApiModelProperty(value = "商户ID", example = "1")
    private Integer merchantId;
}
