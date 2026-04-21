package ru.nik.terraingenerator.lab;

/**
 * Режимы работы лаборатории рельефа.
 */
public enum TerrainInteractionMode {
    PROCEDURAL("procedural"),
    SCULPT("sculpt");

    private final String displayName;

    TerrainInteractionMode(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Возвращает имя режима для панели управления.
     *
     * @return отображаемое имя режима
     */
    public String displayName() {
        return displayName;
    }

    /**
     * Возвращает следующий режим по кругу.
     *
     * @return следующий режим взаимодействия
     */
    public TerrainInteractionMode next() {
        TerrainInteractionMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    @Override
    public String toString() {
        return displayName;
    }

}
