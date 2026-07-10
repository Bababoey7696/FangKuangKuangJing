package com.ame.blocktetris;

/**
 * 单局任务只记录本局增量，不受“从历史分数继续”影响。
 * 每完成一项任务返回 1，调用方据此发放遗迹碎片。
 */
final class RunMissions {
    private int lineTarget;
    private int comboTarget;
    private int clearedLines;
    private int bestCombo;
    private boolean usedSkill;
    private boolean lineRewarded;
    private boolean comboRewarded;
    private boolean skillRewarded;

    void reset(int mode) {
        lineTarget = mode == GameBalance.MODE_CASUAL ? 8 : (mode == GameBalance.MODE_EXTREME ? 15 : 12);
        comboTarget = mode == GameBalance.MODE_CASUAL ? 2 : (mode == GameBalance.MODE_EXTREME ? 4 : 3);
        clearedLines = 0;
        bestCombo = 0;
        usedSkill = false;
        lineRewarded = false;
        comboRewarded = false;
        skillRewarded = false;
    }

    int onLinesCleared(int amount, int combo) {
        clearedLines += Math.max(0, amount);
        bestCombo = Math.max(bestCombo, combo);
        return collectNewRewards();
    }

    int onSkillUsed() {
        usedSkill = true;
        return collectNewRewards();
    }

    private int collectNewRewards() {
        int rewards = 0;
        if (!lineRewarded && clearedLines >= lineTarget) {
            lineRewarded = true;
            rewards++;
        }
        if (!comboRewarded && bestCombo >= comboTarget) {
            comboRewarded = true;
            rewards++;
        }
        if (!skillRewarded && usedSkill) {
            skillRewarded = true;
            rewards++;
        }
        return rewards;
    }

    String menuDescription() {
        return "本局任务：消除 " + lineTarget + " 行 · 达成 " + comboTarget + " 连击 · 使用 1 次技能（每项 +1 碎片）";
    }

    String compactProgress() {
        return "任务 " + Math.min(clearedLines, lineTarget) + "/" + lineTarget
                + "行  连击" + Math.min(bestCombo, comboTarget) + "/" + comboTarget
                + "  技能" + (usedSkill ? "✓" : "-");
    }
}
