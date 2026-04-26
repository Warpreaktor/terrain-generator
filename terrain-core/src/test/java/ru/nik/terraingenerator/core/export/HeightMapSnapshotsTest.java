package ru.nik.terraingenerator.core.export;

import org.junit.jupiter.api.Test;
import ru.nik.terraingenerator.core.grid.GridSize;
import ru.nik.terraingenerator.core.grid.TerrainGrid;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HeightMapSnapshotsTest {

    @Test
    void shouldFlattenTerrainGridInRowMajorOrderAndKeepSignedHeights() {
        TerrainGrid terrainGrid = new TerrainGrid(new GridSize(3, 2));
        terrainGrid.setHeight(0, 0, -1.0f);
        terrainGrid.setHeight(1, 0, 0.0f);
        terrainGrid.setHeight(2, 0, 1.0f);
        terrainGrid.setHeight(0, 1, -2.5f);
        terrainGrid.setHeight(1, 1, 3.25f);
        terrainGrid.setHeight(2, 1, 4.5f);

        HeightMapSnapshot snapshot = HeightMapSnapshots.fromTerrainGrid(terrainGrid);

        assertEquals(3, snapshot.width());
        assertEquals(2, snapshot.height());
        assertArrayEquals(new float[]{-1.0f, 0.0f, 1.0f, -2.5f, 3.25f, 4.5f}, snapshot.heights());
    }
}
