package com.czp.pianotiles;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by caizepeng on 17/1/11.
 */

public class CountDownView extends TextView {
    private final static int DURATION = 1000;
    private List<String> mData;
    private int index = -1;
    private AnimationSet mAnimationSet;
    private ScheduledExecutorService mScheuled;
    private CountDownListener mCountDownListener;
    public interface CountDownListener{
        public void finish();
    }
    public CountDownView(Context context) {
        this(context,null);
    }
    public CountDownView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }
    public CountDownView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAnim();
    }
    private void initAnim() {
        AlphaAnimation alphaAnimation = new AlphaAnimation(1,0);
        ScaleAnimation scaleAnim = new ScaleAnimation(1,2,1,2, Animation.RELATIVE_TO_SELF,0.5f,
                Animation.RELATIVE_TO_SELF,0.5f);
        mAnimationSet = new AnimationSet(true);
        mAnimationSet.addAnimation(alphaAnimation);
        mAnimationSet.addAnimation(scaleAnim);
        mAnimationSet.setDuration(DURATION);
    }
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(index < mData.size()-1){
                index++;
                setText(mData.get(index));
                startAnimation(mAnimationSet);
            }else{
                if(mCountDownListener!=null){
                    if(mScheuled!=null){
                        mScheuled.shutdown();
                    }
                    mCountDownListener.finish();
                }
            }
        }
    };
    public  void init() {
        index = -1;
        setText("");
        if(mData!=null && mData.size() > 0){
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    mHandler.sendEmptyMessage(1);
                }
            };
            mScheuled = Executors.newScheduledThreadPool(1);
            mScheuled.scheduleAtFixedRate(timerTask,2000,DURATION, TimeUnit.MILLISECONDS);
        }

    }
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(mScheuled!=null){
            mScheuled.shutdown();
        }
    }
    public void setCountDown(int start,int end){
        if(end>start){
            if(mData == null)
                mData = new ArrayList<>();
            mData.clear();
            for(int i = start; i < end+1; i++){
                mData.add(String.valueOf(i));
            }
        }
    }

    public List<String> getData() {
        return mData;
    }

    public void setData(List<String> data) {
        mData = data;
    }

    public CountDownListener getCountDownListener() {
        return mCountDownListener;
    }
    public void setCountDownListener(CountDownListener countDownListener) {
        mCountDownListener = countDownListener;
    }
}
