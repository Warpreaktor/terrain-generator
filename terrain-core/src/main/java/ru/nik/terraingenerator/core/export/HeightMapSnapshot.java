package ru.nik.terraingenerator.core.export;

import java.util.Arrays;

/**
 * Нейтральный снимок карты высот.
 *
 * <p>Снимок содержит только размеры сетки и плоский массив signed-высот.
 * Он ничего не знает о воде, биомах, минералах и других предметных слоях.
 * Порядок хранения высот — построчный: индекс вычисляется по формуле
 * {@code y * width + x}.</p>
 *
 * @param width ширина карты в клетках
 * @param height высота карты в клетках
 * @param heights плоский массив высот в построчном порядке
 */
public record HeightMapSnapshot(int width, int height, float[] heights) {

    /**
     * Создаёт снимок карты высот и проверяет его корректность.
     */
    public HeightMapSnapshot {
        if (width <= 0) {
            throw new IllegalArgumentException("Ширина карты высот должна быть больше нуля.");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("Высота карты высот должна быть больше нуля.");
        }
        if (heights == null) {
            throw new IllegalArgumentException("Массив высот не должен быть null.");
        }

        int expectedCellCount = width * height;
        int actualCellCount = heights.length;
        if (actualCellCount != expectedCellCount) {
            throw new IllegalArgumentException(
                    "Размер массива высот не совпадает с размером карты: expected=%d, actual=%d"
                            .formatted(expectedCellCount, actualCellCount)
            );
        }

        heights = Arrays.copyOf(heights, actualCellCount);
    }

    /**
     * Возвращает копию массива высот.
     *
     * @return копия внутреннего массива высот
     */
    @Override
    public float[] heights() {
        return Arrays.copyOf(heights, heights.length);
    }

    /**
     * Возвращает общее количество клеток в карте.
     *
     * @return количество клеток
     */
    public int cellCount() {
        return width * height;
    }

    /**
     * Возвращает высоту клетки по её координатам.
     *
     * @param x координата по оси X
     * @param y координата по оси Y
     * @return signed-высота клетки
     */
    public float heightAt(int x, int y) {
        int linearIndex = indexOf(x, y);
        return heights[linearIndex];
    }

    /**
     * Возвращает линейный индекс клетки в плоском массиве высот.
     *
     * @param x координата по оси X
     * @param y координата по оси Y
     * @return линейный индекс в массиве {@link #heights}
     */
    public int indexOf(int x, int y) {
        validateCoordinates(x, y);
        return y * width + x;
    }

    /**
     * Создаёт новый массив значений с теми же размерами, что и карта высот,
     * и заполняет его указанным значением.
     *
     * @param value значение заполнения
     * @return новый массив длиной {@link #cellCount()}, заполненный указанным значением
     */
    public float[] createFilledValues(float value) {
        float[] filledValues = new float[cellCount()];
        Arrays.fill(filledValues, value);
        return filledValues;
    }

    /**
     * Проверяет, что координаты находятся внутри карты.
     *
     * @param x координата по оси X
     * @param y координата по оси Y
     */
    public void validateCoordinates(int x, int y) {
        boolean insideHorizontalBounds = x >= 0 && x < width;
        boolean insideVerticalBounds = y >= 0 && y < height;
        if (!insideHorizontalBounds || !insideVerticalBounds) {
            throw new IndexOutOfBoundsException(
                    "Координата вне границ карты высот: x=%d, y=%d".formatted(x, y)
            );
        }
    }
}
