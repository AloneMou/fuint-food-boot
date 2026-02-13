package com.fuint.openapi.v1.content.banner.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;

@Data
@ApiModel(value = "Banner信息响应VO")
public class BannerRespVO {
    @ApiModelProperty("自增ID")
    private Integer id;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("展示位置")
    private String position;

    @ApiModelProperty("所属商户ID")
    private Integer merchantId;

    @ApiModelProperty("所属店铺ID")
    private Integer storeId;

    @ApiModelProperty("链接地址")
    private String url;

    @ApiModelProperty("图片地址")
    private String image;

    @ApiModelProperty("描述")
    private String description;

    @ApiModelProperty("排序")
    private Integer sort;

    @ApiModelProperty("A：正常；D：删除")
    private String status;
    
    @ApiModelProperty("创建时间")
    private Date createTime;
}
