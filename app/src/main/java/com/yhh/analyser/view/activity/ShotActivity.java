package com.yhh.analyser.view.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yhh.analyser.R;
import com.yhh.analyser.config.AppConfig;
import com.yhh.analyser.utils.LogUtils;
import com.yhh.analyser.view.BaseActivity;
import com.yhh.analyser.widget.menulistview.SwipeMenu;
import com.yhh.analyser.widget.menulistview.SwipeMenuCreator;
import com.yhh.analyser.widget.menulistview.SwipeMenuItem;
import com.yhh.analyser.widget.menulistview.SwipeMenuListView;
import com.yhh.androidutils.FileUtils;
import com.yhh.androidutils.ScreenUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ShotActivity extends BaseActivity {
    private final static String TAG = LogUtils.DEBUG_TAG + "MyShotActivity";
    private List<String> mShotList;
    private ShotAdapter mAdapter;
    private SwipeMenuListView mListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_myshot);

        createSwipeListView();
    }

    private void createSwipeListView(){
        refresh();
        mListView = (SwipeMenuListView) findViewById(R.id.shot_swipe_ll);
        if(mShotList !=null) {
            mAdapter = new ShotAdapter();
            mListView.setAdapter(mAdapter);
        }
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                        0xCE)));
                // set item width
                openItem.setWidth(ScreenUtils.dp2px(ShotActivity.this, 90));
                // set item title
                openItem.setTitle("Open");
                // set item title fontsize
                openItem.setTitleSize(18);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(ScreenUtils.dp2px(ShotActivity.this, 90));
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
                        showImage(mShotList.get(position));
                        break;
                    case 1:
                        // delete
                        deleteImage(mShotList.get(position));
                        mAdapter.notifyDataSetChanged();
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

//        // test item long click
//        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view,
//                                           int position, long id) {
//                Toast.makeText(getApplicationContext(), position + " long click", Toast.LENGTH_SHORT).show();
//                return false;
//            }
//        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showImage(mShotList.get(position));
            }
        });
    }

    private void refresh(){
        mShotList = listImages(AppConfig.SCREEN_SHOT_DIR);
    }


    public  List<String> listImages(String dir){
        File parentDir = new File(dir);
        String[] files = parentDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                File current = new File(dir, filename);
                return filename.endsWith("png");
            }
        });
        if(files ==null || files.length<1){
            Log.w(TAG,"screen shot num=0");
            return null;
        }
        Arrays.sort(files, Collections.reverseOrder());
        return Arrays.asList(files);
    }

    private Bitmap getBitmp(String path){
        return BitmapFactory.decodeFile(path);
    }


    class ShotAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mShotList==null?0:mShotList.size();
        }

        @Override
        public Object getItem(int position) {
            return mShotList==null?null:mShotList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView ==null){
                convertView = View.inflate(getApplicationContext(),R.layout.item_list_screenshot, null);
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            if(holder == null){
                holder = new ViewHolder();
                convertView.setTag(holder);
                holder.shot_iv = (ImageView) convertView.findViewById(R.id.shotitem_icon_iv);
                holder.name_tv = (TextView) convertView.findViewById(R.id.shotitem_name_tv);
            }
            String name = (String) getItem(position);
            holder.shot_iv.setImageBitmap(getBitmp(AppConfig.SCREEN_SHOT_DIR + name));
            holder.name_tv.setText(name);
            return convertView;
        }

        class ViewHolder{
            ImageView shot_iv;
            TextView name_tv;
        }
    }


    private void showImage(String path){
        View view =LayoutInflater.from(this).inflate(R.layout.image_floating_view, null);
        ImageView imageView = (ImageView)(view.findViewById(R.id.show_image_iv));
        TextView name = (TextView)(view.findViewById(R.id.show_name_tv));
        imageView.setImageBitmap(getBitmp(AppConfig.SCREEN_SHOT_DIR + path));
        name.setText(path.substring(0, path.length()-4));
        new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton(R.string.yes_str,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                            }
                        }).show();
    }

    private void deleteImage(String path){
        FileUtils.deleteFile(AppConfig.SCREEN_SHOT_DIR + path);
        refresh();
    }


}
