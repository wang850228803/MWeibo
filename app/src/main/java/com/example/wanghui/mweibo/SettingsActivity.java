package com.example.wanghui.mweibo;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {
    ListView mList;

    private static final String PREFERENCES_NAME = "com_weibo_sdk_android";
    private static final String TAG ="SettignsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("设置");
        setSupportActionBar(toolbar);
        mList = (ListView)findViewById(R.id.listview);
        mList.setAdapter(new SettingsAdapter(this));
    }

    private String[] mItems = {"缩略图模式"};
    private boolean[] mDefaultValue = {false};

    private class SettingsAdapter extends BaseAdapter{
        private static final String PREFERENCES_NAME = "com_weibo_sdk_android";
        Activity mCxt;
        private SharedPreferences sp;

        public SettingsAdapter(Activity cxt) {
            this.mCxt = cxt;
            sp = mCxt.getSharedPreferences(PREFERENCES_NAME, MODE_APPEND);
        }

        @Override
        public int getCount() {
            return mItems.length;
        }

        @Override
        public View getView(final int position, android.view.View convertView, ViewGroup parent) {
            ViewHolder vh = new ViewHolder();
            if (convertView == null) {
                convertView = mCxt.getLayoutInflater().inflate(R.layout.settings_item, null);
                vh.tv = (TextView) convertView.findViewById(R.id.textview);
                vh.sw = (Switch) convertView.findViewById(R.id.sw);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            vh.tv.setText(mItems[position]);
            boolean def = mDefaultValue[position];
            vh.sw.setChecked(sp.getBoolean("thumb", def));
            vh.sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    handleSwitchOnClickEven(position, isChecked);
                }
            });
            return convertView;
        }

        private void handleSwitchOnClickEven(int pos, boolean isChecked) {
            switch (pos) {
                case 0:
                    sp.edit().putBoolean("thumb", isChecked).commit();
                    Log.i(TAG, "sharedpreference putBulean thumb:" + isChecked);
                    break;
            }
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }
    }

    private class ViewHolder {
        public TextView tv;
        public Switch sw;
    }
}
