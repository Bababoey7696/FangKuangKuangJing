package com.ame.blocktetris;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import java.util.Random;

/**
 * 方块矿境 v1.4：纯 Canvas、完全离线的下落方块游戏。
 * 游戏状态不依赖网络或第三方框架，旧版 SharedPreferences 键会在首次启动时迁移。
 */
public class BlockTetrisView extends View {
    private static final int MAX_COLS = 14;
    private static final int MAX_ROWS = 24;
    private static final int QUEUE_SIZE = 3;
    private static final int MATERIAL_COUNT = 15;
    private static final int LUCKY_MATERIAL = 14;
    private static final int SAVE_SCHEMA = 4;

    private static final int SCREEN_MENU = 0;
    private static final int SCREEN_GAME = 1;
    private static final int SCREEN_SETTINGS = 2;
    private static final int SCREEN_SKINS = 3;
    private static final int SCREEN_TUTORIAL = 4;

    private static final int MODE_CASUAL = 0;
    private static final int MODE_STANDARD = 1;
    private static final int MODE_EXTREME = 2;

    private static final int START_ZERO = 0;
    private static final int START_HIGH = 1;
    private static final int START_LAST = 2;
    private static final int START_CURRENT = 3;

    private static final int SENS_LOW = 0;
    private static final int SENS_STANDARD = 1;
    private static final int SENS_HIGH = 2;

    private static final int BOARD_STANDARD = 0;
    private static final int SETTINGS_ROW_COUNT = 23;

    private static final int CONTROL_NONE = 0;
    private static final int CONTROL_LEFT = 1;
    private static final int CONTROL_RIGHT = 2;
    private static final int CONTROL_DOWN = 3;
    private static final int CONTROL_DROP = 4;

    private static final int CONFIRM_NONE = 0;
    private static final int CONFIRM_RESTART = 1;
    private static final int CONFIRM_RESET_SCORES = 2;
    private static final int CONFIRM_RESET_ALL = 3;
    private static final int CONFIRM_MENU = 4;

    private static final int SKILL_PICKAXE = GameBalance.SKILL_PICKAXE;
    private static final int SKILL_FREEZE = GameBalance.SKILL_FREEZE;
    private static final int SKILL_REFORGE = GameBalance.SKILL_REFORGE;
    private static final int SKILL_STABILIZER = GameBalance.SKILL_STABILIZER;
    private static final int SKILL_SHOCKWAVE = GameBalance.SKILL_SHOCKWAVE;

    private static final int[][][] SHAPES = new int[][][] {
            {
                    {0,1, 1,1, 2,1, 3,1}, {2,0, 2,1, 2,2, 2,3},
                    {0,2, 1,2, 2,2, 3,2}, {1,0, 1,1, 1,2, 1,3}
            },
            {
                    {1,0, 2,0, 1,1, 2,1}, {1,0, 2,0, 1,1, 2,1},
                    {1,0, 2,0, 1,1, 2,1}, {1,0, 2,0, 1,1, 2,1}
            },
            {
                    {1,0, 0,1, 1,1, 2,1}, {1,0, 1,1, 2,1, 1,2},
                    {0,1, 1,1, 2,1, 1,2}, {1,0, 0,1, 1,1, 1,2}
            },
            {
                    {1,0, 2,0, 0,1, 1,1}, {1,0, 1,1, 2,1, 2,2},
                    {1,1, 2,1, 0,2, 1,2}, {0,0, 0,1, 1,1, 1,2}
            },
            {
                    {0,0, 1,0, 1,1, 2,1}, {2,0, 1,1, 2,1, 1,2},
                    {0,1, 1,1, 1,2, 2,2}, {1,0, 0,1, 1,1, 0,2}
            },
            {
                    {0,0, 0,1, 1,1, 2,1}, {1,0, 2,0, 1,1, 1,2},
                    {0,1, 1,1, 2,1, 2,2}, {1,0, 1,1, 0,2, 1,2}
            },
            {
                    {2,0, 0,1, 1,1, 2,1}, {1,0, 1,1, 1,2, 2,2},
                    {0,1, 1,1, 2,1, 0,2}, {0,0, 1,0, 1,1, 1,2}
            }
    };

    private static final int[][] MATERIAL_COLORS = new int[][] {
            {0xFF5B8F3A, 0xFF456F2D, 0xFF895C35},
            {0xFF84898C, 0xFF5B6267, 0xFFAEB2B1},
            {0xFF364850, 0xFF1F2F37, 0xFF32DADA},
            {0xFFA06937, 0xFF673D1F, 0xFFCE8F49},
            {0xFFDAC27D, 0xFFA68D54, 0xFFF1DE9D},
            {0xFF49464C, 0xFF2A2930, 0xFFDE4C40},
            {0xFF7C4EA0, 0xFF4D3070, 0xFFD089F2},
            {0xFFA8893A, 0xFF674F23, 0xFFFFDA52},
            {0xFFB16840, 0xFF6E3D2B, 0xFF50B897},
            {0xFF3D5D4B, 0xFF243A30, 0xFF49E786},
            {0xFF7EBCCF, 0xFF4E849D, 0xFFDEF9FF},
            {0xFF362B46, 0xFF1B1826, 0xFF7E4DAD},
            {0xFF4E7B44, 0xFF304D2C, 0xFF99B752},
            {0xFF5D2F23, 0xFF311C19, 0xFFF67125},
            {0xFFCCA134, 0xFF603A28, 0xFFFFF67C}
    };

    private static final String[] MODE_NAMES = {"休闲", "标准", "极限"};
    private static final String[] MODE_DESCRIPTIONS = {
            "手机舒适曲线：速度较慢，落地缓冲更长",
            "手机平滑曲线：高分后继续加速，但不会瞬间锁死",
            "高速挑战曲线：更快，但仍保留触屏落地缓冲"
    };
    private static final String[] START_NAMES = {"从 0 分开始", "从历史最高分开始", "从最近一局开始"};
    private static final String[] SENS_NAMES = {"低", "标准", "高"};
    private static final String[] BOARD_NAMES = {"标准 10×20", "加长 10×24", "宽阔 12×20", "探索 12×24", "巨型 14×24"};
    private static final int[] BOARD_COLS = {10, 10, 12, 12, 14};
    private static final int[] BOARD_ROWS = {20, 24, 20, 24, 24};
    private static final String[] MUSIC_NAMES = {"遗迹回响", "晶洞脉冲", "熔岩深层", "天空遗迹"};
    private static final String[] SKIN_NAMES = ThemeCatalog.NAMES;
    private static final String[] SKIN_UNLOCK = ThemeCatalog.UNLOCK_TEXT;
    private static final String[] TUTORIAL_TITLES = {"左右移动", "旋转方块", "软降", "快速落地", "落地缓冲", "HOLD 暂存", "使用技能", "本局任务", "暂停游戏"};
    private static final String[] TUTORIAL_TEXT = {
            "按住 ◀ 或 ▶ 可连续移动；首次延迟后才会重复。",
            "点击旋转键。靠墙时会尝试小幅踢墙，减少卡住。",
            "按住 ▼ 可连续下降，每下降一格获得 1 分。",
            "按下后进入准备状态，松手才落地；滑出按钮会取消。",
            "方块触底后不会立刻锁死，仍有短暂时间横移或旋转；移动次数有限，防止无限拖延。",
            "每个正在下落的方块只能暂存或交换一次。",
            "消行与连击积攒能量；设置中可切换技能组，长按技能可查看说明。",
            "开局页会显示三项本局任务。每完成一项自动获得 1 枚遗迹碎片。",
            "暂停后所有触控与自动下落都会停止，恢复时不会补掉落。"
    };

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pixelPaint = new Paint();
    private final Random random = new Random();
    private final SharedPreferences prefs;
    private final AudioEngine audio;
    private final int[][] board = new int[MAX_ROWS][MAX_COLS];
    private final int[] bag = new int[7];
    private final int[] nextTypes = new int[QUEUE_SIZE];
    private final int[] nextMaterials = new int[QUEUE_SIZE];
    private final RunMissions missions = new RunMissions();

    private final RectF boardRect = new RectF();
    private final RectF leftButton = new RectF();
    private final RectF rotateButton = new RectF();
    private final RectF rightButton = new RectF();
    private final RectF downButton = new RectF();
    private final RectF holdButton = new RectF();
    private final RectF pauseButton = new RectF();
    private final RectF dropButton = new RectF();
    private final RectF settingsButton = new RectF();
    private final RectF[] skillButtons = {new RectF(), new RectF(), new RectF()};
    private final RectF menuPrimary = new RectF();
    private final RectF menuSecondary = new RectF();
    private final RectF menuThird = new RectF();
    private final RectF menuFourth = new RectF();
    private final RectF menuFifth = new RectF();
    private final RectF overlayButton1 = new RectF();
    private final RectF overlayButton2 = new RectF();
    private final RectF overlayButton3 = new RectF();
    private final RectF overlayButton4 = new RectF();
    private final RectF confirmYes = new RectF();
    private final RectF confirmNo = new RectF();
    private final RectF backButton = new RectF();
    private final RectF contentClip = new RectF();
    private final RectF tutorialIconRect = new RectF();

    private int safeLeft;
    private int safeTop;
    private int safeRight;
    private int safeBottom;
    private float density;
    private float cell;
    private float boardLeft;
    private float boardTop;
    private float boardRight;
    private float boardBottom;
    private float sideLeft;
    private float sideRight;

    private int screen;
    private int settingsReturnScreen = SCREEN_MENU;
    private int tutorialReturnScreen = SCREEN_MENU;
    private int bagIndex = 7;
    private int currentType;
    private int currentMaterial;
    private int rotation;
    private int pieceX;
    private int pieceY;
    private int heldType = -1;
    private int heldMaterial;
    private boolean holdUsed;

    private int score;
    private int lines;
    private int level;
    private int combo;
    private int maxCombo;
    private int fourLineCount;
    private int perfectStreak;
    private int runFragments;
    private int energy;
    private int piecesSpawned;
    private int nextLuckyAt;
    private int softDropPoints;
    private int hardDropPoints;
    private int lineScore;
    private int comboScore;
    private int perfectScore;
    private int skillScore;
    private boolean scoreDetailsVisible;

    private int highCasual;
    private int highStandard;
    private int highExtreme;
    private int highLinesCasual;
    private int highLinesStandard;
    private int highLinesExtreme;
    private int lastScoreCasual;
    private int lastScoreStandard;
    private int lastScoreExtreme;
    private int lastLinesCasual;
    private int lastLinesStandard;
    private int lastLinesExtreme;
    private int totalFragments;
    private int unlockedSkins;
    private int completedMissionCount;

    private int mode;
    private int startScoreSource;
    private int runStartScore;
    private int runStartLines;
    private int sensitivity;
    private int selectedBoardPreset;
    private int activeBoardPreset;
    private int boardCols = 10;
    private int boardRows = 20;
    private int skin;
    private int skillLoadout;
    private int soundVolume;
    private int musicVolume;
    private int musicTrack;
    private boolean soundEnabled;
    private boolean musicEnabled;
    private boolean vibrationEnabled;
    private boolean ghostEnabled;
    private boolean easterAnimations;
    private boolean colorBlindMode;
    private boolean gridEnabled;
    private boolean previewThree;
    private boolean skillsEnabled;

    private boolean paused;
    private boolean gameOver;
    private boolean backgroundPaused;
    private boolean loopRunning;
    private boolean hardDropArmed;
    private boolean shieldUsed;
    private boolean scoreEggTriggered;
    private boolean fiftyEggTriggered;
    private boolean patternEggTriggered;
    private boolean energyReadyAnnounced;

    private long lastTick;
    private long freezeUntil;
    private long slowUntil;
    private long freezeCooldownUntil;
    private long reforgeCooldownUntil;
    private long stabilizerCooldownUntil;
    private long shockwaveCooldownUntil;
    private long groundedSince;
    private int lockResetCount;
    private long bannerUntil;
    private long easterUntil;
    private String banner = "";

    private int activeControl = CONTROL_NONE;
    private long repeatAt;
    private float touchDownX;
    private float touchDownY;
    private float lastTouchX;
    private float lastTouchY;
    private boolean draggingSettings;
    private float settingsScroll;
    private float settingsDownScroll;
    private boolean draggingSkins;
    private float skinScroll;
    private float skinDownScroll;
    private int pressedSkill = -1;
    private long skillPressAt;
    private boolean skillLongPressShown;
    private int tutorialPage;
    private int confirmAction = CONFIRM_NONE;
    private int titleTapCount;
    private long titleTapWindow;

    private final Particle[] particles = new Particle[72];
    private int particleCursor;

    private final Runnable loop = new Runnable() {
        @Override
        public void run() {
            if (!loopRunning) return;
            long now = SystemClock.uptimeMillis();
            updateHeldControl(now);
            updateSkillLongPress(now);
            updateParticles(now);
            if (screen == SCREEN_GAME && !paused && !gameOver && !backgroundPaused) {
                updateGravity(now);
            }
            postInvalidateOnAnimation();
            postOnAnimation(this);
        }
    };

    public BlockTetrisView(Context context) {
        super(context);
        density = getResources().getDisplayMetrics().density;
        paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        pixelPaint.setAntiAlias(false);
        setFocusable(true);
        setHapticFeedbackEnabled(true);
        for (int i = 0; i < particles.length; i++) particles[i] = new Particle();

        prefs = context.getSharedPreferences("block_tetris", Context.MODE_PRIVATE);
        migratePreferences();
        loadPreferences();
        missions.reset(mode);
        audio = new AudioEngine(context);
        applyAudioSettings();

        boolean tutorialSeen = prefs.getBoolean("tutorial_seen", false);
        screen = tutorialSeen ? SCREEN_MENU : SCREEN_TUTORIAL;
        tutorialReturnScreen = SCREEN_MENU;
        tutorialPage = 0;
        startLoop();
    }

    private void migratePreferences() {
        int schema = prefs.getInt("save_schema", 0);
        if (schema >= SAVE_SCHEMA) return;
        SharedPreferences.Editor editor = prefs.edit();
        int oldHigh = prefs.getInt("high_score", 0);
        if (!prefs.contains("high_score_standard")) editor.putInt("high_score_standard", oldHigh);
        if (!prefs.contains("sound_enabled_v12")) {
            editor.putBoolean("sound_enabled_v12", prefs.getBoolean("sound_enabled", true));
        }
        if (!prefs.contains("high_lines_casual")) {
            editor.putInt("high_lines_casual", estimateLinesForLegacyScore(prefs.getInt("high_score_casual", 0)));
        }
        if (!prefs.contains("high_lines_standard")) {
            int standardHigh = prefs.getInt("high_score_standard", oldHigh);
            editor.putInt("high_lines_standard", estimateLinesForLegacyScore(standardHigh));
        }
        if (!prefs.contains("high_lines_extreme")) {
            editor.putInt("high_lines_extreme", estimateLinesForLegacyScore(prefs.getInt("high_score_extreme", 0)));
        }
        if (!prefs.contains("start_score_source")) editor.putInt("start_score_source", START_ZERO);
        if (!prefs.contains("skill_loadout")) editor.putInt("skill_loadout", 0);
        if (!prefs.contains("completed_missions")) editor.putInt("completed_missions", 0);
        // 保留未知旧键，不执行 clear；迁移标记确保只运行一次。
        editor.putInt("save_schema", SAVE_SCHEMA);
        editor.apply();
    }

