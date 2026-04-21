package ru.nik.terraingenerator.lab.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
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
 * Overlay-панель управления генератором рельефа.
 *
 * <p>Панель устроена как в графических редакторах: сверху всегда есть
 * компактная панель инструментов, а большие окна вызываются по кнопкам.
 * Это позволяет держать UI под рукой, но не перекрывать полкарты постоянным
 * большим блоком настроек.</p>
 */
public final class TerrainControlOverlay implements Disposable {

    //==Панель инструментов
    /**
     * Высота панели инструментов
     */
    private static final float TOOLBAR_HEIGHT = 40f;
    private static final float TOOLBAR_HORIZONTAL_PADDING = 10f;
    private static final float TOOLBAR_VERTICAL_PADDING = 8f;
    private static final float TOOLBAR_BUTTON_WIDTH = 112f;
    private static final float TOOLBAR_SPACING = 8f;

    private static final float GENERATOR_PANEL_WIDTH = 620f;
    private static final float SCULPT_PANEL_WIDTH = 620f;
    private static final float PANEL_MARGIN = 16f;
    private static final float WINDOW_PADDING = 14f;
    private static final float SECTION_TOP_PADDING = 12f;
    private static final float SECTION_BOTTOM_PADDING = 6f;
    private static final float ROW_PADDING = 6f;

    /**
     * Высота строки в панелях инструментов
     */
    private static final float CONTROL_ROW_HEIGHT = 32f;
    private static final float LABEL_COLUMN_MIN_WIDTH = 170f;
    private static final float CONTROL_TABLE_MIN_WIDTH = 360f;
    private static final float WINDOW_HEIGHT_MARGIN = 72f;
    private static final float ROW_CONTROL_SPACING = 8f;
    private static final float BUTTON_MIN_WIDTH = 120f;

    private static final int[] GRID_STEP_OPTIONS = {1, 2, 4, 8};

    private static final float FLATTEN_TARGET_MINIMUM = TerrainGeneratorSettings.minimumSupportedElevation();
    private static final float FLATTEN_TARGET_MAXIMUM = TerrainGeneratorSettings.maximumSupportedElevation();
    private static final float BRUSH_RADIUS_MINIMUM = 1f;
    private static final float BRUSH_RADIUS_MAXIMUM = 80f;
    private static final float BRUSH_STRENGTH_MINIMUM = 0.01f;
    private static final float BRUSH_STRENGTH_MAXIMUM = 0.60f;

    private static final String EMPTY_TEXT = "";
    private static final String GENERATOR_WINDOW_TITLE = "Generator";
    private static final String SCULPT_WINDOW_TITLE = "Brush";
    private static final String APPLY_SEED_BUTTON_TEXT = "Apply seed";
    private static final String NEW_SEED_BUTTON_TEXT = "New seed";
    private static final String GENERATE_NOW_BUTTON_TEXT = "Generate";
    private static final String AUTO_APPLY_CHECKBOX_TEXT = "";
    private static final String CLEAR_SCULPT_BUTTON_TEXT = "Clear sculpt layer";

    private final Listener listener;
    private final Stage stage;
    private final Skin skin;
    private final Window controlWindow;
    private final ScrollPane scrollPane;
    private final Table generatorContentTable;
    private final Table sculptContentTable;
    private final Vector2 stageHitCoordinates = new Vector2();

    private final TextField seedTextField;
    private final SelectBox<TerrainGeneratorPreset> presetSelectBox;
    private final SelectBox<HeightColorMode> heightColorModeSelectBox;
    private final SelectBox<GridOverlayMode> gridOverlayModeSelectBox;
    private final SelectBox<Integer> gridStepSelectBox;
    private final SelectBox<TerrainInteractionMode> interactionModeSelectBox;
    private final SelectBox<SculptTool> sculptToolSelectBox;
    private final CheckBox autoApplyGenerationCheckBox;

