package ru.nik.terraingenerator.core.grid;

/**
 * Размер прямоугольной сетки рельефа.
 *
 * @param width  ширина сетки в клетках
 * @param height высота сетки в клетках
 */
public record GridSize(int width, int height) {

    /**
     * Создаёт размер сетки и проверяет корректность входных значений.
     */
    public GridSize {
        if (width <= 0) {
            throw new IllegalArgumentException("Ширина сетки должна быть больше нуля.");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("Высота сетки должна быть больше нуля.");
        }
    }

    /**
     * Возвращает общее количество клеток в сетке.
     *
     * @return количество клеток
     */
    public int cellCount() {
        return width * height;
    }
}
