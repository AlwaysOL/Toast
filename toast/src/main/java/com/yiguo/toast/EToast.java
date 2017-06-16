package com.yiguo.toast;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;


/**
 * Created by blin on 2016/10/11.
 */

public class EToast {
    public static final int LENGTH_SHORT = 0;
    public static final int LENGTH_LONG = 1;
    private final int ANIMATION_DURATION = 600;
    private WeakReference<Activity> reference;
    private TextView mTextView;
    private ViewGroup container;
    private View v;
    private LinearLayout mContainer;
    private int HIDE_DELAY = 2000;
    private AlphaAnimation outAnimation;
    private AlphaAnimation inAnimation;
    private static boolean isShow = false;
    private String TOAST_TAG = "EToast_Log";
    private static String activityName = "";

    private EToast(Activity activity) {
        reference = new WeakReference<>(activity);
        container = (ViewGroup) activity
                .findViewById(android.R.id.content);
        View viewWithTag = container.findViewWithTag(TOAST_TAG);
        if(viewWithTag == null){
            v = activity.getLayoutInflater().inflate(
                    R.layout.etoast, container);
            v.setTag(TOAST_TAG);
        }else{
            v = viewWithTag;
        }
        mContainer = (LinearLayout) v.findViewById(R.id.mbContainer);
        mTextView = (TextView) v.findViewById(R.id.mbMessage);
        if(!TextUtils.equals(activityName,reference.get().getClass().getName())){
            activityName = reference.get().getClass().getName();
            isShow = false;
        }
    }

    /**
     * @param context must instanceof Activity
     * */
    public static EToast makeText(Context context, CharSequence message, int HIDE_DELAY) {
        if(context instanceof Activity){
            EToast eToast = new EToast((Activity) context);
            if(HIDE_DELAY == LENGTH_LONG){
                eToast.HIDE_DELAY = 2500;
            }else{
                eToast.HIDE_DELAY = 1500;
            }
            eToast.setText(message);
            
            return eToast;
        }else{
            throw new RuntimeException("EToast @param context must instanceof Activity");
        }
    }
    public static EToast makeText(Context context, int resId, int HIDE_DELAY) {
        return makeText(context,context.getText(resId),HIDE_DELAY);
    }
    public void show() {
        inAnimation = new AlphaAnimation(0.0f, 1.0f);
        outAnimation = new AlphaAnimation(1.0f, 0.0f);
        inAnimation.setDuration(ANIMATION_DURATION);
        outAnimation.setDuration(ANIMATION_DURATION);
        outAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        //该动画只要开始，就认为上次显示已经结束，如果这个时候又show，则直接终止动画
                        isShow = false;
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if(!reference.get().isFinishing()){
                            mTextView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
        if(isShow){
            if(mContainer.getAnimation() != null)
                mContainer.getAnimation().cancel();
            mContainer.removeCallbacks(oldRun);
            mContainer.postDelayed(mHideRunnable,HIDE_DELAY);
        }else{
            if(!reference.get().isFinishing())
                mTextView.setVisibility(View.VISIBLE);
            mContainer.startAnimation(inAnimation);
            isShow = true;
        }
        mContainer.postDelayed(mHideRunnable,HIDE_DELAY);
        oldRun = mHideRunnable;
    }
    private static Runnable oldRun;
    private Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            if (reference.get().hasWindowFocus())
                mContainer.startAnimation(outAnimation);
            else{
                if(!reference.get().isFinishing())
                    mTextView.setVisibility(View.GONE);
            }
        }
    };
    public void cancel(){
        if(isShow) {
            isShow = false;
            mTextView.setVisibility(View.GONE);
            mContainer.removeCallbacks(mHideRunnable);
        }
    }
    public void setText(CharSequence s){
        if(v == null) throw new RuntimeException("This Toast was not created with com.yiguo.toast.Toast.makeText()");
        mTextView.setText(s);
    }
    public void setText(int resId) {
        setText(reference.get().getText(resId));
    }
}
