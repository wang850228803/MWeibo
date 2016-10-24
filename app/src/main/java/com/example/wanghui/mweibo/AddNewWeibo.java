package com.example.wanghui.mweibo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.zfdang.multiple_images_selector.ImagesSelectorActivity;
import com.zfdang.multiple_images_selector.SelectorSettings;
import com.zfdang.multiple_images_selector.utilities.DraweeUtils;

import java.io.File;
import java.util.ArrayList;

public class AddNewWeibo extends Activity implements View.OnClickListener{
    private ImageView backView;
    private TextView postView;
    private EditText editText;
    private ImageView addPhotos;
    private GridView gridView;

    private AddWeiboAdapter mAdapter;
    private String TAG = "AddNewWeibo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_weibo);

        backView = (ImageView)findViewById(R.id.ic_back);
        postView = (TextView)findViewById(R.id.post);
        editText = (EditText)findViewById(R.id.edit);
        gridView = (GridView)findViewById(R.id.gridview);

        backView.setOnClickListener(this);
        postView.setOnClickListener(this);
        editText.setOnClickListener(this);

        mAdapter = new AddWeiboAdapter();
        gridView.setAdapter(mAdapter);
    }

    private static final int REQUEST_CODE = 123;
    private ArrayList<String> mResults = new ArrayList<>();

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ic_back:
                this.finish();
                break;
            case R.id.post:
                Intent intent = new Intent();
                intent.putExtra("text", editText.getText());
                intent.putExtra("image", mResults.get(0));
                setResult(RESULT_OK, intent);
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // get selected images from selector
        if(requestCode == REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                mResults = data.getStringArrayListExtra(SelectorSettings.SELECTOR_RESULTS);
                assert mResults != null;

                // show results in textview
                StringBuffer sb = new StringBuffer();
                sb.append(String.format("Totally %d images selected:", mResults.size())).append("\n");
                for(String result : mResults) {
                   sb.append(result).append("\n");
                }
                Log.i(TAG, sb.toString());
                mAdapter.setList(mResults);
                mAdapter.notifyDataSetChanged();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class AddWeiboAdapter extends BaseAdapter {
        private ArrayList<String> list = new ArrayList<>();

        @Override
        public int getCount() {
            return list.size() + 1;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SimpleDraweeView view;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.add_weibo_item, null);
                view = (SimpleDraweeView)convertView.findViewById(R.id.itemimage);
                convertView.setTag(view);
            }
            else
                view = (SimpleDraweeView) convertView.getTag();
            if (position == 0) {
                view.setImageResource(R.drawable.ic_add_a_photo_black_48dp);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // start multiple photos selector
                        Intent intent = new Intent(AddNewWeibo.this, ImagesSelectorActivity.class);
// max number of images to be selected
                        intent.putExtra(SelectorSettings.SELECTOR_MAX_IMAGE_NUMBER, 1);
// min size of image which will be shown; to filter tiny images (mainly icons)
                        intent.putExtra(SelectorSettings.SELECTOR_MIN_IMAGE_SIZE, 100000);
// show camera or not
                        intent.putExtra(SelectorSettings.SELECTOR_SHOW_CAMERA, true);
// pass current selected images as the initial value
                        intent.putStringArrayListExtra(SelectorSettings.SELECTOR_INITIAL_SELECTED_LIST, mResults);
// start the selector
                        startActivityForResult(intent, REQUEST_CODE);
                    }
                });
            }
            else
                DraweeUtils.showThumb(Uri.fromFile(new File(list.get(position - 1))), view);
            return convertView;
        }

        public void setList(ArrayList<String> list) {
            this.list = list;
        }
    }
}
