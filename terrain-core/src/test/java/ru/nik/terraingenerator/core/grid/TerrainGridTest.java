package ru.nik.terraingenerator.core.grid;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TerrainGridTest {

    @Test
    void shouldStoreAndReturnCellHeight() {
        TerrainGrid terrainGrid = new TerrainGrid(new GridSize(4, 3));

        terrainGrid.setHeight(2, 1, 7.5f);

        assertEquals(7.5f, terrainGrid.getHeight(2, 1));
    }

    @Test
    void shouldCalculateHeightRange() {
        TerrainGrid terrainGrid = new TerrainGrid(new GridSize(2, 2));
        terrainGrid.setHeight(0, 0, -2f);
        terrainGrid.setHeight(1, 0, 4f);
        terrainGrid.setHeight(0, 1, 1f);
        terrainGrid.setHeight(1, 1, 3f);

        HeightRange heightRange = terrainGrid.heightRange();

        assertEquals(-2f, heightRange.minimum());
        assertEquals(4f, heightRange.maximum());
    }
}
