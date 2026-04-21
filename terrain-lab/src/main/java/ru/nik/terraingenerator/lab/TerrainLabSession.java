package ru.nik.terraingenerator.lab;

import java.util.List;
import ru.nik.terraingenerator.core.grid.TerrainGrid;

/**
 * Результат одной генерации terrain-лаборатории.
 *
 * @param finalTerrainGrid итоговая процедурная сетка без ручных правок
 * @param pipelineSnapshots снимки после каждого шага pipeline
 * @param pipelineStepNames имена шагов pipeline в том же порядке, что и снимки
 */
public record TerrainLabSession(
        TerrainGrid finalTerrainGrid,
        List<TerrainGrid> pipelineSnapshots,
        List<String> pipelineStepNames
) {
}
