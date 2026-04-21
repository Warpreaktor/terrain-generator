package ru.nik.terraingenerator.lab;

import java.util.ArrayList;
import java.util.List;
import ru.nik.terraingenerator.core.grid.GridSize;
import ru.nik.terraingenerator.core.grid.TerrainGrid;
import ru.nik.terraingenerator.core.operation.AddValueNoiseOperation;
import ru.nik.terraingenerator.core.operation.FillOperation;
import ru.nik.terraingenerator.core.operation.HeightCurveOperation;
import ru.nik.terraingenerator.core.operation.NoiseTransformMode;
import ru.nik.terraingenerator.core.operation.NormalizeOperation;
import ru.nik.terraingenerator.core.operation.SlopeOperation;
import ru.nik.terraingenerator.core.operation.SmoothOperation;
import ru.nik.terraingenerator.core.operation.TerrainOperation;
import ru.nik.terraingenerator.core.operation.TerrainOperationPipeline;

/**
 * Собирает процедурную карту рельефа из низкоуровневых операций библиотеки.
 *
 * Генератор управляется только морфологическими параметрами и не знает ничего
 * о предметной интерпретации мира.
 */
public final class ProceduralTerrainGenerator {

    private static final float NORMALIZED_MIN_HEIGHT = 0f;
    private static final float NORMALIZED_MAX_HEIGHT = 1f;

    private static final long MACRO_NOISE_SEED_OFFSET = 101L;
    private static final long RIDGE_NOISE_SEED_OFFSET = 211L;
    private static final long DETAIL_NOISE_SEED_OFFSET = 307L;

    private static final float MACRO_PERSISTENCE = 0.58f;
    private static final float RIDGE_PERSISTENCE = 0.60f;
    private static final float DETAIL_PERSISTENCE_MIN = 0.42f;
    private static final float DETAIL_PERSISTENCE_MAX = 0.68f;

    private static final float MACRO_AMPLITUDE_MIN_FACTOR = 0.28f;
    private static final float MACRO_AMPLITUDE_MAX_FACTOR = 0.82f;
    private static final float DETAIL_AMPLITUDE_MIN_FACTOR = 0.05f;
    private static final float DETAIL_AMPLITUDE_MAX_FACTOR = 0.34f;
    private static final float RIDGE_AMPLITUDE_FACTOR = 0.62f;
    private static final float RIDGE_SCALE_FACTOR = 0.68f;
    private static final float SLOPE_AMPLITUDE_FACTOR = 1f;

    private static final int MIN_MACRO_OCTAVE_COUNT = 2;
    private static final int MAX_MACRO_OCTAVE_COUNT = 4;
    private static final int MIN_DETAIL_OCTAVE_COUNT = 1;
    private static final int MAX_DETAIL_OCTAVE_COUNT = 5;
    private static final int MAX_SMOOTH_ITERATION_COUNT = 6;

    private static final float EPSILON = 0.0001f;

    private ProceduralTerrainGenerator() {
    }

    /**
     * Создаёт одну процедурную сессию генерации по текущему набору параметров.
     *
     * @param gridSize размер сетки
     * @param seed базовый seed генерации
     * @param terrainGeneratorSettings морфологические настройки рельефа
     * @return процедурная сессия с итоговой картой и промежуточными снимками
     */
    public static TerrainLabSession createSession(
            GridSize gridSize,
            long seed,
            TerrainGeneratorSettings terrainGeneratorSettings
    ) {
        TerrainGrid terrainGrid = new TerrainGrid(gridSize);
        TerrainOperationPipeline terrainOperationPipeline = buildPipeline(seed, terrainGeneratorSettings);
        List<TerrainGrid> pipelineSnapshots = terrainOperationPipeline.snapshots(terrainGrid);
        List<String> pipelineStepNames = extractPipelineStepNames(terrainOperationPipeline);
        TerrainGrid finalTerrainGrid = pipelineSnapshots.isEmpty() ? terrainGrid.copy() : pipelineSnapshots.getLast();

        return new TerrainLabSession(finalTerrainGrid, pipelineSnapshots, pipelineStepNames);
    }

