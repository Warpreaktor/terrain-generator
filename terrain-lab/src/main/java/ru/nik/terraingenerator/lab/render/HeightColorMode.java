package ru.nik.terraingenerator.lab.render;

/**
 * Цветовые режимы отображения сетки высот.
 */
public enum HeightColorMode {
    GRAYSCALE("grayscale"),
    TOPOGRAPHIC("topographic");

    private final String displayName;

    HeightColorMode(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Возвращает имя режима для HUD.
     *
     * @return отображаемое имя
     */
    public String displayName() {
        return displayName;
    }

    /**
     * Возвращает следующий режим по кругу.
     *
     * @return следующий режим
     */
    public HeightColorMode next() {
        HeightColorMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    @Override
    public String toString() {
        return displayName;
    }

}
