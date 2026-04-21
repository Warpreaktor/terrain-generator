package ru.nik.terraingenerator.core.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import ru.nik.terraingenerator.core.grid.GridSize;
import ru.nik.terraingenerator.core.grid.TerrainGrid;

class TerrainOperationPipelineTest {

    @Test
    void shouldApplyOperationsInSequence() {
        TerrainGrid terrainGrid = new TerrainGrid(new GridSize(3, 3));

        TerrainOperationPipeline terrainOperationPipeline = new TerrainOperationPipeline()
                .add(new FillOperation(0f))
                .add(new RadialGradientOperation("raise-center", 1f, 1f, 2f, 10f))
                .add(new NormalizeOperation(0f, 1f));

        terrainOperationPipeline.apply(terrainGrid);

        float centerHeight = terrainGrid.getHeight(1, 1);
        float cornerHeight = terrainGrid.getHeight(0, 0);

        assertEquals(1f, centerHeight);
        assertEquals(0f, cornerHeight);
    }
}
