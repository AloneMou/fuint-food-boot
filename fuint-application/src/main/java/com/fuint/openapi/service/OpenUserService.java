package com.fuint.openapi.service;

import com.fuint.repository.model.MtUser;

import java.util.List;

/**
 * @author Miao
 * @date 2026/1/23
 */
public interface OpenUserService {

    /**
     * 查询用户列表
     *
     * @param mobiles 手机号列表
     * @return 用户列表
     */
    List<MtUser> getUserLsByMobiles(List<String> mobiles);

    /**
     * 更新单个用户信息
     *
     * @param mtUser 用户信息
     */
    void updateMember(MtUser mtUser);


}
