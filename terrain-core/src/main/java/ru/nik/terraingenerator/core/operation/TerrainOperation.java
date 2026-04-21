package ru.nik.terraingenerator.core.operation;

import ru.nik.terraingenerator.core.grid.TerrainGrid;

/**
 * Низкоуровневая операция над сеткой высот.
 */
public interface TerrainOperation {

    /**
     * Возвращает имя операции для логирования, отладки и отображения.
     *
     * @return имя операции
     */
    String name();

    /**
     * Применяет операцию к указанной сетке.
     *
     * @param terrainGrid изменяемая сетка высот
     */
    void apply(TerrainGrid terrainGrid);
}
