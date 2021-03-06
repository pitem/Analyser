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
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.yhh.analyser.R;
import com.yhh.analyser.model.PerfBean;
import com.yhh.analyser.utils.LogUtils;
import com.yhh.analyser.view.BaseFragment;
import com.yhh.analyser.widget.rangebar.RangeBar;

public class PerfCpuFragment extends BaseFragment {
    private static final String TAG = LogUtils.DEBUG_TAG +"PerfCpuFragment";
    

    private RangeBar mCpuBigFreqBar;
    private RangeBar mCpuSmallFreqBar;
    private RatingBar mCpuBigNumBar;
    private RatingBar mCpuSmallNumBar;
    
    private TextView mCpuBigfreqTv;
    private TextView mCpuSmallfreqTv;
    private TextView mCpuBigNumTv;
    private TextView mCpuSmallNumTv;
    
    private Button mReflashBtn;
    private boolean mReflashing;
    
    private int mBigCoreNum;
    private int mBigLowerFrq;
    private int mBigUpperFreq;
    
    private int mSmallCoreNum;
    private int mSmallLowerFrq;
    private int mSmallUpperFreq;
    
    private static String CPU_ONLINE = "/sys/devices/system/cpu/online";
    private static String CPU_LITTLE_FREQ_MAX = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
    private static String CPU_LITTLE_FREQ_MIN = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq";
    private static String CPU_BIG_FREQ_MAX =    "/sys/devices/system/cpu/cpu4/cpufreq/scaling_max_freq";
    private static String CPU_BIG_FREQ_MIN =    "/sys/devices/system/cpu/cpu4/cpufreq/scaling_min_freq";
    
    private PerfBean mPerfBean;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPerfBean = PerfBean.getInstance();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.performance_cpu, null);
        mCpuBigNumBar = (RatingBar) view.findViewById(R.id.cpu_big_number_bar);
        mCpuBigNumTv = (TextView) view.findViewById(R.id.cpu_big_number_tv);
        mCpuBigFreqBar = (RangeBar) view.findViewById(R.id.cpu_big_freq_bar);
        mCpuBigfreqTv = (TextView) view.findViewById(R.id.cpu_big_freq_tv);
        
        mCpuSmallNumBar = (RatingBar) view.findViewById(R.id.cpu_small_number_bar);
        mCpuSmallNumTv = (TextView) view.findViewById(R.id.cpu_small_number_tv);
        mCpuSmallFreqBar = (RangeBar) view.findViewById(R.id.cpu_small_freq_bar);
        mCpuSmallfreqTv = (TextView) view.findViewById(R.id.cpu_small_freq_tv);
        
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
                Log.i(TAG,"reflash compeletely.");
                mReflashing = false;
            }
        }
    };
    
    private void initListener(){
        mCpuBigNumBar.setMax(PerfBean.CPU_BIG_NUM);
        mCpuBigFreqBar.setTickCount(PerfBean.CPU_BIG_FREQ_NUM);
        
        mCpuSmallNumBar.setMax(PerfBean.CPU_LITTER_NUM);
        mCpuSmallFreqBar.setTickCount(PerfBean.CPU_LITTER_FREQ_NUM);
        
        mCpuBigFreqBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onIndexChangeListener(RangeBar rangeBar, int leftThumbIndex, int rightThumbIndex) {
                mBigLowerFrq = leftThumbIndex;
                mBigUpperFreq = rightThumbIndex;
                mCpuBigfreqTv.setText("[  "+ PerfBean.CPU_BIG_FREQ[leftThumbIndex]/1000
                        +",  " + PerfBean.CPU_BIG_FREQ[rightThumbIndex]/1000 +"  ]");
            }
        });
        
        mCpuBigNumBar.setOnRatingBarChangeListener(new OnRatingBarChangeListener(){

            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating,
                    boolean fromUser) {
                mBigCoreNum = (int)rating;
                mCpuBigNumTv.setText(""+mBigCoreNum);
            }
        });
        
        mCpuSmallFreqBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onIndexChangeListener(RangeBar rangeBar, int leftThumbIndex, int rightThumbIndex) {
                mSmallLowerFrq = leftThumbIndex;
                mSmallUpperFreq = rightThumbIndex;
                mCpuSmallfreqTv.setText("[  "+ PerfBean.CPU_LITTER_FREQ[leftThumbIndex]/1000
                        +",  " + PerfBean.CPU_LITTER_FREQ[rightThumbIndex]/1000+"  ]");
            }
        });
        
        mCpuSmallNumBar.setOnRatingBarChangeListener(new OnRatingBarChangeListener(){
            
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating,
                    boolean fromUser) {
                mSmallCoreNum = (int)rating;
                mCpuSmallNumTv.setText(""+mSmallCoreNum);
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
        mCpuBigNumBar.setRating(mBigCoreNum);
        mCpuSmallNumBar.setRating(mSmallCoreNum);
        
        mCpuSmallFreqBar.setThumbIndices(mSmallLowerFrq, mSmallUpperFreq);
        mCpuBigFreqBar.setThumbIndices(mBigLowerFrq, mBigUpperFreq);
    }

    /**
     *  updata cpu data into kernel
     */
    private void updateData(){
        int[] sources = new int[3];
        sources[0] = mPerfBean.getCpuOn(mBigCoreNum, mSmallCoreNum);
        sources[1] = mPerfBean.getBigCpuFreq(mBigUpperFreq, mBigLowerFrq);
        sources[2] = mPerfBean.getLittleCpuFreq(mSmallUpperFreq, mSmallLowerFrq);
        mPerfBean.execute(sources);
    }
    
    
    /**
     * get cpu info from kernel
     */
    private void getData(){
        int[] cores = PerfBean.getCpuOnline(CPU_ONLINE);
        mSmallCoreNum =cores[0];
        mBigCoreNum =cores[1];
        
        mSmallLowerFrq = PerfBean.getLitterCpuFreqLevel(CPU_LITTLE_FREQ_MIN);
        mSmallUpperFreq = PerfBean.getLitterCpuFreqLevel(CPU_LITTLE_FREQ_MAX);
        
        mBigLowerFrq = PerfBean.getBigCpuFreqLevel(CPU_BIG_FREQ_MIN);
        mBigUpperFreq = PerfBean.getBigCpuFreqLevel(CPU_BIG_FREQ_MAX);
    }
    
}
