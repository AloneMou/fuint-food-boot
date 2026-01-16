package com.fuint.repository.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fuint.repository.model.app.MtApp;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Miao
 * @date 2026/1/16
 */
@Mapper
public interface MtAppMapper extends BaseMapper<MtApp> {

    default MtApp findByAppId(String appId) {
        return selectOne(new LambdaQueryWrapper<MtApp>().eq(MtApp::getAppId, appId));
    }

    default void resetAppSecret(Long id, String newSecret) {
        MtApp updateObj = new MtApp();
        updateObj.setId(id);
        updateObj.setAppSecret(newSecret);
        updateById(updateObj);
    }

}
