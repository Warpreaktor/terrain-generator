package ru.nik.terraingenerator.core.operation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ru.nik.terraingenerator.core.grid.TerrainGrid;

/**
 * Последовательность операций над рельефом.
 */
public final class TerrainOperationPipeline {

    private final List<TerrainOperation> operations = new ArrayList<>();

    /**
     * Добавляет новую операцию в конец pipeline.
     *
     * @param operation операция рельефа
     * @return текущий pipeline для цепочки вызовов
     */
    public TerrainOperationPipeline add(TerrainOperation operation) {
        operations.add(operation);
        return this;
    }

    /**
     * Выполняет все операции по порядку над указанной сеткой.
     *
     * @param terrainGrid сетка высот
     */
    public void apply(TerrainGrid terrainGrid) {
        for (TerrainOperation operation : operations) {
            operation.apply(terrainGrid);
        }
    }

    /**
     * Возвращает снимок сетки после каждого шага pipeline.
     *
     * Первый элемент списка всегда соответствует состоянию после первой операции.
     *
     * @param sourceGrid исходная сетка
     * @return список снимков после каждого шага
     */
    public List<TerrainGrid> snapshots(TerrainGrid sourceGrid) {
        TerrainGrid workingGrid = sourceGrid.copy();
        List<TerrainGrid> snapshots = new ArrayList<>(operations.size());

        for (TerrainOperation operation : operations) {
            operation.apply(workingGrid);
            snapshots.add(workingGrid.copy());
        }

        return snapshots;
    }

    /**
     * Возвращает неизменяемый список операций.
     *
     * @return список операций
     */
    public List<TerrainOperation> operations() {
        return Collections.unmodifiableList(operations);
    }
}