    private static int estimateLinesForLegacyScore(int value) {
        if (value <= 0) return 0;
        return Math.min(290, Math.max(0, value / 500));
    }

    private void loadPreferences() {
        highCasual = prefs.getInt("high_score_casual", 0);
        highStandard = prefs.getInt("high_score_standard", prefs.getInt("high_score", 0));
        highExtreme = prefs.getInt("high_score_extreme", 0);
        highLinesCasual = prefs.getInt("high_lines_casual", estimateLinesForLegacyScore(highCasual));
        highLinesStandard = prefs.getInt("high_lines_standard", estimateLinesForLegacyScore(highStandard));
        highLinesExtreme = prefs.getInt("high_lines_extreme", estimateLinesForLegacyScore(highExtreme));
        lastScoreCasual = prefs.getInt("last_score_casual", 0);
        lastScoreStandard = prefs.getInt("last_score_standard", 0);
        lastScoreExtreme = prefs.getInt("last_score_extreme", 0);
        lastLinesCasual = prefs.getInt("last_lines_casual", 0);
        lastLinesStandard = prefs.getInt("last_lines_standard", 0);
        lastLinesExtreme = prefs.getInt("last_lines_extreme", 0);
        totalFragments = prefs.getInt("relic_fragments", 0);
        unlockedSkins = prefs.getInt("unlocked_skins", 1);
        completedMissionCount = prefs.getInt("completed_missions", 0);
        mode = clamp(prefs.getInt("game_mode", MODE_STANDARD), 0, 2);
        startScoreSource = clamp(prefs.getInt("start_score_source", START_ZERO), START_ZERO, START_LAST);
        sensitivity = clamp(prefs.getInt("touch_sensitivity", SENS_STANDARD), 0, 2);
        selectedBoardPreset = clamp(prefs.getInt("board_preset", BOARD_STANDARD), 0, BOARD_NAMES.length - 1);
        activeBoardPreset = selectedBoardPreset;
        boardCols = BOARD_COLS[activeBoardPreset];
        boardRows = BOARD_ROWS[activeBoardPreset];
        skin = clamp(prefs.getInt("skin", 0), 0, ThemeCatalog.count() - 1);
        skillLoadout = clamp(prefs.getInt("skill_loadout", 0), 0, GameBalance.SKILL_LOADOUTS.length - 1);
        soundEnabled = prefs.getBoolean("sound_enabled_v12", true);
        musicEnabled = prefs.getBoolean("music_enabled", true);
        soundVolume = clamp(prefs.getInt("sound_volume", 65), 0, 100);
        musicVolume = clamp(prefs.getInt("music_volume", 22), 0, 100);
        musicTrack = clamp(prefs.getInt("music_track", 0), 0, MUSIC_NAMES.length - 1);
        vibrationEnabled = prefs.getBoolean("vibration_enabled", true);
        ghostEnabled = prefs.getBoolean("ghost_enabled", true);
        easterAnimations = prefs.getBoolean("easter_animations", true);
        colorBlindMode = prefs.getBoolean("color_blind", false);
        gridEnabled = prefs.getBoolean("grid_enabled", true);
        previewThree = prefs.getBoolean("preview_three", true);
        skillsEnabled = prefs.getBoolean("skills_enabled", true);
        updateSkinUnlocks(false);
        if (!isSkinUnlocked(skin)) skin = 0;
    }

    private void saveSetting(String key, boolean value) {
        prefs.edit().putBoolean(key, value).apply();
    }

    private void saveSetting(String key, int value) {
        prefs.edit().putInt(key, value).apply();
    }

    private void applyAudioSettings() {
        audio.configure(soundEnabled, soundVolume, musicEnabled, musicVolume, musicTrack);
        boolean gameplayMusic = screen == SCREEN_GAME && !paused && !gameOver && !backgroundPaused;
        boolean settingsPreview = (screen == SCREEN_SETTINGS || screen == SCREEN_SKINS) && !backgroundPaused;
        audio.setGameActive(gameplayMusic || settingsPreview);
    }

    public void setSafeInsets(int left, int top, int right, int bottom) {
        safeLeft = Math.max(0, left);
        safeTop = Math.max(0, top);
        safeRight = Math.max(0, right);
        safeBottom = Math.max(0, bottom);
        requestLayout();
        invalidate();
    }

    public void onHostPause() {
        backgroundPaused = true;
        cancelActiveTouch();
        if (screen == SCREEN_GAME && !gameOver) paused = true;
        stopLoop();
        audio.setGameActive(false);
        audio.pauseForBackground();
        invalidate();
    }

    public void onHostResume() {
        backgroundPaused = false;
        lastTick = SystemClock.uptimeMillis();
        audio.resumeFromBackground();
        applyAudioSettings();
        startLoop();
        invalidate();
    }

    public void release() {
        stopLoop();
        audio.release();
    }

    private void startLoop() {
        if (loopRunning) return;
        loopRunning = true;
        removeCallbacks(loop);
        post(loop);
    }

    private void stopLoop() {
        loopRunning = false;
        removeCallbacks(loop);
    }

    public boolean handleBackPressed() {
        if (confirmAction != CONFIRM_NONE) {
            confirmAction = CONFIRM_NONE;
            invalidate();
            return true;
        }
        if (screen == SCREEN_SKINS) {
            screen = SCREEN_SETTINGS;
            applyAudioSettings();
            invalidate();
            return true;
        }
        if (screen == SCREEN_SETTINGS) {
            screen = settingsReturnScreen;
            settingsScroll = 0f;
            applyAudioSettings();
            invalidate();
            return true;
        }
        if (screen == SCREEN_TUTORIAL) {
            finishTutorial();
            return true;
        }
        if (screen == SCREEN_GAME) {
            if (gameOver) {
                screen = SCREEN_MENU;
                applyAudioSettings();
            } else if (!paused) {
                setPaused(true);
            } else {
                confirmAction = CONFIRM_MENU;
            }
            invalidate();
            return true;
        }
        return false;
    }

    private void startNewGame() {
        startNewGame(startScoreSource, score, lines);
    }

    private void startNewGame(int source, int currentScore, int currentLines) {
        int initialScore = 0;
        int initialLines = 0;
        if (source == START_HIGH) {
            initialScore = getModeHighScore();
            initialLines = getModeHighLines();
        } else if (source == START_LAST) {
            initialScore = getModeLastScore();
            initialLines = getModeLastLines();
        } else if (source == START_CURRENT) {
            initialScore = Math.max(0, currentScore);
            initialLines = Math.max(0, currentLines);
        }
        startNewGameWithProgress(initialScore, initialLines);
    }

    private void startNewGameWithProgress(int initialScore, int initialLines) {
        activeBoardPreset = selectedBoardPreset;
        boardCols = BOARD_COLS[activeBoardPreset];
        boardRows = BOARD_ROWS[activeBoardPreset];
        updateLayout(getWidth(), getHeight());
        for (int r = 0; r < MAX_ROWS; r++) {
            for (int c = 0; c < MAX_COLS; c++) board[r][c] = 0;
        }
        score = Math.max(0, initialScore);
        lines = Math.max(0, initialLines);
        level = 1 + lines / 10;
        runStartScore = score;
        runStartLines = lines;
        combo = 0;
        maxCombo = 0;
        fourLineCount = 0;
        perfectStreak = 0;
        runFragments = 0;
        missions.reset(mode);
        energy = 0;
        piecesSpawned = 0;
        softDropPoints = 0;
        hardDropPoints = 0;
        lineScore = 0;
        comboScore = 0;
        perfectScore = 0;
        skillScore = 0;
        scoreDetailsVisible = false;
        heldType = -1;
        holdUsed = false;
        shieldUsed = false;
        scoreEggTriggered = prefs.getBoolean("egg_25000_seen", false) && score >= 25000;
        fiftyEggTriggered = isSkinUnlocked(5);
        patternEggTriggered = false;
        energyReadyAnnounced = false;
        paused = false;
        gameOver = false;
        freezeUntil = 0L;
        slowUntil = 0L;
        freezeCooldownUntil = 0L;
        reforgeCooldownUntil = 0L;
        stabilizerCooldownUntil = 0L;
        shockwaveCooldownUntil = 0L;
        groundedSince = 0L;
        lockResetCount = 0;
        easterUntil = 0L;
        bagIndex = 7;
        nextLuckyAt = 38 + random.nextInt(35);
        for (int i = 0; i < QUEUE_SIZE; i++) {
            nextTypes[i] = takeFromBag();
            nextMaterials[i] = randomMaterial();
        }
        spawnNextPiece(true);
        lastTick = SystemClock.uptimeMillis();
        screen = SCREEN_GAME;
        if (runStartScore > 0) showBanner("续分练习：" + runStartScore + " 分 · 等级 " + level, 1450L);
        else showBanner("遗迹探险开始！", 1200L);
        audio.play(AudioEngine.CLICK);
        applyAudioSettings();
        invalidate();
    }

    private void fillBag() {
        for (int i = 0; i < 7; i++) bag[i] = i;
        for (int i = 6; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = bag[i];
            bag[i] = bag[j];
            bag[j] = tmp;
        }
        bagIndex = 0;
    }

    private int takeFromBag() {
        if (bagIndex >= 7) fillBag();
        return bag[bagIndex++];
    }

    private int randomMaterial() {
        if (piecesSpawned >= nextLuckyAt) {
            nextLuckyAt += 42 + random.nextInt(45);
            return LUCKY_MATERIAL;
        }
        int roll = random.nextInt(1000);
        if (roll < 12) return LUCKY_MATERIAL;
        int weighted = random.nextInt(100);
        if (weighted < 14) return 0;
        if (weighted < 27) return 1;
        if (weighted < 38) return 3;
        if (weighted < 48) return 4;
        if (weighted < 57) return 12;
        if (weighted < 65) return 8;
        if (weighted < 72) return 10;
        if (weighted < 79) return 5;
        if (weighted < 85) return 7;
        if (weighted < 90) return 9;
        if (weighted < 94) return 2;
        if (weighted < 97) return 6;
        if (weighted < 99) return 11;
        return 13;
    }

    private void spawnNextPiece(boolean resetHold) {
        currentType = nextTypes[0];
        currentMaterial = nextMaterials[0];
        for (int i = 0; i < QUEUE_SIZE - 1; i++) {
            nextTypes[i] = nextTypes[i + 1];
            nextMaterials[i] = nextMaterials[i + 1];
        }
        nextTypes[QUEUE_SIZE - 1] = takeFromBag();
        piecesSpawned++;
        nextMaterials[QUEUE_SIZE - 1] = randomMaterial();
        rotation = 0;
        pieceX = spawnX();
        pieceY = -1;
        groundedSince = 0L;
        lockResetCount = 0;
        if (resetHold) holdUsed = false;
        if (collides(pieceX, pieceY, rotation)) finishGameOrShield();
    }

    private long dropInterval() {
        return GameBalance.dropIntervalMs(level, mode, SystemClock.uptimeMillis() < slowUntil);
    }

    private void updateGravity(long now) {
        if (now < freezeUntil || !canOperate()) return;
        boolean grounded = collides(pieceX, pieceY + 1, rotation);
        if (grounded) {
            if (groundedSince == 0L) groundedSince = now;
            long delay = GameBalance.lockDelayMs(level, mode, isStabilizerPassiveUnlocked());
            if (now - groundedSince >= delay) lockPiece();
            return;
        }
        groundedSince = 0L;
        if (now - lastTick >= dropInterval()) {
            pieceY++;
            lastTick = now;
        }
    }

    private boolean isGrounded() {
        return collides(pieceX, pieceY + 1, rotation);
    }

    private void resetLockDelayAfterMove() {
        if (!isGrounded()) {
            groundedSince = 0L;
            lockResetCount = 0;
            return;
        }
        if (lockResetCount < GameBalance.maxLockResets(mode)) {
            groundedSince = SystemClock.uptimeMillis();
            lockResetCount++;
        }
    }

    private boolean collides(int x, int y, int rot) {
        int[] shape = SHAPES[currentType][rot];
        for (int i = 0; i < 8; i += 2) {
            int bx = x + shape[i];
            int by = y + shape[i + 1];
            if (bx < 0 || bx >= boardCols || by >= boardRows) return true;
            if (by >= 0 && board[by][bx] != 0) return true;
        }
        return false;
    }

    private boolean canPlaceType(int type, int x, int y, int rot) {
        int old = currentType;
        currentType = type;
        boolean result = !collides(x, y, rot);
        currentType = old;
        return result;
    }

    private void move(int dx, boolean repeated) {
        if (!canOperate()) return;
        if (!collides(pieceX + dx, pieceY, rotation)) {
            pieceX += dx;
            resetLockDelayAfterMove();
            audio.play(AudioEngine.MOVE, repeated ? 0.48f : 0.65f);
        }
    }

    private void rotatePiece() {
        if (!canOperate()) return;
        int nextRotation = (rotation + 1) % 4;
        int[] kicks = {0, -1, 1, -2, 2};
        for (int kick : kicks) {
            if (!collides(pieceX + kick, pieceY, nextRotation)) {
                pieceX += kick;
                rotation = nextRotation;
                resetLockDelayAfterMove();
                audio.play(AudioEngine.ROTATE);
                lightHaptic();
                return;
            }
        }
        showBanner("这里无法旋转", 520L);
    }

    private void stepDown(boolean manual) {
        if (!canOperate()) return;
        if (!collides(pieceX, pieceY + 1, rotation)) {
            pieceY++;
            groundedSince = 0L;
            if (manual) {
                lastTick = SystemClock.uptimeMillis();
                score++;
                softDropPoints++;
                audio.play(AudioEngine.SOFT_DROP, 0.38f);
            }
        } else if (groundedSince == 0L) {
            groundedSince = SystemClock.uptimeMillis();
        }
    }

    private void hardDrop() {
        if (!canOperate()) return;
        int distance = 0;
        while (!collides(pieceX, pieceY + 1, rotation)) {
            pieceY++;
            distance++;
        }
        int points = distance * 2;
        score += points;
        hardDropPoints += points;
        strongHaptic();
        audio.play(AudioEngine.HARD_DROP);
        lockPiece();
    }

