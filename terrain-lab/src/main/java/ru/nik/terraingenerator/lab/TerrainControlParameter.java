package ru.nik.terraingenerator.lab;

/**
 * Параметры, которые можно менять с панели управления лаборатории.
 */
public enum TerrainControlParameter {
    RELIEF_AMPLITUDE("Relief amplitude", 0.05f, 0f, 1.5f) {
        @Override
        public float currentValue(TerrainGeneratorSettings terrainGeneratorSettings) {
            return terrainGeneratorSettings.reliefAmplitude();
        }

        @Override
        protected void assignValue(TerrainGeneratorSettings terrainGeneratorSettings, float value) {
            terrainGeneratorSettings.setReliefAmplitude(value);
        }
    },
    MACRO_SCALE("Macro scale", 12f, 40f, 420f) {
        @Override
        public float currentValue(TerrainGeneratorSettings terrainGeneratorSettings) {
            return terrainGeneratorSettings.macroScale();
        }

        @Override
        protected void assignValue(TerrainGeneratorSettings terrainGeneratorSettings, float value) {
            terrainGeneratorSettings.setMacroScale(value);
        }
    },
    DETAIL_SCALE("Detail scale", 6f, 6f, 160f) {
        @Override
        public float currentValue(TerrainGeneratorSettings terrainGeneratorSettings) {
            return terrainGeneratorSettings.detailScale();
        }

        @Override
        protected void assignValue(TerrainGeneratorSettings terrainGeneratorSettings, float value) {
            terrainGeneratorSettings.setDetailScale(value);
        }
    },
    ROUGHNESS("Roughness", 0.05f, 0f, 1f) {
        @Override
        public float currentValue(TerrainGeneratorSettings terrainGeneratorSettings) {
            return terrainGeneratorSettings.roughness();
        }

        @Override
        protected void assignValue(TerrainGeneratorSettings terrainGeneratorSettings, float value) {
            terrainGeneratorSettings.setRoughness(value);
        }
    },
    SMOOTHNESS("Smoothness", 0.05f, 0f, 1f) {
        @Override
        public float currentValue(TerrainGeneratorSettings terrainGeneratorSettings) {
            return terrainGeneratorSettings.smoothness();
        }

        @Override
        protected void assignValue(TerrainGeneratorSettings terrainGeneratorSettings, float value) {
            terrainGeneratorSettings.setSmoothness(value);
        }
    },
    SLOPE_ANGLE("Slope angle", 15f, 0f, 360f, " deg") {
        @Override
        public float currentValue(TerrainGeneratorSettings terrainGeneratorSettings) {
            return terrainGeneratorSettings.slopeAngleDegrees();
        }

        @Override
        protected void assignValue(TerrainGeneratorSettings terrainGeneratorSettings, float value) {
            terrainGeneratorSettings.setSlopeAngleDegrees(value);
        }
    },
    SLOPE_STRENGTH("Slope strength", 0.05f, 0f, 1f) {
        @Override
        public float currentValue(TerrainGeneratorSettings terrainGeneratorSettings) {
            return terrainGeneratorSettings.slopeStrength();
        }

        @Override
        protected void assignValue(TerrainGeneratorSettings terrainGeneratorSettings, float value) {
            terrainGeneratorSettings.setSlopeStrength(value);
        }
    },
    RIDGE_STRENGTH("Ridge strength", 0.05f, 0f, 1f) {
        @Override
        public float currentValue(TerrainGeneratorSettings terrainGeneratorSettings) {
            return terrainGeneratorSettings.ridgeStrength();
        }

        @Override
        protected void assignValue(TerrainGeneratorSettings terrainGeneratorSettings, float value) {
            terrainGeneratorSettings.setRidgeStrength(value);
        }
    },
    ELEVATION_BIAS("Elevation bias", 0.05f, 0.55f, 2.20f) {
        @Override
        public float currentValue(TerrainGeneratorSettings terrainGeneratorSettings) {
            return terrainGeneratorSettings.elevationBias();
        }

        @Override
        protected void assignValue(TerrainGeneratorSettings terrainGeneratorSettings, float value) {
            terrainGeneratorSettings.setElevationBias(value);
        }
    };

