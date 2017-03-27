package com.mseven.monitor;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.mseven.monitor.util.AccessibilityUtil;
import com.mseven.monitor.util.ActivityUtil;
import com.mseven.monitor.util.SharedPreUtil;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private LinearLayout mLoadingContainer, mListContainer;
    private ListView mListview;
    private MyAdapter myAdapter;
    private List<AppInfo> mAppInfos;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            mLoadingContainer.setVisibility(View.INVISIBLE);
            mListContainer.setVisibility(View.VISIBLE);
            myAdapter = new MyAdapter(MainActivity.this);
            mListview.setAdapter(myAdapter);
            myAdapter.clear();
            myAdapter.addAll(mAppInfos);
        }

    };
    final Runnable mRunningProcessesAvail = new Runnable() {
        public void run() {
            handleRunningProcessesAvail();
        }
    };

    private void handleRunningProcessesAvail() {
        // TODO Auto-generated method stub
        mLoadingContainer.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
    }


    private void initUI() {
        new Thread() {
            @Override
            public void run() {
                mHandler.post(mRunningProcessesAvail);
                mAppInfos = ActivityUtil.getPackageMainActivity(MainActivity.this);
                mHandler.sendEmptyMessage(0);
            }
        }.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AccessibilityUtil.updateLauncherAccessibility(this,true);
        setContentView(R.layout.activity_main);
        //Intent intent = new Intent(this, ActivityMonitorService.class);
        //startService(intent);
        mLoadingContainer = (LinearLayout) findViewById(R.id.loading_container);
        mListContainer = (LinearLayout) findViewById(R.id.list_container);
        mLoadingContainer.setVisibility(View.VISIBLE);
        ListView lv = (ListView) findViewById(android.R.id.list);
        // lv.setOnItemClickListener(this);
        lv.setSaveEnabled(true);
        mListview = lv;
        mListview.setRecyclerListener((AbsListView.RecyclerListener) myAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initUI();
    }

    public class MyAdapter extends ArrayAdapter<AppInfo> {
        private final LayoutInflater mInflater;
        private Context mContext;

        public MyAdapter(Context context) {
            this(context, 0);
        }

        public MyAdapter(Context context, int resource) {
            super(context, resource);
            mContext = context;
            mInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final AppInfo info = getItem(position);
            final View appView = convertView != null ? convertView
                    : createAppInfoRow(parent);
            ((ImageView) appView.findViewById(R.id.app_icon))
                    .setImageDrawable(info.getIcon());
            TextView textView = ((TextView) appView
                    .findViewById(R.id.app_name));
            textView.setText(info.getAppname());
            final Switch button = (Switch) appView.findViewById(R.id.app_switch);
            boolean isCheck = SharedPreUtil.getInstance(mContext).getBoolean(info.getPackname(), false);
            button.setChecked(isCheck);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreUtil.getInstance(mContext).putBoolean(info.getPackname(), button.isChecked());
                }
            });
            appView.setTag(info);
            return appView;
        }

        private View createAppInfoRow(ViewGroup parent) {
            final View row = mInflater.inflate(R.layout.app_info_item,
                    parent, false);
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setPressed(true);
                    Log.d("book", "onClick.....");
                }
            });
            return row;
        }
    }
}
