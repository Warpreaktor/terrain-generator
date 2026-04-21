package ru.nik.terraingenerator.core.grid;

import ru.nik.terraingenerator.core.storage.DenseFloatHeightStorage;
import ru.nik.terraingenerator.core.storage.HeightStorage;

/**
 * Изменяемая сетка высот.
 *
 * Сетка хранит только геометрию поверхности и ничего не знает
 * о предметном смысле этой поверхности.
 */
public final class TerrainGrid {

    private final GridSize size;
    private final HeightStorage storage;

    /**
     * Создаёт новую сетку с плотным хранением данных.
     *
     * @param size размер сетки
     */
    public TerrainGrid(GridSize size) {
        this(size, new DenseFloatHeightStorage(size.cellCount()));
    }

    /**
     * Создаёт новую сетку на основе указанного хранилища.
     *
     * @param size размер сетки
     * @param storage хранилище высот
     */
    public TerrainGrid(GridSize size, HeightStorage storage) {
        if (storage.size() != size.cellCount()) {
            throw new IllegalArgumentException("Размер хранилища не совпадает с размером сетки.");
        }
        this.size = size;
        this.storage = storage;
    }

    /**
     * Возвращает размер сетки.
     *
     * @return размер сетки
     */
    public GridSize size() {
        return size;
    }

    /**
     * Возвращает ширину сетки.
     *
     * @return ширина в клетках
     */
    public int width() {
        return size.width();
    }

    /**
     * Возвращает высоту сетки.
     *
     * @return высота в клетках
     */
    public int height() {
        return size.height();
    }

    /**
     * Проверяет, попадает ли координата в границы сетки.
     *
     * @param x координата по оси X
     * @param y координата по оси Y
     * @return {@code true}, если координата находится внутри сетки
     */
    public boolean contains(int x, int y) {
        return x >= 0 && x < width() && y >= 0 && y < height();
    }

    /**
     * Возвращает высоту клетки.
     *
     * @param x координата по оси X
     * @param y координата по оси Y
     * @return значение высоты
     */
    public float getHeight(int x, int y) {
        return storage.get(indexOf(x, y));
    }

    /**
     * Устанавливает высоту клетки.
     *
     * @param x координата по оси X
     * @param y координата по оси Y
     * @param value новое значение высоты
     */
    public void setHeight(int x, int y, float value) {
        storage.set(indexOf(x, y), value);
    }

    /**
     * Добавляет смещение к высоте клетки.
     *
     * @param x координата по оси X
     * @param y координата по оси Y
     * @param delta добавляемое смещение
     */
    public void addHeight(int x, int y, float delta) {
        storage.add(indexOf(x, y), delta);
    }

    /**
     * Возвращает диапазон высот по всей сетке.
     *
     * @return диапазон высот
     */
    public HeightRange heightRange() {
        float minimumHeight = Float.POSITIVE_INFINITY;
        float maximumHeight = Float.NEGATIVE_INFINITY;

        for (int y = 0; y < height(); y++) {
            for (int x = 0; x < width(); x++) {
                float currentHeight = getHeight(x, y);
                if (currentHeight < minimumHeight) {
                    minimumHeight = currentHeight;
                }
                if (currentHeight > maximumHeight) {
                    maximumHeight = currentHeight;
                }
            }
        }

        return new HeightRange(minimumHeight, maximumHeight);
    }

    /**
     * Создаёт независимую копию сетки.
     *
     * @return копия сетки
     */
    public TerrainGrid copy() {
        return new TerrainGrid(size, storage.copy());
    }

    /**
     * Преобразует двумерную координату клетки во внутренний линейный индекс.
     *
     * @param x координата по оси X
     * @param y координата по оси Y
     * @return линейный индекс
     */
    public int indexOf(int x, int y) {
        validateCoordinates(x, y);
        return y * width() + x;
    }

    /**
     * Проверяет, что координата находится внутри сетки.
     *
     * @param x координата по оси X
     * @param y координата по оси Y
     */
    public void validateCoordinates(int x, int y) {
        if (!contains(x, y)) {
            throw new IndexOutOfBoundsException("Координата вне границ сетки: x=%d, y=%d".formatted(x, y));
        }
    }
}
