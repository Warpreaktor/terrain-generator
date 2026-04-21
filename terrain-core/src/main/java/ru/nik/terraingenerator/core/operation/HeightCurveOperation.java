package ru.nik.terraingenerator.core.operation;

import ru.nik.terraingenerator.core.grid.TerrainGrid;

/**
 * Преобразует нормализованную высоту степенной кривой.
 *
 * Операция полезна после нормализации, когда нужно сместить распределение
 * высот в сторону низин или вершин, не меняя общий диапазон значений.
 */
public final class HeightCurveOperation implements TerrainOperation {

    private static final float NORMALIZED_MIN_HEIGHT = 0f;
    private static final float NORMALIZED_MAX_HEIGHT = 1f;

    private final float exponent;

    /**
     * Создаёт степенную кривую высот.
     *
     * @param exponent показатель степени. Значение больше {@code 1} усиливает низины,
     *                 значение меньше {@code 1} усиливает вершины.
     */
    public HeightCurveOperation(float exponent) {
        if (exponent <= 0f) {
            throw new IllegalArgumentException("Показатель степени должен быть больше нуля.");
        }
        this.exponent = exponent;
    }

    @Override
    public String name() {
        return "height-curve";
    }

    @Override
    public void apply(TerrainGrid terrainGrid) {
        for (int y = 0; y < terrainGrid.height(); y++) {
            for (int x = 0; x < terrainGrid.width(); x++) {
                float rawHeight = terrainGrid.getHeight(x, y);
                float clampedHeight = clamp(rawHeight, NORMALIZED_MIN_HEIGHT, NORMALIZED_MAX_HEIGHT);
                float curvedHeight = (float) Math.pow(clampedHeight, exponent);
                terrainGrid.setHeight(x, y, curvedHeight);
            }
        }
    }

    /**
     * Ограничивает значение указанным диапазоном.
     *
     * @param value исходное значение
     * @param minimum нижняя граница
     * @param maximum верхняя граница
     * @return ограниченное значение
     */
    private float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
