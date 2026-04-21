package ru.nik.terraingenerator.core.operation;

import ru.nik.terraingenerator.core.grid.TerrainGrid;

/**
 * Преобразует signed-высоту степенной кривой относительно нулевого уровня.
 *
 * <p>Операция предполагает, что ноль является смысловым центром рельефа.
 * Отрицательные значения остаются отрицательными, положительные остаются
 * положительными, а кривая меняет только распределение высот относительно
 * максимальной абсолютной амплитуды.</p>
 */
public final class HeightCurveOperation implements TerrainOperation {

    private static final float NORMALIZED_MIN_MAGNITUDE = 0f;
    private static final float NORMALIZED_MAX_MAGNITUDE = 1f;

    private final float exponent;
    private final float maximumAbsoluteHeight;

    /**
     * Создаёт степенную кривую высот.
     *
     * @param exponent показатель степени. Значение больше {@code 1} усиливает
     *                 низкие по модулю значения и делает распределение более
     *                 прижатым к нулю. Значение меньше {@code 1} усиливает
     *                 удалённые от нуля вершины и впадины.
     * @param maximumAbsoluteHeight максимальная абсолютная высота, относительно
     *                              которой выполняется signed-нормализация
     */
    public HeightCurveOperation(float exponent, float maximumAbsoluteHeight) {
        if (exponent <= 0f) {
            throw new IllegalArgumentException("Показатель степени должен быть больше нуля.");
        }
        if (maximumAbsoluteHeight <= 0f) {
            throw new IllegalArgumentException("Максимальная абсолютная высота должна быть больше нуля.");
        }
        this.exponent = exponent;
        this.maximumAbsoluteHeight = maximumAbsoluteHeight;
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
                float signedUnitHeight = clamp(rawHeight / maximumAbsoluteHeight, -NORMALIZED_MAX_MAGNITUDE, NORMALIZED_MAX_MAGNITUDE);
                float signedDirection = Math.signum(signedUnitHeight);
                float absoluteMagnitude = Math.abs(signedUnitHeight);
                float clampedMagnitude = clamp(absoluteMagnitude, NORMALIZED_MIN_MAGNITUDE, NORMALIZED_MAX_MAGNITUDE);
                float curvedMagnitude = (float) Math.pow(clampedMagnitude, exponent);
                float curvedHeight = signedDirection * curvedMagnitude * maximumAbsoluteHeight;
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
