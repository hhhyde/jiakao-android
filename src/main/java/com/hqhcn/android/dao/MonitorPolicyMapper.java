package com.hqhcn.android.dao;

import com.hqhcn.android.entity.MonitorPolicy;
import com.hqhcn.android.entity.MonitorPolicyExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MonitorPolicyMapper {
    long countByExample(MonitorPolicyExample example);

    int deleteByExample(MonitorPolicyExample example);

    int insert(MonitorPolicy record);

    int insertSelective(MonitorPolicy record);

    List<MonitorPolicy> selectByExample(MonitorPolicyExample example);

    int updateByExampleSelective(@Param("record") MonitorPolicy record, @Param("example") MonitorPolicyExample example);

    int updateByExample(@Param("record") MonitorPolicy record, @Param("example") MonitorPolicyExample example);



    List selectByExampleToPage(MonitorPolicyExample example);
}