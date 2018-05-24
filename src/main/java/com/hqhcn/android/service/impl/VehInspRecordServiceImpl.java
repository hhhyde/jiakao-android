package com.hqhcn.android.service.impl;

import com.hqhcn.android.dao.VehInspRecordMapper;
import com.hqhcn.android.entity.Carinfo;
import com.hqhcn.android.entity.VehInspRecord;
import com.hqhcn.android.entity.VehInspRecordExample;
import com.hqhcn.android.service.CarinfoService;
import com.hqhcn.android.service.VehInspRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehInspRecordServiceImpl implements VehInspRecordService {

    @Autowired
    VehInspRecordMapper mapper;
    @Autowired
    CarinfoService carinfoService;


    @Override
    public void create(VehInspRecord entity) {
        mapper.insert(entity);
    }

    @Override
    public void createSelective(VehInspRecord entity) {
        Carinfo carinfo = new Carinfo();
        carinfo.setJlcxh(entity.getJlcxh());
        carinfo.setJlczt(entity.getJlczt());
        carinfo.setKcdddh(entity.getKcdddh());
        carinfoService.update(carinfo);
        mapper.insertSelective(entity);
    }

    @Override
    public List<VehInspRecord> retrieve(VehInspRecordExample example) {
        return mapper.selectByExample(example);
    }

    @Override
    public VehInspRecord retrieve(long id) {
        return mapper.selectByPrimaryKey(id);
    }

    @Override
    public void update(VehInspRecord entity, VehInspRecordExample example) {
        mapper.updateByExample(entity, example);
    }

    @Override
    public void updateSelective(VehInspRecord entity, VehInspRecordExample example) {
        mapper.updateByExampleSelective(entity, example);
    }

    @Override
    public void delete(long id) {
        mapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<VehInspRecord> selectByExampleToPage(VehInspRecordExample example) {
        return mapper.selectByExampleToPage(example);
    }
}
