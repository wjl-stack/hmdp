package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_KEY;
/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public List<ShopType> queryTypeList() {
        String key = CACHE_SHOP_KEY;
        //1.从redis中查询店铺类型缓存
        List<String> list = stringRedisTemplate.opsForList().range(key, 0, -1);
        List<ShopType> shopTypeList = new ArrayList<>();
        //2.判断是否为空
        if (!list.isEmpty()) {
            //3.存在，直接返回
            for (String s : list) {
                ShopType shopType = JSONUtil.toBean(s, ShopType.class);
                shopTypeList.add(shopType);
            }
            return shopTypeList;
        }
        //4.不存在，从数据库中查询写入redis
        shopTypeList = query().orderByAsc("sort").list();
        //5.不存在，返回空
        if (shopTypeList.isEmpty()) {
            return new ArrayList<>();
        }
        //6.存在，写入redis
        for (ShopType shopType : shopTypeList) {
            stringRedisTemplate.opsForList().rightPush(key, JSONUtil.toJsonStr(shopType));
        }
        //7.返回
        return shopTypeList;
    }
}

