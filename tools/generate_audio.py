#!/usr/bin/env python3
"""Generate the offline original-style WAV assets used by Block Mine.

The sounds are synthesized from simple waveforms and do not depend on
third-party music, samples, fonts, or network services.
"""
from pathlib import Path
import math
import random
import struct
import wave

ROOT = Path(__file__).resolve().parents[1]
RAW = ROOT / "app/src/main/res/raw"
MUSIC = ROOT / "app/src/main/assets/music"
RAW.mkdir(parents=True, exist_ok=True)
MUSIC.mkdir(parents=True, exist_ok=True)

def write_wav(path, samples, rate):
    samples = [max(-32767, min(32767, int(v))) for v in samples]
    with wave.open(str(path), "wb") as out:
        out.setnchannels(1)
        out.setsampwidth(2)
        out.setframerate(rate)
        out.writeframes(struct.pack("<%dh" % len(samples), *samples))

def envelope(t, duration, attack=0.01, release=0.08):
    a = min(1.0, t / max(attack, 1e-4))
    r = min(1.0, max(0.0, duration - t) / max(release, 1e-4))
    return a * r

def tone(freq, duration, volume=0.2, rate=22050, sweep=0.0, square=False):
    total = int(rate * duration)
    phase = 0.0
    result = []
    for i in range(total):
        t = i / rate
        current = freq + sweep * (t / max(duration, 1e-6))
        phase += 2.0 * math.pi * current / rate
        wave_value = (1.0 if math.sin(phase) >= 0 else -1.0) if square else math.sin(phase)
        result.append(32767 * volume * envelope(t, duration) * wave_value)
    return result

def sequence(notes, rate=22050, volume=0.18, square=False):
    result = []
    for freq, duration in notes:
        result.extend(tone(freq, duration, volume, rate, square=square))
    return result

def mix(*tracks):
    length = max(map(len, tracks))
    out = [0.0] * length
    for track in tracks:
        for i, value in enumerate(track):
            out[i] += value
    return out

# Short effects.
effects = {
    "sfx_move.wav": tone(400, 0.055, 0.16, square=True),
    "sfx_rotate.wav": tone(610, 0.115, 0.14, sweep=170),
    "sfx_soft_drop.wav": tone(210, 0.045, 0.13, sweep=-70),
    "sfx_hard_drop.wav": mix(tone(95, 0.25, 0.22, sweep=-50), tone(55, 0.25, 0.12, square=True)),
    "sfx_lock.wav": mix(tone(180, 0.15, 0.17, sweep=-80), tone(90, 0.15, 0.10, square=True)),
    "sfx_line.wav": sequence([(520, .06), (660, .12)], volume=.15),
    "sfx_multi_line.wav": sequence([(520, .07), (660, .07), (780, .12)], volume=.16),
    "sfx_four_line.wav": sequence([(440, .07), (660, .07), (880, .20)], volume=.17),
    "sfx_skill.wav": sequence([(520, .10), (780, .10), (1040, .20)], volume=.15),
    "sfx_ready.wav": tone(900, .21, .22, sweep=130),
    "sfx_egg.wav": sequence([(523, .13), (659, .13), (784, .27)], volume=.14),
    "sfx_pause.wav": tone(290, .17, .13, sweep=-90),
    "sfx_game_over.wav": sequence([(330, .16), (247, .16), (196, .16), (131, .22)], volume=.17),
    "sfx_click.wav": tone(600, .045, .12, square=True),
    "sfx_hold.wav": tone(500, .17, .14, sweep=180),
}
for name, samples in effects.items():
    write_wav(RAW / name, samples, 22050)

NOTES = {
    "C2": 65.41, "D2": 73.42, "E2": 82.41, "F2": 87.31, "G2": 98.00, "A2": 110.00,
    "C3": 130.81, "D3": 146.83, "E3": 164.81, "F3": 174.61, "G3": 196.00,
    "A3": 220.00, "B3": 246.94, "C4": 261.63, "D4": 293.66, "E4": 329.63,
    "F4": 349.23, "G4": 392.00, "A4": 440.00, "B4": 493.88,
}

def music_track(path, bass_names, lead_names, seconds, pulse=0.5, seed=1):
    rate = 11025
    total = int(rate * seconds)
    out = [0.0] * total
    rng = random.Random(seed)
    bass = [NOTES[n] for n in bass_names]
    lead = [NOTES[n] for n in lead_names]
    for i in range(total):
        t = i / rate
        beat = int(t / pulse)
        local = (t % pulse) / pulse
        b = bass[beat % len(bass)]
        l = lead[(beat // 2) % len(lead)]
        fade = min(1.0, t / .7, (seconds - t) / .7)
        gate = .45 + .55 * math.exp(-5.0 * local)
        bass_wave = math.sin(2 * math.pi * b * t) + .35 * math.sin(2 * math.pi * b * 2 * t)
        lead_wave = math.sin(2 * math.pi * l * t + .2 * math.sin(2 * math.pi * .25 * t))
        shimmer = math.sin(2 * math.pi * (l * 2) * t) * (.12 if beat % 4 == 0 else .04)
        noise = (rng.random() * 2 - 1) * .012
        out[i] = 32767 * fade * (0.12 * bass_wave + 0.10 * gate * lead_wave + shimmer + noise)
    write_wav(MUSIC / path, out, rate)

music_track("bgm_ruins_echo.wav", ["A2", "E2", "G2", "D2"], ["E3", "A3", "G3", "D3"], 32, .50, 11)
music_track("bgm_crystal_pulse.wav", ["D2", "A2", "E2", "G2"], ["D4", "A3", "E4", "A4"], 24, .375, 22)
music_track("bgm_lava_depth.wav", ["A2", "F2", "G2", "E2"], ["A3", "C4", "D4", "E4"], 32, .50, 33)
music_track("bgm_sky_relic.wav", ["G2", "D3", "A2", "E3"], ["G4", "B4", "A4", "E4"], 24, .375, 44)
