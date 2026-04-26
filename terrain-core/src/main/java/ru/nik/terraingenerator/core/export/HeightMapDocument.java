package ru.nik.terraingenerator.core.export;

/**
 * Версионируемый документ карты высот.
 *
 * <p>Документ служит нейтральным контейнером для сериализации и интеграций.
 * Он описывает только формат, версию, имя и саму карту высот. Любая предметная
 * информация внешних модулей должна жить поверх этого документа, а не внутри него.</p>
 *
 * @param format строковый идентификатор формата
 * @param version версия формата
 * @param name человекочитаемое имя документа
 * @param heightMap снимок карты высот
 */
public record HeightMapDocument(String format, int version, String name, HeightMapSnapshot heightMap) {

    /** Идентификатор нейтрального формата карты высот. */
    public static final String DEFAULT_FORMAT = "height-map";

    /** Первая версия формата карты высот. */
    public static final int DEFAULT_VERSION = 1;

    /** Имя документа по умолчанию. */
    public static final String DEFAULT_NAME = "generated-map";

    /**
     * Создаёт документ и проверяет его корректность.
     */
    public HeightMapDocument {
        if (format == null || format.isBlank()) {
            throw new IllegalArgumentException("Формат документа карты высот не должен быть пустым.");
        }
        if (version <= 0) {
            throw new IllegalArgumentException("Версия документа карты высот должна быть больше нуля.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Имя документа карты высот не должно быть пустым.");
        }
        if (heightMap == null) {
            throw new IllegalArgumentException("Снимок карты высот не должен быть null.");
        }
    }
}