    private final ObjectMap<TerrainControlParameter, TextField> parameterFieldByControl = new ObjectMap<>();
    private final TextField brushRadiusField;
    private final TextField brushStrengthField;
    private final TextField flattenTargetField;

    private PanelSection currentPanelSection;
    private boolean updatingControls;

    /**
     * Слушатель изменений панели управления.
     */
    public interface Listener {
        void onPresetSelected(TerrainGeneratorPreset terrainGeneratorPreset);
        void onApplyGeneratorRequested();
        void onSeedApplied(long seed);
        void onAdvanceSeedRequested();
        void onAutoApplyGenerationChanged(boolean autoApplyGenerationEnabled);
        void onGeneratorParameterChanged(TerrainControlParameter terrainControlParameter, float value);
        void onHeightColorModeChanged(HeightColorMode heightColorMode);
        void onGridOverlayModeChanged(GridOverlayMode gridOverlayMode);
        void onGridStepChanged(int gridStepInCells);
        void onInteractionModeChanged(TerrainInteractionMode terrainInteractionMode);
        void onSculptToolChanged(SculptTool sculptTool);
        void onBrushRadiusChanged(float brushRadiusInCells);
        void onBrushStrengthChanged(float brushStrength);
        void onFlattenTargetChanged(float flattenTargetHeight);
        void onClearSculptLayerRequested();
        void onStatusPanelToggleRequested();
    }

    /**
     * Внутренние разделы плавающего окна.
     */
    private enum PanelSection {
        GENERATOR,
        SCULPT
    }

    /**
     * Создаёт overlay-панель управления.
     *
     * @param listener получатель событий от элементов управления
     */
    public TerrainControlOverlay(Listener listener) {
        this.listener = listener;
        this.stage = new Stage(new ScreenViewport());
        this.skin = TerrainUiSkinFactory.create();
        this.controlWindow = createWindow();
        this.seedTextField = createSeedTextField();
        this.presetSelectBox = createPresetSelectBox();
        this.heightColorModeSelectBox = createHeightColorModeSelectBox();
        this.gridOverlayModeSelectBox = createGridOverlayModeSelectBox();
        this.gridStepSelectBox = createGridStepSelectBox();
        this.interactionModeSelectBox = createInteractionModeSelectBox();
        this.sculptToolSelectBox = createSculptToolSelectBox();
        this.autoApplyGenerationCheckBox = createAutoApplyGenerationCheckBox();
        this.brushRadiusField = createBrushRadiusField();
        this.brushStrengthField = createBrushStrengthField();
        this.flattenTargetField = createFlattenTargetField();
        this.generatorContentTable = createGeneratorContentTable();
        this.sculptContentTable = createSculptContentTable();
        this.scrollPane = createScrollPane();
        this.currentPanelSection = PanelSection.GENERATOR;

        buildStageLayout();
        setVisible(false);
    }

    public Stage stage() {
        return stage;
    }

    public void act(float deltaTime) {
        stage.act(deltaTime);
    }

    public void draw() {
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        float minimumVisibleRowsHeight = CONTROL_ROW_HEIGHT * 10f;
        float availableHeight = Math.max(minimumVisibleRowsHeight, height - WINDOW_HEIGHT_MARGIN);
        controlWindow.setHeight(availableHeight);
        repositionWindow();
    }

    public void setVisible(boolean visible) {
        controlWindow.setVisible(visible);
        controlWindow.setTouchable(visible ? Touchable.enabled : Touchable.disabled);
    }

    public boolean isVisible() {
        return controlWindow.isVisible();
    }

    public void hideWindows() {
        setVisible(false);
    }

    public void toggleGeneratorPanel() {
        togglePanelSection(PanelSection.GENERATOR);
        listener.onInteractionModeChanged(TerrainInteractionMode.PROCEDURAL);
    }

    public void toggleSculptPanel() {
        togglePanelSection(PanelSection.SCULPT);
        listener.onInteractionModeChanged(TerrainInteractionMode.SCULPT);
    }

