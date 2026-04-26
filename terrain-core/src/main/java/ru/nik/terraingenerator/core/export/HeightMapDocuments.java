package ru.nik.terraingenerator.core.export;

import ru.nik.terraingenerator.core.grid.TerrainGrid;

/**
 * Фабрика нейтральных документов карты высот.
 */
public final class HeightMapDocuments {

    private HeightMapDocuments() {
    }

    /**
     * Создаёт документ карты высот с указанным именем.
     *
     * @param documentName имя документа
     * @param heightMapSnapshot снимок карты высот
     * @return документ нейтрального формата карты высот
     */
    public static HeightMapDocument create(String documentName, HeightMapSnapshot heightMapSnapshot) {
        String resolvedDocumentName = resolveDocumentName(documentName);
        return new HeightMapDocument(
                HeightMapDocument.DEFAULT_FORMAT,
                HeightMapDocument.DEFAULT_VERSION,
                resolvedDocumentName,
                heightMapSnapshot
        );
    }

    /**
     * Создаёт документ карты высот с именем по умолчанию.
     *
     * @param heightMapSnapshot снимок карты высот
     * @return документ нейтрального формата карты высот
     */
    public static HeightMapDocument createDefault(HeightMapSnapshot heightMapSnapshot) {
        return create(HeightMapDocument.DEFAULT_NAME, heightMapSnapshot);
    }

    /**
     * Создаёт документ карты высот напрямую из сетки рельефа.
     *
     * @param documentName имя документа
     * @param terrainGrid сетка рельефа
     * @return документ нейтрального формата карты высот
     */
    public static HeightMapDocument fromTerrainGrid(String documentName, TerrainGrid terrainGrid) {
        HeightMapSnapshot heightMapSnapshot = HeightMapSnapshots.fromTerrainGrid(terrainGrid);
        return create(documentName, heightMapSnapshot);
    }

    /**
     * Создаёт документ карты высот с именем по умолчанию напрямую из сетки рельефа.
     *
     * @param terrainGrid сетка рельефа
     * @return документ нейтрального формата карты высот
     */
    public static HeightMapDocument fromTerrainGrid(TerrainGrid terrainGrid) {
        return fromTerrainGrid(HeightMapDocument.DEFAULT_NAME, terrainGrid);
    }

    /**
     * Возвращает итоговое имя документа.
     *
     * @param requestedDocumentName имя, запрошенное вызывающим кодом
     * @return валидное имя документа
     */
    private static String resolveDocumentName(String requestedDocumentName) {
        boolean missingDocumentName = requestedDocumentName == null || requestedDocumentName.isBlank();
        if (missingDocumentName) {
            return HeightMapDocument.DEFAULT_NAME;
        }
        return requestedDocumentName;
    }
}
