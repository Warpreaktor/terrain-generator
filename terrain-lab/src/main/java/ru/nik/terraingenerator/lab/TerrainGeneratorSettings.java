package ru.nik.terraingenerator.lab;

/**
 * Набор параметров процедурной генерации рельефа.
 *
 * Здесь описывается только морфология поверхности:
 * масштаб форм, сила перепадов, шероховатость, сглаженность
 * и направленный уклон. Никакой предметной логики мира в этих
 * настройках нет.
 */
public final class TerrainGeneratorSettings {

    private static final float MIN_RELIEF_AMPLITUDE = 0f;
    private static final float MAX_RELIEF_AMPLITUDE = 1.5f;

    private static final float MIN_MACRO_SCALE = 40f;
    private static final float MAX_MACRO_SCALE = 420f;

    private static final float MIN_DETAIL_SCALE = 6f;
    private static final float MAX_DETAIL_SCALE = 160f;

    private static final float MIN_NORMALIZED_FACTOR = 0f;
    private static final float MAX_NORMALIZED_FACTOR = 1f;

    private static final float MIN_SLOPE_ANGLE_DEGREES = 0f;
    private static final float FULL_CIRCLE_DEGREES = 360f;

    private static final float MIN_ELEVATION_BIAS = 0.55f;
    private static final float MAX_ELEVATION_BIAS = 2.20f;

    private float reliefAmplitude;
    private float macroScale;
    private float detailScale;
    private float roughness;
    private float smoothness;
    private float slopeAngleDegrees;
    private float slopeStrength;
    private float ridgeStrength;
    private float elevationBias;

    /**
     * Создаёт новый набор параметров.
     *
     * @param reliefAmplitude общая амплитуда перепадов высоты
     * @param macroScale размер крупных форм в клетках
     * @param detailScale размер мелких деталей в клетках
     * @param roughness шероховатость микрорельефа в диапазоне от 0 до 1
     * @param smoothness степень сглаженности в диапазоне от 0 до 1
     * @param slopeAngleDegrees угол общего уклона в градусах
     * @param slopeStrength сила общего уклона в диапазоне от 0 до 1
     * @param ridgeStrength выраженность вытянутых гряд в диапазоне от 0 до 1
     * @param elevationBias смещение распределения высот после нормализации
     */
    public TerrainGeneratorSettings(
            float reliefAmplitude,
            float macroScale,
            float detailScale,
            float roughness,
            float smoothness,
            float slopeAngleDegrees,
            float slopeStrength,
            float ridgeStrength,
            float elevationBias
    ) {
        setReliefAmplitude(reliefAmplitude);
        setMacroScale(macroScale);
        setDetailScale(detailScale);
        setRoughness(roughness);
        setSmoothness(smoothness);
        setSlopeAngleDegrees(slopeAngleDegrees);
        setSlopeStrength(slopeStrength);
        setRidgeStrength(ridgeStrength);
        setElevationBias(elevationBias);
    }

    /**
     * Возвращает копию набора параметров.
     *
     * @return независимая копия настроек
     */
    public TerrainGeneratorSettings copy() {
        return new TerrainGeneratorSettings(
                reliefAmplitude,
                macroScale,
                detailScale,
                roughness,
                smoothness,
                slopeAngleDegrees,
                slopeStrength,
                ridgeStrength,
                elevationBias
        );
    }

    /**
     * Создаёт настройки по одному из предустановленных профилей.
     *
     * @param terrainGeneratorPreset профиль рельефа
     * @return новый набор параметров
     */
    public static TerrainGeneratorSettings fromPreset(TerrainGeneratorPreset terrainGeneratorPreset) {
        return terrainGeneratorPreset.createSettings();
    }

    public float reliefAmplitude() {
        return reliefAmplitude;
    }

    public float macroScale() {
        return macroScale;
    }

    public float detailScale() {
        return detailScale;
    }

    public float roughness() {
        return roughness;
    }

    public float smoothness() {
        return smoothness;
    }

    public float slopeAngleDegrees() {
        return slopeAngleDegrees;
    }

    public float slopeStrength() {
        return slopeStrength;
    }

    public float ridgeStrength() {
        return ridgeStrength;
    }

    public float elevationBias() {
        return elevationBias;
    }

    /**
     * Явно устанавливает общую амплитуду рельефа.
     *
     * @param value новое значение амплитуды
     */
    public void setReliefAmplitude(float value) {
        reliefAmplitude = clamp(value, MIN_RELIEF_AMPLITUDE, MAX_RELIEF_AMPLITUDE);
    }

    /**
     * Явно устанавливает масштаб крупных форм.
     *
     * @param value новое значение масштаба
     */
    public void setMacroScale(float value) {
        macroScale = clamp(value, MIN_MACRO_SCALE, MAX_MACRO_SCALE);
    }

