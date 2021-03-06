/**
 * @author yuanhh1
 * 
 * @email yuanhh1@lenovo.com
 * 
 */
package com.yhh.analyser.view.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.yhh.analyser.R;
import com.yhh.analyser.model.PerfBean;
import com.yhh.analyser.utils.LogUtils;
import com.yhh.analyser.view.BaseFragment;
import com.yhh.analyser.widget.rangebar.RangeBar;

public class PerfGpuFragment extends BaseFragment {
    private static final String TAG = LogUtils.DEBUG_TAG +"PerfGpuFragment";

    private RangeBar mGpuFreqBar;
    private TextView mGpufreqTv;

    private int mLowerFrq;
    private int mUpperFreq;

    private Button mReflashBtn;
    private boolean mReflashing;

    private static String GPU_FREQ_MAX = "/sys/class/kgsl/kgsl-3d0/max_pwrlevel";
    private static String GPU_FREQ_MIN = "/sys/class/kgsl/kgsl-3d0/min_pwrlevel";

    private PerfBean mPerfBean;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPerfBean = PerfBean.getInstance();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.performance_gpu, null);
        mGpuFreqBar = (RangeBar) view.findViewById(R.id.gpu_freq_bar);
        mGpufreqTv = (TextView) view.findViewById(R.id.gpu_freq_tv);

        mReflashBtn = (Button) view.findViewById(R.id.reflash_btn);
        
        initListener();
        getData();
        updateUI();
        
        return view;
    }
    
    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            if(msg.what == 1){
                getData();
                updateUI();
                Toast.makeText(mContext, R.string.reflash_comlepte, Toast.LENGTH_SHORT).show();
                mReflashing = false;
            }
        }
    };
    
    private void initListener(){
        mGpuFreqBar.setTickCount(PerfBean.GPU_FREQ_NUM);
        
        mGpuFreqBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onIndexChangeListener(RangeBar rangeBar, int leftThumbIndex, int rightThumbIndex) {
                Log.i(TAG,"leftThumbIndex="+leftThumbIndex+", rightThumbIndex="+rightThumbIndex);
                mLowerFrq = PerfBean.GPU_FREQ_NUM - leftThumbIndex -1;
                mUpperFreq = PerfBean.GPU_FREQ_NUM - rightThumbIndex - 1;
                mGpufreqTv.setText("[  "+ PerfBean.GPU_FREQ[mLowerFrq]
                        +",  " + PerfBean.GPU_FREQ[mUpperFreq] +"  ]");
            }
        });
        
        
        mReflashBtn.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if(mReflashing){
                    return;
                }
                mReflashing = true;
                updateData();
                Toast.makeText(mContext, R.string.reflash_working, Toast.LENGTH_SHORT).show();
                new Thread(new Runnable(){

                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            
                        }
                        mHandler.sendMessage(mHandler.obtainMessage(1));
                    }
                    
                }).start();
            }
        });
    }
    
    private void updateUI(){
        mGpuFreqBar.setThumbIndices(PerfBean.GPU_FREQ_NUM - 1- mLowerFrq, PerfBean.GPU_FREQ_NUM -1 -mUpperFreq);
    }
    
    /**
     * get cpu info from kernel
     */
    private void getData(){
        mLowerFrq = PerfBean.getGpuFreqLevel(GPU_FREQ_MIN);
        mUpperFreq = PerfBean.getGpuFreqLevel(GPU_FREQ_MAX);
        Log.i(TAG,"get gpu frq ["+mUpperFreq+","+mLowerFrq+"]");
    }
    
    /**
     *  updata gpu data into kernel
     */
    private void updateData(){
        Log.i(TAG,"set gpu frq ["+mUpperFreq+","+mLowerFrq+"]");
        int[] sources = new int[1];
        sources[0] = mPerfBean.getGpuFreq(mUpperFreq, mLowerFrq);
        mPerfBean.execute(sources);
    }
}