    public boolean isPointerOverUi(int screenX, int screenY) {
        stageHitCoordinates.set(screenX, screenY);
        stage.screenToStageCoordinates(stageHitCoordinates);
        Actor hitActor = stage.hit(stageHitCoordinates.x, stageHitCoordinates.y, true);
        return hitActor != null;
    }

    public boolean hasKeyboardFocus() {
        return stage.getKeyboardFocus() != null;
    }

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
            boolean autoApplyGenerationEnabled,
            long currentSeed,
            boolean statusPanelVisible
    ) {
        updatingControls = true;
        try {
            presetSelectBox.setSelected(terrainGeneratorPreset);
            synchronizeTextField(seedTextField, Long.toString(currentSeed));
            heightColorModeSelectBox.setSelected(heightColorMode);
            gridOverlayModeSelectBox.setSelected(gridOverlayMode);
            gridStepSelectBox.setSelected(gridStepInCells);
            interactionModeSelectBox.setSelected(terrainInteractionMode);
            sculptToolSelectBox.setSelected(sculptTool);
            autoApplyGenerationCheckBox.setChecked(autoApplyGenerationEnabled);

            for (TerrainControlParameter terrainControlParameter : TerrainControlParameter.values()) {
                TextField valueField = parameterFieldByControl.get(terrainControlParameter);
                synchronizeTextField(valueField, terrainControlParameter.displayValue(terrainGeneratorSettings));
            }

            synchronizeTextField(brushRadiusField, formatSingleDecimal(brushRadiusInCells));
            synchronizeTextField(brushStrengthField, formatTwoDecimals(brushStrength));
            synchronizeTextField(flattenTargetField, formatTwoDecimals(flattenTargetHeight));
        } finally {
            updatingControls = false;
        }
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }

    private Window createWindow() {
        Window window = new Window(GENERATOR_WINDOW_TITLE, skin);
        window.padTop(34f);
        window.setMovable(true);
        window.setResizable(false);
        window.setKeepWithinStage(true);
        return window;
    }

    private ScrollPane createScrollPane() {
        ScrollPane contentScrollPane = new ScrollPane(generatorContentTable, skin);
        contentScrollPane.setFadeScrollBars(false);
        contentScrollPane.setScrollingDisabled(true, false);
        contentScrollPane.setOverscroll(false, false);
        return contentScrollPane;
    }

    private void buildStageLayout() {
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.top().left();
        rootTable.add(createToolbarTable())
                .growX()
                .height(TOOLBAR_HEIGHT)
                .padTop(TOOLBAR_VERTICAL_PADDING)
                .padLeft(TOOLBAR_HORIZONTAL_PADDING)
                .padRight(TOOLBAR_HORIZONTAL_PADDING);
        stage.addActor(rootTable);

        controlWindow.add(scrollPane).grow().minWidth(GENERATOR_PANEL_WIDTH);
        stage.addActor(controlWindow);
        applyPanelSection(PanelSection.GENERATOR);
        repositionWindow();
    }

    private Table createToolbarTable() {
        Table toolbarTable = new Table();
        toolbarTable.left();
        toolbarTable.defaults().height(TOOLBAR_HEIGHT - TOOLBAR_VERTICAL_PADDING).padRight(TOOLBAR_SPACING);

        TextButton generatorButton = createButton("Generator", this::toggleGeneratorPanel);
        TextButton sculptButton = createButton("Sculpt", this::toggleSculptPanel);
        TextButton statusButton = createButton("Status", listener::onStatusPanelToggleRequested);

        toolbarTable.add(generatorButton).width(TOOLBAR_BUTTON_WIDTH);
        toolbarTable.add(sculptButton).width(TOOLBAR_BUTTON_WIDTH);
        toolbarTable.add(statusButton).width(TOOLBAR_BUTTON_WIDTH);
        return toolbarTable;
    }

    private void togglePanelSection(PanelSection requestedPanelSection) {
        boolean sameSectionRequested = currentPanelSection == requestedPanelSection;
        if (isVisible() && sameSectionRequested) {
            setVisible(false);
            return;
        }

        applyPanelSection(requestedPanelSection);
        setVisible(true);
    }

    private void applyPanelSection(PanelSection panelSection) {
        currentPanelSection = panelSection;
        if (panelSection == PanelSection.GENERATOR) {
            scrollPane.setActor(generatorContentTable);
            controlWindow.getTitleLabel().setText(GENERATOR_WINDOW_TITLE);
            controlWindow.setWidth(GENERATOR_PANEL_WIDTH);
        } else {
            scrollPane.setActor(sculptContentTable);
            controlWindow.getTitleLabel().setText(SCULPT_WINDOW_TITLE);
            controlWindow.setWidth(SCULPT_PANEL_WIDTH);
        }
        repositionWindow();
    }

    private void repositionWindow() {
        float viewportWidth = stage.getViewport().getWorldWidth();
        float viewportHeight = stage.getViewport().getWorldHeight();
        float windowX = viewportWidth - controlWindow.getWidth() - PANEL_MARGIN;
        float windowY = viewportHeight - controlWindow.getHeight() - TOOLBAR_HEIGHT - PANEL_MARGIN;
        controlWindow.setPosition(windowX, Math.max(PANEL_MARGIN, windowY));
    }

    private Table createGeneratorContentTable() {
        Table contentTable = new Table();
        contentTable.pad(WINDOW_PADDING);
        contentTable.top().left();
        contentTable.defaults().growX().padBottom(ROW_PADDING);

        addGeneratorSection(contentTable);
        addDisplaySection(contentTable);
        return contentTable;
    }

    private Table createSculptContentTable() {
        Table contentTable = new Table();
        contentTable.pad(WINDOW_PADDING);
        contentTable.top().left();
        contentTable.defaults().growX().padBottom(ROW_PADDING);
        addSculptSection(contentTable);
        return contentTable;
    }

    private void addGeneratorSection(Table contentTable) {
        addSectionTitle(contentTable, "Generator");
        addSelectBoxRow(contentTable, "Preset", presetSelectBox);
        addControlRow(contentTable, "Seed", seedTextField);
        addButtonsRow(contentTable,
                createButton(APPLY_SEED_BUTTON_TEXT, this::applySeedFromField),
                createButton(NEW_SEED_BUTTON_TEXT, listener::onAdvanceSeedRequested));
        addControlRow(contentTable, "Auto apply", autoApplyGenerationCheckBox);
        addSingleButtonRow(contentTable, createButton(GENERATE_NOW_BUTTON_TEXT, listener::onApplyGeneratorRequested));

        for (TerrainControlParameter terrainControlParameter : TerrainControlParameter.values()) {
            addNumericParameterRow(contentTable, terrainControlParameter);
        }
    }

    private void addDisplaySection(Table contentTable) {
        addSectionTitle(contentTable, "Display");
        addSelectBoxRow(contentTable, "Palette", heightColorModeSelectBox);
        addSelectBoxRow(contentTable, "Grid overlay", gridOverlayModeSelectBox);
        addSelectBoxRow(contentTable, "Grid step", gridStepSelectBox);
    }

    private void addSculptSection(Table contentTable) {
        addSectionTitle(contentTable, "Sculpt");
        addSelectBoxRow(contentTable, "Interaction", interactionModeSelectBox);
        addSelectBoxRow(contentTable, "Tool", sculptToolSelectBox);
        addNumericFieldRow(contentTable, "Brush radius", brushRadiusField);
        addNumericFieldRow(contentTable, "Brush strength", brushStrengthField);
        addNumericFieldRow(contentTable, "Flatten target", flattenTargetField);
        addSingleButtonRow(contentTable, createButton(CLEAR_SCULPT_BUTTON_TEXT, listener::onClearSculptLayerRequested));
    }

    private void addSectionTitle(Table contentTable, String titleText) {
        Label sectionTitleLabel = new Label(titleText, skin, "section-title");
        sectionTitleLabel.setAlignment(Align.left);

        contentTable.add(sectionTitleLabel)
                .colspan(2)
                .growX()
                .left()
                .padTop(SECTION_TOP_PADDING)
                .padBottom(SECTION_BOTTOM_PADDING);
        contentTable.row();
    }

    private void addSelectBoxRow(Table contentTable, String labelText, SelectBox<?> selectBox) {
        addControlRow(contentTable, labelText, selectBox);
    }

    private void addNumericParameterRow(Table contentTable, TerrainControlParameter terrainControlParameter) {
        TextField valueField = createParameterField(terrainControlParameter);
        parameterFieldByControl.put(terrainControlParameter, valueField);
        addNumericFieldRow(contentTable, terrainControlParameter.displayName(), valueField);
    }

    private void addControlRow(Table contentTable, String labelText, Actor controlActor) {
        Label rowLabel = new Label(labelText, skin);
        rowLabel.setAlignment(Align.left);
        contentTable.add(rowLabel)
                .left()
                .minWidth(LABEL_COLUMN_MIN_WIDTH)
                .height(CONTROL_ROW_HEIGHT)
                .padRight(ROW_CONTROL_SPACING);
        contentTable.add(controlActor)
                .growX()
                .minWidth(CONTROL_TABLE_MIN_WIDTH)
                .height(CONTROL_ROW_HEIGHT);
        contentTable.row();
    }

    private void addNumericFieldRow(Table contentTable, String labelText, TextField valueField) {
        addControlRow(contentTable, labelText, valueField);
    }

    private void addButtonsRow(Table contentTable, TextButton leftButton, TextButton rightButton) {
        Table actionRowTable = new Table();
        actionRowTable.defaults().height(CONTROL_ROW_HEIGHT).padRight(ROW_CONTROL_SPACING);
        actionRowTable.add(leftButton).growX().minWidth(BUTTON_MIN_WIDTH);
        actionRowTable.add(rightButton).growX().minWidth(BUTTON_MIN_WIDTH).padRight(0f);

        contentTable.add(actionRowTable).colspan(2).growX().minWidth(CONTROL_TABLE_MIN_WIDTH);
        contentTable.row();
    }

    private void addSingleButtonRow(Table contentTable, TextButton button) {
        contentTable.add(button)
                .colspan(2)
                .growX()
                .minWidth(CONTROL_TABLE_MIN_WIDTH)
                .height(CONTROL_ROW_HEIGHT);
        contentTable.row();
    }

    private TextField createSeedTextField() {
        TextField textField = new TextField(EMPTY_TEXT, skin);
        textField.setMessageText("Seed");
        textField.setTextFieldFilter(new SignedIntegerFieldFilter());
        textField.setTextFieldListener((field, character) -> {
            if (character == '\n' || character == '\r') {
                applySeedFromField();
            }
        });
        return textField;
    }

    private TextField createParameterField(TerrainControlParameter terrainControlParameter) {
        TextField valueField = createDecimalField();
        installCommitHandlers(valueField, () -> commitGeneratorParameter(terrainControlParameter, valueField));
        return valueField;
    }

    private TextField createBrushRadiusField() {
        TextField valueField = createDecimalField();
        installCommitHandlers(valueField, () -> commitBrushRadius(valueField));
        return valueField;
    }

    private TextField createBrushStrengthField() {
        TextField valueField = createDecimalField();
        installCommitHandlers(valueField, () -> commitBrushStrength(valueField));
        return valueField;
    }

    private TextField createFlattenTargetField() {
        TextField valueField = createDecimalField();
        installCommitHandlers(valueField, () -> commitFlattenTarget(valueField));
        return valueField;
    }

    private TextField createDecimalField() {
        TextField valueField = new TextField(EMPTY_TEXT, skin);
        valueField.setTextFieldFilter(new SignedDecimalTextFieldFilter());
        return valueField;
    }

    private void installCommitHandlers(TextField valueField, Runnable commitAction) {
        valueField.setTextFieldListener((field, character) -> {
            if (character == '\n' || character == '\r') {
                commitAction.run();
            }
        });
        valueField.addListener(new FocusListener() {
            @Override
            public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
                if (!focused) {
                    commitAction.run();
                }
            }
        });
    }

    private SelectBox<TerrainGeneratorPreset> createPresetSelectBox() {
        SelectBox<TerrainGeneratorPreset> selectBox = new SelectBox<>(skin);
        selectBox.setItems(TerrainGeneratorPreset.values());
        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!updatingControls) {
                    listener.onPresetSelected(selectBox.getSelected());
                }
            }
        });
        return selectBox;
    }

    private SelectBox<HeightColorMode> createHeightColorModeSelectBox() {
        SelectBox<HeightColorMode> selectBox = new SelectBox<>(skin);
        selectBox.setItems(HeightColorMode.values());
        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!updatingControls) {
                    listener.onHeightColorModeChanged(selectBox.getSelected());
                }
            }
        });
        return selectBox;
    }

    private SelectBox<GridOverlayMode> createGridOverlayModeSelectBox() {
        SelectBox<GridOverlayMode> selectBox = new SelectBox<>(skin);
        selectBox.setItems(GridOverlayMode.values());
        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!updatingControls) {
                    listener.onGridOverlayModeChanged(selectBox.getSelected());
                }
            }
        });
        return selectBox;
    }

    private SelectBox<Integer> createGridStepSelectBox() {
        SelectBox<Integer> selectBox = new SelectBox<>(skin);
        Integer[] gridStepItems = new Integer[GRID_STEP_OPTIONS.length];
        for (int index = 0; index < GRID_STEP_OPTIONS.length; index++) {
            gridStepItems[index] = GRID_STEP_OPTIONS[index];
        }
        selectBox.setItems(gridStepItems);
        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!updatingControls) {
                    listener.onGridStepChanged(selectBox.getSelected());
                }
            }
        });
        return selectBox;
    }

    private SelectBox<TerrainInteractionMode> createInteractionModeSelectBox() {
        SelectBox<TerrainInteractionMode> selectBox = new SelectBox<>(skin);
        selectBox.setItems(TerrainInteractionMode.values());
        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!updatingControls) {
                    listener.onInteractionModeChanged(selectBox.getSelected());
                }
            }
        });
        return selectBox;
    }

    private SelectBox<SculptTool> createSculptToolSelectBox() {
        SelectBox<SculptTool> selectBox = new SelectBox<>(skin);
        selectBox.setItems(SculptTool.values());
        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!updatingControls) {
                    listener.onSculptToolChanged(selectBox.getSelected());
                }
            }
        });
        return selectBox;
    }

    private CheckBox createAutoApplyGenerationCheckBox() {
        CheckBox checkBox = new CheckBox(AUTO_APPLY_CHECKBOX_TEXT, skin);
        checkBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!updatingControls) {
                    listener.onAutoApplyGenerationChanged(checkBox.isChecked());
                }
            }
        });
        return checkBox;
    }

    private TextButton createButton(String text, Runnable action) {
        TextButton button = new TextButton(text, skin);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!updatingControls) {
                    action.run();
                    button.setChecked(false);
                }
            }
        });
        return button;
    }

    private void applySeedFromField() {
        if (updatingControls) {
            return;
        }
        String seedText = seedTextField.getText();
        if (seedText == null || seedText.isBlank() || "-".equals(seedText)) {
            return;
        }
        try {
            long parsedSeed = Long.parseLong(seedText);
            listener.onSeedApplied(parsedSeed);
        } catch (NumberFormatException ignored) {
            Gdx.app.debug("terrain-lab", "Seed field contains invalid number: " + seedText);
        }
    }

    private void commitGeneratorParameter(TerrainControlParameter terrainControlParameter, TextField valueField) {
        if (updatingControls) {
            return;
        }
        parseAndApplyFloat(
                valueField,
                terrainControlParameter.minimumValue(),
                terrainControlParameter.maximumValue(),
                value -> listener.onGeneratorParameterChanged(terrainControlParameter, value)
        );
    }

    private void commitBrushRadius(TextField valueField) {
        if (updatingControls) {
            return;
        }
        parseAndApplyFloat(valueField, BRUSH_RADIUS_MINIMUM, BRUSH_RADIUS_MAXIMUM, listener::onBrushRadiusChanged);
    }

    private void commitBrushStrength(TextField valueField) {
        if (updatingControls) {
            return;
        }
        parseAndApplyFloat(valueField, BRUSH_STRENGTH_MINIMUM, BRUSH_STRENGTH_MAXIMUM, listener::onBrushStrengthChanged);
    }

    private void commitFlattenTarget(TextField valueField) {
        if (updatingControls) {
            return;
        }
        parseAndApplyFloat(valueField, FLATTEN_TARGET_MINIMUM, FLATTEN_TARGET_MAXIMUM, listener::onFlattenTargetChanged);
    }

    private void parseAndApplyFloat(TextField valueField, float minimumValue, float maximumValue, FloatValueConsumer consumer) {
        String rawText = valueField.getText();
        if (rawText == null || rawText.isBlank() || "-".equals(rawText) || ".".equals(rawText) || "-.".equals(rawText)) {
            return;
        }
        try {
            float parsedValue = Float.parseFloat(rawText);
            float clampedValue = clamp(parsedValue, minimumValue, maximumValue);
            consumer.accept(clampedValue);
        } catch (NumberFormatException ignored) {
            Gdx.app.debug("terrain-lab", "Numeric field contains invalid float: " + rawText);
        }
    }

    private void synchronizeTextField(TextField textField, String newText) {
        boolean fieldHasKeyboardFocus = stage.getKeyboardFocus() == textField;
        if (!fieldHasKeyboardFocus && !newText.equals(textField.getText())) {
            textField.setText(newText);
        }
    }

    private float clamp(float value, float minimumValue, float maximumValue) {
        return Math.max(minimumValue, Math.min(maximumValue, value));
    }

    private String formatSingleDecimal(float value) {
        return "%.1f".formatted(value);
    }

    private String formatTwoDecimals(float value) {
        return "%.2f".formatted(value);
    }

    @FunctionalInterface
    private interface FloatValueConsumer {
        void accept(float value);
    }

    /**
     * Фильтр целых signed-значений для seed.
     */
    private static final class SignedIntegerFieldFilter implements TextField.TextFieldFilter {
        private static final char MINUS_SIGN = '-';

        @Override
        public boolean acceptChar(TextField textField, char character) {
            if (Character.isDigit(character)) {
                return true;
            }
            return character == MINUS_SIGN && textField.getCursorPosition() == 0 && textField.getText().indexOf(MINUS_SIGN) < 0;
        }
    }

    /**
     * Фильтр signed-десятичных чисел.
     */
    private static final class SignedDecimalTextFieldFilter implements TextField.TextFieldFilter {
        private static final char DECIMAL_SEPARATOR = '.';
        private static final char MINUS_SIGN = '-';

        @Override
        public boolean acceptChar(TextField textField, char character) {
            if (Character.isDigit(character)) {
                return true;
            }

            boolean isDecimalSeparator = character == DECIMAL_SEPARATOR;
            boolean alreadyContainsDecimalSeparator = textField.getText().indexOf(DECIMAL_SEPARATOR) >= 0;
            if (isDecimalSeparator) {
                return !alreadyContainsDecimalSeparator;
            }

            boolean isMinusSign = character == MINUS_SIGN;
            boolean minusAlreadyPresent = textField.getText().indexOf(MINUS_SIGN) >= 0;
            return isMinusSign && textField.getCursorPosition() == 0 && !minusAlreadyPresent;
        }
    }
}
