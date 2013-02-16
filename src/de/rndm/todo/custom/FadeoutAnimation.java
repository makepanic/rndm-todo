package de.rndm.todo.custom;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

/**
 * Created with IntelliJ IDEA.
 * User: momo
 * Date: 16.02.13
 * Time: 13:42
 */
public class FadeoutAnimation extends Animation {

    private Animation animation;
    private View target;
    private Handler handler;

    public FadeoutAnimation(Context ctx, int id, Handler handler, final View target){
        this.animation = AnimationUtils.loadAnimation(ctx, id);
        this.handler = handler;
        this.target = target;

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                reset();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                target.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    public void reset(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                animation.reset();
                target.setVisibility(View.VISIBLE);
            }
        });
    }

    public void start(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                target.startAnimation(animation);
            }
        });
    }
}
