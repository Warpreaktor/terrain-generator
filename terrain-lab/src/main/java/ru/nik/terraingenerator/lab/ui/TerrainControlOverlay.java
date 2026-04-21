package ru.nik.terraingenerator.lab.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import ru.nik.terraingenerator.lab.SculptTool;
import ru.nik.terraingenerator.lab.TerrainControlParameter;
import ru.nik.terraingenerator.lab.TerrainGeneratorPreset;
import ru.nik.terraingenerator.lab.TerrainGeneratorSettings;
import ru.nik.terraingenerator.lab.TerrainInteractionMode;
import ru.nik.terraingenerator.lab.render.GridOverlayMode;
import ru.nik.terraingenerator.lab.render.HeightColorMode;

/**
 * Отдельная overlay-панель управления, которая живёт поверх рендера карты.
 *
 * <p>Панель построена на Scene2D.UI и не знает, как именно генерируется рельеф.
 * Она только показывает текущие параметры и передаёт изменения наружу через listener.</p>
 */
public final class TerrainControlOverlay implements Disposable {

    private static final float PANEL_WIDTH = 430f;
    private static final float PANEL_MARGIN = 16f;
    private static final float WINDOW_PADDING = 12f;
    private static final float SECTION_TOP_PADDING = 10f;
    private static final float SECTION_BOTTOM_PADDING = 4f;
    private static final float ROW_PADDING = 6f;
    private static final float CONTROL_ROW_HEIGHT = 28f;
    private static final float SLIDER_ROW_HEIGHT = 18f;
    private static final float VALUE_LABEL_MIN_WIDTH = 84f;
    private static final float BUTTON_MIN_WIDTH = 96f;
    private static final float SECTION_SPACING = 10f;
    private static final float WINDOW_HEIGHT_MARGIN = 32f;
    private static final int GRID_STEP_OPTION_COUNT = 4;
    private static final int[] GRID_STEP_OPTIONS = {1, 2, 4, 8};
    private static final float FLATTEN_TARGET_MINIMUM = 0f;
    private static final float FLATTEN_TARGET_MAXIMUM = 1f;
    private static final float FLATTEN_TARGET_STEP = 0.01f;
    private static final float BRUSH_RADIUS_STEP = 1f;
    private static final float BRUSH_STRENGTH_STEP = 0.01f;
    private static final float BRUSH_RADIUS_MINIMUM = 1f;
    private static final float BRUSH_RADIUS_MAXIMUM = 80f;
    private static final float BRUSH_STRENGTH_MINIMUM = 0.01f;
    private static final float BRUSH_STRENGTH_MAXIMUM = 0.60f;
    private static final String EMPTY_SEED_TEXT = "";
    private static final String APPLY_GENERATOR_BUTTON_TEXT = "Apply generation";
    private static final String INCREMENT_SEED_BUTTON_TEXT = "New seed";
    private static final String RESET_PRESET_BUTTON_TEXT = "Reset from preset";
    private static final String CLEAR_SCULPT_BUTTON_TEXT = "Clear sculpt layer";

    private final Listener listener;
    private final Stage stage;
    private final Skin skin;
    private final Window window;
    private final Vector2 stageHitCoordinates = new Vector2();

    private final TextField seedTextField;
    private final SelectBox<TerrainGeneratorPreset> presetSelectBox;
    private final SelectBox<HeightColorMode> heightColorModeSelectBox;
    private final SelectBox<GridOverlayMode> gridOverlayModeSelectBox;
    private final SelectBox<Integer> gridStepSelectBox;
    private final SelectBox<TerrainInteractionMode> interactionModeSelectBox;
    private final SelectBox<SculptTool> sculptToolSelectBox;
    private final Slider brushRadiusSlider;
    private final Slider brushStrengthSlider;
    private final Slider flattenTargetSlider;
    private final ObjectMap<TerrainControlParameter, Slider> parameterSliderByControl = new ObjectMap<>();
    private final ObjectMap<TerrainControlParameter, Label> parameterValueLabelByControl = new ObjectMap<>();
    private final Label brushRadiusValueLabel;
    private final Label brushStrengthValueLabel;
    private final Label flattenTargetValueLabel;

