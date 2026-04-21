package ru.nik.terraingenerator.lab;

import ru.nik.terraingenerator.core.grid.GridSize;
import ru.nik.terraingenerator.core.grid.TerrainGrid;

/**
 * Слой ручных правок, который накладывается поверх процедурного рельефа.
 *
 * Слой хранит только отклонение от базовой процедурной карты. Благодаря этому
 * можно отдельно смотреть результат генератора и отдельно управлять ручной правкой.
 */
public final class TerrainSculptLayer {

    /**
     * Минимальная поддерживаемая signed-высота итогового рельефа.
     */
    private static final float MIN_SUPPORTED_SIGNED_HEIGHT = TerrainGeneratorSettings.minimumSupportedElevation();

    /**
     * Максимальная поддерживаемая signed-высота итогового рельефа.
     */
    private static final float MAX_SUPPORTED_SIGNED_HEIGHT = TerrainGeneratorSettings.maximumSupportedElevation();
    private static final float MINIMUM_BRUSH_RADIUS = 0.25f;
    private static final float DISTANCE_FALLOFF_POWER = 2f;
    private static final int SMOOTH_SAMPLE_RADIUS_IN_CELLS = 1;

    private final TerrainGrid sculptDeltaGrid;

    /**
     * Создаёт пустой слой правок для сетки указанного размера.
     *
     * @param gridSize размер сетки
     */
    public TerrainSculptLayer(GridSize gridSize) {
        this.sculptDeltaGrid = new TerrainGrid(gridSize);
    }

    /**
     * Полностью очищает все ручные изменения.
     */
    public void clear() {
        for (int y = 0; y < sculptDeltaGrid.height(); y++) {
            for (int x = 0; x < sculptDeltaGrid.width(); x++) {
                sculptDeltaGrid.setHeight(x, y, 0f);
            }
        }
    }

    /**
     * Собирает итоговую карту, объединяя процедурную основу и слой правок.
     *
     * @param proceduralBaseGrid базовая процедурная карта
     * @return итоговая сетка высот
     */
    public TerrainGrid composeFinalTerrain(TerrainGrid proceduralBaseGrid) {
        TerrainGrid composedTerrainGrid = proceduralBaseGrid.copy();

        for (int y = 0; y < composedTerrainGrid.height(); y++) {
            for (int x = 0; x < composedTerrainGrid.width(); x++) {
                float combinedHeight = combinedHeight(proceduralBaseGrid, sculptDeltaGrid, x, y);
                composedTerrainGrid.setHeight(x, y, combinedHeight);
            }
        }

        return composedTerrainGrid;
    }

    /**
     * Применяет выбранную кисть к слою правок.
     *
     * @param proceduralBaseGrid базовая процедурная карта
     * @param sculptTool выбранный инструмент
     * @param centerCellX координата центра кисти по оси X
     * @param centerCellY координата центра кисти по оси Y
     * @param brushRadiusInCells радиус кисти в клетках
     * @param brushStrength сила воздействия кисти
     * @param flattenTargetHeight целевая высота для режима flatten
     */
    public void applyBrush(
            TerrainGrid proceduralBaseGrid,
            SculptTool sculptTool,
            int centerCellX,
            int centerCellY,
            float brushRadiusInCells,
            float brushStrength,
            float flattenTargetHeight
    ) {
        float effectiveBrushRadius = Math.max(MINIMUM_BRUSH_RADIUS, brushRadiusInCells);
        int brushCellRadius = (int) Math.ceil(effectiveBrushRadius);
        int minimumX = centerCellX - brushCellRadius;
        int maximumX = centerCellX + brushCellRadius;
        int minimumY = centerCellY - brushCellRadius;
        int maximumY = centerCellY + brushCellRadius;

        TerrainGrid sourceDeltaGrid = sculptTool == SculptTool.SMOOTH ? sculptDeltaGrid.copy() : sculptDeltaGrid;

        for (int y = minimumY; y <= maximumY; y++) {
            for (int x = minimumX; x <= maximumX; x++) {
                if (!proceduralBaseGrid.contains(x, y)) {
                    continue;
                }

                float deltaX = x - centerCellX;
                float deltaY = y - centerCellY;
                float radialDistance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                if (radialDistance > effectiveBrushRadius) {
                    continue;
                }

                float normalizedDistance = radialDistance / effectiveBrushRadius;
                float distanceFalloff = 1f - normalizedDistance;
                float weightedBrushStrength = brushStrength * (float) Math.pow(distanceFalloff, DISTANCE_FALLOFF_POWER);
                float baseHeight = proceduralBaseGrid.getHeight(x, y);
                float currentCombinedHeight = combinedHeight(proceduralBaseGrid, sourceDeltaGrid, x, y);
                float targetCombinedHeight = switch (sculptTool) {
                    case RAISE -> currentCombinedHeight + weightedBrushStrength;
                    case LOWER -> currentCombinedHeight - weightedBrushStrength;
                    case SMOOTH -> smoothTargetHeight(proceduralBaseGrid, sourceDeltaGrid, x, y, currentCombinedHeight, weightedBrushStrength);
                    case FLATTEN -> interpolate(currentCombinedHeight, flattenTargetHeight, weightedBrushStrength);
                };

                float clampedTargetHeight = clamp(
                        targetCombinedHeight,
                        MIN_SUPPORTED_SIGNED_HEIGHT,
                        MAX_SUPPORTED_SIGNED_HEIGHT
                );
                float nextDeltaHeight = clampedTargetHeight - baseHeight;
                sculptDeltaGrid.setHeight(x, y, nextDeltaHeight);
            }
        }
    }

