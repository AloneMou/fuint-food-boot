package com.fuint.openapi.v1.content.banner.vo.request;

import com.fuint.framework.pojo.PageParams;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value = "Banner分页查询请求VO")
public class BannerPageReqVO extends PageParams {


    @ApiModelProperty(value = "标题（模糊搜索）", example = "活动")
    private String title;

    @ApiModelProperty(value = "展示位置", example = "m_home_banner", allowableValues = "m_home_ads,m_home_banner")
    private String position;

    @ApiModelProperty(value = "状态：A-正常； N-禁用", example = "A")
    private String status;

    @ApiModelProperty(value = "店铺ID", example = "1")
    private Integer storeId;

    @ApiModelProperty(value = "商户ID", example = "1")
    private Integer merchantId;
}
