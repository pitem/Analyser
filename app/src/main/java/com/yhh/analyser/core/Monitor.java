package com.yhh.analyser.core;

import android.content.Context;
import android.util.Log;

import com.yhh.analyser.R;
import com.yhh.analyser.config.AppConfig;
import com.yhh.analyser.utils.ConstUtils;
import com.yhh.analyser.utils.DebugLog;
import com.yhh.analyser.utils.FileUtils;
import com.yhh.analyser.utils.TimeUtils;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by yuanhh1 on 2015/8/19.
 */
public abstract class Monitor {
    private static final String TAG = ConstUtils.DEBUG_TAG+ "Monitor";

    public static String resultFilePath;
    private BufferedWriter bw;

    protected Context mContext;
    private String[] itemTitles;
    private String[] itemUnitTitles;

    public Monitor(Context context){
        mContext = context;
        onStart();
    }

    public void onStart(){
        createMonitorFile();
        writeTitle2File();
        initResouces();
    }

    public void onDestroy(){
        close();
    };

    /**
     * 获取监控信息的头信息
     */
    public abstract String getMonitorTitle();

    /**
     * 开始监控
     */
    public abstract String monitor();

    /**
     * 将信息持久化到文件中
     *in
     * @param infos
     */
    protected void write2File(String... infos){
        StringBuffer sb = new StringBuffer();
        sb.append(TimeUtils.getStandardTime()+",");

        for (String info: infos ) {
            sb.append(info).append(",");
        }
        sb.append(ConstUtils.LINE_END);
        DebugLog.d("Body: "+sb.toString());
        try {
            bw.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭文件流
     */
    public void close() {
        try {
            if (bw != null) {
                bw.close();
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    public String getItemName(int index){
        return itemTitles[index];
    }

    public String getItemUnit(int index){
        return itemUnitTitles[index];
    }


    private void createMonitorFile() {
        resultFilePath = AppConfig.MONITOR_DIR + "/" + TimeUtils.getTime();
        FileUtils.createFile(resultFilePath);
    }

    private void writeTitle2File(){
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultFilePath)));
            String title = getMonitorTitle();
            bw.write(title);
            DebugLog.d("Write title="+ title);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void initResouces(){
        itemTitles = mContext.getResources().getStringArray(R.array.monitor_items);
        itemUnitTitles = mContext.getResources().getStringArray(R.array.monitor_unit_items);
    }

}
