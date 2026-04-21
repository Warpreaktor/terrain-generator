package ru.nik.terraingenerator.core.operation;

import ru.nik.terraingenerator.core.grid.TerrainGrid;
import ru.nik.terraingenerator.core.noise.ValueNoise2D;

/**
 * Добавляет к рельефу детерминированный шум.
 */
public final class AddValueNoiseOperation implements TerrainOperation {

    private static final float NOISE_VALUE_CENTER = 0.5f;
    private static final float NOISE_VALUE_SCALE = 2f;
    private static final float RIDGED_PEAK_VALUE = 1f;

    private final ValueNoise2D valueNoise2D;
    private final float featureSize;
    private final float amplitude;
    private final int octaveCount;
    private final float persistence;
    private final NoiseTransformMode noiseTransformMode;

    /**
     * Создаёт операцию добавления value noise.
     *
     * @param seed seed генератора шума
     * @param featureSize размер характерной детали в клетках
     * @param amplitude амплитуда влияния шума
     * @param octaveCount количество октав
     * @param persistence спад амплитуды между октавами
     */
    public AddValueNoiseOperation(long seed, float featureSize, float amplitude, int octaveCount, float persistence) {
        this(seed, featureSize, amplitude, octaveCount, persistence, NoiseTransformMode.STANDARD);
    }

    /**
     * Создаёт операцию добавления value noise с указанным режимом интерпретации шума.
     *
     * @param seed seed генератора шума
     * @param featureSize размер характерной детали в клетках
     * @param amplitude амплитуда влияния шума
     * @param octaveCount количество октав
     * @param persistence спад амплитуды между октавами
     * @param noiseTransformMode режим преобразования sampled значения
     */
    public AddValueNoiseOperation(
            long seed,
            float featureSize,
            float amplitude,
            int octaveCount,
            float persistence,
            NoiseTransformMode noiseTransformMode
    ) {
        if (featureSize <= 0f) {
            throw new IllegalArgumentException("Размер детали должен быть больше нуля.");
        }
        if (octaveCount <= 0) {
            throw new IllegalArgumentException("Количество октав должно быть больше нуля.");
        }
        if (persistence <= 0f) {
            throw new IllegalArgumentException("Persistence должно быть больше нуля.");
        }
        this.valueNoise2D = new ValueNoise2D(seed);
        this.featureSize = featureSize;
        this.amplitude = amplitude;
        this.octaveCount = octaveCount;
        this.persistence = persistence;
        this.noiseTransformMode = noiseTransformMode;
    }

    @Override
    public String name() {
        return "value-noise";
    }

    @Override
    public void apply(TerrainGrid terrainGrid) {
        for (int y = 0; y < terrainGrid.height(); y++) {
            for (int x = 0; x < terrainGrid.width(); x++) {
                float octaveAmplitude = 1f;
                float amplitudeSum = 0f;
                float accumulatedNoiseValue = 0f;
                float sampleScale = 1f / featureSize;

                for (int octaveIndex = 0; octaveIndex < octaveCount; octaveIndex++) {
                    float sampledNoiseValue = valueNoise2D.sample(x * sampleScale, y * sampleScale);
                    float transformedNoiseValue = transformNoiseValue(sampledNoiseValue);

                    accumulatedNoiseValue += transformedNoiseValue * octaveAmplitude;
                    amplitudeSum += octaveAmplitude;

                    octaveAmplitude *= persistence;
                    sampleScale *= 2f;
                }

                float normalizedNoiseValue = accumulatedNoiseValue / amplitudeSum;
                terrainGrid.addHeight(x, y, normalizedNoiseValue * amplitude);
            }
        }
    }

    /**
     * Преобразует sampled значение шума в зависимости от требуемого характера формы.
     *
     * @param sampledNoiseValue sampled значение генератора в диапазоне от 0 до 1
     * @return преобразованное значение в диапазоне около -1..1
     */
    private float transformNoiseValue(float sampledNoiseValue) {
        float centeredNoiseValue = sampledNoiseValue * NOISE_VALUE_SCALE - NOISE_VALUE_CENTER * NOISE_VALUE_SCALE;

        return switch (noiseTransformMode) {
            case STANDARD -> centeredNoiseValue;
            case RIDGED -> {
                float distanceFromCenter = Math.abs(centeredNoiseValue);
                yield (RIDGED_PEAK_VALUE - distanceFromCenter) * NOISE_VALUE_SCALE - RIDGED_PEAK_VALUE;
            }
            case BILLOW -> {
                float absoluteNoiseValue = Math.abs(centeredNoiseValue);
                yield absoluteNoiseValue * NOISE_VALUE_SCALE - RIDGED_PEAK_VALUE;
            }
        };
    }

}
