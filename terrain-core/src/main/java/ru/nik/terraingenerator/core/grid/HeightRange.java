package ru.nik.terraingenerator.core.grid;

/**
 * Диапазон значений высоты в сетке.
 *
 * @param minimum минимальная высота
 * @param maximum максимальная высота
 */
public record HeightRange(float minimum, float maximum) {

    /**
     * Создаёт диапазон и проверяет его корректность.
     */
    public HeightRange {
        if (minimum > maximum) {
            throw new IllegalArgumentException("Минимальная высота не может быть больше максимальной.");
        }
    }

    /**
     * Возвращает ширину диапазона.
     *
     * @return разница между максимумом и минимумом
     */
    public float span() {
        return maximum - minimum;
    }
}