    private void holdPiece() {
        if (!canOperate()) return;
        if (holdUsed) {
            showBanner("该方块已经暂存过", 750L);
            audio.play(AudioEngine.CLICK, 0.45f);
            return;
        }
        int oldType = currentType;
        int oldMaterial = currentMaterial;
        if (heldType < 0) {
            heldType = oldType;
            heldMaterial = oldMaterial;
            spawnNextPiece(false);
        } else {
            int swapType = heldType;
            int swapMaterial = heldMaterial;
            heldType = oldType;
            heldMaterial = oldMaterial;
            currentType = swapType;
            currentMaterial = swapMaterial;
            rotation = 0;
            pieceX = spawnX();
            pieceY = -1;
            if (!findSafeCurrentPlacement()) {
                currentType = oldType;
                currentMaterial = oldMaterial;
                heldType = swapType;
                heldMaterial = swapMaterial;
                showBanner("暂存交换空间不足", 900L);
                return;
            }
        }
        holdUsed = true;
        lastTick = SystemClock.uptimeMillis();
        audio.play(AudioEngine.HOLD);
        lightHaptic();
        showBanner("方块已暂存", 620L);
    }

    private boolean findSafeCurrentPlacement() {
        int[] xOffsets = {0, -1, 1, -2, 2};
        int[] yOffsets = {-1, -2, 0};
        for (int yOff : yOffsets) {
            for (int xOff : xOffsets) {
                if (!collides(spawnX() + xOff, yOff, 0)) {
                    pieceX = spawnX() + xOff;
                    pieceY = yOff;
                    rotation = 0;
                    return true;
                }
            }
        }
        return false;
    }

    private void lockPiece() {
        int[] shape = SHAPES[currentType][rotation];
        boolean aboveTop = false;
        boolean lucky = currentMaterial == LUCKY_MATERIAL;
        for (int i = 0; i < 8; i += 2) {
            int bx = pieceX + shape[i];
            int by = pieceY + shape[i + 1];
            if (by < 0) aboveTop = true;
            else if (by < boardRows && bx >= 0 && bx < boardCols) board[by][bx] = packCell(currentMaterial, currentType);
        }
        audio.play(AudioEngine.LOCK, 0.72f);
        if (aboveTop) {
            finishGameOrShield();
            return;
        }
        checkPatternEgg();
        int cleared = clearLines(false);
        if (cleared == 0) {
            combo = 0;
            perfectStreak = 0;
        }
        if (lucky && !gameOver) triggerLuckyEvent(cleared);
        checkScoreEasterEggs();
        if (!gameOver) spawnNextPiece(true);
        lastTick = SystemClock.uptimeMillis();
    }

    private int clearLines(boolean fromSkill) {
        int cleared = 0;
        int rowMask = 0;
        for (int r = boardRows - 1; r >= 0; r--) {
            boolean full = true;
            for (int c = 0; c < boardCols; c++) {
                if (board[r][c] == 0) {
                    full = false;
                    break;
                }
            }
            if (full) {
                if (r < 31) rowMask |= (1 << r);
                spawnRowParticles(r, fromSkill ? 0xFFFFB74D : 0xFFB8F3FF);
                cleared++;
                for (int rr = r; rr > 0; rr--) {
                    System.arraycopy(board[rr - 1], 0, board[rr], 0, boardCols);
                }
                for (int c = 0; c < boardCols; c++) board[0][c] = 0;
                r++;
            }
        }
        if (cleared <= 0) return 0;

        int oldEnergy = energy;
        int oldLevel = level;
        if (fromSkill) {
            int gained = 60 * cleared * level;
            score += gained;
            skillScore += gained;
        } else {
            int[] base = {0, 100, 300, 500, 800};
            combo++;
            maxCombo = Math.max(maxCombo, combo);
            int gained = base[Math.min(4, cleared)] * level;
            int comboBonus = Math.max(0, combo - 1) * 55 * level;
            score += gained + comboBonus;
            lineScore += gained;
            comboScore += comboBonus;
            lines += cleared;
            fourLineCount += cleared == 4 ? 1 : 0;
            int energyGain = cleared * 17 + Math.max(0, combo - 1) * 4 + (cleared == 4 ? 10 : 0);
            if (totalFragments >= 24) energyGain += cleared * 3;
            energy = Math.min(100, energy + energyGain);
            level = 1 + lines / 10;

            boolean noHoles = countHoles() == 0;
            if (noHoles) {
                perfectStreak++;
                int reward = (80 + perfectStreak * 35) * level;
                score += reward;
                perfectScore += reward;
                if (perfectStreak >= 2) showBanner("完美矿工 ×" + perfectStreak + "  +" + reward, 1150L);
            } else {
                perfectStreak = 0;
            }

            if (cleared == 4) {
                addFragments(2, "发现远古矿脉  +2 碎片");
                audio.play(AudioEngine.FOUR_LINE);
            } else if (cleared >= 2) {
                audio.play(AudioEngine.MULTI_LINE);
            } else {
                audio.play(AudioEngine.LINE);
            }
            if (combo >= 3) addFragments(1, "矿脉连锁  +1 碎片");
            if (cleared != 4 && perfectStreak < 2) {
                if (combo > 1) showBanner("Combo ×" + combo + "  +" + comboBonus, 950L);
                else showBanner(cleared == 1 ? "单行消除" : cleared + " 行消除", 760L);
            }
            if (level > oldLevel) showBanner("深入遗迹：等级 " + level, 900L);
            int missionRewards = missions.onLinesCleared(cleared, combo);
            awardMissionRewards(missionRewards);
        }
        if (oldEnergy < 100 && energy >= 100 && !energyReadyAnnounced) {
            energyReadyAnnounced = true;
            audio.play(AudioEngine.READY);
            showBanner("技能能量已充满", 950L);
        }
        if (energy < 100) energyReadyAnnounced = false;
        saveHighScore();
        return cleared;
    }

    private int countHoles() {
        int holes = 0;
        for (int c = 0; c < boardCols; c++) {
            boolean blockSeen = false;
            for (int r = 0; r < boardRows; r++) {
                if (board[r][c] != 0) blockSeen = true;
                else if (blockSeen) holes++;
            }
        }
        return holes;
    }

    private void triggerLuckyEvent(int cleared) {
        int event = random.nextInt(4);
        if (cleared == 4) event = 3;
        if (event == 0) {
            int bonus = 600 + random.nextInt(401);
            score += bonus;
            lineScore += bonus;
            showBanner("彩虹幸运矿：遗迹宝藏  +" + bonus, 1300L);
        } else if (event == 1) {
            int before = energy;
            energy = Math.min(100, energy + 20);
            showBanner("彩虹幸运矿：充能 +" + (energy - before) + "%", 1200L);
        } else if (event == 2) {
            slowUntil = SystemClock.uptimeMillis() + 5200L;
            showBanner("彩虹幸运矿：时流减缓", 1200L);
        } else {
            addFragments(1, "彩虹幸运矿：遗迹碎片 +1");
        }
        if (easterAnimations) easterUntil = SystemClock.uptimeMillis() + 5000L;
        audio.play(AudioEngine.EGG);
        saveHighScore();
    }

    private void useSkill(int skill) {
        if (!skillsEnabled) {
            showBanner("技能已在设置中关闭", 850L);
            return;
        }
        if (!canOperate()) return;
        long now = SystemClock.uptimeMillis();
        int cost = GameBalance.SKILL_COSTS[skill];
        boolean used = false;
        if (skill == SKILL_PICKAXE) {
            if (energy < cost) {
                showBanner("附魔矿镐需要 " + cost + "% 能量", 850L);
                return;
            }
            boolean bottomEmpty = true;
            for (int c = 0; c < boardCols; c++) if (board[boardRows - 1][c] != 0) bottomEmpty = false;
            if (bottomEmpty) {
                showBanner("最底层为空，未消耗能量", 950L);
                return;
            }
            spawnRowParticles(boardRows - 1, 0xFFFFC04D);
            for (int r = boardRows - 1; r > 0; r--) System.arraycopy(board[r - 1], 0, board[r], 0, boardCols);
            for (int c = 0; c < boardCols; c++) board[0][c] = 0;
            energy -= cost;
            int reward = 120 * level;
            score += reward;
            skillScore += reward;
            showBanner("附魔矿镐：凿除底层", 1100L);
            strongHaptic();
            used = true;
        } else if (skill == SKILL_FREEZE) {
            if (now < freezeCooldownUntil) {
                showBanner("时间冻结冷却 " + secondsLeft(freezeCooldownUntil, now) + " 秒", 850L);
                return;
            }
            if (now < freezeUntil) {
                showBanner("时间冻结不可叠加", 750L);
                return;
            }
            if (energy < cost) {
                showBanner("时间冻结需要 " + cost + "% 能量", 850L);
                return;
            }
            energy -= cost;
            freezeUntil = now + 5000L;
            freezeCooldownUntil = now + GameBalance.skillCooldownMs(skill);
            groundedSince = 0L;
            showBanner("时间冻结：自动下落停止 5 秒", 1200L);
            lightHaptic();
            used = true;
        } else if (skill == SKILL_REFORGE) {
            if (now < reforgeCooldownUntil) {
                showBanner("方块重铸冷却 " + secondsLeft(reforgeCooldownUntil, now) + " 秒", 850L);
                return;
            }
            if (energy < cost) {
                showBanner("方块重铸需要 " + cost + "% 能量", 850L);
                return;
            }
            int oldType = currentType;
            int oldRotation = rotation;
            int oldX = pieceX;
            int oldY = pieceY;
            int candidate = oldType;
            for (int i = 0; i < 10 && candidate == oldType; i++) candidate = random.nextInt(7);
            currentType = candidate;
            rotation = 0;
            if (!findPlacementNear(oldX, oldY)) {
                currentType = oldType;
                rotation = oldRotation;
                pieceX = oldX;
                pieceY = oldY;
                showBanner("重铸后没有安全位置，未消耗能量", 1100L);
                return;
            }
            energy -= cost;
            reforgeCooldownUntil = now + GameBalance.skillCooldownMs(skill);
            resetLockDelayAfterMove();
            showBanner("方块重铸完成", 850L);
            lightHaptic();
            used = true;
        } else if (skill == SKILL_STABILIZER) {
            if (now < stabilizerCooldownUntil) {
                showBanner("稳速器冷却 " + secondsLeft(stabilizerCooldownUntil, now) + " 秒", 850L);
                return;
            }
            if (energy < cost) {
                showBanner("稳速器需要 " + cost + "% 能量", 850L);
                return;
            }
            energy -= cost;
            slowUntil = Math.max(slowUntil, now + 8000L);
            stabilizerCooldownUntil = now + GameBalance.skillCooldownMs(skill);
            groundedSince = now;
            showBanner("稳速器：8 秒内降低下落速度", 1200L);
            lightHaptic();
            used = true;
        } else if (skill == SKILL_SHOCKWAVE) {
            if (now < shockwaveCooldownUntil) {
                showBanner("震荡波冷却 " + secondsLeft(shockwaveCooldownUntil, now) + " 秒", 850L);
                return;
            }
            if (energy < cost) {
                showBanner("震荡波需要 " + cost + "% 能量", 850L);
                return;
            }
            int removed = removeHighestBlocks(10);
            if (removed == 0) {
                showBanner("棋盘为空，未消耗能量", 850L);
                return;
            }
            energy -= cost;
            shockwaveCooldownUntil = now + GameBalance.skillCooldownMs(skill);
            int reward = removed * 35 * Math.max(1, level);
            score += reward;
            skillScore += reward;
            showBanner("震荡波：震碎 " + removed + " 块  +" + reward, 1200L);
            strongHaptic();
            used = true;
        }
        if (used) {
            audio.play(AudioEngine.SKILL);
            int missionRewards = missions.onSkillUsed();
            awardMissionRewards(missionRewards);
        }
        if (energy < 100) energyReadyAnnounced = false;
        saveHighScore();
    }

    private int removeHighestBlocks(int limit) {
        int removed = 0;
        for (int r = 0; r < boardRows && removed < limit; r++) {
            for (int c = 0; c < boardCols && removed < limit; c++) {
                if (board[r][c] != 0) {
                    board[r][c] = 0;
                    removed++;
                    spawnBurst(boardLeft + (c + 0.5f) * cell, boardTop + (r + 0.5f) * cell, 0xFF9FF6FF, 5);
                }
            }
        }
        return removed;
    }

    private void awardMissionRewards(int count) {
        if (count <= 0) return;
        completedMissionCount += count;
        prefs.edit().putInt("completed_missions", completedMissionCount).apply();
        addFragments(count, "本局任务完成  +" + count + " 碎片");
    }

    private boolean findPlacementNear(int originX, int originY) {
        int[] xOffsets = {0, -1, 1, -2, 2, -3, 3};
        int[] yOffsets = {0, -1, -2, 1};
        for (int dy : yOffsets) {
            for (int dx : xOffsets) {
                if (!collides(originX + dx, originY + dy, 0)) {
                    pieceX = originX + dx;
                    pieceY = originY + dy;
                    return true;
                }
            }
        }
        return false;
    }

    private int secondsLeft(long until, long now) {
        return Math.max(1, (int) Math.ceil((until - now) / 1000.0));
    }

    private void finishGameOrShield() {
        if (skillsEnabled && isShieldUnlocked() && !shieldUsed) {
            shieldUsed = true;
            int removed = 0;
            for (int r = 0; r < 5; r++) {
                for (int c = 0; c < boardCols; c++) {
                    if (board[r][c] != 0) {
                        board[r][c] = 0;
                        removed++;
                    }
                }
            }
            rotation = 0;
            pieceX = spawnX();
            pieceY = -1;
            showBanner("遗迹护盾自动触发，清理 " + removed + " 块", 1500L);
            spawnBurst(boardLeft + cell * boardCols * 0.5f, boardTop + cell * 2f, 0xFF7DE8FF, 28);
            audio.play(AudioEngine.SKILL);
            strongHaptic();
            if (!collides(pieceX, pieceY, rotation)) {
                lastTick = SystemClock.uptimeMillis();
                return;
            }
        }
        gameOver = true;
        paused = false;
        cancelActiveTouch();
        saveRunHistory();
        saveHighScore();
        audio.play(AudioEngine.GAME_OVER);
        audio.setGameActive(false);
    }

    private boolean isShieldUnlocked() {
        return totalFragments >= 15 || prefs.getBoolean("shield_unlocked", false);
    }

    private void checkPatternEgg() {
        if (patternEggTriggered) return;
        boolean enough = true;
        for (int c = 0; c < 5; c++) {
            if (board[boardRows - 1][c] == 0 || board[boardRows - 1][boardCols - 1 - c] == 0) {
                enough = false;
                break;
            }
            if (cellShape(board[boardRows - 1][c]) != cellShape(board[boardRows - 1][boardCols - 1 - c])) {
                enough = false;
                break;
            }
        }
        if (enough) {
            patternEggTriggered = true;
            addFragments(1, "隐藏石门纹路亮起  +1 碎片");
            audio.play(AudioEngine.EGG);
        }
    }

