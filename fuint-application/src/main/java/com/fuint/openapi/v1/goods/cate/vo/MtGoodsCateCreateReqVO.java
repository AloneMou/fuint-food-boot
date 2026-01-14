package com.fuint.openapi.v1.goods.cate.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Date;

/**
 * Foot-Fuint-Backend-master
 *
 * @author mjw
 * @since 2026/1/14 23:00
 */
@Data
public class MtGoodsCateCreateReqVO {

    @NotBlank(message = "分类名称不能为空")
    @ApiModelProperty(value = "分类名称", required = true)
    private String name;

    @ApiModelProperty(value = "LOGO地址")
    private String logo;

    @ApiModelProperty("分类描述")
    private String description;

    @ApiModelProperty("排序")
    private Integer sort;

//    @NotBlank(message = "状态不能为空")
//    @ApiModelProperty("A：正常；D：删除")
//    private String status;
}
