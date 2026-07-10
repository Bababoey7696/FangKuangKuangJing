package com.ame.blocktetris;

/** 皮肤名称、解锁条件和世界背景色集中表。 */
final class ThemeCatalog {
    static final String[] NAMES = {
            "草原矿洞", "冰晶洞穴", "熔岩遗迹", "深海遗迹", "天空遗迹", "远古核心",
            "月影矿坑", "樱晶洞窟", "矩阵矿井"
    };

    static final String[] UNLOCK_TEXT = {
            "默认解锁", "5 枚遗迹碎片", "12 枚遗迹碎片", "20 枚遗迹碎片", "30 枚遗迹碎片", "单局达到 50000 分",
            "40 枚遗迹碎片", "55 枚遗迹碎片", "完成 12 个本局任务"
    };

    // sky, ground, deep
    static final int[][] WORLD_COLORS = {
            {0xFF66B5DA, 0xFF529745, 0xFF774E30},
            {0xFF87CBE8, 0xFFBCEBFA, 0xFF4F7694},
            {0xFF5B2B2A, 0xFF3E2426, 0xFF24191C},
            {0xFF235A70, 0xFF1F7280, 0xFF123743},
            {0xFF8CCCE8, 0xFFE9F3F3, 0xFF7EA7B4},
            {0xFF2D2143, 0xFF5D3B69, 0xFF191327},
            {0xFF191D3A, 0xFF343B72, 0xFF101426},
            {0xFFF2A6C5, 0xFFB85D82, 0xFF5B2948},
            {0xFF16252A, 0xFF1E6B64, 0xFF0B171A}
    };

    private ThemeCatalog() {}

    static int count() {
        return NAMES.length;
    }
}