    private boolean updatingControls;

    /**
     * Слушатель изменений панели управления.
     */
    public interface Listener {

        /**
         * Применяет выбранный пресет к генератору.
         *
         * @param terrainGeneratorPreset выбранный профиль рельефа
         */
        void onPresetSelected(TerrainGeneratorPreset terrainGeneratorPreset);

        /**
         * Повторно запускает генерацию с текущими параметрами.
         */
        void onApplyGeneratorRequested();

        /**
         * Устанавливает новое seed и запускает генерацию.
         *
         * @param seed новое seed-значение
         */
        void onSeedApplied(long seed);

        /**
         * Просит генератор перейти к следующему seed.
         */
        void onAdvanceSeedRequested();

        /**
         * Сбрасывает параметры генерации к выбранному пресету.
         */
        void onResetSettingsFromPresetRequested();

        /**
         * Изменяет одно из числовых свойств генератора.
         *
         * @param terrainControlParameter изменяемый параметр
         * @param value новое значение параметра
         */
        void onGeneratorParameterChanged(TerrainControlParameter terrainControlParameter, float value);

        /**
         * Меняет цветовую палитру отображения высот.
         *
         * @param heightColorMode новый режим палитры
         */
        void onHeightColorModeChanged(HeightColorMode heightColorMode);

        /**
         * Меняет режим отображения сетки поверх карты.
         *
         * @param gridOverlayMode новый режим сетки
         */
        void onGridOverlayModeChanged(GridOverlayMode gridOverlayMode);

        /**
         * Меняет шаг визуальной сетки.
         *
         * @param gridStepInCells новый шаг сетки в клетках
         */
        void onGridStepChanged(int gridStepInCells);

        /**
         * Меняет текущий режим взаимодействия с картой.
         *
         * @param terrainInteractionMode новый режим взаимодействия
         */
        void onInteractionModeChanged(TerrainInteractionMode terrainInteractionMode);

        /**
         * Меняет активный инструмент ручной правки.
         *
         * @param sculptTool новый инструмент кисти
         */
        void onSculptToolChanged(SculptTool sculptTool);

        /**
         * Меняет радиус кисти ручной правки.
         *
         * @param brushRadiusInCells новый радиус в клетках
         */
        void onBrushRadiusChanged(float brushRadiusInCells);

        /**
         * Меняет силу кисти ручной правки.
         *
         * @param brushStrength новая сила кисти
         */
        void onBrushStrengthChanged(float brushStrength);

        /**
         * Меняет целевую высоту инструмента flatten.
         *
         * @param flattenTargetHeight новая целевая высота
         */
        void onFlattenTargetChanged(float flattenTargetHeight);

        /**
         * Полностью очищает слой ручной правки.
         */
        void onClearSculptLayerRequested();
    }

    /**
     * Создаёт панель управления поверх окна генератора.
     *
     * @param listener получатель событий от UI-элементов панели
     */
    public TerrainControlOverlay(Listener listener) {
        this.listener = listener;
        this.stage = new Stage(new ScreenViewport());
        this.skin = TerrainUiSkinFactory.create();
        this.window = createWindow();
        this.seedTextField = createSeedTextField();
        this.presetSelectBox = createPresetSelectBox();
        this.heightColorModeSelectBox = createHeightColorModeSelectBox();
        this.gridOverlayModeSelectBox = createGridOverlayModeSelectBox();
        this.gridStepSelectBox = createGridStepSelectBox();
        this.interactionModeSelectBox = createInteractionModeSelectBox();
        this.sculptToolSelectBox = createSculptToolSelectBox();
        this.brushRadiusSlider = createBrushRadiusSlider();
        this.brushStrengthSlider = createBrushStrengthSlider();
        this.flattenTargetSlider = createFlattenTargetSlider();
        this.brushRadiusValueLabel = createValueLabel();
        this.brushStrengthValueLabel = createValueLabel();
        this.flattenTargetValueLabel = createValueLabel();

        buildStageLayout();
        setVisible(true);
    }