    private void checkScoreEasterEggs() {
        if (!scoreEggTriggered && score >= 25000) {
            scoreEggTriggered = true;
            score += 2500;
            prefs.edit().putBoolean("egg_25000_seen", true).apply();
            if (easterAnimations) easterUntil = SystemClock.uptimeMillis() + 12000L;
            showBanner("25000 分矿工勋章：旧彩蛋保留！", 1900L);
            audio.play(AudioEngine.EGG);
        }
        if (!fiftyEggTriggered && score >= 50000) {
            fiftyEggTriggered = true;
            unlockedSkins |= 1 << 5;
            prefs.edit().putInt("unlocked_skins", unlockedSkins).putBoolean("ancient_core_unlocked", true).apply();
            addFragments(5, "远古核心已解锁  +5 碎片");
            if (easterAnimations) easterUntil = SystemClock.uptimeMillis() + 15000L;
            showBanner("发现隐藏皮肤：远古核心", 2100L);
            audio.play(AudioEngine.EGG);
        }
        if (score >= 10000 && !prefs.getBoolean("milestone_10000", false)) {
            prefs.edit().putBoolean("milestone_10000", true).apply();
            addFragments(2, "首次达到 10000 分  +2 碎片");
        }
    }

    private void addFragments(int amount, String message) {
        if (amount <= 0) return;
        runFragments += amount;
        totalFragments += amount;
        prefs.edit().putInt("relic_fragments", totalFragments).apply();
        boolean changed = updateSkinUnlocks(true);
        showBanner(message, changed ? 1450L : 1050L);
    }

    private boolean updateSkinUnlocks(boolean persist) {
        int old = unlockedSkins;
        unlockedSkins |= 1;
        if (totalFragments >= 5) unlockedSkins |= 1 << 1;
        if (totalFragments >= 12) unlockedSkins |= 1 << 2;
        if (totalFragments >= 20) unlockedSkins |= 1 << 3;
        if (totalFragments >= 30) unlockedSkins |= 1 << 4;
        if (prefs.getBoolean("ancient_core_unlocked", false)) unlockedSkins |= 1 << 5;
        if (totalFragments >= 40) unlockedSkins |= 1 << 6;
        if (totalFragments >= 55) unlockedSkins |= 1 << 7;
        if (completedMissionCount >= 12) unlockedSkins |= 1 << 8;
        if (totalFragments >= 15 && !prefs.getBoolean("shield_unlocked", false) && persist) {
            prefs.edit().putBoolean("shield_unlocked", true).apply();
        }
        if (persist && old != unlockedSkins) prefs.edit().putInt("unlocked_skins", unlockedSkins).apply();
        return old != unlockedSkins;
    }

    private boolean isSkinUnlocked(int index) {
        return (unlockedSkins & (1 << index)) != 0;
    }

    private void saveHighScore() {
        int old = getModeHighScore();
        if (score <= old) return;
        if (mode == MODE_CASUAL) {
            highCasual = score;
            highLinesCasual = lines;
            prefs.edit().putInt("high_score_casual", score).putInt("high_lines_casual", lines).apply();
        } else if (mode == MODE_EXTREME) {
            highExtreme = score;
            highLinesExtreme = lines;
            prefs.edit().putInt("high_score_extreme", score).putInt("high_lines_extreme", lines).apply();
        } else {
            highStandard = score;
            highLinesStandard = lines;
            prefs.edit().putInt("high_score_standard", score).putInt("high_score", score)
                    .putInt("high_lines_standard", lines).apply();
        }
    }

    private void saveRunHistory() {
        if (mode == MODE_CASUAL) {
            lastScoreCasual = score;
            lastLinesCasual = lines;
            prefs.edit().putInt("last_score_casual", score).putInt("last_lines_casual", lines).apply();
        } else if (mode == MODE_EXTREME) {
            lastScoreExtreme = score;
            lastLinesExtreme = lines;
            prefs.edit().putInt("last_score_extreme", score).putInt("last_lines_extreme", lines).apply();
        } else {
            lastScoreStandard = score;
            lastLinesStandard = lines;
            prefs.edit().putInt("last_score_standard", score).putInt("last_lines_standard", lines).apply();
        }
    }

    private int getModeHighScore() {
        return mode == MODE_CASUAL ? highCasual : (mode == MODE_EXTREME ? highExtreme : highStandard);
    }

    private int getModeHighLines() {
        return mode == MODE_CASUAL ? highLinesCasual : (mode == MODE_EXTREME ? highLinesExtreme : highLinesStandard);
    }

    private int getModeLastScore() {
        return mode == MODE_CASUAL ? lastScoreCasual : (mode == MODE_EXTREME ? lastScoreExtreme : lastScoreStandard);
    }

    private int getModeLastLines() {
        return mode == MODE_CASUAL ? lastLinesCasual : (mode == MODE_EXTREME ? lastLinesExtreme : lastLinesStandard);
    }

    private String getStartSourceLabel() {
        if (startScoreSource == START_HIGH) return START_NAMES[START_HIGH] + "（" + getModeHighScore() + "）";
        if (startScoreSource == START_LAST) return START_NAMES[START_LAST] + "（" + getModeLastScore() + "）";
        return START_NAMES[START_ZERO];
    }

    private boolean canOperate() {
        return screen == SCREEN_GAME && !paused && !gameOver && !backgroundPaused;
    }

    private void setPaused(boolean value) {
        if (gameOver) return;
        paused = value;
        cancelActiveTouch();
        lastTick = SystemClock.uptimeMillis();
        if (!value && isGrounded()) groundedSince = lastTick;
        audio.play(AudioEngine.PAUSE);
        applyAudioSettings();
    }

    private void showBanner(String text, long duration) {
        banner = text;
        bannerUntil = SystemClock.uptimeMillis() + duration;
    }

    private void lightHaptic() {
        if (vibrationEnabled) performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
    }

    private void strongHaptic() {
        if (vibrationEnabled) performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
    }

    private int packCell(int material, int shape) {
        return 1 + material + shape * 32;
    }

    private int cellMaterial(int packed) {
        return packed <= 0 ? 0 : (packed - 1) % 32;
    }

    private int cellShape(int packed) {
        return packed <= 0 ? 0 : (packed - 1) / 32;
    }

    private int spawnX() {
        return Math.max(0, (boardCols - 4) / 2);
    }

    private int ghostY() {
        int y = pieceY;
        while (!collides(pieceX, y + 1, rotation)) y++;
        return y;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        updateLayout(w, h);
    }

    private void updateLayout(int w, int h) {
        if (w <= 0 || h <= 0) return;
        float leftEdge = safeLeft + 8f * density;
        float rightEdge = w - safeRight - 8f * density;
        float topEdge = safeTop + 4f * density;
        float bottomEdge = h - safeBottom - 8f * density;
        float headerHeight = 66f * density;
        float gap = 8f * density;
        float row1H = Math.min(68f * density, h * 0.084f);
        float row2H = Math.min(62f * density, h * 0.076f);
        float controlsHeight = row1H + row2H + gap * 2f;
        float controlsTop = bottomEdge - controlsHeight;
        float boardAreaTop = topEdge + headerHeight;
        float boardAreaBottom = controlsTop - gap;
        float usableWidth = rightEdge - leftEdge;
        float boardWidthLimit = usableWidth * 0.69f;
        cell = Math.min(boardWidthLimit / boardCols, (boardAreaBottom - boardAreaTop) / boardRows);
        cell = Math.max(8f * density, cell);
        boardLeft = leftEdge;
        boardTop = boardAreaTop + Math.max(0f, (boardAreaBottom - boardAreaTop - boardRows * cell) * 0.5f);
        boardRight = boardLeft + boardCols * cell;
        boardBottom = boardTop + boardRows * cell;
        boardRect.set(boardLeft, boardTop, boardRight, boardBottom);
        sideLeft = boardRight + 10f * density;
        sideRight = rightEdge;

        float buttonGap = 8f * density;
        float row1Top = controlsTop;
        float smallW = (usableWidth - 3f * buttonGap) / 4f;
        // 操作顺序：左移 → 软降 → 旋转 → 右移。按钮矩形同时用于绘制和触控，避免视觉与点击区域错位。
        leftButton.set(leftEdge, row1Top, leftEdge + smallW, row1Top + row1H);
        downButton.set(leftButton.right + buttonGap, row1Top, leftButton.right + buttonGap + smallW, row1Top + row1H);
        rotateButton.set(downButton.right + buttonGap, row1Top, downButton.right + buttonGap + smallW, row1Top + row1H);
        rightButton.set(rotateButton.right + buttonGap, row1Top, rightEdge, row1Top + row1H);

        float row2Top = row1Top + row1H + buttonGap;
        float holdW = usableWidth * 0.24f;
        float pauseW = usableWidth * 0.24f;
        holdButton.set(leftEdge, row2Top, leftEdge + holdW, row2Top + row2H);
        pauseButton.set(holdButton.right + buttonGap, row2Top, holdButton.right + buttonGap + pauseW, row2Top + row2H);
        dropButton.set(pauseButton.right + buttonGap * 1.7f, row2Top, rightEdge, row2Top + row2H);

        float sideW = Math.max(1f, sideRight - sideLeft);
        settingsButton.set(sideLeft, boardTop, sideRight, boardTop + 31f * density);
        float skillsBottom = boardBottom;
        float skillH = Math.min(46f * density, Math.max(31f * density, sideW * 0.64f));
        float skillGap = 6f * density;
        for (int i = 0; i < 3; i++) {
            float top = skillsBottom - (3 - i) * skillH - (2 - i) * skillGap;
            skillButtons[i].set(sideLeft, top, sideRight, top + skillH);
        }

        float center = (leftEdge + rightEdge) * 0.5f;
        float menuW = Math.min(330f * density, usableWidth * 0.84f);
        float menuH = Math.min(52f * density, Math.max(42f * density, h * 0.061f));
        float menuGap = Math.min(10f * density, Math.max(6f * density, h * 0.010f));
        float menuTotal = menuH * 5f + menuGap * 4f;
        float latestMenuStart = bottomEdge - menuTotal;
        float preferredMenuStart = h * 0.47f;
        float earliestMenuStart = safeTop + 220f * density;
        float menuStart = Math.min(preferredMenuStart, latestMenuStart);
        if (latestMenuStart >= earliestMenuStart) menuStart = Math.max(earliestMenuStart, menuStart);
        else menuStart = Math.max(safeTop + 175f * density, latestMenuStart);
        menuPrimary.set(center - menuW/2f, menuStart, center + menuW/2f, menuStart + menuH);
        menuSecondary.set(menuPrimary.left, menuPrimary.bottom + menuGap, menuPrimary.right, menuPrimary.bottom + menuGap + menuH);
        menuThird.set(menuPrimary.left, menuSecondary.bottom + menuGap, menuPrimary.right, menuSecondary.bottom + menuGap + menuH);
        menuFourth.set(menuPrimary.left, menuThird.bottom + menuGap, menuPrimary.right, menuThird.bottom + menuGap + menuH);
        menuFifth.set(menuPrimary.left, menuFourth.bottom + menuGap, menuPrimary.right, menuFourth.bottom + menuGap + menuH);

        float overlayW = Math.min(300f * density, usableWidth * 0.78f);
        float overlayH = 48f * density;
        float overlayStart = h * 0.43f;
        overlayButton1.set(center-overlayW/2f, overlayStart, center+overlayW/2f, overlayStart+overlayH);
        overlayButton2.set(overlayButton1.left, overlayButton1.bottom+10f*density, overlayButton1.right, overlayButton1.bottom+10f*density+overlayH);
        overlayButton3.set(overlayButton1.left, overlayButton2.bottom+10f*density, overlayButton1.right, overlayButton2.bottom+10f*density+overlayH);
        overlayButton4.set(overlayButton1.left, overlayButton3.bottom+10f*density, overlayButton1.right, overlayButton3.bottom+10f*density+overlayH);

        float confirmW = Math.min(130f*density, usableWidth*0.34f);
        confirmYes.set(center-confirmW-8f*density, h*0.57f, center-8f*density, h*0.57f+48f*density);
        confirmNo.set(center+8f*density, h*0.57f, center+confirmW+8f*density, h*0.57f+48f*density);
        backButton.set(leftEdge, topEdge + 8f*density, leftEdge + 78f*density, topEdge + 48f*density);
        contentClip.set(leftEdge, topEdge + 62f*density, rightEdge, bottomEdge);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (screen == SCREEN_GAME) drawGame(canvas);
        else if (screen == SCREEN_SETTINGS) drawSettings(canvas);
        else if (screen == SCREEN_SKINS) drawSkins(canvas);
        else if (screen == SCREEN_TUTORIAL) drawTutorial(canvas);
        else drawMenu(canvas);
        drawParticles(canvas);
        if (confirmAction != CONFIRM_NONE) drawConfirm(canvas);
    }

    private void drawGame(Canvas canvas) {
        drawWorldBackground(canvas);
        drawHeader(canvas, "遗迹探险版");
        drawBoardFrame(canvas);
        drawBoard(canvas);
        drawSidePanel(canvas);
        drawControls(canvas);
        drawBanner(canvas);
        if (paused && !gameOver) drawPauseOverlay(canvas);
        if (gameOver) drawGameOver(canvas);
    }

    private void drawWorldBackground(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        boolean special = easterAnimations && SystemClock.uptimeMillis() < easterUntil;
        int safeSkin = clamp(skin, 0, ThemeCatalog.count() - 1);
        int sky = ThemeCatalog.WORLD_COLORS[safeSkin][0];
        int ground = ThemeCatalog.WORLD_COLORS[safeSkin][1];
        int deep = ThemeCatalog.WORLD_COLORS[safeSkin][2];
        if (special) sky = blend(sky, 0xFFFFD25A, 0.25f);
        canvas.drawColor(sky);
        float horizon = getHeight() * 0.75f;
        pixelPaint.setColor(ground);
        canvas.drawRect(0, horizon, w, horizon + 20f*density, pixelPaint);
        pixelPaint.setColor(deep);
        canvas.drawRect(0, horizon + 20f*density, w, h, pixelPaint);
        int tile = Math.max(14, (int)(18f*density));
        for (int y = (int)horizon + tile; y < h; y += tile) {
            for (int x = 0; x < w; x += tile) {
                int seed = (x/tile)*31 + (y/tile)*47 + skin*73;
                pixelPaint.setColor((seed & 1) == 0 ? lighten(deep, 18) : darken(deep, 12));
                canvas.drawRect(x+tile*0.18f, y+tile*0.22f, x+tile*0.45f, y+tile*0.47f, pixelPaint);
            }
        }
        if (special) {
            for (int i = 0; i < 20; i++) {
                float x = (i * 79 + 37) % Math.max(1, w);
                float y = (i * 131 + 19) % Math.max(1, (int)horizon);
                float s = (2 + i % 3) * density;
                pixelPaint.setColor((i & 1) == 0 ? 0xFFFFED84 : 0xFF9FF6FF);
                canvas.drawRect(x, y, x+s, y+s, pixelPaint);
            }
        }
    }

