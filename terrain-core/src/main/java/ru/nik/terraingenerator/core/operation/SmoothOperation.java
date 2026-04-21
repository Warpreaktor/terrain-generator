package ru.nik.terraingenerator.core.operation;

import ru.nik.terraingenerator.core.grid.TerrainGrid;

/**
 * Выполняет локальное сглаживание рельефа по соседям.
 */
public final class SmoothOperation implements TerrainOperation {

    private final int iterationCount;

    /**
     * Создаёт операцию сглаживания.
     *
     * @param iterationCount количество проходов сглаживания
     */
    public SmoothOperation(int iterationCount) {
        if (iterationCount <= 0) {
            throw new IllegalArgumentException("Количество проходов сглаживания должно быть больше нуля.");
        }
        this.iterationCount = iterationCount;
    }

    @Override
    public String name() {
        return "smooth";
    }

    @Override
    public void apply(TerrainGrid terrainGrid) {
        for (int iterationIndex = 0; iterationIndex < iterationCount; iterationIndex++) {
            TerrainGrid sourceGrid = terrainGrid.copy();

            for (int y = 0; y < terrainGrid.height(); y++) {
                for (int x = 0; x < terrainGrid.width(); x++) {
                    float summedHeight = 0f;
                    int sampledCellCount = 0;

                    for (int offsetY = -1; offsetY <= 1; offsetY++) {
                        for (int offsetX = -1; offsetX <= 1; offsetX++) {
                            int neighbourX = x + offsetX;
                            int neighbourY = y + offsetY;

                            if (!sourceGrid.contains(neighbourX, neighbourY)) {
                                continue;
                            }

                            summedHeight += sourceGrid.getHeight(neighbourX, neighbourY);
                            sampledCellCount++;
                        }
                    }

                    float averagedHeight = summedHeight / sampledCellCount;
                    terrainGrid.setHeight(x, y, averagedHeight);
                }
            }
        }
    }
}
