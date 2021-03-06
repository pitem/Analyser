/**
 * @author yuanhh1
 *
 * @email yuanhh1@lenovo.com
 *
 */
package com.yhh.analyser.model;

import android.content.Context;

import com.yhh.androidutils.NumberUtils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;


/**
 * Info factory that contains all kinds of infomation.
 *
 */
public class InfoFactory {
    private DecimalFormat mFormat;
    private CpuInfo mCpuInfo;
    private GpuInfo mGpuInfo;
    private MemoryInfo mMemoryInfo;
    private BatteryWorker mBatteryWorker;
    private PowerInfo mPowerInfo;
    private ScreenInfo mScreenInfo;
    private TrafficInfo mTrafficInfo;
    private Context mContext;

    private BatteryModel mBattery;

    private static InfoFactory mInfoFactory;

    private InfoFactory(){
        mCpuInfo = new CpuInfo();
        mGpuInfo = new GpuInfo();
        mMemoryInfo = new MemoryInfo();
        mBatteryWorker = new BatteryWorker();
        mTrafficInfo = new TrafficInfo();
        mPowerInfo = new PowerInfo();
        mScreenInfo = new ScreenInfo();

        mFormat = new DecimalFormat();
        mFormat.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
        mFormat.setGroupingUsed(false);
        mFormat.setMaximumFractionDigits(2);
        mFormat.setMinimumFractionDigits(2);
    }

    public static InfoFactory getInstance(){
        if(null == mInfoFactory){
            mInfoFactory = new InfoFactory();
        }
        return mInfoFactory;
    }

    /********* INIT ***********/
    public void init(Context context){
        mContext = context;
        mBatteryWorker.init(context);
        mBatteryWorker.register();
        mBattery = mBatteryWorker.getBattery();

        mTrafficInfo.init(context);
    }

    public void destory(){
        mBatteryWorker.unregister();
    }

    /******* CPU *********/
    public String getCpuPidUsedRatio(int pid){
        return mCpuInfo.getCpuRatio();
    }

    public String getCpuPidUsedRatioComplete(int pid) {
        return mCpuInfo.getCpuRatioComplete();
    }


    public CpuInfo getCpuInfo(){
        return mCpuInfo;
    }

    /**
     * 0: total cpu
     * 1-8: cpu 0~7
     *
     * @return
     */
    public ArrayList<String> getCpuRatioList(){
        return mCpuInfo.getCpuRatioList();
    }

    public String getCpuUsedRatioBySeperate(){
        ArrayList<String> cpuRatios = mCpuInfo.getCpuRatioList();
        StringBuffer cpuRatioArray = new StringBuffer();
        for(int i=1; i<cpuRatios.size();i++){
            cpuRatioArray.append(cpuRatios.get(i) + "/");
        }
        for (int i = 0; i < mCpuInfo.getCpuNums() - cpuRatios.size() + 1; i++) {
            cpuRatioArray.append("0.00,");
        }
        return cpuRatioArray.toString();
    }

    public String getCpuFreqList(){
        return mCpuInfo.getCpuFreqs();
    }

    /******* GPU *********/
    public String getGpuClock(){
        return mFormat.format(mGpuInfo.getGpuClock());
    }

    public String getGpuRate(){
        return mFormat.format(mGpuInfo.getGpuRate());
    }

    /******* MEMORY *********/
    public String getMemoryPidUsedSize(int pid, Context context){
        long pidMemory =mMemoryInfo.getPidMemorySize(pid, context);
        return mFormat.format((double) pidMemory / 1024);
    }

    public String getMemoryUnusedSize(Context context){
        long freeMemory = mMemoryInfo.getFreeMemorySize(context);
        return mFormat.format((double) freeMemory / 1024);
    }

    /******* POWER *********/
    public String getPowerCurrent(){
        return mFormat.format(mPowerInfo.getcurrent());
    }

    /******* SCREEN *********/
    public String getScreenBrightness() {
        return String.valueOf(mScreenInfo.getBrightness());
    }

    /******* BATTERY *********/
    public String getBatteryLevel(){
        return String.valueOf(mBattery.getLevel());
    }

    public String getBatteryTemperature(){
        return String.valueOf(mBattery.getTemperature()/10.0);
    }

    public String getBatteryVoltage(){
        return String.valueOf(mBattery.getVoltage()/1000.0);
    }

    /********Traffic*********/
    public String getTrafficSendSpeed(){
        return NumberUtils.format(mTrafficInfo.getSendSpeed(), 2);
    }

    public String getTrafficRevSpeed(){
        return NumberUtils.format(mTrafficInfo.getRevSpeed(), 2);
    }
}