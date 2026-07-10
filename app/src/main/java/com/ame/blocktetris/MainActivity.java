package com.ame.blocktetris;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;

public class MainActivity extends Activity {
    private BlockTetrisView gameView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        gameView = new BlockTetrisView(this);
        refreshSystemUi();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            gameView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                    gameView.setSafeInsets(
                            insets.getSystemWindowInsetLeft(),
                            insets.getSystemWindowInsetTop(),
                            insets.getSystemWindowInsetRight(),
                            insets.getSystemWindowInsetBottom());
                    return insets;
                }
            });
        }
        setContentView(gameView);
    }

    public void refreshSystemUi() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    protected void onPause() {
        if (gameView != null) gameView.onHostPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshSystemUi();
        if (gameView != null) gameView.onHostResume();
    }

    @Override
    protected void onDestroy() {
        if (gameView != null) gameView.release();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (gameView != null && gameView.handleBackPressed()) return;
        super.onBackPressed();
    }
}
