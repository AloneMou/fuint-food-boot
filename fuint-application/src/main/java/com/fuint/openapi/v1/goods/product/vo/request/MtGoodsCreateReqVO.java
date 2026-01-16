package com.fuint.openapi.v1.goods.product.vo.request;

import com.fuint.openapi.v1.goods.product.vo.model.GoodsSpecItemCreateReqVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品创建请求VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "商品创建请求VO")
public class MtGoodsCreateReqVO {

    @NotBlank(message = "商品名称不能为空")
    @ApiModelProperty(value = "商品名称", required = true, example = "拿铁咖啡")
    private String name;

    @ApiModelProperty(value = "商品编码", example = "G001")
    private String goodsNo;

    @ApiModelProperty(value = "商品类型", example = "food")
    private String type;

    @NotNull(message = "商品分类不能为空")
    @ApiModelProperty(value = "商品分类ID", required = true, example = "1")
    private Integer cateId;

    @ApiModelProperty(value = "商户ID", example = "1")
    private Integer merchantId;

    @ApiModelProperty(value = "店铺ID", example = "1")
    private Integer storeId;

    @ApiModelProperty(value = "商品描述", example = "经典拿铁")
    private String description;

    @ApiModelProperty(value = "商品图片列表", example = "[\"https://example.com/image1.jpg\"]")
    private List<String> images;

    @NotNull(message = "商品LOGO不能为空")
    @ApiModelProperty(value = "商品LOGO（第一张图片）", example = "https://example.com/logo.jpg")
    private String logo;

    @NotNull(message = "商品价格不能为空")
    @ApiModelProperty(value = "商品价格（元）", required = true, example = "18.00")
    private BigDecimal price;

    @ApiModelProperty(value = "划线价格（元）", example = "20.00")
    private BigDecimal linePrice;

    @ApiModelProperty(value = "商品重量（克）", example = "500")
    private BigDecimal weight;

    @ApiModelProperty(value = "库存数量", example = "100")
    private Integer stock;

    @ApiModelProperty(value = "初始销量", example = "0")
    private Integer initSale;

    @ApiModelProperty(value = "卖点", example = "香浓美味")
    private String salePoint;

    @ApiModelProperty(value = "是否可使用积分：Y-是；N-否", example = "Y")
    private String canUsePoint;

    @ApiModelProperty(value = "是否会员折扣：Y-是；N-否", example = "Y")
    private String isMemberDiscount;

    @ApiModelProperty(value = "是否单规格：Y-是；N-否", example = "Y")
    private String isSingleSpec;

    @ApiModelProperty(value = "服务时间（分钟）", example = "10")
    private Integer serviceTime;

    @ApiModelProperty(value = "可用优惠券ID列表，逗号分隔", example = "1,2,3")
    private List<Integer> couponIds;

    @ApiModelProperty(value = "排序", example = "100")
    private Integer sort;

    @ApiModelProperty(value = "商品规格列表")
    private List<GoodsSpecItemCreateReqVO> specData;

    @ApiModelProperty(value = "商品SKU列表")
    private List<GoodsSkuCreateReqVO> skuData;
}
