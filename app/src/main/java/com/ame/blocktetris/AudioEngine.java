package com.ame.blocktetris;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.SystemClock;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

final class AudioEngine {
    static final int MOVE = 1;
    static final int ROTATE = 2;
    static final int SOFT_DROP = 3;
    static final int HARD_DROP = 4;
    static final int LOCK = 5;
    static final int LINE = 6;
    static final int MULTI_LINE = 7;
    static final int FOUR_LINE = 8;
    static final int SKILL = 9;
    static final int READY = 10;
    static final int EGG = 11;
    static final int PAUSE = 12;
    static final int GAME_OVER = 13;
    static final int CLICK = 14;
    static final int HOLD = 15;

    private final SoundPool pool;
    private final Map<Integer, Integer> sounds = new HashMap<>();
    private final Context appContext;
    private final String[] musicAssets = {
            "music/bgm_ruins_echo.wav",
            "music/bgm_crystal_pulse.wav",
            "music/bgm_lava_depth.wav",
            "music/bgm_sky_relic.wav"
    };
    private MediaPlayer music;
    private int musicTrack = -1;
    private boolean soundEnabled = true;
    private boolean musicEnabled = true;
    private boolean gameActive;
    private float soundVolume = 0.65f;
    private float musicVolume = 0.22f;
    private long lastMoveSound;
    private boolean released;

    AudioEngine(Context context) {
        appContext = context.getApplicationContext();
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        pool = new SoundPool.Builder().setMaxStreams(5).setAudioAttributes(attributes).build();
        sounds.put(MOVE, pool.load(context, R.raw.sfx_move, 1));
        sounds.put(ROTATE, pool.load(context, R.raw.sfx_rotate, 1));
        sounds.put(SOFT_DROP, pool.load(context, R.raw.sfx_soft_drop, 1));
        sounds.put(HARD_DROP, pool.load(context, R.raw.sfx_hard_drop, 1));
        sounds.put(LOCK, pool.load(context, R.raw.sfx_lock, 1));
        sounds.put(LINE, pool.load(context, R.raw.sfx_line, 1));
        sounds.put(MULTI_LINE, pool.load(context, R.raw.sfx_multi_line, 1));
        sounds.put(FOUR_LINE, pool.load(context, R.raw.sfx_four_line, 1));
        sounds.put(SKILL, pool.load(context, R.raw.sfx_skill, 1));
        sounds.put(READY, pool.load(context, R.raw.sfx_ready, 1));
        sounds.put(EGG, pool.load(context, R.raw.sfx_egg, 1));
        sounds.put(PAUSE, pool.load(context, R.raw.sfx_pause, 1));
        sounds.put(GAME_OVER, pool.load(context, R.raw.sfx_game_over, 1));
        sounds.put(CLICK, pool.load(context, R.raw.sfx_click, 1));
        sounds.put(HOLD, pool.load(context, R.raw.sfx_hold, 1));
        loadMusicTrack(0);
    }

    void configure(boolean soundEnabled, int soundPercent, boolean musicEnabled, int musicPercent, int requestedTrack) {
        this.soundEnabled = soundEnabled;
        this.musicEnabled = musicEnabled;
        soundVolume = Math.max(0f, Math.min(1f, soundPercent / 100f));
        musicVolume = Math.max(0f, Math.min(1f, musicPercent / 100f));
        int normalizedTrack = Math.max(0, Math.min(musicAssets.length - 1, requestedTrack));
        if (normalizedTrack != musicTrack || music == null) loadMusicTrack(normalizedTrack);
        if (music != null) music.setVolume(musicVolume, musicVolume);
        updateMusic();
    }

    private void loadMusicTrack(int track) {
        if (released) return;
        if (music != null) {
            try {
                if (music.isPlaying()) music.stop();
            } catch (IllegalStateException ignored) {
            }
            try { music.release(); } catch (RuntimeException ignored) { }
            music = null;
        }
        musicTrack = track;
        AssetFileDescriptor afd = null;
        try {
            afd = appContext.getAssets().openFd(musicAssets[track]);
            music = new MediaPlayer();
            music.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build());
            music.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            music.setLooping(true);
            music.setVolume(musicVolume, musicVolume);
            music.prepare();
        } catch (IOException | RuntimeException e) {
            if (music != null) {
                try { music.release(); } catch (RuntimeException ignored) { }
                music = null;
            }
        } finally {
            if (afd != null) {
                try { afd.close(); } catch (IOException ignored) { }
            }
        }
    }

    void setGameActive(boolean active) {
        gameActive = active;
        updateMusic();
    }

    void play(int sound) {
        play(sound, 1f);
    }

    void play(int sound, float relativeVolume) {
        if (released || !soundEnabled || soundVolume <= 0f) return;
        if (sound == MOVE || sound == SOFT_DROP) {
            long now = SystemClock.uptimeMillis();
            if (now - lastMoveSound < 58L) return;
            lastMoveSound = now;
        }
        Integer id = sounds.get(sound);
        if (id == null) return;
        float volume = soundVolume * Math.max(0f, Math.min(1f, relativeVolume));
        pool.play(id, volume, volume, 1, 0, 1f);
    }

    void pauseForBackground() {
        if (released) return;
        pool.autoPause();
        if (music != null && music.isPlaying()) music.pause();
    }

    void resumeFromBackground() {
        if (released) return;
        pool.autoResume();
        updateMusic();
    }

    private void updateMusic() {
        if (released || music == null) return;
        boolean shouldPlay = musicEnabled && musicVolume > 0f && gameActive;
        try {
            if (shouldPlay && !music.isPlaying()) music.start();
            else if (!shouldPlay && music.isPlaying()) music.pause();
        } catch (IllegalStateException ignored) {
        }
    }

    void release() {
        if (released) return;
        released = true;
        pool.release();
        if (music != null) {
            try { music.release(); } catch (RuntimeException ignored) { }
            music = null;
        }
        sounds.clear();
    }
}
