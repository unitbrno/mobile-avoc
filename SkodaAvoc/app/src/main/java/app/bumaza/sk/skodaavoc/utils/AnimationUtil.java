package app.bumaza.sk.skodaavoc.utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.support.v7.widget.RecyclerView;

/**
 * Created by Budy on 8.10.17.
 */

public class AnimationUtil {

    public static void animate(RecyclerView.ViewHolder hodler, boolean goesDown){

        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator animator = ObjectAnimator.ofFloat(hodler.itemView, "translationY",
                goesDown==true ? 200 : -200, 0);
        animator.setDuration(500);

        animatorSet.playTogether(animator);
        animatorSet.start();
    }
}
