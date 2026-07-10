package com.ame.blocktetris;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.view.MotionEvent;

import java.lang.reflect.Field;

/**
 * v1.4.1 的触控保护层。
 *
 * BlockTetrisView 在 ACTION_DOWN 时立即移动一格，并启动长按连发。旧版左右键的
 * 首次连发等待只有 125–235ms，普通短按可能在抬手前多触发一次。本类保留“按下
 * 立即移动”的响应，只把左右方向的首次连发等待延后；软降仍使用原来的快速参数。
 *
 * 这是主输入控制器进一步拆分前的兼容适配层，不改变存档格式和游戏状态逻辑。
 */
final class StableTouchBlockTetrisView extends BlockTetrisView {
    private static final int CONTROL_LEFT = 1;
    private static final int CONTROL_RIGHT = 2;

    private static final int SENS_LOW = 0;
    private static final int SENS_STANDARD = 1;
    private static final int SENS_HIGH = 2;

    private static final Field ACTIVE_CONTROL_FIELD;
    private static final Field REPEAT_AT_FIELD;

    static {
        Field activeControl = null;
        Field repeatAt = null;
        try {
            activeControl = BlockTetrisView.class.getDeclaredField("activeControl");
            repeatAt = BlockTetrisView.class.getDeclaredField("repeatAt");
            activeControl.setAccessible(true);
            repeatAt.setAccessible(true);
        } catch (ReflectiveOperationException ignored) {
            // 字段不可用时回退到原始触控行为，避免启动崩溃。
        }
        ACTIVE_CONTROL_FIELD = activeControl;
        REPEAT_AT_FIELD = repeatAt;
    }

    private final SharedPreferences preferences;

    StableTouchBlockTetrisView(Context context) {
        super(context);
        preferences = context.getSharedPreferences("block_tetris", Context.MODE_PRIVATE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = super.onTouchEvent(event);
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            extendHorizontalRepeatDelay();
        }
        return handled;
    }

    private void extendHorizontalRepeatDelay() {
        if (ACTIVE_CONTROL_FIELD == null || REPEAT_AT_FIELD == null) return;
        try {
            int control = ACTIVE_CONTROL_FIELD.getInt(this);
            if (control != CONTROL_LEFT && control != CONTROL_RIGHT) return;
            REPEAT_AT_FIELD.setLong(this,
                    SystemClock.uptimeMillis() + horizontalInitialDelayMs());
        } catch (IllegalAccessException ignored) {
            // 回退到父类行为；不让触控兼容层影响游戏启动和运行。
        }
    }

    private long horizontalInitialDelayMs() {
        int sensitivity = preferences.getInt("touch_sensitivity", SENS_STANDARD);
        if (sensitivity == SENS_LOW) return 380L;
        if (sensitivity == SENS_HIGH) return 260L;
        return 320L;
    }
}
