package com.fuint.openapi.v1.goods.product.vo.request;

import com.fuint.framework.pojo.PageParams;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;


@EqualsAndHashCode(callSuper = true)
@Data
public class CGoodsListPageReqVO extends PageParams {

    @ApiModelProperty(value = "用户ID（用于计算个性化价格）", example = "1")
    private Integer userId;

    @NotNull(message = "店铺ID不能为空")
    @ApiModelProperty(value = "店铺ID", example = "1")
    private Integer storeId;

    @NotNull(message = "商户ID不能为空")
    @ApiModelProperty(value = "商户ID", example = "1")
    private Integer merchantId;

    @ApiModelProperty(value = "分类ID", example = "1")
    private Integer cateId;

    @ApiModelProperty(value = "商品类型", example = "goods")
    private String type;

    @ApiModelProperty(value = "商品名称(模糊查询)", example = "1")
    private String name;

    @ApiModelProperty(value = "是否有库存", example = "Y", hidden = true)
    private String hasStock;

    @ApiModelProperty(value = "商品状态", example = "1", hidden = true)
    private String status;

}
