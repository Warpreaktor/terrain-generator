package ru.nik.terraingenerator.lab.render;

import com.badlogic.gdx.graphics.Color;

/**
 * Преобразует значение нормализованной высоты в цвет.
 */
public final class TerrainColorResolver {

    private static final Color[] TOPOGRAPHIC_COLORS = {
            new Color(0.10f, 0.10f, 0.10f, 1f),
            new Color(0.18f, 0.18f, 0.18f, 1f),
            new Color(0.28f, 0.28f, 0.28f, 1f),
            new Color(0.40f, 0.40f, 0.40f, 1f),
            new Color(0.56f, 0.56f, 0.56f, 1f),
            new Color(0.72f, 0.72f, 0.72f, 1f),
            new Color(0.86f, 0.86f, 0.86f, 1f),
            new Color(0.96f, 0.96f, 0.96f, 1f)
    };

    private static final int LAST_TOPOGRAPHIC_INDEX = TOPOGRAPHIC_COLORS.length - 1;

    private TerrainColorResolver() {
    }

    /**
     * Возвращает цвет для указанной высоты и режима.
     *
     * @param normalizedHeight высота в диапазоне от 0 до 1
     * @param heightColorMode выбранный цветовой режим
     * @return цвет клетки
     */
    public static Color resolve(float normalizedHeight, HeightColorMode heightColorMode) {
        float clampedHeight = Math.max(0f, Math.min(1f, normalizedHeight));

        return switch (heightColorMode) {
            case GRAYSCALE -> grayscaleColor(clampedHeight);
            case TOPOGRAPHIC -> topographicColor(clampedHeight);
        };
    }

    /**
     * Возвращает контрастный монохромный цвет для обычного режима просмотра.
     *
     * @param normalizedHeight высота в диапазоне от 0 до 1
     * @return оттенок серого
     */
    private static Color grayscaleColor(float normalizedHeight) {
        float contrastedHeight = applyContrast(normalizedHeight, 1.35f);
        float gammaCorrectedHeight = (float) Math.pow(contrastedHeight, 1.10f);
        return new Color(gammaCorrectedHeight, gammaCorrectedHeight, gammaCorrectedHeight, 1f);
    }

    /**
     * Возвращает ступенчатую топографическую палитру.
     *
     * @param normalizedHeight высота в диапазоне от 0 до 1
     * @return цвет ступени высоты
     */
    private static Color topographicColor(float normalizedHeight) {
        float contrastedHeight = applyContrast(normalizedHeight, 1.55f);
        int paletteIndex = Math.min(
                LAST_TOPOGRAPHIC_INDEX,
                (int) Math.floor(contrastedHeight * TOPOGRAPHIC_COLORS.length)
        );
        return new Color(TOPOGRAPHIC_COLORS[paletteIndex]);
    }

    /**
     * Усиливает различие между низкими и высокими значениями.
     *
     * @param normalizedHeight высота в диапазоне от 0 до 1
     * @param contrastFactor коэффициент контраста
     * @return значение после усиления контраста
     */
    private static float applyContrast(float normalizedHeight, float contrastFactor) {
        float shiftedHeight = normalizedHeight - 0.5f;
        float contrastedHeight = shiftedHeight * contrastFactor + 0.5f;
        return Math.max(0f, Math.min(1f, contrastedHeight));
    }
}