    /**
     * Вычисляет целевую высоту для кисти сглаживания.
     *
     * @param proceduralBaseGrid базовая процедурная карта
     * @param sourceDeltaGrid снимок слоя правок до начала операции
     * @param cellX координата клетки по оси X
     * @param cellY координата клетки по оси Y
     * @param currentCombinedHeight текущая итоговая высота клетки
     * @param weightedBrushStrength сила воздействия кисти с учётом falloff
     * @return новая целевая высота
     */
    private float smoothTargetHeight(
            TerrainGrid proceduralBaseGrid,
            TerrainGrid sourceDeltaGrid,
            int cellX,
            int cellY,
            float currentCombinedHeight,
            float weightedBrushStrength
    ) {
        float neighbourHeightSum = 0f;
        int sampledCellCount = 0;

        for (int offsetY = -SMOOTH_SAMPLE_RADIUS_IN_CELLS; offsetY <= SMOOTH_SAMPLE_RADIUS_IN_CELLS; offsetY++) {
            for (int offsetX = -SMOOTH_SAMPLE_RADIUS_IN_CELLS; offsetX <= SMOOTH_SAMPLE_RADIUS_IN_CELLS; offsetX++) {
                int neighbourX = cellX + offsetX;
                int neighbourY = cellY + offsetY;
                if (!proceduralBaseGrid.contains(neighbourX, neighbourY)) {
                    continue;
                }

                neighbourHeightSum += combinedHeight(proceduralBaseGrid, sourceDeltaGrid, neighbourX, neighbourY);
                sampledCellCount++;
            }
        }

        float averagedNeighbourHeight = neighbourHeightSum / sampledCellCount;
        return interpolate(currentCombinedHeight, averagedNeighbourHeight, weightedBrushStrength);
    }

    /**
     * Возвращает итоговую высоту клетки после наложения слоя правок.
     *
     * @param proceduralBaseGrid базовая процедурная карта
     * @param deltaGrid снимок слоя правок
     * @param cellX координата клетки по оси X
     * @param cellY координата клетки по оси Y
     * @return итоговая высота клетки
     */
    private float combinedHeight(TerrainGrid proceduralBaseGrid, TerrainGrid deltaGrid, int cellX, int cellY) {
        float baseHeight = proceduralBaseGrid.getHeight(cellX, cellY);
        float deltaHeight = deltaGrid.getHeight(cellX, cellY);
        float combinedHeight = baseHeight + deltaHeight;
        return clamp(combinedHeight, MIN_SUPPORTED_SIGNED_HEIGHT, MAX_SUPPORTED_SIGNED_HEIGHT);
    }

    /**
     * Линейно смешивает два значения.
     *
     * @param currentValue текущее значение
     * @param targetValue целевое значение
     * @param alpha коэффициент смешивания
     * @return смешанное значение
     */
    private float interpolate(float currentValue, float targetValue, float alpha) {
        float clampedAlpha = clamp(alpha, 0f, 1f);
        return currentValue + (targetValue - currentValue) * clampedAlpha;
    }

    /**
     * Ограничивает значение указанным диапазоном.
     *
     * @param value исходное значение
     * @param minimum нижняя граница
     * @param maximum верхняя граница
     * @return ограниченное значение
     */
    private float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
