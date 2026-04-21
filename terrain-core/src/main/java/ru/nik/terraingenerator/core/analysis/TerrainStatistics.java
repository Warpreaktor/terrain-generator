package ru.nik.terraingenerator.core.analysis;

import ru.nik.terraingenerator.core.grid.HeightRange;
import ru.nik.terraingenerator.core.grid.TerrainGrid;

/**
 * Простейшая сводная статистика по сетке рельефа.
 *
 * @param heightRange диапазон высот
 * @param averageHeight средняя высота по карте
 */
public record TerrainStatistics(HeightRange heightRange, float averageHeight) {

    /**
     * Собирает статистику по сетке высот.
     *
     * @param terrainGrid исследуемая сетка
     * @return сводная статистика
     */
    public static TerrainStatistics from(TerrainGrid terrainGrid) {
        HeightRange heightRange = terrainGrid.heightRange();
        float heightSum = 0f;

        for (int y = 0; y < terrainGrid.height(); y++) {
            for (int x = 0; x < terrainGrid.width(); x++) {
                heightSum += terrainGrid.getHeight(x, y);
            }
        }

        float averageHeight = heightSum / terrainGrid.size().cellCount();
        return new TerrainStatistics(heightRange, averageHeight);
    }
}
