package ru.nik.terraingenerator.core.noise;

/**
 * Детерминированный генератор двумерного value noise.
 */
public final class ValueNoise2D {

    private static final long HASH_MULTIPLIER_X = 0x9E3779B97F4A7C15L;
    private static final long HASH_MULTIPLIER_Y = 0xC2B2AE3D27D4EB4FL;
    private static final long HASH_MULTIPLIER_SEED = 0x165667B19E3779F9L;

    private final long seed;

    /**
     * Создаёт генератор шума с указанным seed.
     *
     * @param seed детерминирующий seed
     */
    public ValueNoise2D(long seed) {
        this.seed = seed;
    }

    /**
     * Возвращает значение шума в диапазоне от 0 до 1.
     *
     * @param x координата X
     * @param y координата Y
     * @return значение шума от 0 до 1
     */
    public float sample(float x, float y) {
        int baseX = fastFloor(x);
        int baseY = fastFloor(y);

        float localX = x - baseX;
        float localY = y - baseY;

        float corner00 = randomValue(baseX, baseY);
        float corner10 = randomValue(baseX + 1, baseY);
        float corner01 = randomValue(baseX, baseY + 1);
        float corner11 = randomValue(baseX + 1, baseY + 1);

        float smoothX = smoothStep(localX);
        float smoothY = smoothStep(localY);

        float lowerBand = linearInterpolation(corner00, corner10, smoothX);
        float upperBand = linearInterpolation(corner01, corner11, smoothX);
        return linearInterpolation(lowerBand, upperBand, smoothY);
    }

    /**
     * Возвращает псевдослучайное стабильное значение для узла сетки.
     *
     * @param x координата X
     * @param y координата Y
     * @return значение в диапазоне от 0 до 1
     */
    public float randomValue(int x, int y) {
        long hash = seed * HASH_MULTIPLIER_SEED;
        hash ^= x * HASH_MULTIPLIER_X;
        hash ^= y * HASH_MULTIPLIER_Y;
        hash ^= (hash >>> 33);
        hash *= 0xff51afd7ed558ccdl;
        hash ^= (hash >>> 33);
        hash *= 0xc4ceb9fe1a85ec53l;
        hash ^= (hash >>> 33);

        long positiveHash = hash & 0x7fffffffffffffffL;
        return positiveHash / (float) Long.MAX_VALUE;
    }

    /**
     * Выполняет плавное сглаживание коэффициента интерполяции.
     *
     * @param alpha исходный коэффициент
     * @return сглаженный коэффициент
     */
    public float smoothStep(float alpha) {
        return alpha * alpha * (3f - 2f * alpha);
    }

    /**
     * Возвращает линейную интерполяцию между двумя значениями.
     *
     * @param startValue начальное значение
     * @param endValue конечное значение
     * @param alpha коэффициент от 0 до 1
     * @return интерполированное значение
     */
    public float linearInterpolation(float startValue, float endValue, float alpha) {
        return startValue + (endValue - startValue) * alpha;
    }

    /**
     * Быстро округляет число вниз.
     *
     * @param value исходное значение
     * @return целая часть вниз
     */
    public int fastFloor(float value) {
        int truncated = (int) value;
        return value < truncated ? truncated - 1 : truncated;
    }
}
