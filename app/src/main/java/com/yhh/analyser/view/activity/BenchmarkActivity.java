/**
 * @author yuanhh1
 * 
 * @email yuanhh1@lenovo.com
 * 
 */
package com.yhh.analyser.view.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.yhh.analyser.R;
import com.yhh.analyser.config.AppConfig;
import com.yhh.analyser.model.PerfBean;
import com.yhh.analyser.model.TempInfo;
import com.yhh.analyser.model.app.PhoneInfo;
import com.yhh.analyser.provider.AutoWorker;
import com.yhh.analyser.utils.DialogUtils;
import com.yhh.analyser.utils.FileMediaScanner;
import com.yhh.analyser.utils.LogUtils;
import com.yhh.analyser.utils.UploadUtils;
import com.yhh.analyser.view.BaseActivity;
import com.yhh.androidutils.FileUtils;
import com.yhh.androidutils.ShellUtils;
import com.yhh.androidutils.TimeUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BenchmarkActivity extends BaseActivity {
    private static final String TAG = LogUtils.DEBUG_TAG +"antutu";
    
    private Button mRobotBtn;
    private Button mViewBtn;
    private Button mUploadBtn;
    private EditText mCount;
    private TextView mCurrentLoopTv;
    private TextView mStatusInfoTv;
    private int selectedFileIndex;
    
    private SharedPreferences.Editor mRobotEditor;
    private SharedPreferences mPref;
    private SharedPreferences.Editor mEditor;
    
    private AutoWorker mRobot;
    private boolean isStart; //自动化是否已经启动
    private boolean isStop; //是否停止运行
    private FileMediaScanner mFileMediaScanner;
    private UploadUtils mUploadUtils;
    
    private String mFileName;
    private BufferedWriter bw;
    
    public static String MONITOR_PATH_TAG="monitor_path";
    private static final String CASE_BENCHMARK= "BenchMark";
    private static final String KEY_BENCHAMRK = "key_benchmark";
    
    private static final int VIEW_MODE = 1;
    private static final int UPLOAD_MODE = 2;
    
    boolean isThermalControl;
    int startTmp;
    boolean isLoogConfig;
    int cpuBigNumUpper;
    int cpuBigNumLower;
    int cpuSmallNumUpper;
    int cpuSmallNumLower;
    int cpuBigFreqUpper;
    int cpuBigFreqLower;
    int cpuSmallFreqUpper;
    int cpuSmallFreqLower;
    int gpuFreqUpper;
    int gpuFreqLower;
    
    int curLoop;
    
    int mRunCount;
    int mLoopCount;
    private static final String LOOP_COUNT = "loop_count";
    
    private PerfBean mPerfBean;

    public BenchmarkActivity() {
        mFileMediaScanner = new FileMediaScanner(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.benchmark_runner);
        
        mPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mEditor = mPref.edit();
        mPerfBean = PerfBean.getInstance();
        
        initUI();
        mUploadUtils = new UploadUtils(this);
        mRobot = new AutoWorker(this);
        mRobotBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getString(R.string.start_benchmark).equals(mRobotBtn.getText().toString())) {
                    saveData();
                    isStop = false;
                    loopRunBenchmark();
                    mRobotBtn.setText(getString(R.string.stop_benchmark));
                    mEditor.putBoolean(KEY_BENCHAMRK, true).commit();
                }else{
                    isStop = true;
                    mRobot.autoStop();
                    mRobotBtn.setText(getString(R.string.start_benchmark));
                    mEditor.putBoolean(KEY_BENCHAMRK, false).commit();
                }
            }
        });
        mRobot.setOnAutomaticListener(new AutoWorker.OnAutomaticListener() {
            
            @Override
            public void onRunComplete() {
                Log.i(TAG,"onRunComplete");
                updateSdcard();
                mEditor.putBoolean(KEY_BENCHAMRK, false).commit();
                
                String[] files = listFiles();
                upload(files[0]);
            }
            
            @Override
            public void onPrepareComplete() {
                Log.i(TAG,"onPrepareComplete");
                DialogUtils.closeLoading();
            }
        });
        
        // copy robot需要在 setOnAutomaticListener之后
        mRobot.copyRobotResouce();
        //必须在copy resource之后再获取robot editor，否则会出现异常
        mRobotEditor = getSharedPreferences("robot_config", 0).edit(); 
    }
    
    @Override
    protected void onResume() {
        isStart = mPref.getBoolean(KEY_BENCHAMRK, false);
        if(isStart){
            mRobotBtn.setText(getString(R.string.stop_benchmark));
        }else{
            mRobotBtn.setText(getString(R.string.start_benchmark));
        }
        readRefs();
        mLoopCount = this.getSumLoop();
        mCurrentLoopTv.setText("LOOP= "+ + mLoopCount);
        super.onResume();
    }
    
    private void updateSdcard(){
        ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(5);
        scheduledThreadPool.schedule(new Runnable() {
         
            @Override
            public void run() {
                mFileMediaScanner.scan(AppConfig.ROOT_DIR);
            }
        }, 1, TimeUnit.NANOSECONDS);
    }
    
    private void upload(String filename){
        String path = AppConfig.ROOT_DIR+filename;
        String key = getUploadKey(filename);
        mUploadUtils.uploadFile(key, path);
    }
    
    private void initUI(){
        mCount = (EditText) findViewById(R.id.benchmark_count);
        mStatusInfoTv = (TextView) findViewById(R.id.status_info_tv);
        mCurrentLoopTv = (TextView) findViewById(R.id.loop_count_tv);
        mRobotBtn = (Button) findViewById(R.id.benchmark_run);
        mViewBtn = (Button) findViewById(R.id.benchmark_view);
        mUploadBtn = (Button) findViewById(R.id.benchmark_upload);
        
        mViewBtn.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                showDataDialog(VIEW_MODE);
            }
       });
        
        mUploadBtn.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View arg0) {
                showDataDialog(UPLOAD_MODE);
            }
        });
    }
    
    public void readRefs(){
        String startTmpStr = mPref.getString(BenchmarkSettingsActivity.START_TEMP_KEY, "");
        if("".equals(startTmpStr)){
            startTmp =999;
        }else{
            startTmp = Integer.valueOf(startTmpStr);
        }
        
        isThermalControl = mPref.getBoolean(BenchmarkSettingsActivity.THERMAL_CONTROL_KEY, true);
        isLoogConfig = mPref.getBoolean(BenchmarkSettingsActivity.LOOP_CONFIG_KEY, true);
        cpuBigNumUpper = mPref.getInt(BenchmarkSettingsActivity.CPU_BIG_NUM_UPPER_KEY, 2);
        cpuBigNumLower = mPref.getInt(BenchmarkSettingsActivity.CPU_BIG_NUM_LOWER_KEY, 0);
        cpuSmallNumUpper = mPref.getInt(BenchmarkSettingsActivity.CPU_SMALL_NUM_UPPER_KEY, 4);
        cpuSmallNumLower = mPref.getInt(BenchmarkSettingsActivity.CPU_SMALL_NUM_LOWER_KEY, 0);
        cpuBigFreqUpper = mPref.getInt(BenchmarkSettingsActivity.CPU_BIG_FRQ_UPPER_KEY, 2);
        cpuBigFreqLower = mPref.getInt(BenchmarkSettingsActivity.CPU_BIG_FRQ_LOWER_KEY, 0);
        cpuSmallFreqUpper = mPref.getInt(BenchmarkSettingsActivity.CPU_SMALL_FRQ_UPPER_KEY, 4);
        cpuSmallFreqLower = mPref.getInt(BenchmarkSettingsActivity.CPU_SMALL_FRQ_LOWER_KEY, 0);
        gpuFreqUpper = mPref.getInt(BenchmarkSettingsActivity.GPU_FRQ_UPPER_KEY, 4);
        gpuFreqLower = mPref.getInt(BenchmarkSettingsActivity.GPU_FRQ_LOWER_KEY, 0);
        mRunCount = mPref.getInt(LOOP_COUNT, 1);
        
        mCount.setText(mRunCount+"");
    }
    
    private void saveData(){
        String countStr = mCount.getText().toString();
        try{
            int count = Integer.valueOf(countStr);
            mEditor.putInt(LOOP_COUNT, count);
            mEditor.commit();
        }catch(Exception e){
            Log.e(TAG,"count format wrong:"+countStr,e);
            Toast.makeText(this, "请输入整数", Toast.LENGTH_SHORT).show();
        }
    }
    
    public void loopRunBenchmark(){
        createFile();
        writeTitle2File(isLoogConfig);
        
        curLoop = 0;
        final String cmd = mRobot.getAutoCmd(CASE_BENCHMARK);
        new Thread(new Runnable(){

            @Override
            public void run() {
                Log.i(TAG,"==>    isLoogConfig="+isLoogConfig);
                if(isLoogConfig){
                    for(int i = cpuBigNumUpper; i>=cpuBigNumLower &&!isStop; i++){
                        for(int j=cpuSmallNumUpper; j>=cpuSmallNumLower &&!isStop; j++){
                            for(int k=cpuBigFreqUpper; k>=cpuBigFreqLower &&!isStop; k++){
                                for(int l=cpuSmallFreqUpper; l>=cpuSmallFreqLower &&!isStop; l++){
                                    for(int m=gpuFreqUpper; m>=gpuFreqLower &&!isStop; m++){
                                        StringBuffer sb = new StringBuffer();
                                        sb.append(i).append(",");
                                        sb.append(j).append(",");
                                        sb.append(PerfBean.CPU_BIG_FREQ[k]/1000).append(",");
                                        sb.append(PerfBean.CPU_LITTER_FREQ[l]/1000).append(",");
                                        sb.append(PerfBean.GPU_FREQ[PerfBean.GPU_FREQ_NUM - m -1]).append(",");
                                        
                                        String status = setPerf(i,j,k,l,m);
                                        Log.i(TAG,"waitForTmp START");
                                        waitForTmp();
                                        Log.i(TAG,"waitForTmp END");
                                        if(!isStop){
                                            writeInfo2File(sb.toString());
                                            mHandler.sendMessage(mHandler.obtainMessage(1, status));
                                            ShellUtils.execCommand(cmd);
//                                            ShellUtils.runShell(cmd);
                                        }
                                       
                                    }
                                }
                            }
                        }
                    }
                }else{
                    waitForTmp();
                    if(!isStop) {
                        ShellUtils.execCommand(cmd);
                    }
                }
                mHandler.sendMessage(mHandler.obtainMessage(2));
        }}).start();
        
    }
    
    private void waitForTmp(){
        float curTmp =TempInfo.get();
        while(curTmp >startTmp &&!isStop){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            curTmp =TempInfo.get();
        }
        Log.i(TAG,"curTmp="+curTmp);
    }
    
    public Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            if(msg.what ==1){
                mStatusInfoTv.setText((String)(msg.obj));
                curLoop++;
                mCurrentLoopTv.setText("LOOP= "+curLoop + "/" + mLoopCount);
            }else if(msg.what ==2){
                isStop = true;
                Log.i(TAG,"onRunComplete");
                updateSdcard();
                mEditor.putBoolean(KEY_BENCHAMRK, false).commit();
                
                String[] files = listFiles();
                upload(files[0]);
            }
        }
    };
    
    private String setPerf(int cpuBigNum, int cpuSmallNum, int cpuBigFreq, int cpuSmallFreq, int gpuFreq){
        int[] sources = new int[4];
        sources[0] = mPerfBean.getCpuOn(cpuBigNum, cpuSmallNum);
        sources[1] = mPerfBean.getBigCpuFreq(cpuBigFreq, cpuBigFreq);
        sources[2] = mPerfBean.getLittleCpuFreq(cpuSmallFreq, cpuSmallFreq);
        sources[3] = mPerfBean.getGpuFreq(gpuFreq, gpuFreq);
        mPerfBean.execute(sources);
        
        StringBuffer sb = new StringBuffer();

        sb.append("CPU大核：").append(cpuBigNum).append("个， ");
        sb.append(PerfBean.CPU_BIG_FREQ[cpuBigFreq]/1000).append("MHz");
        sb.append("\n");
        
        sb.append("CPU小核：").append(cpuSmallNum).append("个， ");
        sb.append(PerfBean.CPU_LITTER_FREQ[cpuSmallFreq]/1000).append("MHz");
        sb.append("\n");
        
        sb.append("GPU频率：");
        sb.append(PerfBean.GPU_FREQ[PerfBean.GPU_FREQ_NUM - gpuFreq - 1]).append("MHz");
        return sb.toString();
    }
    
    private int getSumLoop(){
        int loop = (cpuBigNumUpper - cpuBigNumLower+1)*(cpuSmallNumUpper - cpuSmallNumLower +1) *
                (cpuBigFreqUpper - cpuBigFreqLower +1) *(cpuSmallFreqUpper - cpuSmallFreqLower +1)*
                (gpuFreqUpper - gpuFreqLower +1);
        Log.i(TAG,"getSumLoop = "+ loop);
        if(loop<1){
            loop = 1;
        }
        return loop;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.menu_setting, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_settings){
            Intent i = new Intent(this, BenchmarkSettingsActivity.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }
    
    private String getUploadKey(String fileName){
        StringBuilder key=new StringBuilder();
        
        String brand="";
        String version = PhoneInfo.getInternalVersion();
        
        String[] tmp = version.split("_");
        if(tmp.length>1){
            brand = tmp[0];
        }
        
        String regex = "1\\d[0-1][0-9][0-3][0-9]";
        Matcher matcher = Pattern.compile(regex).matcher(version);
        if(matcher.find()){
            version = tmp[1]+"_"+matcher.group();
        }else{
            version="unknown";
        }
        
        String imei = PhoneInfo.getIMEI(this);
        
        key.append(brand).append("/");
        key.append(version).append("/");
        key.append(imei).append("/");
        key.append(fileName);
        return key.toString();
    }
    
    
    public void showDataDialog(final int mode){
        updateSdcard();
        final String[] files = listFiles();
        
        new AlertDialog.Builder(this)
        .setTitle(R.string.dialog_choose_title)
        .setSingleChoiceItems(files, -1, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedFileIndex = which;
            }
        })
        .setNegativeButton(R.string.no_str, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                
            }
        })
        .setPositiveButton(R.string.yes_str, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(files==null || files.length <1){
                    return;
                }
                
                if(mode == VIEW_MODE){
                    Intent intent = new Intent(BenchmarkActivity.this, BenchmarkChartActivity.class);
                    intent.putExtra(MONITOR_PATH_TAG, files[selectedFileIndex]);
                    startActivity(intent);
                }else if(mode == UPLOAD_MODE){
                    Toast.makeText(BenchmarkActivity.this, "正在上传中...", Toast.LENGTH_LONG).show();
                    new Thread(new Runnable(){

                        @Override
                        public void run() {
                            upload(files[selectedFileIndex]);
                        }
                        
                    }).start();
                }
            }
        }).show();
    }
    
    private void createFile(){
        String curTime = TimeUtils.getCurrentTime(TimeUtils.DATETIME_UNDERLINE_FORMAT);
        mFileName =  AppConfig.BENCHMACH_FILE  + curTime+".csv";
        mRobotEditor.putString(CASE_BENCHMARK+ "#file", curTime).commit();
        try {
            FileUtils.createFile(mFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
   
    private void writeTitle2File(boolean hasConfig){
        StringBuffer title = new StringBuffer();
        if(hasConfig){
            title.append("CpuBigNum").append(",");
            title.append("CpuLittleNum").append(",");
            title.append("CpuBigFreq").append(",");
            title.append("CpuLittleFreq").append(",");
            title.append("GpuFreq").append(",");
        }
        title.append("StartTime").append(",");
        title.append("StartTemp").append(",");
        title.append("ChangedTemp").append(",");
        title.append("Scores").append(",");
        title.append("Multi_Task").append(",");
        title.append("Android_Runtime").append(",");
        title.append("CPU_Int").append(",");
        title.append("CPU_Float").append(",");
        title.append("Single_CPU_Int").append(",");
        title.append("Single_CPU_Float").append(",");
        title.append("RAM_Calc").append(",");
        title.append("RAM_Speed").append(",");
        title.append("2D_Draw").append(",");
        title.append("3D_Draw").append(",");
        title.append("SDW_IO").append(",");
        title.append("DB_IO").append("\n");
        writeInfo2File(title.toString());
    }
    
    public void writeInfo2File(String info){
        try {
            File resultFile = new File(mFileName);
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultFile,true)));
            bw.append(info);
            bw.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }
    
    private String[] listFiles(){
        File parentDir = new File(AppConfig.ROOT_DIR);
        String[] files = parentDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                File current = new File(dir, filename);
                return current.isFile() && filename.endsWith(".csv");
            }
        });
        if(files ==null || files.length <=0){
            return null;
        }
        Arrays.sort(files, Collections.reverseOrder());
        return files;
    }
}
