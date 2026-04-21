package ru.nik.terraingenerator.lab;

/**
 * Инструменты ручной правки рельефа.
 */
public enum SculptTool {
    RAISE("raise"),
    LOWER("lower"),
    SMOOTH("smooth"),
    FLATTEN("flatten");

    private final String displayName;

    SculptTool(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Возвращает имя инструмента для панели управления.
     *
     * @return отображаемое имя инструмента
     */
    public String displayName() {
        return displayName;
    }

    /**
     * Возвращает следующий инструмент по кругу.
     *
     * @return следующий инструмент
     */
    public SculptTool next() {
        SculptTool[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    @Override
    public String toString() {
        return displayName;
    }

}
