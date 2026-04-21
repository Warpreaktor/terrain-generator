package ru.nik.terraingenerator.lab;

/**
 * Предустановленные профили морфологии рельефа.
 *
 * Каждый профиль задаёт только форму поверхности и не несёт предметного смысла.
 */
public enum TerrainGeneratorPreset {
    CUSTOM("custom"),
    PLAIN("plain"),
    ROLLING_PLAINS("rolling plains"),
    HILLS("hills"),
    RIDGE_FIELD("ridge field"),
    MOUNTAINS("mountains");

    private final String displayName;

    TerrainGeneratorPreset(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Возвращает отображаемое имя профиля.
     *
     * @return имя профиля для панели управления
     */
    public String displayName() {
        return displayName;
    }

    /**
     * Возвращает следующий выбираемый профиль, пропуская пользовательский режим.
     *
     * @return следующий профиль генерации
     */
    public TerrainGeneratorPreset nextSelectable() {
        TerrainGeneratorPreset[] values = values();
        int candidateIndex = ordinal();
        do {
            candidateIndex = (candidateIndex + 1) % values.length;
        } while (values[candidateIndex] == CUSTOM);
        return values[candidateIndex];
    }

    /**
     * Создаёт настройки, соответствующие выбранному профилю.
     *
     * @return параметры генерации рельефа
     */
    public TerrainGeneratorSettings createSettings() {
        return switch (this) {
            case CUSTOM -> PLAIN.createSettings();
            case PLAIN -> new TerrainGeneratorSettings(0.20f, 320f, 90f, 0.10f, 0.88f, 20f, 0.05f, 0.00f, 1.12f);
            case ROLLING_PLAINS -> new TerrainGeneratorSettings(0.34f, 240f, 70f, 0.24f, 0.66f, 35f, 0.08f, 0.08f, 1.02f);
            case HILLS -> new TerrainGeneratorSettings(0.56f, 180f, 48f, 0.42f, 0.50f, 55f, 0.12f, 0.22f, 0.95f);
            case RIDGE_FIELD -> new TerrainGeneratorSettings(0.72f, 170f, 36f, 0.48f, 0.40f, 90f, 0.10f, 0.58f, 0.94f);
            case MOUNTAINS -> new TerrainGeneratorSettings(1.05f, 145f, 26f, 0.70f, 0.24f, 75f, 0.18f, 0.82f, 0.88f);
        };
    }

    @Override
    public String toString() {
        return displayName;
    }

}