    /**
     * Собирает pipeline низкоуровневых операций на основе параметров генерации.
     *
     * @param seed базовый seed генерации
     * @param terrainGeneratorSettings настройки морфологии поверхности
     * @return собранный pipeline операций
     */
    private static TerrainOperationPipeline buildPipeline(long seed, TerrainGeneratorSettings terrainGeneratorSettings) {
        TerrainOperationPipeline terrainOperationPipeline = new TerrainOperationPipeline();
        terrainOperationPipeline.add(new FillOperation(0f));

        float reliefAmplitude = terrainGeneratorSettings.reliefAmplitude();
        float smoothness = terrainGeneratorSettings.smoothness();
        float roughness = terrainGeneratorSettings.roughness();
        float ridgeStrength = terrainGeneratorSettings.ridgeStrength();

        float slopeAngleRadians = (float) Math.toRadians(terrainGeneratorSettings.slopeAngleDegrees());
        float slopeDirectionX = (float) Math.cos(slopeAngleRadians);
        float slopeDirectionY = (float) Math.sin(slopeAngleRadians);
        float slopeAmplitude = reliefAmplitude * terrainGeneratorSettings.slopeStrength() * SLOPE_AMPLITUDE_FACTOR;

        float macroAmplitudeFactor = lerp(MACRO_AMPLITUDE_MIN_FACTOR, MACRO_AMPLITUDE_MAX_FACTOR, 1f - smoothness * 0.5f);
        float macroNoiseAmplitude = reliefAmplitude * macroAmplitudeFactor;
        float detailAmplitudeFactor = lerp(DETAIL_AMPLITUDE_MIN_FACTOR, DETAIL_AMPLITUDE_MAX_FACTOR, roughness);
        float detailNoiseAmplitude = reliefAmplitude * detailAmplitudeFactor;
        float ridgeNoiseAmplitude = reliefAmplitude * ridgeStrength * RIDGE_AMPLITUDE_FACTOR;
        float ridgeNoiseScale = terrainGeneratorSettings.macroScale() * RIDGE_SCALE_FACTOR;
        float detailPersistence = lerp(DETAIL_PERSISTENCE_MIN, DETAIL_PERSISTENCE_MAX, roughness);

        int macroOctaveCount = roundToInt(lerp(MIN_MACRO_OCTAVE_COUNT, MAX_MACRO_OCTAVE_COUNT, roughness));
        int detailOctaveCount = roundToInt(lerp(MIN_DETAIL_OCTAVE_COUNT, MAX_DETAIL_OCTAVE_COUNT, roughness));
        int smoothIterationCount = roundToInt(lerp(0f, MAX_SMOOTH_ITERATION_COUNT, smoothness));

        if (slopeAmplitude > EPSILON) {
            terrainOperationPipeline.add(new SlopeOperation(slopeDirectionX, slopeDirectionY, slopeAmplitude));
        }

        terrainOperationPipeline.add(new AddValueNoiseOperation(
                seed + MACRO_NOISE_SEED_OFFSET,
                terrainGeneratorSettings.macroScale(),
                macroNoiseAmplitude,
                macroOctaveCount,
                MACRO_PERSISTENCE,
                NoiseTransformMode.STANDARD
        ));

        if (ridgeNoiseAmplitude > EPSILON) {
            terrainOperationPipeline.add(new AddValueNoiseOperation(
                    seed + RIDGE_NOISE_SEED_OFFSET,
                    ridgeNoiseScale,
                    ridgeNoiseAmplitude,
                    macroOctaveCount,
                    RIDGE_PERSISTENCE,
                    NoiseTransformMode.RIDGED
            ));
        }

        terrainOperationPipeline.add(new AddValueNoiseOperation(
                seed + DETAIL_NOISE_SEED_OFFSET,
                terrainGeneratorSettings.detailScale(),
                detailNoiseAmplitude,
                detailOctaveCount,
                detailPersistence,
                NoiseTransformMode.BILLOW
        ));

        if (smoothIterationCount > 0) {
            terrainOperationPipeline.add(new SmoothOperation(smoothIterationCount));
        }

        terrainOperationPipeline.add(new NormalizeOperation(NORMALIZED_MIN_HEIGHT, NORMALIZED_MAX_HEIGHT));

        float elevationBias = terrainGeneratorSettings.elevationBias();
        if (Math.abs(elevationBias - 1f) > EPSILON) {
            terrainOperationPipeline.add(new HeightCurveOperation(elevationBias));
        }

        return terrainOperationPipeline;
    }

    /**
     * Собирает список имён операций pipeline для отладки и панели управления.
     *
     * @param terrainOperationPipeline pipeline операций
     * @return список имён шагов
     */
    private static List<String> extractPipelineStepNames(TerrainOperationPipeline terrainOperationPipeline) {
        List<String> pipelineStepNames = new ArrayList<>(terrainOperationPipeline.operations().size());
        for (TerrainOperation terrainOperation : terrainOperationPipeline.operations()) {
            pipelineStepNames.add(terrainOperation.name());
        }
        return pipelineStepNames;
    }

    /**
     * Линейно смешивает два значения.
     *
     * @param minimum начальное значение
     * @param maximum конечное значение
     * @param alpha коэффициент смешивания
     * @return смешанное значение
     */
    private static float lerp(float minimum, float maximum, float alpha) {
        return minimum + (maximum - minimum) * alpha;
    }

    /**
     * Округляет вещественное значение до ближайшего целого.
     *
     * @param value исходное значение
     * @return округлённое целое значение
     */
    private static int roundToInt(float value) {
        return Math.round(value);
    }
}
