package com.iobits.budgetexpensemanager.utils;


import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.constraintlayout.widget.ConstraintLayout;

public class PopupMenuCustomLayout {
    PopupMenuCustomOnClickListener onClickListener;
    Context context;
    PopupWindow popupWindow;
    int rLayoutId;
    View popupView;

    public PopupMenuCustomLayout(Context context, int rLayoutId, PopupMenuCustomOnClickListener onClickListener) {
        this.context = context;
        this.onClickListener = onClickListener;
        this.rLayoutId = rLayoutId;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(rLayoutId, null);
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int width = ConstraintLayout.LayoutParams.WRAP_CONTENT; // 60% of the screen width
        int height = ConstraintLayout.LayoutParams.WRAP_CONTENT;
        popupWindow = new PopupWindow(popupView, width, height, true);

        popupWindow.setElevation(10);
        LinearLayout linearLayout = (LinearLayout) popupView;
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            View v = linearLayout.getChildAt(i);
            v.setOnClickListener(v1 -> {
                onClickListener.onClick(v1.getId());
                popupWindow.dismiss();
            });
        }
    }

    public void show() {
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
    }

    public void shows() {
        popupWindow.showAtLocation(popupView, Gravity.TOP, 0, 0);
    }

    public void dismiss() {
        popupWindow.dismiss();
    }

    public void show(View anchorView, int gravity, int offsetX, int offsetY) {
        popupWindow.showAsDropDown(anchorView, -2 * (anchorView.getWidth()), -1 * (anchorView.getHeight()));
    }

    public void show2(View anchorView, int gravity, int offsetX, int offsetY) {
        popupWindow.showAsDropDown(anchorView, -2 * (anchorView.getWidth()), -3 * (anchorView.getHeight()));
    }

    public void showSettingDialog(View anchorView) {
        popupWindow.showAsDropDown(anchorView, -4 * (anchorView.getWidth()), 0);
    }

    public void showSettingDialogTop(View anchorView) {
        popupWindow.showAsDropDown(anchorView, -3 * (anchorView.getWidth()), -6 * (anchorView.getHeight()));
    }

    public interface PopupMenuCustomOnClickListener {
        void onClick(int menuItemId);
    }
}