    /**
     * Явно устанавливает масштаб мелких деталей.
     *
     * @param value новое значение масштаба
     */
    public void setDetailScale(float value) {
        detailScale = clamp(value, MIN_DETAIL_SCALE, MAX_DETAIL_SCALE);
    }

    /**
     * Явно устанавливает шероховатость поверхности.
     *
     * @param value новое значение шероховатости
     */
    public void setRoughness(float value) {
        roughness = clamp(value, MIN_NORMALIZED_FACTOR, MAX_NORMALIZED_FACTOR);
    }

    /**
     * Явно устанавливает сглаженность поверхности.
     *
     * @param value новое значение сглаженности
     */
    public void setSmoothness(float value) {
        smoothness = clamp(value, MIN_NORMALIZED_FACTOR, MAX_NORMALIZED_FACTOR);
    }

    /**
     * Явно устанавливает угол общего уклона.
     *
     * @param valueDegrees новое значение угла в градусах
     */
    public void setSlopeAngleDegrees(float valueDegrees) {
        slopeAngleDegrees = wrapAngle(valueDegrees);
    }

    /**
     * Явно устанавливает силу общего уклона.
     *
     * @param value новое значение силы уклона
     */
    public void setSlopeStrength(float value) {
        slopeStrength = clamp(value, MIN_NORMALIZED_FACTOR, MAX_NORMALIZED_FACTOR);
    }

    /**
     * Явно устанавливает выраженность гряд.
     *
     * @param value новое значение выраженности гряд
     */
    public void setRidgeStrength(float value) {
        ridgeStrength = clamp(value, MIN_NORMALIZED_FACTOR, MAX_NORMALIZED_FACTOR);
    }

    /**
     * Явно устанавливает смещение распределения высот.
     *
     * @param value новое значение смещения распределения
     */
    public void setElevationBias(float value) {
        elevationBias = clamp(value, MIN_ELEVATION_BIAS, MAX_ELEVATION_BIAS);
    }

    /**
     * Добавляет смещение к общей амплитуде рельефа.
     *
     * @param delta величина изменения
     */
    public void adjustReliefAmplitude(float delta) {
        setReliefAmplitude(reliefAmplitude + delta);
    }

    /**
     * Добавляет смещение к масштабу крупных форм.
     *
     * @param delta величина изменения
     */
    public void adjustMacroScale(float delta) {
        setMacroScale(macroScale + delta);
    }

    /**
     * Добавляет смещение к масштабу мелких деталей.
     *
     * @param delta величина изменения
     */
    public void adjustDetailScale(float delta) {
        setDetailScale(detailScale + delta);
    }

    /**
     * Добавляет смещение к шероховатости рельефа.
     *
     * @param delta величина изменения
     */
    public void adjustRoughness(float delta) {
        setRoughness(roughness + delta);
    }

    /**
     * Добавляет смещение к сглаженности рельефа.
     *
     * @param delta величина изменения
     */
    public void adjustSmoothness(float delta) {
        setSmoothness(smoothness + delta);
    }

    /**
     * Поворачивает общий уклон рельефа.
     *
     * @param deltaDegrees величина изменения угла в градусах
     */
    public void adjustSlopeAngleDegrees(float deltaDegrees) {
        setSlopeAngleDegrees(slopeAngleDegrees + deltaDegrees);
    }

    /**
     * Добавляет смещение к силе глобального уклона.
     *
     * @param delta величина изменения
     */
    public void adjustSlopeStrength(float delta) {
        setSlopeStrength(slopeStrength + delta);
    }

    /**
     * Добавляет смещение к выраженности гряд.
     *
     * @param delta величина изменения
     */
    public void adjustRidgeStrength(float delta) {
        setRidgeStrength(ridgeStrength + delta);
    }

    /**
     * Добавляет смещение к смещению распределения высот.
     *
     * @param delta величина изменения
     */
    public void adjustElevationBias(float delta) {
        setElevationBias(elevationBias + delta);
    }

    /**
     * Ограничивает значение заданным диапазоном.
     *
     * @param value исходное значение
     * @param minimum нижняя граница
     * @param maximum верхняя граница
     * @return ограниченное значение
     */
    private float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    /**
     * Нормализует угол уклона в пределах полного круга.
     *
     * @param rawAngleDegrees исходное значение угла
     * @return нормализованный угол в градусах
     */
    private float wrapAngle(float rawAngleDegrees) {
        float normalizedAngle = rawAngleDegrees % FULL_CIRCLE_DEGREES;
        if (normalizedAngle < MIN_SLOPE_ANGLE_DEGREES) {
            normalizedAngle += FULL_CIRCLE_DEGREES;
        }
        return normalizedAngle;
    }
}
