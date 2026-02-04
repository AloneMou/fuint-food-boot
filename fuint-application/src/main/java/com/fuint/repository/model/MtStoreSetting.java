package com.fuint.repository.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Miao
 * @date 2026/2/4
 */
@Getter
@Setter
@TableName("mt_store_setting")
@ApiModel(value = "MtStore配置对象", description = "店铺配置表")
public class MtStoreSetting {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 门店ID
     */
    private Integer storeId;

    /**
     * 自动接单
     */
    private Integer autoAccept;

}
