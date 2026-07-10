package com.ame.blocktetris;

/**
 * 集中管理游戏平衡参数，避免速度、技能和操作容错散落在主 View 中。
 * v1.4 的目标是让高分阶段在触屏设备上仍然可操作，而不是追求主机版极限重力。
 */
final class GameBalance {
    static final int MODE_CASUAL = 0;
    static final int MODE_STANDARD = 1;
    static final int MODE_EXTREME = 2;

    static final int SKILL_PICKAXE = 0;
    static final int SKILL_FREEZE = 1;
    static final int SKILL_REFORGE = 2;
    static final int SKILL_STABILIZER = 3;
    static final int SKILL_SHOCKWAVE = 4;

    static final int[][] SKILL_LOADOUTS = {
            {SKILL_PICKAXE, SKILL_FREEZE, SKILL_REFORGE},
            {SKILL_STABILIZER, SKILL_FREEZE, SKILL_REFORGE},
            {SKILL_PICKAXE, SKILL_SHOCKWAVE, SKILL_REFORGE}
    };

    static final String[] LOADOUT_NAMES = {"均衡探索", "生存稳速", "爆破采掘"};
    static final String[] SKILL_NAMES = {"矿镐", "冻结", "重铸", "稳速", "震荡"};
    static final int[] SKILL_COSTS = {100, 75, 45, 60, 85};

    private static final long[] STANDARD_INTERVALS_MS = {
            900, 825, 755, 690, 630, 575, 525, 480, 440, 405,
            375, 350, 328, 308, 290, 274, 260, 248, 238, 228,
            220, 213, 206, 200, 195, 190, 186, 182, 178, 175
    };

    private GameBalance() {}

    static long dropIntervalMs(int level, int mode, boolean slowed) {
        int index = Math.max(0, level - 1);
        long base;
        if (index < STANDARD_INTERVALS_MS.length) {
            base = STANDARD_INTERVALS_MS[index];
        } else {
            // 30 级以后只缓慢趋近原始下限；各模式再使用自己的手机操作封顶值。
            // 标准模式会在 165ms 封顶，极限模式可继续缓慢逼近 125ms，不会突然跳速。
            base = Math.max(150L, 175L - (index - 29L) * 2L);
        }
        if (mode == MODE_CASUAL) base = Math.round(base * 1.22f);
        else if (mode == MODE_EXTREME) base = Math.round(base * 0.82f);

        long floor = mode == MODE_CASUAL ? 210L : (mode == MODE_EXTREME ? 125L : 165L);
        base = Math.max(floor, base);
        if (slowed) base = Math.round(base * 1.65f);
        return base;
    }

    static long lockDelayMs(int level, int mode, boolean stabilizerUnlocked) {
        long delay = 500L - Math.min(140L, Math.max(0, level - 1) * 5L);
        if (mode == MODE_CASUAL) delay += 90L;
        else if (mode == MODE_EXTREME) delay -= 70L;
        if (stabilizerUnlocked) delay += 80L;
        return Math.max(280L, Math.min(650L, delay));
    }

    static int maxLockResets(int mode) {
        return mode == MODE_EXTREME ? 7 : 10;
    }

    static long skillCooldownMs(int skill) {
        switch (skill) {
            case SKILL_FREEZE: return 17000L;
            case SKILL_REFORGE: return 9000L;
            case SKILL_STABILIZER: return 14000L;
            case SKILL_SHOCKWAVE: return 12000L;
            default: return 0L;
        }
    }

    static String skillDescription(int skill) {
        switch (skill) {
            case SKILL_PICKAXE: return "100% 能量，清除最底层；空底层不扣能量";
            case SKILL_FREEZE: return "75% 能量，停止自动下落 5 秒，不能叠加";
            case SKILL_REFORGE: return "45% 能量，变为不同形状；无安全位置则取消";
            case SKILL_STABILIZER: return "60% 能量，8 秒内明显降低自动下落速度";
            case SKILL_SHOCKWAVE: return "85% 能量，从最高堆叠处震碎最多 10 个方块";
            default: return "";
        }
    }
}
