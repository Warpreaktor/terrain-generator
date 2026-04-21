package ru.nik.terraingenerator.core.operation;

import ru.nik.terraingenerator.core.grid.HeightRange;
import ru.nik.terraingenerator.core.grid.TerrainGrid;

/**
 * Нормализует всю карту высот в заданный диапазон.
 */
public final class NormalizeOperation implements TerrainOperation {

    private final float targetMinimumHeight;
    private final float targetMaximumHeight;

    /**
     * Создаёт операцию нормализации.
     *
     * @param targetMinimumHeight желаемый минимум
     * @param targetMaximumHeight желаемый максимум
     */
    public NormalizeOperation(float targetMinimumHeight, float targetMaximumHeight) {
        if (targetMinimumHeight >= targetMaximumHeight) {
            throw new IllegalArgumentException("Минимум диапазона должен быть меньше максимума.");
        }
        this.targetMinimumHeight = targetMinimumHeight;
        this.targetMaximumHeight = targetMaximumHeight;
    }

    @Override
    public String name() {
        return "normalize";
    }

    @Override
    public void apply(TerrainGrid terrainGrid) {
        HeightRange sourceHeightRange = terrainGrid.heightRange();
        float sourceSpan = sourceHeightRange.span();

        if (sourceSpan == 0f) {
            for (int y = 0; y < terrainGrid.height(); y++) {
                for (int x = 0; x < terrainGrid.width(); x++) {
                    terrainGrid.setHeight(x, y, targetMinimumHeight);
                }
            }
            return;
        }

        float targetSpan = targetMaximumHeight - targetMinimumHeight;

        for (int y = 0; y < terrainGrid.height(); y++) {
            for (int x = 0; x < terrainGrid.width(); x++) {
                float normalizedSourceValue = (terrainGrid.getHeight(x, y) - sourceHeightRange.minimum()) / sourceSpan;
                float targetValue = targetMinimumHeight + normalizedSourceValue * targetSpan;
                terrainGrid.setHeight(x, y, targetValue);
            }
        }
    }
}
