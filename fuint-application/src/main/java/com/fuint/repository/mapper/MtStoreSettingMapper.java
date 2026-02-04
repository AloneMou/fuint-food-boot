package com.fuint.repository.mapper;

import com.fuint.repository.base.BaseMapperX;
import com.fuint.repository.model.MtStoreSetting;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Miao
 * @date 2026/2/4
 */
@Mapper
public interface MtStoreSettingMapper extends BaseMapperX<MtStoreSetting> {

    default MtStoreSetting selectSettingByStoreId(Integer storeId) {
        return selectOne(MtStoreSetting::getStoreId, storeId);
    }
}
