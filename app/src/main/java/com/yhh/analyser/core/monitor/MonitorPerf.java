package com.yhh.analyser.core.monitor;

import android.content.Context;

import com.yhh.analyser.model.CpuInfo;
import com.yhh.analyser.model.GpuInfo;
import com.yhh.analyser.model.MemoryInfo;
import com.yhh.analyser.core.MonitorFactory;
import com.yhh.androidutils.NumberUtils;

import java.util.ArrayList;

/**
 * Created by yuanhh1 on 2015/8/19.
 */
public class MonitorPerf extends Monitor {
    private MemoryInfo mMemoryInfo;
    private GpuInfo mGpuInfo;
    private CpuInfo mCpuInfo;

    private ArrayList<String> mContentList;

    @Override
    public Integer[] getItems() {
        return new Integer[]{
                MonitorFactory.CPU_USED_RATIO,
                MonitorFactory.GPU_USED_RATIO,
                MonitorFactory.GPU_CLOCK,
                MonitorFactory.MEM_FREE,
        };
    }

    @Override
    public String getFileType() {
        return "_Performance";
    }

    public MonitorPerf(Context context) {
        super(context);

    }

    @Override
    public void onStart() {
        super.onStart();
        mContentList = new ArrayList<>();
        mMemoryInfo = new MemoryInfo();
        mGpuInfo = new GpuInfo();
        mCpuInfo = new CpuInfo();
    }

    @Override
    public String monitor() {
        mCpuInfo.updateAllCpu();

        mContentList.clear();
        mContentList.add(String.valueOf(mCpuInfo.getCpuRatioList().get(0)));
        mContentList.add(String.valueOf(NumberUtils.format(mGpuInfo.getGpuRate(), 2)));
        mContentList.add(String.valueOf(mGpuInfo.getGpuClock()));
        mContentList.add(String.valueOf(mMemoryInfo.getFreeMemorySize(mContext) / 1024));

        write2File(mContentList);

        return getFloatBody(mContentList);
    }
}
