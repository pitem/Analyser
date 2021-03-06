package com.yhh.analyser.view.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.yhh.analyser.R;
import com.yhh.analyser.adapter.MonitorFileAdapter;
import com.yhh.analyser.config.AppConfig;
import com.yhh.analyser.view.BaseActivity;
import com.yhh.analyser.widget.menulistview.SwipeMenu;
import com.yhh.analyser.widget.menulistview.SwipeMenuCreator;
import com.yhh.analyser.widget.menulistview.SwipeMenuItem;
import com.yhh.analyser.widget.menulistview.SwipeMenuListView;
import com.yhh.androidutils.ArrayUtils;
import com.yhh.androidutils.FileUtils;
import com.yhh.androidutils.ScreenUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MonitorAnalyticActivity extends BaseActivity {
    private List<String> mShotList;
    private MonitorFileAdapter mAdapter;
    private SwipeMenuListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytic_data);
        createSwipeListView();
    }

    @Override
    protected boolean hasBackButton() {
        return true;
    }


    private void createSwipeListView(){
        String[] fileArray = FileUtils.listFiles(AppConfig.MONITOR_DIR);
        if(fileArray ==null){
            return;
        }

        mShotList = ArrayUtils.toList(fileArray);
        Collections.sort(mShotList, Collections.reverseOrder());
        mListView = (SwipeMenuListView) findViewById(R.id.shot_swipe_ll);
        if(mShotList !=null) {
            mAdapter = new MonitorFileAdapter(mShotList);
            mListView.setAdapter(mAdapter);
        }
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(
                        mContext);
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                        0xCE)));
                // set item width
                openItem.setWidth(ScreenUtils.dp2px(mContext, 90));
                // set item title
                openItem.setTitle("解析");
                // set item title font size
                openItem.setTitleSize(18);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        mContext);
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(ScreenUtils.dp2px(mContext, 90));
                // set a icon
                deleteItem.setIcon(R.drawable.ic_delete);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };

        mListView.setMenuCreator(creator);

        mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        // open
                        analyticFile(mShotList.get(position));
                        break;
                    case 1:
                        // delete
                        deleteMonitorFile(mShotList.get(position));
                        break;
                }
                return false;
            }
        });

        // set SwipeListener
        mListView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {

            @Override
            public void onSwipeStart(int position) {
                // swipe start
            }

            @Override
            public void onSwipeEnd(int position) {
                // swipe end
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                analyticFile(mShotList.get(position));
            }
        });
        // test item long click
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {

                deleteMonitorFile(mShotList.get(position));
                return false;
            }
        });
    }

    private void refresh(){
        String[] array =  FileUtils.listFiles(AppConfig.MONITOR_DIR);
        mShotList = array ==null? null: Arrays.asList(array);
        mAdapter = new MonitorFileAdapter(mShotList);
        mListView.setAdapter(mAdapter);

    }

    private void analyticFile(String path){
        Intent intent = new Intent(mContext, ChartMonitorActivity.class);
        intent.putExtra(MonitorAppActivity.MONITOR_PATH, path);
        startActivity(intent);
    }

    private void deleteMonitorFile(String path){
        FileUtils.deleteFile(AppConfig.MONITOR_DIR + path);
        refresh();
    }

}
