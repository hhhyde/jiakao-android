package com.hqhcn.android.service.impl;

import com.hqhcn.android.dao.CameraInfoMapper;
import com.hqhcn.android.entity.CameraInfo;
import com.hqhcn.android.entity.CameraInfoExample;
import com.hqhcn.android.service.CameraInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CameraInfoServiceImpl implements CameraInfoService {

    @Autowired
    CameraInfoMapper mapper;

    @Override
    public List selectByExample(CameraInfoExample example) {
        return mapper.selectByExample(example);
    }

    @Override
    public List select(CameraInfo entity) {
        CameraInfoExample example = new CameraInfoExample();
        CameraInfoExample.Criteria criteria = example.createCriteria();

        if (StringUtils.isNotEmpty(entity.getKsld())){
            criteria.andKsldEqualTo(entity.getKsld());
        }
        if (StringUtils.isNotEmpty(entity.getKsxm())){
            criteria.andKsxmEqualTo(entity.getKsxm());
        }
        if (StringUtils.isNotEmpty(entity.getKskm())){
            criteria.andKskmEqualTo(entity.getKskm());
        }

        return selectByExample(example);
    }

    @Override
    public void insert(CameraInfo entity) {
        mapper.insert(entity);
    }

    @Override
    public List selectByExampleToPage(CameraInfoExample example) {
        return mapper.selectByExampleToPage(example);
    }

    @Override
    public CameraInfo get(long id) {
        return mapper.selectByPrimaryKey(id);
    }

    @Override
    public void updateByPrimaryKeySelective(CameraInfo entity) {
        mapper.updateByPrimaryKeySelective(entity);
    }

    @Override
    public void delete(long id) {
        mapper.deleteByPrimaryKey(id);
    }
}
