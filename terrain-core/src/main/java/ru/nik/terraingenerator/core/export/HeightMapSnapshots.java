package ru.nik.terraingenerator.core.export;

import ru.nik.terraingenerator.core.grid.TerrainGrid;

/**
 * Утилита преобразования {@link TerrainGrid} в нейтральный снимок карты высот.
 */
public final class HeightMapSnapshots {

    private HeightMapSnapshots() {
    }

    /**
     * Создаёт снимок карты высот на основе текущего состояния сетки рельефа.
     *
     * <p>Высоты копируются в построчном порядке без нормализации и без изменения
     * знака. Это значит, что отрицательные значения, нулевой уровень и положительные
     * значения сохраняются в snapshot как есть.</p>
     *
     * @param terrainGrid исходная сетка рельефа
     * @return независимый снимок карты высот
     */
    public static HeightMapSnapshot fromTerrainGrid(TerrainGrid terrainGrid) {
        if (terrainGrid == null) {
            throw new IllegalArgumentException("Сетка рельефа не должна быть null.");
        }

        int gridWidth = terrainGrid.width();
        int gridHeight = terrainGrid.height();
        int cellCount = gridWidth * gridHeight;
        float[] flattenedHeights = new float[cellCount];

        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++) {
                int linearIndex = y * gridWidth + x;
                float cellHeight = terrainGrid.getHeight(x, y);
                flattenedHeights[linearIndex] = cellHeight;
            }
        }

        return new HeightMapSnapshot(gridWidth, gridHeight, flattenedHeights);
    }
}
