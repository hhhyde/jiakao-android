package com.hqhcn.android.dao;

import com.hqhcn.android.entity.Gps;
import com.hqhcn.android.entity.GpsExample;
import com.hqhcn.android.entity.GpsKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface GpsMapper {
    long countByExample(GpsExample example);

    int deleteByExample(GpsExample example);

    int deleteByPrimaryKey(GpsKey key);

    int insert(Gps record);

    int insertSelective(Gps record);

    List<Gps> selectByExampleWithBLOBs(GpsExample example);

    List<Gps> selectByExample(GpsExample example);

    Gps selectByPrimaryKey(GpsKey key);

    int updateByExampleSelective(@Param("record") Gps record, @Param("example") GpsExample example);

    int updateByExampleWithBLOBs(@Param("record") Gps record, @Param("example") GpsExample example);

    int updateByExample(@Param("record") Gps record, @Param("example") GpsExample example);

    int updateByPrimaryKeySelective(Gps record);

    int updateByPrimaryKeyWithBLOBs(Gps record);

    int updateByPrimaryKey(Gps record);

    int appendGPSByPrimaryKey(Gps record);
}