    /**
     * Возвращает stage панели управления для InputMultiplexer.
     *
     * @return stage панели
     */
    public Stage stage() {
        return stage;
    }

    /**
     * Обновляет состояние анимаций и виджетов панели.
     *
     * @param deltaTime прошедшее время кадра
     */
    public void act(float deltaTime) {
        if (window.isVisible()) {
            stage.act(deltaTime);
        }
    }

    /**
     * Рисует панель поверх карты.
     */
    public void draw() {
        if (window.isVisible()) {
            stage.draw();
        }
    }

    /**
     * Изменяет размеры viewport панели после resize окна.
     *
     * @param width новая ширина окна
     * @param height новая высота окна
     */
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        float availableHeight = Math.max(CONTROL_ROW_HEIGHT * 10f, height - WINDOW_HEIGHT_MARGIN);
        window.setHeight(availableHeight);
    }

    /**
     * Делает панель видимой или скрывает её.
     *
     * @param visible флаг видимости панели
     */
    public void setVisible(boolean visible) {
        window.setVisible(visible);
        window.setTouchable(visible ? com.badlogic.gdx.scenes.scene2d.Touchable.enabled : com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
    }

    /**
     * Переключает видимость панели.
     */
    public void toggleVisible() {
        setVisible(!isVisible());
    }

    /**
     * Возвращает флаг видимости панели.
     *
     * @return {@code true}, если панель сейчас показана
     */
    public boolean isVisible() {
        return window.isVisible();
    }

    /**
     * Проверяет, находится ли курсор над любым виджетом панели.
     *
     * @param screenX координата курсора по оси X в экранных координатах
     * @param screenY координата курсора по оси Y в экранных координатах
     * @return {@code true}, если курсор попадает по элементу UI
     */
    public boolean isPointerOverUi(int screenX, int screenY) {
        if (!isVisible()) {
            return false;
        }

        stageHitCoordinates.set(screenX, screenY);
        stage.screenToStageCoordinates(stageHitCoordinates);
        Actor hitActor = stage.hit(stageHitCoordinates.x, stageHitCoordinates.y, true);
        return hitActor != null;
    }

    /**
     * Синхронизирует значения виджетов с текущим состоянием приложения.
     *
     * @param terrainGeneratorPreset активный пресет генерации
     * @param terrainGeneratorSettings активные числовые параметры генерации
     * @param heightColorMode текущая палитра высот
     * @param gridOverlayMode текущий режим сетки
     * @param gridStepInCells текущий шаг сетки в клетках
     * @param terrainInteractionMode активный режим взаимодействия
     * @param sculptTool активный инструмент ручной правки
     * @param brushRadiusInCells текущий радиус кисти
     * @param brushStrength текущая сила кисти
     * @param flattenTargetHeight текущая цель инструмента flatten
     * @param currentSeed текущее seed-значение
     */
    public void synchronize(
            TerrainGeneratorPreset terrainGeneratorPreset,
            TerrainGeneratorSettings terrainGeneratorSettings,
            HeightColorMode heightColorMode,
            GridOverlayMode gridOverlayMode,
            int gridStepInCells,
            TerrainInteractionMode terrainInteractionMode,
            SculptTool sculptTool,
            float brushRadiusInCells,
            float brushStrength,
            float flattenTargetHeight,
            long currentSeed
    ) {
        updatingControls = true;
        try {
            presetSelectBox.setSelected(terrainGeneratorPreset);
            String seedText = Long.toString(currentSeed);
            boolean seedFieldHasKeyboardFocus = stage.getKeyboardFocus() == seedTextField;
            if (!seedFieldHasKeyboardFocus && !seedText.equals(seedTextField.getText())) {
                seedTextField.setText(seedText);
            }
            heightColorModeSelectBox.setSelected(heightColorMode);
            gridOverlayModeSelectBox.setSelected(gridOverlayMode);
            gridStepSelectBox.setSelected(gridStepInCells);
            interactionModeSelectBox.setSelected(terrainInteractionMode);
            sculptToolSelectBox.setSelected(sculptTool);

            for (TerrainControlParameter terrainControlParameter : TerrainControlParameter.values()) {
                Slider parameterSlider = parameterSliderByControl.get(terrainControlParameter);
                float parameterValue = terrainControlParameter.currentValue(terrainGeneratorSettings);
                parameterSlider.setValue(parameterValue);
                parameterValueLabelByControl.get(terrainControlParameter)
                        .setText(terrainControlParameter.displayValue(terrainGeneratorSettings));
            }

            brushRadiusSlider.setValue(brushRadiusInCells);
            brushRadiusValueLabel.setText(formatSingleDecimal(brushRadiusInCells));
            brushStrengthSlider.setValue(brushStrength);
            brushStrengthValueLabel.setText(formatTwoDecimals(brushStrength));
            flattenTargetSlider.setValue(flattenTargetHeight);
            flattenTargetValueLabel.setText(formatTwoDecimals(flattenTargetHeight));
        } finally {
            updatingControls = false;
        }
    }

    /**
     * Освобождает ресурсы stage и skin.
     */
    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }

    /**
     * Создаёт окно панели управления.
     *
     * @return окно overlay-панели
     */
    private Window createWindow() {
        Window controlWindow = new Window("Terrain controls", skin);
        controlWindow.padTop(34f);
        controlWindow.setMovable(true);
        controlWindow.setResizable(false);
        controlWindow.setKeepWithinStage(true);
        return controlWindow;
    }

    /**
     * Строит корневую layout-структуру stage и наполняет окно элементами управления.
     */
    private void buildStageLayout() {
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.top().right().pad(PANEL_MARGIN);
        stage.addActor(rootTable);

        Table contentTable = createContentTable();
        ScrollPane scrollPane = new ScrollPane(contentTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setOverscroll(false, false);

        window.add(scrollPane).grow().minWidth(PANEL_WIDTH);
        rootTable.add(window).width(PANEL_WIDTH).top().right();
    }

    /**
     * Создаёт содержимое окна панели управления.
     *
     * @return таблица с элементами управления
     */
    private Table createContentTable() {
        Table contentTable = new Table();
        contentTable.pad(WINDOW_PADDING);
        contentTable.defaults().growX().padBottom(ROW_PADDING);

        addGeneratorSection(contentTable);
        addDisplaySection(contentTable);
        addSculptSection(contentTable);
        return contentTable;
    }

    /**
     * Создаёт секцию управления процедурной генерацией.
     *
     * @param contentTable корневая таблица панели
     */
    private void addGeneratorSection(Table contentTable) {
        addSectionTitle(contentTable, "Generator");

        contentTable.add(new Label("Preset", skin)).left();
        contentTable.add(presetSelectBox).growX();
        contentTable.row();

        Table seedRowTable = new Table();
        seedRowTable.defaults().padRight(ROW_PADDING).height(CONTROL_ROW_HEIGHT);
        TextButton applySeedButton = createButton("Apply seed", this::applySeedFromField);
        TextButton incrementSeedButton = createButton(INCREMENT_SEED_BUTTON_TEXT, listener::onAdvanceSeedRequested);
        seedRowTable.add(seedTextField).growX();
        seedRowTable.add(applySeedButton).minWidth(BUTTON_MIN_WIDTH);
        seedRowTable.add(incrementSeedButton).minWidth(BUTTON_MIN_WIDTH);
        contentTable.add(new Label("Seed", skin)).left();
        contentTable.add(seedRowTable).growX();
        contentTable.row();

        TextButton applyGenerationButton = createButton(APPLY_GENERATOR_BUTTON_TEXT, listener::onApplyGeneratorRequested);
        TextButton resetFromPresetButton = createButton(RESET_PRESET_BUTTON_TEXT, listener::onResetSettingsFromPresetRequested);
        Table actionRowTable = new Table();
        actionRowTable.defaults().padRight(ROW_PADDING).height(CONTROL_ROW_HEIGHT);
        actionRowTable.add(applyGenerationButton).growX();
        actionRowTable.add(resetFromPresetButton).growX();
        contentTable.add(actionRowTable).colspan(2).growX().padBottom(SECTION_SPACING);
        contentTable.row();

        for (TerrainControlParameter terrainControlParameter : TerrainControlParameter.values()) {
            addGeneratorSliderRow(contentTable, terrainControlParameter);
        }
    }

    /**
     * Создаёт секцию отображения карты.
     *
     * @param contentTable корневая таблица панели
     */
    private void addDisplaySection(Table contentTable) {
        addSectionTitle(contentTable, "Display");
        addSelectBoxRow(contentTable, "Palette", heightColorModeSelectBox);
        addSelectBoxRow(contentTable, "Grid overlay", gridOverlayModeSelectBox);
        addSelectBoxRow(contentTable, "Grid step", gridStepSelectBox);
    }

    /**
     * Создаёт секцию ручной правки рельефа.
     *
     * @param contentTable корневая таблица панели
     */
    private void addSculptSection(Table contentTable) {
        addSectionTitle(contentTable, "Sculpt");
        addSelectBoxRow(contentTable, "Interaction", interactionModeSelectBox);
        addSelectBoxRow(contentTable, "Tool", sculptToolSelectBox);
        addSimpleSliderRow(contentTable, "Brush radius", brushRadiusSlider, brushRadiusValueLabel);
        addSimpleSliderRow(contentTable, "Brush strength", brushStrengthSlider, brushStrengthValueLabel);
        addSimpleSliderRow(contentTable, "Flatten target", flattenTargetSlider, flattenTargetValueLabel);

        TextButton clearSculptButton = createButton(CLEAR_SCULPT_BUTTON_TEXT, listener::onClearSculptLayerRequested);
        contentTable.add(clearSculptButton).colspan(2).growX().height(CONTROL_ROW_HEIGHT).padTop(ROW_PADDING);
        contentTable.row();
    }

    /**
     * Добавляет строку-заголовок секции.
     *
     * @param contentTable корневая таблица панели
     * @param titleText текст заголовка
     */
    private void addSectionTitle(Table contentTable, String titleText) {
        Label sectionTitleLabel = new Label(titleText, skin, "section-title");
        sectionTitleLabel.setAlignment(Align.left);

        contentTable.add(sectionTitleLabel)
                .colspan(2)
                .growX()
                .padTop(SECTION_TOP_PADDING)
                .padBottom(SECTION_BOTTOM_PADDING)
                .left();
        contentTable.row();
    }

    /**
     * Добавляет строку с SelectBox.
     *
     * @param contentTable корневая таблица панели
     * @param labelText подпись строки
     * @param selectBox выпадающий список
     */
    private void addSelectBoxRow(Table contentTable, String labelText, SelectBox<?> selectBox) {
        contentTable.add(new Label(labelText, skin)).left().height(CONTROL_ROW_HEIGHT);
        contentTable.add(selectBox).growX().height(CONTROL_ROW_HEIGHT);
        contentTable.row();
    }

    /**
     * Добавляет строку параметра генерации с отдельной подписью значения и ползунком.
     *
     * @param contentTable корневая таблица панели
     * @param terrainControlParameter описатель параметра генерации
     */
    private void addGeneratorSliderRow(Table contentTable, TerrainControlParameter terrainControlParameter) {
        Slider valueSlider = createParameterSlider(terrainControlParameter);
        Label valueLabel = createValueLabel();
        parameterSliderByControl.put(terrainControlParameter, valueSlider);
        parameterValueLabelByControl.put(terrainControlParameter, valueLabel);

        Table headerRowTable = new Table();
        headerRowTable.add(new Label(terrainControlParameter.displayName(), skin)).left().growX();
        headerRowTable.add(valueLabel).right().minWidth(VALUE_LABEL_MIN_WIDTH);
        contentTable.add(headerRowTable).colspan(2).growX();
        contentTable.row();

        contentTable.add(valueSlider)
                .colspan(2)
                .growX()
                .height(SLIDER_ROW_HEIGHT)
                .padBottom(ROW_PADDING);
        contentTable.row();
    }

    /**
     * Добавляет обычную строку ползунка с подписью и текущим значением.
     *
     * @param contentTable корневая таблица панели
     * @param labelText подпись строки
     * @param slider ползунок
     * @param valueLabel подпись текущего значения
     */
    private void addSimpleSliderRow(Table contentTable, String labelText, Slider slider, Label valueLabel) {
        Table headerRowTable = new Table();
        headerRowTable.add(new Label(labelText, skin)).left().growX();
        headerRowTable.add(valueLabel).right().minWidth(VALUE_LABEL_MIN_WIDTH);
        contentTable.add(headerRowTable).colspan(2).growX();
        contentTable.row();

        contentTable.add(slider)
                .colspan(2)
                .growX()
                .height(SLIDER_ROW_HEIGHT)
                .padBottom(ROW_PADDING);
        contentTable.row();
    }

    /**
     * Создаёт текстовое поле для seed-значения.
     *
     * @return настроенное поле ввода seed
     */
    private TextField createSeedTextField() {
        TextField textField = new TextField(EMPTY_SEED_TEXT, skin);
        textField.setMessageText("Seed");
        textField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        textField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (updatingControls) {
                    return;
                }
                // Поле seed применяется отдельной кнопкой, поэтому здесь только даём stage обработать ввод.
            }
        });
        return textField;
    }

    /**
     * Создаёт выпадающий список пресетов генерации.
     *
     * @return select box пресетов
     */
    private SelectBox<TerrainGeneratorPreset> createPresetSelectBox() {
        SelectBox<TerrainGeneratorPreset> selectBox = new SelectBox<>(skin);
        selectBox.setItems(TerrainGeneratorPreset.values());
        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (updatingControls) {
                    return;
                }
                listener.onPresetSelected(selectBox.getSelected());
            }
        });
        return selectBox;
    }

    /**
     * Создаёт выпадающий список режимов палитры.
     *
     * @return select box режимов палитры
     */
    private SelectBox<HeightColorMode> createHeightColorModeSelectBox() {
        SelectBox<HeightColorMode> selectBox = new SelectBox<>(skin);
        selectBox.setItems(HeightColorMode.values());
        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (updatingControls) {
                    return;
                }
                listener.onHeightColorModeChanged(selectBox.getSelected());
            }
        });
        return selectBox;
    }

    /**
     * Создаёт выпадающий список режимов сетки.
     *
     * @return select box режимов сетки
     */
    private SelectBox<GridOverlayMode> createGridOverlayModeSelectBox() {
        SelectBox<GridOverlayMode> selectBox = new SelectBox<>(skin);
        selectBox.setItems(GridOverlayMode.values());
        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (updatingControls) {
                    return;
                }
                listener.onGridOverlayModeChanged(selectBox.getSelected());
            }
        });
        return selectBox;
    }

    /**
     * Создаёт выпадающий список шага визуальной сетки.
     *
     * @return select box шага сетки
     */
    private SelectBox<Integer> createGridStepSelectBox() {
        SelectBox<Integer> selectBox = new SelectBox<>(skin);
        Integer[] gridStepItems = new Integer[GRID_STEP_OPTION_COUNT];
        for (int optionIndex = 0; optionIndex < GRID_STEP_OPTION_COUNT; optionIndex++) {
            gridStepItems[optionIndex] = GRID_STEP_OPTIONS[optionIndex];
        }
        selectBox.setItems(gridStepItems);
        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (updatingControls) {
                    return;
                }
                listener.onGridStepChanged(selectBox.getSelected());
            }
        });
        return selectBox;
    }

    /**
     * Создаёт выпадающий список режимов взаимодействия.
     *
     * @return select box режимов взаимодействия
     */
    private SelectBox<TerrainInteractionMode> createInteractionModeSelectBox() {
        SelectBox<TerrainInteractionMode> selectBox = new SelectBox<>(skin);
        selectBox.setItems(TerrainInteractionMode.values());
        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (updatingControls) {
                    return;
                }
                listener.onInteractionModeChanged(selectBox.getSelected());
            }
        });
        return selectBox;
    }

    /**
     * Создаёт выпадающий список инструментов ручной правки.
     *
     * @return select box инструментов
     */
    private SelectBox<SculptTool> createSculptToolSelectBox() {
        SelectBox<SculptTool> selectBox = new SelectBox<>(skin);
        selectBox.setItems(SculptTool.values());
        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (updatingControls) {
                    return;
                }
                listener.onSculptToolChanged(selectBox.getSelected());
            }
        });
        return selectBox;
    }

    /**
     * Создаёт ползунок числового параметра генератора.
     *
     * @param terrainControlParameter описатель параметра
     * @return настроенный slider
     */
    private Slider createParameterSlider(TerrainControlParameter terrainControlParameter) {
        Slider slider = new Slider(
                terrainControlParameter.minimumValue(),
                terrainControlParameter.maximumValue(),
                terrainControlParameter.sliderStep(),
                false,
                skin,
                "default-horizontal"
        );
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (updatingControls) {
                    return;
                }
                listener.onGeneratorParameterChanged(terrainControlParameter, slider.getValue());
            }
        });
        return slider;
    }

    /**
     * Создаёт ползунок радиуса кисти.
     *
     * @return slider радиуса кисти
     */
    private Slider createBrushRadiusSlider() {
        Slider slider = new Slider(BRUSH_RADIUS_MINIMUM, BRUSH_RADIUS_MAXIMUM, BRUSH_RADIUS_STEP, false, skin, "default-horizontal");
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (updatingControls) {
                    return;
                }
                listener.onBrushRadiusChanged(slider.getValue());
            }
        });
        return slider;
    }

    /**
     * Создаёт ползунок силы кисти.
     *
     * @return slider силы кисти
     */
    private Slider createBrushStrengthSlider() {
        Slider slider = new Slider(BRUSH_STRENGTH_MINIMUM, BRUSH_STRENGTH_MAXIMUM, BRUSH_STRENGTH_STEP, false, skin, "default-horizontal");
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (updatingControls) {
                    return;
                }
                listener.onBrushStrengthChanged(slider.getValue());
            }
        });
        return slider;
    }

    /**
     * Создаёт ползунок целевой высоты flatten-инструмента.
     *
     * @return slider целевой высоты flatten
     */
    private Slider createFlattenTargetSlider() {
        Slider slider = new Slider(FLATTEN_TARGET_MINIMUM, FLATTEN_TARGET_MAXIMUM, FLATTEN_TARGET_STEP, false, skin, "default-horizontal");
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (updatingControls) {
                    return;
                }
                listener.onFlattenTargetChanged(slider.getValue());
            }
        });
        return slider;
    }

    /**
     * Создаёт стандартную кнопку панели с назначенным обработчиком.
     *
     * @param buttonText текст кнопки
     * @param action действие, которое нужно выполнить при нажатии
     * @return кнопка панели управления
     */
    private TextButton createButton(String buttonText, Runnable action) {
        TextButton textButton = new TextButton(buttonText, skin);
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (updatingControls) {
                    return;
                }
                action.run();
            }
        });
        return textButton;
    }

    /**
     * Создаёт правую подпись для текущего числового значения.
     *
     * @return настроенная label для значения
     */
    private Label createValueLabel() {
        Label valueLabel = new Label(EMPTY_SEED_TEXT, skin);
        valueLabel.setAlignment(Align.right);
        return valueLabel;
    }

    /**
     * Применяет seed, введённый в текстовое поле.
     */
    private void applySeedFromField() {
        String rawSeedText = seedTextField.getText();
        if (rawSeedText == null || rawSeedText.isBlank()) {
            return;
        }

        try {
            long parsedSeed = Long.parseLong(rawSeedText);
            listener.onSeedApplied(parsedSeed);
        } catch (NumberFormatException ignored) {
            Gdx.app.error("TerrainControlOverlay", "Некорректное значение seed: " + rawSeedText);
        }
    }

    /**
     * Форматирует число с одним знаком после запятой.
     *
     * @param value исходное значение
     * @return строка для отображения
     */
    private String formatSingleDecimal(float value) {
        return "%.1f".formatted(value);
    }

    /**
     * Форматирует число с двумя знаками после запятой.
     *
     * @param value исходное значение
     * @return строка для отображения
     */
    private String formatTwoDecimals(float value) {
        return "%.2f".formatted(value);
    }
}
