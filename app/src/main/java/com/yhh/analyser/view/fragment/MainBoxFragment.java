/**
 * @author yuanhh1
 * 
 * @email yuanhh1@lenovo.com
 * 
 */
package com.yhh.analyser.view.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.yhh.analyser.R;
import com.yhh.analyser.utils.ConstUtils;
import com.yhh.analyser.view.activity.AdbWirelessActivity;
import com.yhh.analyser.view.activity.AutomaticActivity;
import com.yhh.analyser.view.activity.BenchmarkActivity;
import com.yhh.analyser.view.activity.BrightnessActivity;
import com.yhh.analyser.view.activity.KernelActivity;
import com.yhh.analyser.view.activity.LogAnalyActivity;
import com.yhh.analyser.view.activity.LogViewActivity;
import com.yhh.analyser.view.activity.NodeViewActivity;
import com.yhh.analyser.view.activity.OneKeySleepActivity;
import com.yhh.analyser.view.activity.SyncTimeActivity;
import com.yhh.analyser.view.activity.WakeLockActivity;
import com.yhh.androidutils.AppUtils;
import com.yhh.androidutils.FileUtils;
import com.yhh.androidutils.IOUtils;
import com.yhh.terminal.Term;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainBoxFragment extends Fragment{
    private static final String TAG =  ConstUtils.DEBUG_TAG+ "MainBoxFragment";
    private Context mContext;

    private List<Map<String, Object>> mDataList;
    private SimpleAdapter mAdapter;
    private GridView mGridView;


    private final int[] mItemImage = new int[]{
            R.drawable.box_view_node,
            R.drawable.box_brightness,
            R.drawable.box_wakelock,
            R.drawable.box_adbwireless,

            R.drawable.box_parse_log,
            R.drawable.box_view_log,
            R.drawable.box_terminal,
            R.drawable.box_antutu,

            R.drawable.box_auto,
            R.drawable.box_kernel,
            R.drawable.box_cpu,
            R.drawable.box_io,

            R.drawable.box_vibrator,
            R.drawable.box_1,
            R.drawable.box_2,
            R.drawable.box_more
    };

    private final String[] mItemName = new String[]{
            "查看节点","调节亮度","唤醒锁","远程ADB",
            "解析Log","查看Log","模拟终端", "安兔兔跑分",
            "自动化Case","模拟死机", "CPU压测", "IO压测",
            "震动微调器","一键休眠","时钟微调","重启压测"
    };


    private Class[] targetClasses = new Class[]{
            NodeViewActivity.class,
            BrightnessActivity.class,
            WakeLockActivity.class,
            AdbWirelessActivity.class,
            
            LogAnalyActivity.class,
            LogViewActivity.class,
            Term.class,
            BenchmarkActivity.class,

            AutomaticActivity.class,
            KernelActivity.class,

            OneKeySleepActivity.class,
            SyncTimeActivity.class
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getActivity();
        setData();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.toolbox_more, null);
        initView(v);
        return v;
    }

    public void setData(){
        mDataList = new ArrayList<>();
        for(int i=0;i<mItemName.length;i++){
            Map<String, Object> map = new HashMap<>();
            map.put("image", mItemImage[i]);
            map.put("text", mItemName[i]);
            mDataList.add(map);
        }
    }

    private void initView(View v){
        String[] from = {"image", "text"};
        int[] to = {R.id.image, R.id.text};
        mAdapter = new SimpleAdapter(mContext, mDataList, R.layout.main_toolbox_item, from, to);

        mGridView = (GridView) v.findViewById(R.id.more_gv);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                final int index = position;
                if (position <= 9) {
                    Intent intent = new Intent(mContext, targetClasses[index]);
                    mContext.startActivity(intent);
                } else {
                    startApp(position-9);
                }
            }

        });
    }

    public void runApp( int rawId, String AppName, String pkgName, String ActName){
        if(AppUtils.isAppInstalled(mContext, pkgName)){
            //启动apk
            AppUtils.startApp(mContext, pkgName, ActName);
        }else{
            String path = Environment.getExternalStorageDirectory().toString() + "/systemAnalyzer/"+AppName+".apk";
            //资源拷贝
            copyRaw2Local(mContext, rawId, path);
            //启动安装过程
            AppUtils.installApp(mContext, path);
        }
    }

        public static boolean copyRaw2Local(Context context, int rawId, String targetPath) {
        File file = new File(targetPath);
        //资源已经拷贝,无需重复拷贝
        if (file.exists()) {
            return true;
        }

            try {
                if (!FileUtils.createFile(targetPath)) {
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            InputStream is = null;
        FileOutputStream fos = null;
        try {
            is = context.getResources().openRawResource(rawId);
            fos = new FileOutputStream(targetPath);
            byte[] buffer = new byte[2048];
            int count;
            while ((count = is.read(buffer)) != -1) {
                fos.write(buffer, 0, count);
            }
            return true;
        } catch (IOException e) {
            Log.e(TAG, "copyRaw2Local Exception ", e);
        } finally {
            IOUtils.closeQuietly(fos, is);
        }
        return false;
    }

    public void startApp(int index){
        String AppName;
        String pkgName;
        String ActName ;
        int rawId;

        switch(index){
            case 1:
                rawId = R.raw.cputiger;
                AppName = "cputiger";
                pkgName = "com.tiger.cpu";
                ActName = "com.tiger.cpu.Main";
                runApp(rawId, AppName, pkgName, ActName);
                break;

            case 2:
                rawId = R.raw.iotiger;
                AppName = "iotiger";
                pkgName = "com.tiger.io";
                ActName = "com.tiger.io.IOActivity";
                runApp(rawId, AppName, pkgName, ActName);
                break;


            case 3:
                rawId = R.raw.vibrator_tool;
                AppName = "vibrator_tool";
                pkgName = "com.lenovo.vibratortool";
                ActName = "com.lenovo.vibratortool.MainActivity";
                runApp(rawId, AppName, pkgName, ActName);
                break;

            case 4:
                Intent intent = new Intent(mContext, targetClasses[10]);
                mContext.startActivity(intent);
                break;

            case 5:
                Intent intent2 = new Intent(mContext, targetClasses[11]);
                mContext.startActivity(intent2);
                break;

            case 6:
                rawId = R.raw.rebooter;
                AppName = "Rebooter";
                pkgName = "com.yhh.rebooter";
                ActName = "com.yhh.rebooter.MainActivity";
                runApp(rawId, AppName, pkgName, ActName);
                break;

            default:
                Toast.makeText(mContext, "正在设计筹划中，敬请期待！", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}