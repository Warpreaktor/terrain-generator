package ru.nik.terraingenerator.core.operation;

import ru.nik.terraingenerator.core.grid.TerrainGrid;

/**
 * Добавляет к карте линейный уклон по направлению.
 */
public final class SlopeOperation implements TerrainOperation {

    private final float xComponent;
    private final float yComponent;
    private final float amplitude;

    /**
     * Создаёт операцию линейного уклона.
     *
     * @param xComponent вклад направления по оси X
     * @param yComponent вклад направления по оси Y
     * @param amplitude итоговая амплитуда уклона
     */
    public SlopeOperation(float xComponent, float yComponent, float amplitude) {
        float vectorLength = (float) Math.sqrt(xComponent * xComponent + yComponent * yComponent);
        if (vectorLength == 0f) {
            throw new IllegalArgumentException("Направление уклона не может быть нулевым.");
        }
        this.xComponent = xComponent / vectorLength;
        this.yComponent = yComponent / vectorLength;
        this.amplitude = amplitude;
    }

    @Override
    public String name() {
        return "slope";
    }

    @Override
    public void apply(TerrainGrid terrainGrid) {
        float centerX = (terrainGrid.width() - 1) * 0.5f;
        float centerY = (terrainGrid.height() - 1) * 0.5f;
        float normalizationDistance = Math.max(terrainGrid.width(), terrainGrid.height()) * 0.5f;

        for (int y = 0; y < terrainGrid.height(); y++) {
            for (int x = 0; x < terrainGrid.width(); x++) {
                float translatedX = x - centerX;
                float translatedY = y - centerY;
                float projection = (translatedX * xComponent + translatedY * yComponent) / normalizationDistance;
                terrainGrid.addHeight(x, y, projection * amplitude);
            }
        }
    }
}
