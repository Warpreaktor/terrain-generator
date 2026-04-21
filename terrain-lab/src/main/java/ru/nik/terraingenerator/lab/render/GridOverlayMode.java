package ru.nik.terraingenerator.lab.render;

/**
 * Режимы отображения сетки поверх карты высот.
 */
public enum GridOverlayMode {
    OFF("off", 0f),
    LIGHT("light", 0.10f),
    STRONG("strong", 0.24f);

    private final String displayName;
    private final float alpha;

    GridOverlayMode(String displayName, float alpha) {
        this.displayName = displayName;
        this.alpha = alpha;
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
     * Возвращает прозрачность линий сетки.
     *
     * @return значение прозрачности
     */
    public float alpha() {
        return alpha;
    }

    /**
     * Возвращает следующий режим по кругу.
     *
     * @return следующий режим
     */
    public GridOverlayMode next() {
        GridOverlayMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    @Override
    public String toString() {
        return displayName;
    }

}
