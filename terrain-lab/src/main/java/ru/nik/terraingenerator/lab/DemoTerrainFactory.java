package ru.nik.terraingenerator.lab;

import ru.nik.terraingenerator.core.grid.GridSize;

/**
 * Совместимый фасад для старого имени фабрики.
 *
 * Класс оставлен, чтобы не ломать существующие ссылки во время перехода
 * на более универсальный генератор рельефа.
 */
@Deprecated(forRemoval = true)
public final class DemoTerrainFactory {

    private DemoTerrainFactory() {
    }

    /**
     * Делегирует создание сессии новому процедурному генератору.
     *
     * @param gridSize размер сетки
     * @param seed базовый seed
     * @return процедурная сессия генерации
     */
    public static TerrainLabSession createSession(GridSize gridSize, long seed) {
        TerrainGeneratorSettings terrainGeneratorSettings = TerrainGeneratorPreset.ROLLING_PLAINS.createSettings();
        return ProceduralTerrainGenerator.createSession(gridSize, seed, terrainGeneratorSettings);
    }
}
