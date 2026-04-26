package ru.nik.terraingenerator.core.export;

import org.junit.jupiter.api.Test;
import ru.nik.terraingenerator.core.grid.GridSize;
import ru.nik.terraingenerator.core.grid.TerrainGrid;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HeightMapJsonWriterTest {

    @Test
    void shouldSerializeNeutralHeightMapDocumentToJson() {
        TerrainGrid terrainGrid = new TerrainGrid(new GridSize(2, 2));
        terrainGrid.setHeight(0, 0, -1.25f);
        terrainGrid.setHeight(1, 0, 0.0f);
        terrainGrid.setHeight(0, 1, 2.5f);
        terrainGrid.setHeight(1, 1, 3.75f);

        HeightMapDocument document = HeightMapDocuments.fromTerrainGrid("demo-map", terrainGrid);

        String jsonPayload = HeightMapJsonWriter.toJson(document);

        assertTrue(jsonPayload.contains("\"format\" : \"height-map\""));
        assertTrue(jsonPayload.contains("\"version\" : 1"));
        assertTrue(jsonPayload.contains("\"name\" : \"demo-map\""));
        assertTrue(jsonPayload.contains("\"width\" : 2"));
        assertTrue(jsonPayload.contains("\"height\" : 2"));
        assertTrue(jsonPayload.contains("-1.25, 0.0"));
        assertTrue(jsonPayload.contains("2.5, 3.75"));
    }
}
