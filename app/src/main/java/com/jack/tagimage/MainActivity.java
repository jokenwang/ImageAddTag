package com.jack.tagimage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.imagezoom.ImageViewTouch;
import com.jack.tagimage.model.TagItem;
import com.jack.tagimage.utils.EffectUtil;
import com.jack.tagimage.utils.StringUtils;
import com.jack.tagimage.views.LabelSelector;
import com.jack.tagimage.views.LabelView;
import com.jack.tagimage.views.MyHighlightView;
import com.jack.tagimage.views.MyImageViewDrawableOverlay;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity" ;

    //绘图区域
    @InjectView(R.id.drawing_view_container)
    ViewGroup drawArea;

    private MyImageViewDrawableOverlay mImageView;
    private LabelSelector labelSelector;

    //当前选择底部按钮
    private TextView currentBtn;
    //当前图片
    private Bitmap currentBitmap;
    //用于预览的小图片
    private Bitmap smallImageBackgroud;
    //小白点标签
    private LabelView emptyLabelView;

    private List<LabelView> labels = new ArrayList<LabelView>();

    //标签区域
    private View commonLabelArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        EffectUtil.clear();
        initView();
        initEvent();
    }

    private void initView() {

        mImageView = (MyImageViewDrawableOverlay)findViewById(R.id.drawable_overlay);

        //添加标签选择器
        RelativeLayout.LayoutParams rparams = new RelativeLayout.LayoutParams(App.getApp().getScreenWidth(), App.getApp().getScreenHeight());
        labelSelector = new LabelSelector(this);
        labelSelector.setLayoutParams(rparams);
        drawArea.addView(labelSelector);
        labelSelector.hide();

        //初始化空白标签
        emptyLabelView = new LabelView(this);
        emptyLabelView.setEmpty();
        EffectUtil.addLabelEditable(mImageView, drawArea, emptyLabelView,
                App.getApp().getScreenWidth(), App.getApp().getScreenHeight());
        emptyLabelView.setVisibility(View.INVISIBLE);

        TagItem tagItem = new TagItem(AppConstants.POST_TYPE_TAG,"testtt");
        addLabel(tagItem);
    }

    private void initEvent() {
        labelSelector.setTxtClicked(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                EditTextActivity.openTextEdit(MainActivity.this,"",8, AppConstants.ACTION_EDIT_LABEL);

            }
        });

        labelSelector.setAddrClicked(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                EditTextActivity.openTextEdit(MainActivity.this, "", 8, AppConstants.ACTION_EDIT_LABEL_POI);

            }
        });
        mImageView.setOnDrawableEventListener(wpEditListener);
        mImageView.setSingleTapListener(new ImageViewTouch.OnImageViewTouchSingleTapListener() {
            @Override
            public void onSingleTapConfirmed() {
                emptyLabelView.updateLocation((int) mImageView.getmLastMotionScrollX(),
                        (int) mImageView.getmLastMotionScrollY());
                emptyLabelView.setVisibility(View.VISIBLE);
                labelSelector.showToTop();
                drawArea.postInvalidate();
            }

        });


        labelSelector.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        labelSelector.hide();
                        emptyLabelView.updateLocation((int) labelSelector.getmLastTouchX(),
                                (int) labelSelector.getmLastTouchY());
                        emptyLabelView.setVisibility(View.VISIBLE);
                    }
                });


    }

    private boolean setCurrentBtn(TextView btn) {
        if (currentBtn == null) {
            currentBtn = btn;
        } else if (currentBtn.equals(btn)) {
            return false;
        } else {
            currentBtn.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }
        Drawable myImage = getResources().getDrawable(R.drawable.select_icon);
        btn.setCompoundDrawablesWithIntrinsicBounds(null, null, null, myImage);
        currentBtn = btn;
        return true;
    }

    private MyImageViewDrawableOverlay.OnDrawableEventListener wpEditListener   = new MyImageViewDrawableOverlay.OnDrawableEventListener() {
        @Override
        public void onMove(MyHighlightView view) {
        }

        @Override
        public void onFocusChange(MyHighlightView newFocus, MyHighlightView oldFocus) {
        }

        @Override
        public void onDown(MyHighlightView view) {

        }

        @Override
        public void onClick(MyHighlightView view) {
            labelSelector.hide();
        }

        @Override
        public void onClick(final LabelView label) {
            if (label.equals(emptyLabelView)) {
                return;
            }
            Log.e(TAG, "onClick: 温馨提示是否需要删除该标签！");
        }
    };

    //添加标签
    private void addLabel(TagItem tagItem) {
        labelSelector.hide();
        emptyLabelView.setVisibility(View.INVISIBLE);
        if (labels.size() >= 5) {
//            alert("温馨提示", "您只能添加5个标签！", "确定", null, null, null, true);
        } else {
            int left = emptyLabelView.getLeft();
            int top = emptyLabelView.getTop();
            if (labels.size() == 0 && left == 0 && top == 0) {
                left = mImageView.getWidth() / 2 - 10;
                top = mImageView.getWidth() / 2;
            }
            LabelView label = new LabelView(MainActivity.this);
            label.init(tagItem);
            EffectUtil.addLabelEditable(mImageView, drawArea, label, left, top);
            labels.add(label);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        labelSelector.hide();
        super.onActivityResult(requestCode, resultCode, data);
        if (AppConstants.ACTION_EDIT_LABEL== requestCode && data != null) {
            String text = data.getStringExtra(AppConstants.PARAM_EDIT_TEXT);
            if(StringUtils.isNotEmpty(text)){
                TagItem tagItem = new TagItem(AppConstants.POST_TYPE_TAG,text);
                addLabel(tagItem);
            }
        }else if(AppConstants.ACTION_EDIT_LABEL_POI== requestCode && data != null){
            String text = data.getStringExtra(AppConstants.PARAM_EDIT_TEXT);
            if(StringUtils.isNotEmpty(text)){
                TagItem tagItem = new TagItem(AppConstants.POST_TYPE_POI,text);
                addLabel(tagItem);
            }
        }
    }
}