    private void drawHeader(Canvas canvas, String subtitle) {
        float center = (safeLeft + getWidth() - safeRight) * 0.5f;
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setStyle(Paint.Style.FILL);
        paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        paint.setTextSize(Math.min(29f*density, getWidth()*0.073f));
        paint.setColor(0xFF2C241C);
        canvas.drawText("方块矿境", center+2f*density, safeTop+38f*density+2f*density, paint);
        paint.setColor(0xFFF7F2D8);
        canvas.drawText("方块矿境", center, safeTop+38f*density, paint);
        paint.setTextSize(Math.min(10.5f*density, getWidth()*0.027f));
        paint.setColor(0xFF274C55);
        canvas.drawText("BLOCK MINE · v1.4 " + subtitle, center, safeTop+57f*density, paint);
    }

    private void drawBoardFrame(Canvas canvas) {
        float pad = 7f*density;
        pixelPaint.setColor(0xFF38271D);
        canvas.drawRect(boardLeft-pad, boardTop-pad, boardRight+pad, boardBottom+pad, pixelPaint);
        pixelPaint.setColor(SystemClock.uptimeMillis() < easterUntil ? 0xFFE7B84E : 0xFF76502F);
        canvas.drawRect(boardLeft-3f*density, boardTop-3f*density, boardRight+3f*density, boardBottom+3f*density, pixelPaint);
        pixelPaint.setColor(0xFF1A2226);
        canvas.drawRect(boardRect, pixelPaint);
    }

    private void drawBoard(Canvas canvas) {
        for (int r = 0; r < boardRows; r++) {
            for (int c = 0; c < boardCols; c++) {
                float x = boardLeft + c*cell;
                float y = boardTop + r*cell;
                pixelPaint.setColor(((r+c)&1)==0 ? 0xFF1D282B : 0xFF202C2F);
                canvas.drawRect(x, y, x+cell, y+cell, pixelPaint);
                if (gridEnabled) {
                    pixelPaint.setStyle(Paint.Style.STROKE);
                    pixelPaint.setStrokeWidth(1f);
                    pixelPaint.setColor(0x22FFFFFF);
                    canvas.drawRect(x, y, x+cell, y+cell, pixelPaint);
                    pixelPaint.setStyle(Paint.Style.FILL);
                }
                int packed = board[r][c];
                if (packed != 0) drawBlock(canvas, x, y, cell, cellMaterial(packed), cellShape(packed), c, r, 255);
            }
        }
        if (!gameOver) {
            int[] shape = SHAPES[currentType][rotation];
            if (ghostEnabled) {
                int gy = ghostY();
                for (int i = 0; i < 8; i += 2) {
                    int bx = pieceX + shape[i];
                    int by = gy + shape[i+1];
                    if (by >= 0) drawGhost(canvas, boardLeft+bx*cell, boardTop+by*cell, cell, currentMaterial, currentType);
                }
            }
            for (int i = 0; i < 8; i += 2) {
                int bx = pieceX + shape[i];
                int by = pieceY + shape[i+1];
                if (by >= 0) drawBlock(canvas, boardLeft+bx*cell, boardTop+by*cell, cell, currentMaterial, currentType, bx, by, 255);
            }
        }
        long now = SystemClock.uptimeMillis();
        if (now < freezeUntil) {
            float remain = (freezeUntil-now)/5000f;
            pixelPaint.setColor(0x447BE8FF);
            canvas.drawRect(boardLeft, boardTop, boardRight, boardTop+cell*(1.2f+remain), pixelPaint);
            if (freezeUntil-now < 1100L) {
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTextSize(15f*density);
                paint.setColor(0xFFFFFFFF);
                canvas.drawText("冻结即将结束", (boardLeft+boardRight)/2f, boardTop+cell*3f, paint);
            }
        }
    }

    private void drawSidePanel(Canvas canvas) {
        if (sideRight <= sideLeft + 2f*density) return;
        drawButton(canvas, settingsButton, "设置", 0xFF4E6870, false);
        float y = settingsButton.bottom + 7f*density;
        float holdPanelH = 66f*density;
        drawPanel(canvas, sideLeft, y, sideRight, y+holdPanelH);
        drawSmallLabel(canvas, "HOLD", (sideLeft+sideRight)/2f, y+14f*density);
        if (heldType >= 0) drawPreviewPiece(canvas, heldType, heldMaterial, sideLeft, y+18f*density, sideRight, y+holdPanelH-2f*density, 0);
        else drawSmallLabel(canvas, "空", (sideLeft+sideRight)/2f, y+43f*density);

        y += holdPanelH + 7f*density;
        float previewH = previewThree ? 139f*density : 61f*density;
        drawPanel(canvas, sideLeft, y, sideRight, y+previewH);
        drawSmallLabel(canvas, previewThree ? "未来 3 块" : "下一块", (sideLeft+sideRight)/2f, y+14f*density);
        int count = previewThree ? 3 : 1;
        float eachH = (previewH-18f*density)/count;
        for (int i = 0; i < count; i++) {
            drawPreviewPiece(canvas, nextTypes[i], nextMaterials[i], sideLeft, y+18f*density+i*eachH, sideRight, y+18f*density+(i+1)*eachH, i);
        }

        y += previewH + 7f*density;
        float infoBottom = Math.min(skillButtons[0].top-7f*density, y+137f*density);
        if (infoBottom > y+55f*density) {
            drawPanel(canvas, sideLeft, y, sideRight, infoBottom);
            float h = infoBottom-y;
            drawStat(canvas, "分数", String.valueOf(score), sideLeft, sideRight, y+h*0.18f);
            drawStat(canvas, "最高", String.valueOf(Math.max(score, getModeHighScore())), sideLeft, sideRight, y+h*0.42f);
            drawStat(canvas, "等级", String.valueOf(level), sideLeft, sideRight, y+h*0.66f);
            drawStat(canvas, "碎片", totalFragments+" ( +"+runFragments+" )", sideLeft, sideRight, y+h*0.89f);
        }

        for (int slot = 0; slot < skillButtons.length; slot++) {
            int skill = skillForSlot(slot);
            drawSkillButton(canvas, slot, skill);
        }

        float energyTop = skillButtons[0].top - 17f*density;
        if (energyTop > y) {
            pixelPaint.setColor(0xFF263638);
            canvas.drawRect(sideLeft, energyTop, sideRight, energyTop+10f*density, pixelPaint);
            pixelPaint.setColor(0xFFFFD35C);
            canvas.drawRect(sideLeft, energyTop, sideLeft+(sideRight-sideLeft)*energy/100f, energyTop+10f*density, pixelPaint);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(8f*density);
            paint.setColor(0xFFFFFFFF);
            canvas.drawText(energy+"%", (sideLeft+sideRight)/2f, energyTop+9f*density, paint);
        }
    }

    private void drawSkillButton(Canvas canvas, int slot, int skill) {
        RectF rect = skillButtons[slot];
        long now = SystemClock.uptimeMillis();
        long cooldownUntil = cooldownUntilForSkill(skill);
        int cost = GameBalance.SKILL_COSTS[skill];
        boolean cool = now < cooldownUntil;
        boolean ready = skillsEnabled && energy >= cost && !cool;
        int color = ready ? 0xFF9A7131 : 0xFF4B5A58;
        String label = GameBalance.SKILL_NAMES[skill];
        String text;
        if (!skillsEnabled) text = label + " 关";
        else if (cool) text = label + " " + secondsLeft(cooldownUntil, now) + "s";
        else text = label + " " + cost + "%";
        drawButton(canvas, rect, text, color, pressedSkill == slot);
        if (cool) {
            float total = Math.max(1f, GameBalance.skillCooldownMs(skill));
            float ratio = Math.max(0f, Math.min(1f, (cooldownUntil-now)/total));
            pixelPaint.setColor(0x990F1719);
            canvas.drawRect(rect.left, rect.top, rect.right, rect.top+rect.height()*ratio, pixelPaint);
        }
        if (slot == 2 && isShieldUnlocked()) {
            paint.setTextAlign(Paint.Align.RIGHT);
            paint.setTextSize(7.5f*density);
            paint.setColor(shieldUsed ? 0xFF9A9A9A : 0xFF9AF4FF);
            canvas.drawText(shieldUsed ? "盾已用" : "盾就绪", rect.right-3f*density, rect.bottom-3f*density, paint);
        }
    }

    private int skillForSlot(int slot) {
        return GameBalance.SKILL_LOADOUTS[skillLoadout][slot];
    }

    private long cooldownUntilForSkill(int skill) {
        if (skill == SKILL_FREEZE) return freezeCooldownUntil;
        if (skill == SKILL_REFORGE) return reforgeCooldownUntil;
        if (skill == SKILL_STABILIZER) return stabilizerCooldownUntil;
        if (skill == SKILL_SHOCKWAVE) return shockwaveCooldownUntil;
        return 0L;
    }

    private void drawPreviewPiece(Canvas canvas, int type, int material, float l, float t, float r, float b, int seedOffset) {
        float previewCell = Math.min((r-l)/5.2f, (b-t)/4.2f);
        int[] shape = SHAPES[type][0];
        float minX = 4f, maxX = 0f, minY = 4f, maxY = 0f;
        for (int i = 0; i < 8; i += 2) {
            minX = Math.min(minX, shape[i]); maxX = Math.max(maxX, shape[i]);
            minY = Math.min(minY, shape[i+1]); maxY = Math.max(maxY, shape[i+1]);
        }
        float width = (maxX-minX+1f)*previewCell;
        float height = (maxY-minY+1f)*previewCell;
        float ox = (l+r-width)/2f-minX*previewCell;
        float oy = (t+b-height)/2f-minY*previewCell;
        for (int i = 0; i < 8; i += 2) {
            drawBlock(canvas, ox+shape[i]*previewCell, oy+shape[i+1]*previewCell, previewCell, material, type, shape[i]+seedOffset*7, shape[i+1], 255);
        }
    }

    private void drawControls(Canvas canvas) {
        drawButton(canvas, leftButton, "◀", 0xFF4B7447, activeControl==CONTROL_LEFT);
        drawButton(canvas, rotateButton, "↻", 0xFF526984, false);
        drawButton(canvas, rightButton, "▶", 0xFF4B7447, activeControl==CONTROL_RIGHT);
        drawButton(canvas, downButton, "▼", 0xFF4B7447, activeControl==CONTROL_DOWN);
        drawButton(canvas, holdButton, holdUsed ? "HOLD 已用" : "HOLD", 0xFF496E76, false);
        drawButton(canvas, pauseButton, paused ? "继续" : "暂停", 0xFF684E75, false);
        drawButton(canvas, dropButton, hardDropArmed ? "松手确认" : "快速落地", 0xFF8A5B2F, hardDropArmed);
    }

    private void drawGhost(Canvas canvas, float x, float y, float size, int material, int shape) {
        int base = transformedColor(MATERIAL_COLORS[material][0]);
        pixelPaint.setStyle(Paint.Style.FILL);
        pixelPaint.setColor(withAlpha(base, 62));
        canvas.drawRect(x+size*0.12f, y+size*0.12f, x+size*0.88f, y+size*0.88f, pixelPaint);
        pixelPaint.setStyle(Paint.Style.STROKE);
        pixelPaint.setStrokeWidth(Math.max(1f, size*0.07f));
        pixelPaint.setColor(0x99E1F7E8);
        canvas.drawRect(x+size*0.12f, y+size*0.12f, x+size*0.88f, y+size*0.88f, pixelPaint);
        pixelPaint.setStyle(Paint.Style.FILL);
        if (colorBlindMode) drawShapeMark(canvas, x, y, size, shape, 110);
    }

    private void drawBlock(Canvas canvas, float x, float y, float size, int material, int shape, int gx, int gy, int alpha) {
        material = clamp(material, 0, MATERIAL_COUNT-1);
        int c0 = transformedColor(MATERIAL_COLORS[material][0]);
        int c1 = transformedColor(MATERIAL_COLORS[material][1]);
        int c2 = transformedColor(MATERIAL_COLORS[material][2]);
        float gap = Math.max(1f, size*0.045f);
        pixelPaint.setColor(withAlpha(c1, alpha));
        canvas.drawRect(x+gap, y+gap, x+size-gap, y+size-gap, pixelPaint);
        pixelPaint.setColor(withAlpha(c0, alpha));
        canvas.drawRect(x+gap, y+gap, x+size-gap, y+size*0.78f, pixelPaint);
        pixelPaint.setColor(withAlpha(lighten(c0, 34), alpha));
        canvas.drawRect(x+gap, y+gap, x+size-gap, y+size*0.13f, pixelPaint);
        canvas.drawRect(x+gap, y+gap, x+size*0.12f, y+size-gap, pixelPaint);
        pixelPaint.setColor(withAlpha(darken(c1, 24), alpha));
        canvas.drawRect(x+size*0.84f, y+size*0.15f, x+size-gap, y+size-gap, pixelPaint);
        canvas.drawRect(x+size*0.14f, y+size*0.84f, x+size-gap, y+size-gap, pixelPaint);
        int seed = gx*97 + gy*53 + material*211 + skin*997;
        float p = Math.max(1.5f, size*0.13f);
        for (int i = 0; i < 4; i++) {
            int sx = Math.abs(seed+i*43)%6;
            int sy = Math.abs(seed*3+i*29)%6;
            float px = x+size*(0.14f+sx*0.11f);
            float py = y+size*(0.18f+sy*0.10f);
            int color = i<2 ? c2 : lighten(c1, 28);
            pixelPaint.setColor(withAlpha(color, alpha));
            canvas.drawRect(px, py, px+p, py+p, pixelPaint);
        }
        drawMaterialAccent(canvas, x, y, size, material, c0, c2, alpha);
        drawSkinAccent(canvas, x, y, size, alpha);
        if (colorBlindMode) drawShapeMark(canvas, x, y, size, shape, alpha);
    }

    private void drawSkinAccent(Canvas canvas,float x,float y,float size,int alpha){
        if(skin==6){
            pixelPaint.setColor(withAlpha(0xFFD9E3FF,Math.min(alpha,155)));
            canvas.drawRect(x+size*0.20f,y+size*0.20f,x+size*0.28f,y+size*0.28f,pixelPaint);
            canvas.drawRect(x+size*0.68f,y+size*0.28f,x+size*0.76f,y+size*0.36f,pixelPaint);
        }else if(skin==7){
            pixelPaint.setColor(withAlpha(0xFFFFD7E8,Math.min(alpha,150)));
            canvas.drawRect(x+size*0.24f,y+size*0.24f,x+size*0.36f,y+size*0.36f,pixelPaint);
            canvas.drawRect(x+size*0.58f,y+size*0.56f,x+size*0.70f,y+size*0.68f,pixelPaint);
        }else if(skin==8){
            pixelPaint.setColor(withAlpha(0xFF76FFE3,Math.min(alpha,165)));
            canvas.drawRect(x+size*0.20f,y+size*0.48f,x+size*0.80f,y+size*0.55f,pixelPaint);
            canvas.drawRect(x+size*0.48f,y+size*0.20f,x+size*0.55f,y+size*0.80f,pixelPaint);
        }
    }

