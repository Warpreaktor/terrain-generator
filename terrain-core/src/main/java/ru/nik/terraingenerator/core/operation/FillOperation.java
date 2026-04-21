package ru.nik.terraingenerator.core.operation;

import ru.nik.terraingenerator.core.grid.TerrainGrid;

/**
 * Заполняет всю сетку одной и той же высотой.
 */
public final class FillOperation implements TerrainOperation {

    private final float heightValue;

    /**
     * Создаёт операцию заполнения указанной высотой.
     *
     * @param heightValue значение высоты для всех клеток
     */
    public FillOperation(float heightValue) {
        this.heightValue = heightValue;
    }

    @Override
    public String name() {
        return "fill";
    }

    @Override
    public void apply(TerrainGrid terrainGrid) {
        for (int y = 0; y < terrainGrid.height(); y++) {
            for (int x = 0; x < terrainGrid.width(); x++) {
                terrainGrid.setHeight(x, y, heightValue);
            }
        }
    }
}
