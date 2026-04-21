package ru.nik.terraingenerator.core.storage;

import java.util.Arrays;

/**
 * Сплошное хранение высот в обычном массиве {@code float[]}.
 */
public final class DenseFloatHeightStorage implements HeightStorage {

    private final float[] values;

    /**
     * Создаёт хранилище указанного размера.
     *
     * @param size количество значений
     */
    public DenseFloatHeightStorage(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Размер хранилища должен быть больше нуля.");
        }
        this.values = new float[size];
    }

    private DenseFloatHeightStorage(float[] values) {
        this.values = values;
    }

    @Override
    public float get(int index) {
        return values[index];
    }

    @Override
    public void set(int index, float value) {
        values[index] = value;
    }

    @Override
    public void add(int index, float delta) {
        values[index] += delta;
    }

    @Override
    public int size() {
        return values.length;
    }

    @Override
    public HeightStorage copy() {
        return new DenseFloatHeightStorage(Arrays.copyOf(values, values.length));
    }
}
