package ru.nik.terraingenerator.core.export;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Сериализатор нейтрального документа карты высот в JSON.
 *
 * <p>Класс не зависит от внешних JSON-библиотек и сохраняет только тот контракт,
 * который нужен ядру генератора: формат, версию, имя и саму карту высот.</p>
 */
public final class HeightMapJsonWriter {

    private static final String LINE_BREAK = "\n";
    private static final String INDENT_UNIT = "  ";
    private static final String VALUE_SEPARATOR = ", ";

    private HeightMapJsonWriter() {
    }

    /**
     * Преобразует документ карты высот в JSON-строку.
     *
     * @param document документ карты высот
     * @return JSON-представление документа
     */
    public static String toJson(HeightMapDocument document) {
        if (document == null) {
            throw new IllegalArgumentException("Документ карты высот не должен быть null.");
        }

        HeightMapSnapshot heightMapSnapshot = document.heightMap();
        int mapWidth = heightMapSnapshot.width();
        int mapHeight = heightMapSnapshot.height();

        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append('{').append(LINE_BREAK);
        appendStringField(jsonBuilder, 1, "format", document.format(), true);
        appendNumberField(jsonBuilder, 1, "version", document.version(), true);
        appendStringField(jsonBuilder, 1, "name", document.name(), true);
        jsonBuilder.append(indent(1)).append("\"heightMap\" : {").append(LINE_BREAK);
        appendNumberField(jsonBuilder, 2, "width", mapWidth, true);
        appendNumberField(jsonBuilder, 2, "height", mapHeight, true);
        jsonBuilder.append(indent(2)).append("\"heights\" : [").append(LINE_BREAK);
        appendHeights(jsonBuilder, heightMapSnapshot);
        jsonBuilder.append(LINE_BREAK);
        jsonBuilder.append(indent(2)).append(']').append(LINE_BREAK);
        jsonBuilder.append(indent(1)).append('}').append(LINE_BREAK);
        jsonBuilder.append('}');
        return jsonBuilder.toString();
    }

    /**
     * Сохраняет документ карты высот в JSON-файл.
     *
     * @param document документ карты высот
     * @param outputPath путь до целевого файла
     * @throws IOException если произошла ошибка записи
     */
    public static void writeToFile(HeightMapDocument document, Path outputPath) throws IOException {
        if (outputPath == null) {
            throw new IllegalArgumentException("Путь выходного файла не должен быть null.");
        }

        Path parentDirectory = outputPath.getParent();
        boolean hasParentDirectory = parentDirectory != null;
        if (hasParentDirectory) {
            Files.createDirectories(parentDirectory);
        }

        String jsonPayload = toJson(document);
        Files.writeString(outputPath, jsonPayload, StandardCharsets.UTF_8);
    }

    /**
     * Добавляет в JSON строковое поле.
     */
    private static void appendStringField(
            StringBuilder jsonBuilder,
            int indentLevel,
            String fieldName,
            String fieldValue,
            boolean appendTrailingComma
    ) {
        jsonBuilder.append(indent(indentLevel))
                .append('"').append(fieldName).append("\" : ")
                .append('"').append(escapeJson(fieldValue)).append('"');

        if (appendTrailingComma) {
            jsonBuilder.append(',');
        }
        jsonBuilder.append(LINE_BREAK);
    }

    /**
     * Добавляет в JSON числовое поле.
     */
    private static void appendNumberField(
            StringBuilder jsonBuilder,
            int indentLevel,
            String fieldName,
            Number fieldValue,
            boolean appendTrailingComma
    ) {
        jsonBuilder.append(indent(indentLevel))
                .append('"').append(fieldName).append("\" : ")
                .append(fieldValue);

        if (appendTrailingComma) {
            jsonBuilder.append(',');
        }
        jsonBuilder.append(LINE_BREAK);
    }

    /**
     * Добавляет в JSON-массив значения высот в построчном порядке.
     */
    private static void appendHeights(StringBuilder jsonBuilder, HeightMapSnapshot heightMapSnapshot) {
        int mapWidth = heightMapSnapshot.width();
        int mapHeight = heightMapSnapshot.height();
        String arrayIndent = indent(3);

        for (int y = 0; y < mapHeight; y++) {
            jsonBuilder.append(arrayIndent);
            for (int x = 0; x < mapWidth; x++) {
                int linearIndex = heightMapSnapshot.indexOf(x, y);
                float heightValue = heightMapSnapshot.heightAt(x, y);
                jsonBuilder.append(Float.toString(heightValue));

                boolean isLastValueInRow = x == mapWidth - 1;
                boolean isLastValueInDocument = y == mapHeight - 1 && isLastValueInRow;
                if (!isLastValueInDocument) {
                    jsonBuilder.append(VALUE_SEPARATOR);
                }
            }
            if (y < mapHeight - 1) {
                jsonBuilder.append(LINE_BREAK);
            }
        }
    }

    /**
     * Возвращает строку отступа заданного уровня.
     */
    private static String indent(int level) {
        return INDENT_UNIT.repeat(level);
    }

    /**
     * Экранирует JSON-строку по минимально необходимым правилам.
     */
    private static String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
