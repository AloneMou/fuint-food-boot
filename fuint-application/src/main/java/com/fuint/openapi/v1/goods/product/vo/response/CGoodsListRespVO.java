package com.fuint.openapi.v1.goods.product.vo.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fuint.framework.pojo.SortablePageParam;
import com.fuint.openapi.v1.goods.product.vo.model.CGoodsSkuVO;
import com.fuint.openapi.v1.goods.product.vo.model.GoodsSkuRespVO;
import com.fuint.openapi.v1.goods.product.vo.model.GoodsSkuVO;
import com.fuint.openapi.v1.goods.product.vo.model.GoodsSpecItemVO;
import com.fuint.repository.model.MtGoodsSpec;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * C端商品列表响应VO
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "C端商品列表响应VO")
public class CGoodsListRespVO {

    @ApiModelProperty(value = "商品ID", example = "1")
    private Integer id;

    @ApiModelProperty(value = "商品名称", example = "拿铁咖啡")
    private String name;

    @ApiModelProperty(value = "商品编码", example = "G001")
    private String goodsNo;

    @ApiModelProperty(value = "商品类型", example = "goods")
    private String type;

    @ApiModelProperty(value = "商品分类ID", example = "1")
    private Integer cateId;

    @ApiModelProperty(value = "商品分类名称", example = "饮品")
    private String cateName;

    @ApiModelProperty(value = "商户ID", example = "1")
    private Integer merchantId;

    @ApiModelProperty(value = "店铺ID", example = "1")
    private Integer storeId;

    @ApiModelProperty(value = "店铺名称", example = "总店")
    private String storeName;

    @ApiModelProperty(value = "商品描述", example = "经典拿铁")
    private String description;

    @ApiModelProperty(value = "商品图片列表")
    private List<String> images;

    @ApiModelProperty(value = "商品LOGO", example = "https://example.com/logo.jpg")
    private String logo;

    @ApiModelProperty(value = "商品价格（元）", example = "18.00")
    private BigDecimal price;

    @ApiModelProperty(value = "划线价格（元）", example = "20.00")
    private BigDecimal linePrice;

    @ApiModelProperty(value = "商品重量（克）", example = "500")
    private BigDecimal weight;

    @ApiModelProperty(value = "库存数量", example = "100")
    private Integer stock;

    @ApiModelProperty(value = "初始销量", example = "0")
    private Integer initSale;

    @ApiModelProperty(value = "实际销量", example = "50")
    private Integer saleNum;

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

    @ApiModelProperty(value = "可用优惠券ID列表", example = "1,2,3")
    private List<Integer> couponIds;

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

    @ApiModelProperty(value = "商品规格列表")
    private List<GoodsSpecItemVO> specData = new ArrayList<>();

    @ApiModelProperty(value = "商品SKU列表")
    private List<GoodsSkuRespVO> skuData = new ArrayList<>();

    @ApiModelProperty(value = "动态价格（元）", example = "18.00")
    private BigDecimal dynamicPrice;

}