    private void drawMaterialAccent(Canvas canvas, float x, float y, float size, int material, int c0, int c2, int alpha) {
        if (material == LUCKY_MATERIAL) {
            int[] rainbow = {0xFFEC5353, 0xFFF4B844, 0xFF4DC471, 0xFF5199EB};
            for (int i = 0; i < 4; i++) {
                float qx = x+size*(0.13f+(i%2)*0.39f);
                float qy = y+size*(0.15f+(i/2)*0.39f);
                pixelPaint.setColor(withAlpha(rainbow[i], alpha));
                canvas.drawRect(qx, qy, qx+size*0.34f, qy+size*0.34f, pixelPaint);
            }
            pixelPaint.setColor(withAlpha(Color.WHITE, alpha));
            canvas.drawRect(x+size*0.45f, y+size*0.18f, x+size*0.55f, y+size*0.78f, pixelPaint);
            canvas.drawRect(x+size*0.22f, y+size*0.43f, x+size*0.78f, y+size*0.53f, pixelPaint);
        } else if (material == 0) {
            pixelPaint.setColor(withAlpha(darken(c0, 18), alpha));
            canvas.drawRect(x+size*0.08f, y+size*0.58f, x+size*0.92f, y+size*0.84f, pixelPaint);
        } else if (material == 10 || material == 6) {
            pixelPaint.setColor(withAlpha(Color.WHITE, Math.min(alpha, 190)));
            canvas.drawRect(x+size*0.24f, y+size*0.62f, x+size*0.34f, y+size*0.77f, pixelPaint);
            canvas.drawRect(x+size*0.34f, y+size*0.48f, x+size*0.45f, y+size*0.63f, pixelPaint);
        } else if (material == 13) {
            pixelPaint.setColor(withAlpha(c2, alpha));
            canvas.drawRect(x+size*0.22f, y+size*0.18f, x+size*0.37f, y+size*0.52f, pixelPaint);
            canvas.drawRect(x+size*0.37f, y+size*0.42f, x+size*0.72f, y+size*0.57f, pixelPaint);
        }
    }

    private void drawShapeMark(Canvas canvas, float x, float y, float size, int shape, int alpha) {
        int mark = withAlpha(Color.WHITE, Math.min(alpha, 190));
        pixelPaint.setColor(mark);
        float u = Math.max(1.5f, size*0.09f);
        float cx = x+size*0.5f;
        float cy = y+size*0.5f;
        switch (shape) {
            case 0:
                canvas.drawRect(x+size*0.25f, cy-u/2f, x+size*0.75f, cy+u/2f, pixelPaint); break;
            case 1:
                canvas.drawRect(cx-u*1.2f, cy-u*1.2f, cx+u*1.2f, cy+u*1.2f, pixelPaint); break;
            case 2:
                canvas.drawRect(cx-u/2f, y+size*0.24f, cx+u/2f, y+size*0.76f, pixelPaint);
                canvas.drawRect(x+size*0.30f, cy-u/2f, x+size*0.70f, cy+u/2f, pixelPaint); break;
            case 3:
                canvas.drawRect(x+size*0.25f, y+size*0.30f, x+size*0.52f, y+size*0.45f, pixelPaint);
                canvas.drawRect(x+size*0.48f, y+size*0.55f, x+size*0.75f, y+size*0.70f, pixelPaint); break;
            case 4:
                canvas.drawRect(x+size*0.48f, y+size*0.30f, x+size*0.75f, y+size*0.45f, pixelPaint);
                canvas.drawRect(x+size*0.25f, y+size*0.55f, x+size*0.52f, y+size*0.70f, pixelPaint); break;
            case 5:
                canvas.drawRect(x+size*0.27f, y+size*0.27f, x+size*0.38f, y+size*0.72f, pixelPaint);
                canvas.drawRect(x+size*0.27f, y+size*0.61f, x+size*0.72f, y+size*0.72f, pixelPaint); break;
            default:
                canvas.drawRect(x+size*0.62f, y+size*0.27f, x+size*0.73f, y+size*0.72f, pixelPaint);
                canvas.drawRect(x+size*0.28f, y+size*0.61f, x+size*0.73f, y+size*0.72f, pixelPaint); break;
        }
    }

    private int transformedColor(int color) {
        switch (skin) {
            case 1: return tint(color, 0xFFBDEBFF, 0.24f);
            case 2: return tint(color, 0xFFFF5B35, 0.20f);
            case 3: return tint(color, 0xFF38D5CF, 0.22f);
            case 4: return tint(color, 0xFFE8F7FF, 0.18f);
            case 5: return tint(color, 0xFFCF74FF, 0.25f);
            case 6: return tint(color, 0xFF778CFF, 0.28f);
            case 7: return tint(color, 0xFFFF8FC4, 0.24f);
            case 8: return tint(color, 0xFF3CFFD0, 0.24f);
            default: return color;
        }
    }

    private int tint(int color, int tint, float amount) {
        return blend(color, tint, amount);
    }

    private int blend(int a, int b, float amount) {
        amount = Math.max(0f, Math.min(1f, amount));
        int r = Math.round(Color.red(a)*(1f-amount)+Color.red(b)*amount);
        int g = Math.round(Color.green(a)*(1f-amount)+Color.green(b)*amount);
        int bl = Math.round(Color.blue(a)*(1f-amount)+Color.blue(b)*amount);
        return Color.rgb(r,g,bl);
    }

    private int lighten(int color, int delta) {
        return Color.rgb(Math.min(255, Color.red(color)+delta), Math.min(255, Color.green(color)+delta), Math.min(255, Color.blue(color)+delta));
    }

    private int darken(int color, int delta) {
        return Color.rgb(Math.max(0, Color.red(color)-delta), Math.max(0, Color.green(color)-delta), Math.max(0, Color.blue(color)-delta));
    }

    private int withAlpha(int color, int alpha) {
        return Color.argb(clamp(alpha,0,255), Color.red(color), Color.green(color), Color.blue(color));
    }

    private void drawPanel(Canvas canvas, float l, float t, float r, float b) {
        pixelPaint.setColor(0xE6263130);
        canvas.drawRect(l,t,r,b,pixelPaint);
        pixelPaint.setStyle(Paint.Style.STROKE);
        pixelPaint.setStrokeWidth(2f*density);
        pixelPaint.setColor(0xFF7D5431);
        canvas.drawRect(l,t,r,b,pixelPaint);
        pixelPaint.setStyle(Paint.Style.FILL);
    }

    private void drawSmallLabel(Canvas canvas, String text, float x, float y) {
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(9f*density);
        paint.setColor(0xFFE0E8D3);
        canvas.drawText(text,x,y,paint);
    }

    private void drawStat(Canvas canvas, String label, String value, float l, float r, float y) {
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(7.8f*density);
        paint.setColor(0xFFAABEB0);
        canvas.drawText(label,(l+r)/2f,y-5f*density,paint);
        paint.setTextSize(value.length()>8 ? 9f*density : 11.5f*density);
        paint.setColor(0xFFF6E6A6);
        canvas.drawText(value,(l+r)/2f,y+8f*density,paint);
    }

    private void drawButton(Canvas canvas, RectF rect, String text, int color, boolean pressed) {
        if (rect.width() <= 0 || rect.height() <= 0) return;
        float inset = pressed ? 3f*density : 0f;
        float l = rect.left+inset;
        float t = rect.top+inset;
        float r = rect.right-inset;
        float b = rect.bottom-inset;
        int actual = pressed ? darken(color, 28) : color;
        pixelPaint.setColor(0xFF2D221B);
        canvas.drawRect(l-3f*density,t-3f*density,r+3f*density,b+3f*density,pixelPaint);
        pixelPaint.setColor(actual);
        canvas.drawRect(l,t,r,b,pixelPaint);
        pixelPaint.setColor(lighten(actual,30));
        canvas.drawRect(l,t,r,t+Math.min(6f*density,(b-t)*0.18f),pixelPaint);
        pixelPaint.setColor(darken(actual,30));
        canvas.drawRect(l,b-Math.min(6f*density,(b-t)*0.18f),r,b,pixelPaint);
        paint.setTextAlign(Paint.Align.CENTER);
        float textSize;
        if (text.length() <= 2) textSize=Math.min(24f*density, rect.height()*0.45f);
        else if (rect.width() < 85f*density) textSize=8.3f*density;
        else textSize=12.5f*density;
        paint.setTextSize(textSize);
        float maxTextWidth=Math.max(10f,rect.width()-12f*density);
        while(textSize>7f*density && paint.measureText(text)>maxTextWidth){
            textSize-=0.5f*density;
            paint.setTextSize(textSize);
        }
        paint.setColor(Color.WHITE);
        float baseline = (t+b)/2f-(paint.ascent()+paint.descent())/2f;
        canvas.drawText(text,(l+r)/2f,baseline,paint);
    }

    private void drawBanner(Canvas canvas) {
        if (banner.length()==0 || SystemClock.uptimeMillis()>bannerUntil) return;
        float l = boardLeft+cell*0.35f;
        float r = boardRight-cell*0.35f;
        float t = boardTop+cell*8.4f;
        float b = t+44f*density;
        pixelPaint.setColor(0xE629352F);
        canvas.drawRect(l,t,r,b,pixelPaint);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(banner.length()>18 ? 10.5f*density : 13f*density);
        paint.setColor(0xFFF6E184);
        float baseline=(t+b)/2f-(paint.ascent()+paint.descent())/2f;
        canvas.drawText(banner,(l+r)/2f,baseline,paint);
    }

    private void drawPauseOverlay(Canvas canvas) {
        pixelPaint.setColor(0xD20C1212);
        canvas.drawRect(0,0,getWidth(),getHeight(),pixelPaint);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(29f*density);
        paint.setColor(0xFFF6E8A6);
        canvas.drawText("遗迹探索已暂停",getWidth()/2f,getHeight()*0.32f,paint);
        drawButton(canvas,overlayButton1,"继续游戏",0xFF4E8447,false);
        drawButton(canvas,overlayButton2,"重新开始",0xFF8A5B2F,false);
        drawButton(canvas,overlayButton3,"设置",0xFF4E6870,false);
        drawButton(canvas,overlayButton4,"返回主菜单",0xFF684E75,false);
    }

    private void drawGameOver(Canvas canvas) {
        pixelPaint.setColor(0xE80D1111);
        canvas.drawRect(0,0,getWidth(),getHeight(),pixelPaint);
        float cx=getWidth()/2f;
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(28f*density);
        paint.setColor(0xFFEA684F);
        canvas.drawText("矿井封闭",cx,getHeight()*0.22f,paint);
        paint.setTextSize(13f*density);
        paint.setColor(0xFFF5E7AA);
        float y=getHeight()*0.275f;
        if (!scoreDetailsVisible) {
            canvas.drawText("总分 "+score+"    历史 "+Math.max(score,getModeHighScore()),cx,y,paint);
            canvas.drawText("起始 "+runStartScore+"    本局新增 "+Math.max(0,score-runStartScore),cx,y+25f*density,paint);
            canvas.drawText("等级 "+level+"    总消行 "+lines+"    最大 Combo ×"+maxCombo,cx,y+50f*density,paint);
        } else {
            canvas.drawText("消行 "+lineScore+"    连击 "+comboScore,cx,y,paint);
            canvas.drawText("无空洞 "+perfectScore+"    降落 "+(softDropPoints+hardDropPoints),cx,y+25f*density,paint);
            canvas.drawText("技能得分 "+skillScore+"    总分 "+score,cx,y+50f*density,paint);
        }
        drawButton(canvas,overlayButton1,"按开局设置再来",0xFF4E8447,false);
        drawButton(canvas,overlayButton2,"从本局分数继续",0xFF8A5B2F,false);
        drawButton(canvas,overlayButton3,scoreDetailsVisible?"返回统计":"计分明细",0xFF4E6870,false);
        drawButton(canvas,overlayButton4,"返回主菜单",0xFF684E75,false);
    }

    private void drawMenu(Canvas canvas) {
        drawMenuBackground(canvas, skin);
        drawHeader(canvas,"手机优化版");
        float cx=getWidth()/2f;
        float infoTop=safeTop+92f*density;
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(17f*density);
        paint.setColor(0xFFF7E7AA);
        canvas.drawText(MODE_NAMES[mode]+"模式",cx,infoTop,paint);
        paint.setTextSize(10.5f*density);
        paint.setColor(0xFFD8E6DC);
        drawCenteredWrappedText(canvas,MODE_DESCRIPTIONS[mode],cx,infoTop+23f*density,getWidth()*0.86f,15f*density);
        paint.setTextSize(12f*density);
        paint.setColor(0xFFFFD35C);
        canvas.drawText("最高分 "+getModeHighScore()+"  ·  遗迹碎片 "+totalFragments,cx,infoTop+59f*density,paint);
        paint.setTextSize(9.5f*density);
        paint.setColor(0xFFD8E6DC);
        canvas.drawText("棋盘："+BOARD_NAMES[selectedBoardPreset]+"  ·  技能组："+GameBalance.LOADOUT_NAMES[skillLoadout],cx,infoTop+79f*density,paint);

        float missionBottom=menuPrimary.top-10f*density;
        float missionTop=Math.max(infoTop+86f*density,missionBottom-50f*density);
        if(missionTop>missionBottom-24f*density)missionTop=missionBottom-34f*density;
        float missionLeft=safeLeft+14f*density;
        float missionRight=getWidth()-safeRight-14f*density;
        drawPanel(canvas,missionLeft,missionTop,missionRight,missionBottom);
        paint.setTextSize(9f*density);
        paint.setColor(0xFFE8F4E8);
        drawCenteredWrappedText(canvas,missions.menuDescription(),cx,missionTop+17f*density,missionRight-missionLeft-18f*density,13f*density);

        drawButton(canvas,menuPrimary,"开始探索",0xFF4E8447,false);
        drawButton(canvas,menuSecondary,"开局："+getStartSourceLabel(),0xFF8A5B2F,false);
        drawButton(canvas,menuThird,"模式："+MODE_NAMES[mode],0xFF526984,false);
        drawButton(canvas,menuFourth,"设置 · 皮肤 · 技能",0xFF4E6870,false);
        drawButton(canvas,menuFifth,"重新查看玩法说明",0xFF684E75,false);
    }