    private static final float ACCELERATED_STEP_MULTIPLIER = 4f;

    private final String displayName;
    private final float baseStep;
    private final float minimumValue;
    private final float maximumValue;
    private final String valueSuffix;

    TerrainControlParameter(String displayName, float baseStep, float minimumValue, float maximumValue) {
        this(displayName, baseStep, minimumValue, maximumValue, "");
    }

    TerrainControlParameter(String displayName, float baseStep, float minimumValue, float maximumValue, String valueSuffix) {
        this.displayName = displayName;
        this.baseStep = baseStep;
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
        this.valueSuffix = valueSuffix;
    }

    /**
     * Возвращает отображаемое имя параметра.
     *
     * @return имя строки панели управления
     */
    public String displayName() {
        return displayName;
    }

    /**
     * Возвращает минимальное допустимое значение параметра.
     *
     * @return нижняя граница значения
     */
    public float minimumValue() {
        return minimumValue;
    }

    /**
     * Возвращает максимальное допустимое значение параметра.
     *
     * @return верхняя граница значения
     */
    public float maximumValue() {
        return maximumValue;
    }

    /**
     * Возвращает шаг ползунка параметра.
     *
     * @return минимальный шаг изменения значения
     */
    public float sliderStep() {
        return baseStep;
    }

    /**
     * Возвращает следующий параметр по кругу.
     *
     * @return следующий параметр
     */
    public TerrainControlParameter next() {
        TerrainControlParameter[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    /**
     * Возвращает предыдущий параметр по кругу.
     *
     * @return предыдущий параметр
     */
    public TerrainControlParameter previous() {
        TerrainControlParameter[] values = values();
        int previousIndex = ordinal() - 1;
        if (previousIndex < 0) {
            previousIndex = values.length - 1;
        }
        return values[previousIndex];
    }

    /**
     * Применяет изменение к выбранной настройке.
     *
     * @param terrainGeneratorSettings настройки генерации
     * @param direction направление изменения: {@code -1} или {@code 1}
     * @param accelerated {@code true}, если нужно использовать увеличенный шаг
     */
    public void adjust(TerrainGeneratorSettings terrainGeneratorSettings, int direction, boolean accelerated) {
        float stepMultiplier = accelerated ? ACCELERATED_STEP_MULTIPLIER : 1f;
        float signedDelta = baseStep * direction * stepMultiplier;
        float targetValue = currentValue(terrainGeneratorSettings) + signedDelta;
        assignValue(terrainGeneratorSettings, targetValue);
    }

    /**
     * Возвращает текущее значение параметра из набора настроек.
     *
     * @param terrainGeneratorSettings настройки генерации
     * @return текущее числовое значение параметра
     */
    public abstract float currentValue(TerrainGeneratorSettings terrainGeneratorSettings);

    /**
     * Устанавливает новое значение параметра в набор настроек.
     *
     * @param terrainGeneratorSettings настройки генерации
     * @param value новое значение параметра
     */
    public void setValue(TerrainGeneratorSettings terrainGeneratorSettings, float value) {
        assignValue(terrainGeneratorSettings, value);
    }

    /**
     * Возвращает строковое представление текущего значения параметра.
     *
     * @param terrainGeneratorSettings настройки генерации
     * @return строка со значением
     */
    public String displayValue(TerrainGeneratorSettings terrainGeneratorSettings) {
        return formatFloat(currentValue(terrainGeneratorSettings)) + valueSuffix;
    }

    /**
     * Записывает значение в соответствующее поле набора настроек.
     *
     * @param terrainGeneratorSettings настройки генерации
     * @param value новое значение параметра
     */
    protected abstract void assignValue(TerrainGeneratorSettings terrainGeneratorSettings, float value);

    /**
     * Форматирует числовое значение для вывода на панель.
     *
     * @param value исходное значение
     * @return форматированная строка
     */
    private String formatFloat(float value) {
        return "%.2f".formatted(value);
    }
}
