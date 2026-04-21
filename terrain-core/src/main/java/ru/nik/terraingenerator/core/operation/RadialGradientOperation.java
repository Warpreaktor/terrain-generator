package ru.nik.terraingenerator.core.operation;

import ru.nik.terraingenerator.core.grid.TerrainGrid;

/**
 * Добавляет к сетке радиальный вклад с плавным затуханием.
 *
 * Операция универсальна: при положительной амплитуде она поднимает область,
 * при отрицательной — опускает.
 */
public final class RadialGradientOperation implements TerrainOperation {

    private final String operationName;
    private final float centerX;
    private final float centerY;
    private final float radius;
    private final float amplitude;

    /**
     * Создаёт радиальную операцию.
     *
     * @param operationName имя операции
     * @param centerX центр по оси X
     * @param centerY центр по оси Y
     * @param radius радиус влияния
     * @param amplitude максимальная добавка к высоте
     */
    public RadialGradientOperation(String operationName, float centerX, float centerY, float radius, float amplitude) {
        if (radius <= 0f) {
            throw new IllegalArgumentException("Радиус должен быть больше нуля.");
        }
        this.operationName = operationName;
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
        this.amplitude = amplitude;
    }

    @Override
    public String name() {
        return operationName;
    }

    @Override
    public void apply(TerrainGrid terrainGrid) {
        float radiusSquared = radius * radius;

        for (int y = 0; y < terrainGrid.height(); y++) {
            for (int x = 0; x < terrainGrid.width(); x++) {
                float deltaX = x - centerX;
                float deltaY = y - centerY;
                float distanceSquared = deltaX * deltaX + deltaY * deltaY;

                if (distanceSquared > radiusSquared) {
                    continue;
                }

                float distance = (float) Math.sqrt(distanceSquared);
                float normalizedDistance = distance / radius;
                float falloff = 1f - smoothStep(normalizedDistance);
                terrainGrid.addHeight(x, y, amplitude * falloff);
            }
        }
    }

    /**
     * Возвращает сглаженную функцию затухания.
     *
     * @param alpha коэффициент от 0 до 1
     * @return сглаженное значение
     */
    private float smoothStep(float alpha) {
        return alpha * alpha * (3f - 2f * alpha);
    }
}