    private void drawMenuBackground(Canvas canvas, int previewSkin) {
        int oldSkin=skin;
        skin=previewSkin;
        drawWorldBackground(canvas);
        skin=oldSkin;
        pixelPaint.setColor(0x44101818);
        canvas.drawRect(0,0,getWidth(),getHeight(),pixelPaint);
    }

    private void drawSettings(Canvas canvas) {
        drawMenuBackground(canvas,skin);
        pixelPaint.setColor(0xAA101818);
        canvas.drawRect(0,0,getWidth(),getHeight(),pixelPaint);
        drawButton(canvas,backButton,"返回",0xFF4E6870,false);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(23f*density);
        paint.setColor(0xFFF7E7AA);
        canvas.drawText("设置",getWidth()/2f,safeTop+39f*density,paint);
        canvas.save();
        canvas.clipRect(contentClip);
        float rowH=46f*density;
        float y=contentClip.top-settingsScroll;
        drawSettingRow(canvas,y,rowH,"游戏模式",MODE_NAMES[mode]); y+=rowH;
        drawSettingRow(canvas,y,rowH,"模式说明",MODE_DESCRIPTIONS[mode]); y+=rowH;
        drawSettingRow(canvas,y,rowH,"开局分数",getStartSourceLabel()); y+=rowH;
        String boardValue = BOARD_NAMES[selectedBoardPreset];
        if (settingsReturnScreen == SCREEN_GAME && selectedBoardPreset != activeBoardPreset) boardValue += "（下局）";
        drawSettingRow(canvas,y,rowH,"棋盘规模",boardValue); y+=rowH;
        drawSettingRow(canvas,y,rowH,"音效",soundEnabled?"开":"关"); y+=rowH;
        drawSettingRow(canvas,y,rowH,"音效音量",soundVolume+"%"); y+=rowH;
        drawSettingRow(canvas,y,rowH,"背景音乐",musicEnabled?"开":"关"); y+=rowH;
        drawSettingRow(canvas,y,rowH,"音乐曲目",MUSIC_NAMES[musicTrack]); y+=rowH;
        drawSettingRow(canvas,y,rowH,"音乐音量",musicVolume+"%"); y+=rowH;
        drawSettingRow(canvas,y,rowH,"轻微震动",vibrationEnabled?"开":"关"); y+=rowH;
        drawSettingRow(canvas,y,rowH,"触控灵敏度",SENS_NAMES[sensitivity]); y+=rowH;
        drawSettingRow(canvas,y,rowH,"幽灵落点",ghostEnabled?"显示":"隐藏"); y+=rowH;
        drawSettingRow(canvas,y,rowH,"彩蛋动画",easterAnimations?"开":"关"); y+=rowH;
        drawSettingRow(canvas,y,rowH,"材质皮肤",SKIN_NAMES[skin]); y+=rowH;
        drawSettingRow(canvas,y,rowH,"色盲辅助纹理",colorBlindMode?"开":"关"); y+=rowH;
        drawSettingRow(canvas,y,rowH,"网格",gridEnabled?"显示":"隐藏"); y+=rowH;
        drawSettingRow(canvas,y,rowH,"未来 3 块",previewThree?"显示 3 块":"只显示 1 块"); y+=rowH;
        drawSettingRow(canvas,y,rowH,"技能配置",GameBalance.LOADOUT_NAMES[skillLoadout]); y+=rowH;
        drawSettingRow(canvas,y,rowH,"被动技能",passiveSkillSummary()); y+=rowH;
        drawSettingRow(canvas,y,rowH,"技能系统",skillsEnabled?"启用":"关闭"); y+=rowH;
        drawSettingRow(canvas,y,rowH,"重新查看玩法说明","打开"); y+=rowH;
        drawSettingRow(canvas,y,rowH,"重置最高分","需要二次确认"); y+=rowH;
        drawSettingRow(canvas,y,rowH,"重置所有存档","需要二次确认"); y+=rowH;
        canvas.restore();
    }

