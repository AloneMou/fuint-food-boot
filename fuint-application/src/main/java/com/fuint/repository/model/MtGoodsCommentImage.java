package com.fuint.repository.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 商品评价图片表
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Getter
@Setter
@TableName("mt_goods_comment_image")
@ApiModel(value = "MtGoodsCommentImage对象", description = "商品评价图片表")
public class MtGoodsCommentImage implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("自增ID")
    @TableId(value = "ID", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("评价ID")
    private Integer commentId;

    @ApiModelProperty("图片地址")
    private String imageUrl;

    @ApiModelProperty("排序")
    private Integer sort;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("状态 A-正常 D-删除")
    private String status;

}