    private void drawSettingRow(Canvas canvas,float y,float h,String label,String value) {
        float l=contentClip.left;
        float r=contentClip.right;
        pixelPaint.setColor(0xDD263130);
        canvas.drawRect(l,y,r,y+h-3f*density,pixelPaint);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(12f*density);
        paint.setColor(0xFFF0EAD0);
        canvas.drawText(label,l+12f*density,y+h*0.58f,paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        float maxWidth=Math.max(60f*density,r-l-132f*density);
        float size=10.5f*density;
        paint.setTextSize(size);
        while(size>7.2f*density && paint.measureText(value)>maxWidth){
            size-=0.5f*density;
            paint.setTextSize(size);
        }
        String shown=value;
        while(shown.length()>3 && paint.measureText(shown)>maxWidth){
            shown=shown.substring(0,shown.length()-2);
        }
        if(!shown.equals(value))shown+="…";
        paint.setColor(0xFFFFD35C);
        canvas.drawText(shown,r-12f*density,y+h*0.58f,paint);
    }

    private String passiveSkillSummary(){
        StringBuilder b=new StringBuilder();
        if(isStabilizerPassiveUnlocked())b.append("稳固落地");
        if(isShieldUnlocked()){if(b.length()>0)b.append(" · ");b.append("遗迹护盾");}
        if(totalFragments>=24){if(b.length()>0)b.append(" · ");b.append("能量回收");}
        return b.length()==0?"尚未解锁":b.toString();
    }

    private boolean isStabilizerPassiveUnlocked(){
        return totalFragments>=8;
    }

    private void drawSkins(Canvas canvas) {
        drawMenuBackground(canvas,skin);
        pixelPaint.setColor(0xAA101818);
        canvas.drawRect(0,0,getWidth(),getHeight(),pixelPaint);
        drawButton(canvas,backButton,"返回",0xFF4E6870,false);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(22f*density);
        paint.setColor(0xFFF7E7AA);
        canvas.drawText("遗迹皮肤 · "+ThemeCatalog.count()+" 套",getWidth()/2f,safeTop+38f*density,paint);
        float l=safeLeft+12f*density;
        float r=getWidth()-safeRight-12f*density;
        float top=safeTop+68f*density;
        float bottom=getHeight()-safeBottom-10f*density;
        float cardH=78f*density;
        float gap=6f*density;
        canvas.save();
        canvas.clipRect(l,top,r,bottom);
        float y=top-skinScroll;
        for(int i=0;i<ThemeCatalog.count();i++){
            boolean unlocked=isSkinUnlocked(i);
            int card= i==skin?0xEE526E60:0xDD263130;
            pixelPaint.setColor(card);
            canvas.drawRect(l,y,r,y+cardH,pixelPaint);
            float preview=cardH-12f*density;
            drawSkinSwatches(canvas,i,l+7f*density,y+6f*density,l+7f*density+preview,y+cardH-6f*density);
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setTextSize(13f*density);
            paint.setColor(unlocked?0xFFF4ECD0:0xFF9EA8A4);
            canvas.drawText(SKIN_NAMES[i],l+preview+18f*density,y+cardH*0.40f,paint);
            paint.setTextSize(9f*density);
            paint.setColor(unlocked?0xFFFFD35C:0xFFB2B2B2);
            String state=i==skin?"当前使用":(unlocked?"已解锁 · 点击使用":"未解锁 · "+SKIN_UNLOCK[i]);
            float maxWidth=r-(l+preview+30f*density);
            while(state.length()>3 && paint.measureText(state)>maxWidth)state=state.substring(0,state.length()-2);
            canvas.drawText(state,l+preview+18f*density,y+cardH*0.72f,paint);
            y+=cardH+gap;
        }
        canvas.restore();
        if(ThemeCatalog.count()*(cardH+gap)>bottom-top){
            paint.setTextAlign(Paint.Align.RIGHT);
            paint.setTextSize(8.5f*density);
            paint.setColor(0xFFCAD8D2);
            canvas.drawText("上下滑动查看更多",r,bottom-3f*density,paint);
        }
    }

    private void drawSkinSwatches(Canvas canvas,int previewSkin,float l,float t,float r,float b){
        int old=skin;
        skin=previewSkin;
        float size=(r-l)/3f;
        for(int i=0;i<6;i++){
            drawBlock(canvas,l+(i%3)*size,t+(i/3)*size,size,Math.min(i*2,MATERIAL_COUNT-1),i%7,i,previewSkin,255);
        }
        skin=old;
    }

    private void drawTutorial(Canvas canvas) {
        drawMenuBackground(canvas,skin);
        pixelPaint.setColor(0xBB101818);
        canvas.drawRect(0,0,getWidth(),getHeight(),pixelPaint);
        float cx=getWidth()/2f;
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(15f*density);
        paint.setColor(0xFFFFD35C);
        canvas.drawText("新手引导 "+(tutorialPage+1)+" / "+TUTORIAL_TITLES.length,cx,getHeight()*0.20f,paint);
        paint.setTextSize(28f*density);
        paint.setColor(0xFFF7E7AA);
        canvas.drawText(TUTORIAL_TITLES[tutorialPage],cx,getHeight()*0.30f,paint);
        paint.setTextSize(13f*density);
        paint.setColor(0xFFD8E6DC);
        drawCenteredWrappedText(canvas,TUTORIAL_TEXT[tutorialPage],cx,getHeight()*0.39f,getWidth()*0.76f,22f*density);
        drawTutorialIcon(canvas,tutorialPage,cx,getHeight()*0.53f);
        drawButton(canvas,menuPrimary,tutorialPage==TUTORIAL_TITLES.length-1?"完成":"下一步",0xFF4E8447,false);
        drawButton(canvas,menuSecondary,"跳过并进入主菜单",0xFF684E75,false);
    }

    private void drawTutorialIcon(Canvas canvas,int page,float cx,float cy){
        tutorialIconRect.set(cx-74f*density,cy-42f*density,cx+74f*density,cy+42f*density);
        String text;
        switch(page){
            case 0:text="◀   ▶";break;
            case 1:text="↻";break;
            case 2:text="▼";break;
            case 3:text="按下 → 松手";break;
            case 4:text="触底后仍可移动";break;
            case 5:text="HOLD";break;
            case 6:text="技能组 1 / 2 / 3";break;
            case 7:text="任务 +1 碎片";break;
            default:text="Ⅱ";break;
        }
        drawButton(canvas,tutorialIconRect,text,0xFF526984,false);
    }

    private void drawCenteredWrappedText(Canvas canvas,String text,float cx,float y,float width,float lineHeight){
        int max=Math.max(8,(int)(width/(12f*density)));
        int start=0;
        int line=0;
        while(start<text.length()){
            int end=Math.min(text.length(),start+max);
            canvas.drawText(text.substring(start,end),cx,y+line*lineHeight,paint);
            start=end;
            line++;
        }
    }

    private void drawConfirm(Canvas canvas) {
        pixelPaint.setColor(0xDD080C0D);
        canvas.drawRect(0,0,getWidth(),getHeight(),pixelPaint);
        float cx=getWidth()/2f;
        float top=getHeight()*0.37f;
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(22f*density);
        paint.setColor(0xFFF7E7AA);
        String title;
        String detail;
        if(confirmAction==CONFIRM_RESTART){title="重新开始？";detail="当前局面和本局统计会被清空";}
        else if(confirmAction==CONFIRM_RESET_SCORES){title="重置全部最高分？";detail="三个模式的最高分都会归零";}
        else if(confirmAction==CONFIRM_RESET_ALL){title="重置所有存档？";detail="碎片、皮肤、彩蛋和设置都会恢复默认";}
        else {title="返回主菜单？";detail="本局进度不会保存";}
        canvas.drawText(title,cx,top,paint);
        paint.setTextSize(11f*density);
        paint.setColor(0xFFD8E6DC);
        canvas.drawText(detail,cx,top+35f*density,paint);
        drawButton(canvas,confirmYes,"确认",0xFF9A4D43,false);
        drawButton(canvas,confirmNo,"取消",0xFF4E6870,false);
    }

    private void drawScoreDetails() {
        scoreDetailsVisible=!scoreDetailsVisible;
        audio.play(AudioEngine.CLICK);
    }

    private void finishTutorial() {
        prefs.edit().putBoolean("tutorial_seen",true).apply();
        screen=tutorialReturnScreen;
        tutorialPage=0;
        audio.play(AudioEngine.CLICK);
        applyAudioSettings();
        invalidate();
    }

    private void handleSettingTap(float x,float y){
        if(backButton.contains(x,y)){
            screen=settingsReturnScreen;
            settingsScroll=0f;
            applyAudioSettings();
            audio.play(AudioEngine.CLICK);
            return;
        }
        if(!contentClip.contains(x,y))return;
        float rowH=46f*density;
        int index=(int)((y-contentClip.top+settingsScroll)/rowH);
        switch(index){
            case 0:
                mode=(mode+1)%3;missions.reset(mode);saveSetting("game_mode",mode);break;
            case 1:
                showBanner(MODE_DESCRIPTIONS[mode],1200L);break;
            case 2:
                startScoreSource=(startScoreSource+1)%3;saveSetting("start_score_source",startScoreSource);break;
            case 3:
                selectedBoardPreset=(selectedBoardPreset+1)%BOARD_NAMES.length;
                saveSetting("board_preset",selectedBoardPreset);
                if(settingsReturnScreen==SCREEN_GAME && !gameOver){
                    showBanner("棋盘规模将在下一局生效",1100L);
                }else{
                    activeBoardPreset=selectedBoardPreset;
                    boardCols=BOARD_COLS[activeBoardPreset];
                    boardRows=BOARD_ROWS[activeBoardPreset];
                    updateLayout(getWidth(),getHeight());
                }
                break;
            case 4:
                soundEnabled=!soundEnabled;saveSetting("sound_enabled_v12",soundEnabled);break;
            case 5:
                soundVolume=nextVolume(soundVolume);saveSetting("sound_volume",soundVolume);break;
            case 6:
                musicEnabled=!musicEnabled;saveSetting("music_enabled",musicEnabled);break;
            case 7:
                musicTrack=(musicTrack+1)%MUSIC_NAMES.length;saveSetting("music_track",musicTrack);
                showBanner("正在播放："+MUSIC_NAMES[musicTrack],900L);break;
            case 8:
                musicVolume=nextVolume(musicVolume);saveSetting("music_volume",musicVolume);break;
            case 9:
                vibrationEnabled=!vibrationEnabled;saveSetting("vibration_enabled",vibrationEnabled);break;
            case 10:
                sensitivity=(sensitivity+1)%3;saveSetting("touch_sensitivity",sensitivity);break;
            case 11:
                ghostEnabled=!ghostEnabled;saveSetting("ghost_enabled",ghostEnabled);break;
            case 12:
                easterAnimations=!easterAnimations;saveSetting("easter_animations",easterAnimations);break;
            case 13:
                screen=SCREEN_SKINS;break;
            case 14:
                colorBlindMode=!colorBlindMode;saveSetting("color_blind",colorBlindMode);break;
            case 15:
                gridEnabled=!gridEnabled;saveSetting("grid_enabled",gridEnabled);break;
            case 16:
                previewThree=!previewThree;saveSetting("preview_three",previewThree);break;
            case 17:
                skillLoadout=(skillLoadout+1)%GameBalance.SKILL_LOADOUTS.length;
                saveSetting("skill_loadout",skillLoadout);
                showBanner("技能配置："+GameBalance.LOADOUT_NAMES[skillLoadout],1000L);break;
            case 18:
                showBanner("被动技能："+passiveSkillSummary(),1400L);break;
            case 19:
                skillsEnabled=!skillsEnabled;saveSetting("skills_enabled",skillsEnabled);break;
            case 20:
                tutorialPage=0;tutorialReturnScreen=SCREEN_SETTINGS;screen=SCREEN_TUTORIAL;break;
            case 21:
                confirmAction=CONFIRM_RESET_SCORES;break;
            case 22:
                confirmAction=CONFIRM_RESET_ALL;break;
            default:break;
        }
        applyAudioSettings();
        audio.play(AudioEngine.CLICK);
    }

    private int nextVolume(int value){
        if(value<25)return 25;
        if(value<50)return 50;
        if(value<75)return 75;
        if(value<100)return 100;
        return 0;
    }

    private void handleSkinTap(float x,float y){
        if(backButton.contains(x,y)){
            screen=SCREEN_SETTINGS;
            applyAudioSettings();
            audio.play(AudioEngine.CLICK);
            return;
        }
        float top=safeTop+68f*density;
        float cardH=78f*density;
        float gap=6f*density;
        int index=(int)((y-top+skinScroll)/(cardH+gap));
        if(index<0||index>=ThemeCatalog.count())return;
        if(!isSkinUnlocked(index)){
            showBanner("解锁条件："+SKIN_UNLOCK[index],1200L);
            audio.play(AudioEngine.CLICK,0.45f);
            return;
        }
        skin=index;
        saveSetting("skin",skin);
        showBanner("已切换："+SKIN_NAMES[skin],900L);
        audio.play(AudioEngine.CLICK);
    }

    private void handleConfirm(boolean yes){
        if(!yes){confirmAction=CONFIRM_NONE;audio.play(AudioEngine.CLICK);return;}
        int action=confirmAction;
        confirmAction=CONFIRM_NONE;
        if(action==CONFIRM_RESTART){startNewGameWithProgress(runStartScore,runStartLines);}
        else if(action==CONFIRM_MENU){screen=SCREEN_MENU;paused=false;gameOver=false;applyAudioSettings();}
        else if(action==CONFIRM_RESET_SCORES){
            highCasual=highStandard=highExtreme=0;
            highLinesCasual=highLinesStandard=highLinesExtreme=0;
            prefs.edit().putInt("high_score_casual",0).putInt("high_score_standard",0).putInt("high_score_extreme",0)
                    .putInt("high_score",0).putInt("high_lines_casual",0).putInt("high_lines_standard",0)
                    .putInt("high_lines_extreme",0).apply();
            showBanner("三个模式最高分已重置",1100L);
        }else if(action==CONFIRM_RESET_ALL){
            prefs.edit().clear().putInt("save_schema",SAVE_SCHEMA).apply();
            loadPreferences();
            missions.reset(mode);
            totalFragments=0;unlockedSkins=1;skin=0;completedMissionCount=0;
            updateLayout(getWidth(),getHeight());
            applyAudioSettings();
            showBanner("所有存档已重置",1100L);
        }
        audio.play(AudioEngine.CLICK);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action=event.getActionMasked();
        float x=event.getX();
        float y=event.getY();
        lastTouchX=x;
        lastTouchY=y;
        if(action==MotionEvent.ACTION_DOWN){
            touchDownX=x;touchDownY=y;
            draggingSettings=false;
            if(confirmAction!=CONFIRM_NONE)return true;
            if(screen==SCREEN_MENU){
                if(menuPrimary.contains(x,y))startNewGame();
                else if(menuSecondary.contains(x,y)){startScoreSource=(startScoreSource+1)%3;saveSetting("start_score_source",startScoreSource);audio.play(AudioEngine.CLICK);invalidate();}
                else if(menuThird.contains(x,y)){mode=(mode+1)%3;missions.reset(mode);saveSetting("game_mode",mode);audio.play(AudioEngine.CLICK);invalidate();}
                else if(menuFourth.contains(x,y)){settingsReturnScreen=SCREEN_MENU;screen=SCREEN_SETTINGS;settingsScroll=0f;audio.play(AudioEngine.CLICK);invalidate();}
                else if(menuFifth.contains(x,y)){tutorialPage=0;tutorialReturnScreen=SCREEN_MENU;screen=SCREEN_TUTORIAL;audio.play(AudioEngine.CLICK);invalidate();}
                return true;
            }
            if(screen==SCREEN_TUTORIAL){
                if(menuPrimary.contains(x,y)){
                    if(tutorialPage<TUTORIAL_TITLES.length-1)tutorialPage++;else finishTutorial();
                    audio.play(AudioEngine.CLICK);invalidate();
                }else if(menuSecondary.contains(x,y))finishTutorial();
                return true;
            }
            if(screen==SCREEN_SETTINGS){
                settingsDownScroll=settingsScroll;
                return true;
            }
            if(screen==SCREEN_SKINS){
                skinDownScroll=skinScroll;
                draggingSkins=false;
                return true;
            }
            if(screen!=SCREEN_GAME)return true;

            if(gameOver){
                if(overlayButton1.contains(x,y))startNewGame();
                else if(overlayButton2.contains(x,y))startNewGame(START_CURRENT,score,lines);
                else if(overlayButton3.contains(x,y)){drawScoreDetails();invalidate();}
                else if(overlayButton4.contains(x,y)){screen=SCREEN_MENU;applyAudioSettings();audio.play(AudioEngine.CLICK);invalidate();}
                return true;
            }
            if(paused){
                if(overlayButton1.contains(x,y))setPaused(false);
                else if(overlayButton2.contains(x,y))confirmAction=CONFIRM_RESTART;
                else if(overlayButton3.contains(x,y)){settingsReturnScreen=SCREEN_GAME;screen=SCREEN_SETTINGS;settingsScroll=0f;}
                else if(overlayButton4.contains(x,y))confirmAction=CONFIRM_MENU;
                invalidate();return true;
            }
            if(settingsButton.contains(x,y)){setPaused(true);settingsReturnScreen=SCREEN_GAME;screen=SCREEN_SETTINGS;settingsScroll=0f;invalidate();return true;}
            for(int i=0;i<3;i++){
                if(skillButtons[i].contains(x,y)){
                    pressedSkill=i;skillPressAt=SystemClock.uptimeMillis();skillLongPressShown=false;invalidate();return true;
                }
            }
            if(dropButton.contains(x,y)){
                hardDropArmed=true;activeControl=CONTROL_DROP;lightHaptic();invalidate();return true;
            }
            if(leftButton.contains(x,y)){beginRepeat(CONTROL_LEFT);move(-1,false);return true;}
            if(rightButton.contains(x,y)){beginRepeat(CONTROL_RIGHT);move(1,false);return true;}
            if(downButton.contains(x,y)){beginRepeat(CONTROL_DOWN);stepDown(true);return true;}
            if(rotateButton.contains(x,y)){rotatePiece();return true;}
            if(holdButton.contains(x,y)){holdPiece();return true;}
            if(pauseButton.contains(x,y)){setPaused(true);return true;}
            if(boardRect.contains(x,y)){rotatePiece();return true;}
            handleLegacyTitleTap(x,y);
            invalidate();return true;
        }
        if(action==MotionEvent.ACTION_MOVE){
            if(confirmAction!=CONFIRM_NONE)return true;
            if(screen==SCREEN_SETTINGS){
                float dy=y-touchDownY;
                if(Math.abs(dy)>7f*density)draggingSettings=true;
                float maxScroll=Math.max(0f,SETTINGS_ROW_COUNT*46f*density-contentClip.height());
                settingsScroll=Math.max(0f,Math.min(maxScroll,settingsDownScroll-dy));
                invalidate();return true;
            }
            if(screen==SCREEN_SKINS){
                float dy=y-touchDownY;
                if(Math.abs(dy)>7f*density)draggingSkins=true;
                float top=safeTop+68f*density;
                float bottom=getHeight()-safeBottom-10f*density;
                float content=ThemeCatalog.count()*(78f*density+6f*density);
                float maxScroll=Math.max(0f,content-(bottom-top));
                skinScroll=Math.max(0f,Math.min(maxScroll,skinDownScroll-dy));
                invalidate();return true;
            }
            if(screen==SCREEN_GAME){
                if(hardDropArmed&&!dropButton.contains(x,y)){hardDropArmed=false;activeControl=CONTROL_NONE;invalidate();}
                if(activeControl==CONTROL_LEFT&&!leftButton.contains(x,y))activeControl=CONTROL_NONE;
                if(activeControl==CONTROL_RIGHT&&!rightButton.contains(x,y))activeControl=CONTROL_NONE;
                if(activeControl==CONTROL_DOWN&&!downButton.contains(x,y))activeControl=CONTROL_NONE;
                if(pressedSkill>=0&&!skillButtons[pressedSkill].contains(x,y)){pressedSkill=-1;invalidate();}
            }
            return true;
        }
        if(action==MotionEvent.ACTION_UP){
            if(confirmAction!=CONFIRM_NONE){
                if(confirmYes.contains(x,y))handleConfirm(true);
                else if(confirmNo.contains(x,y))handleConfirm(false);
                invalidate();return true;
            }
            if(screen==SCREEN_SKINS){
                if(!draggingSkins)handleSkinTap(x,y);
                invalidate();return true;
            }
            if(screen==SCREEN_SETTINGS){
                if(!draggingSettings)handleSettingTap(x,y);
                invalidate();return true;
            }
            if(screen==SCREEN_GAME){
                if(pressedSkill>=0){
                    int slot=pressedSkill;
                    boolean inside=skillButtons[slot].contains(x,y);
                    pressedSkill=-1;
                    if(inside&&!skillLongPressShown)useSkill(skillForSlot(slot));
                    invalidate();return true;
                }
                if(hardDropArmed){
                    float dx=x-touchDownX;float dy=y-touchDownY;
                    float limit=Math.min(dropButton.width(),dropButton.height())*0.56f;
                    boolean safe=dropButton.contains(x,y)&&dx*dx+dy*dy<limit*limit;
                    hardDropArmed=false;activeControl=CONTROL_NONE;
                    if(safe)hardDrop();
                    invalidate();return true;
                }
                activeControl=CONTROL_NONE;
            }
            return true;
        }
        if(action==MotionEvent.ACTION_CANCEL){cancelActiveTouch();invalidate();return true;}
        return true;
    }

    private void handleLegacyTitleTap(float x,float y){
        if(y>safeTop+65f*density||x<getWidth()*0.22f||x>getWidth()*0.78f)return;
        long now=SystemClock.uptimeMillis();
        if(now-titleTapWindow>3000L)titleTapCount=0;
        titleTapWindow=now;titleTapCount++;
        if(titleTapCount>=5){
            titleTapCount=0;
            if(highStandard<25000){
                highStandard=25000;
                highLinesStandard=estimateLinesForLegacyScore(25000);
                prefs.edit().putInt("high_score_standard",25000).putInt("high_score",25000)
                        .putInt("high_lines_standard",highLinesStandard).putBoolean("egg_25000_seen",true).apply();
                showBanner("旧版矿工档案已恢复：25000",1700L);
            }else showBanner("25000 分矿工档案已确认",1000L);
            audio.play(AudioEngine.EGG);
        }
    }

    private void beginRepeat(int control){
        activeControl=control;
        long now=SystemClock.uptimeMillis();
        repeatAt=now+repeatDelay();
    }

    private long repeatDelay(){
        return sensitivity==SENS_LOW?235L:(sensitivity==SENS_HIGH?125L:175L);
    }

    private long repeatInterval(){
        return sensitivity==SENS_LOW?112L:(sensitivity==SENS_HIGH?54L:80L);
    }

    private void updateHeldControl(long now){
        if(activeControl==CONTROL_NONE||activeControl==CONTROL_DROP||!canOperate())return;
        if(now<repeatAt)return;
        if(activeControl==CONTROL_LEFT)move(-1,true);
        else if(activeControl==CONTROL_RIGHT)move(1,true);
        else if(activeControl==CONTROL_DOWN)stepDown(true);
        repeatAt=now+repeatInterval();
    }

    private void updateSkillLongPress(long now){
        if(pressedSkill<0||skillLongPressShown||now-skillPressAt<550L)return;
        skillLongPressShown=true;
        int skill=skillForSlot(pressedSkill);
        showBanner(GameBalance.SKILL_NAMES[skill]+"："+GameBalance.skillDescription(skill),2000L);
        lightHaptic();
    }

    private void cancelActiveTouch(){
        activeControl=CONTROL_NONE;
        hardDropArmed=false;
        pressedSkill=-1;
    }

    private void spawnRowParticles(int row,int color){
        float y=boardTop+(row+0.5f)*cell;
        for(int c=0;c<boardCols;c++)spawnBurst(boardLeft+(c+0.5f)*cell,y,color,3);
    }

    private void spawnBurst(float x,float y,int color,int count){
        long now=SystemClock.uptimeMillis();
        for(int i=0;i<count;i++){
            Particle p=particles[particleCursor++%particles.length];
            double angle=random.nextDouble()*Math.PI*2.0;
            float speed=(35f+random.nextFloat()*95f)*density;
            p.x=x;p.y=y;p.vx=(float)Math.cos(angle)*speed;p.vy=(float)Math.sin(angle)*speed-35f*density;
            p.color=color;p.start=now;p.end=now+420L+random.nextInt(280);p.size=(2f+random.nextFloat()*3f)*density;
        }
    }

    private void updateParticles(long now){
        for(Particle p:particles){
            if(p.end<=now||p.start==0L)continue;
            float dt=0.016f;
            p.x+=p.vx*dt;p.y+=p.vy*dt;p.vy+=190f*density*dt;
        }
    }

    private void drawParticles(Canvas canvas){
        long now=SystemClock.uptimeMillis();
        for(Particle p:particles){
            if(p.end<=now||p.start==0L)continue;
            float ratio=(p.end-now)/(float)(p.end-p.start);
            pixelPaint.setColor(withAlpha(p.color,(int)(255*ratio)));
            canvas.drawRect(p.x-p.size/2f,p.y-p.size/2f,p.x+p.size/2f,p.y+p.size/2f,pixelPaint);
        }
    }

    private static final class Particle{
        float x,y,vx,vy,size;
        int color;
        long start,end;
    }
}
