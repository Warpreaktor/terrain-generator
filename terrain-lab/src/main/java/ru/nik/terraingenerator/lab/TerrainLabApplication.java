package ru.nik.terraingenerator.lab;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import java.util.List;
import ru.nik.terraingenerator.core.analysis.TerrainStatistics;
import ru.nik.terraingenerator.core.grid.GridSize;
import ru.nik.terraingenerator.core.grid.TerrainGrid;
import ru.nik.terraingenerator.lab.render.GridOverlayMode;
import ru.nik.terraingenerator.lab.render.HeightColorMode;
import ru.nik.terraingenerator.lab.render.TerrainGridRenderer;
import ru.nik.terraingenerator.lab.ui.TerrainControlOverlay;

/**
 * libGDX-приложение лаборатории, в котором можно смотреть процедурный рельеф,
 * крутить параметры генерации и вручную доправлять высоты поверх базовой карты.
 */
public final class TerrainLabApplication extends ApplicationAdapter {

    private static final int GRID_WIDTH = 600;
    private static final int GRID_HEIGHT = 600;
    private static final GridSize LAB_GRID_SIZE = new GridSize(GRID_WIDTH, GRID_HEIGHT);

    private static final float CAMERA_MOVE_SPEED = 350f;
    private static final float ZOOM_STEP = 0.04f;
    private static final float MIN_CAMERA_ZOOM = 0.15f;
    private static final float MAX_CAMERA_ZOOM = 8.0f;
    private static final long INITIAL_SEED = 7319421L;
    private static final int[] GRID_STEP_OPTIONS = {1, 2, 4, 8};

    private static final float DEFAULT_FONT_SCALE = 0.95f;
    private static final float STATUS_PANEL_WIDTH = 360f;
    private static final float STATUS_PANEL_HEIGHT = 258f;
    private static final float STATUS_PANEL_PADDING = 12f;
    private static final float STATUS_PANEL_TITLE_OFFSET = 20f;
    private static final float STATUS_PANEL_LINE_HEIGHT = 22f;
    private static final float STATUS_PANEL_CORNER_MARGIN = 16f;
    private static final float STATUS_PANEL_BACKGROUND_ALPHA = 0.72f;
    private static final float STATUS_PANEL_BORDER_ALPHA = 0.90f;

    private static final int GENERATOR_PANEL_TOGGLE_KEY = Input.Keys.F1;
    private static final int STATUS_PANEL_TOGGLE_KEY = Input.Keys.F2;
    private static final int SCULPT_PANEL_TOGGLE_KEY = Input.Keys.F3;

    private static final float BRUSH_RADIUS_MIN = 1f;
    private static final float BRUSH_RADIUS_MAX = 80f;
    private static final float BRUSH_RADIUS_STEP = 1f;
    private static final float BRUSH_RADIUS_ACCELERATED_STEP = 4f;
    private static final float BRUSH_STRENGTH_MIN = 0.01f;
    private static final float BRUSH_STRENGTH_MAX = 0.60f;
    private static final float BRUSH_STRENGTH_STEP = 0.01f;
    private static final float BRUSH_STRENGTH_ACCELERATED_STEP = 0.04f;
    private static final float DEFAULT_BRUSH_RADIUS = 9f;
    private static final float DEFAULT_BRUSH_STRENGTH = 0.08f;
    private static final float DEFAULT_FLATTEN_TARGET_HEIGHT = TerrainGeneratorSettings.zeroElevation();
    private static final float MINIMUM_DISPLAY_ABSOLUTE_ELEVATION = 0.25f;

    private static final Color STATUS_PANEL_BACKGROUND_COLOR = new Color(0.06f, 0.08f, 0.10f, STATUS_PANEL_BACKGROUND_ALPHA);
    private static final Color STATUS_PANEL_BORDER_COLOR = new Color(1f, 1f, 1f, STATUS_PANEL_BORDER_ALPHA);

    private final Vector3 cursorWorldPosition = new Vector3();

    private OrthographicCamera worldCamera;
    private OrthographicCamera hudCamera;
    private SpriteBatch spriteBatch;
    private BitmapFont bitmapFont;
    private ShapeRenderer hudShapeRenderer;
    private TerrainGridRenderer terrainGridRenderer;
    private TerrainControlOverlay terrainControlOverlay;

    private TerrainGeneratorSettings terrainGeneratorSettings;
    private TerrainGeneratorPreset terrainGeneratorPreset;
    private TerrainControlParameter selectedControlParameter;
    private TerrainInteractionMode terrainInteractionMode;
    private SculptTool sculptTool;

    private TerrainSculptLayer terrainSculptLayer;
    private TerrainGrid proceduralTerrainGrid;
    private TerrainGrid sculptedTerrainGrid;
    private List<TerrainGrid> pipelineSnapshots;
    private List<String> pipelineStepNames;

    private HeightColorMode heightColorMode;
    private GridOverlayMode gridOverlayMode;
    private int gridStepIndex;
    private boolean showPipelineSnapshots;
    private int snapshotIndex;
    private long currentSeed;
    private boolean autoApplyGenerationEnabled;
    private boolean statusPanelVisible;

    private int selectedCellX;
    private int selectedCellY;
    private boolean cursorInsideTerrain;

    private float brushRadiusInCells;
    private float brushStrength;
    private float flattenTargetHeight;
    private boolean visibleTerrainDirty;

    @Override
    public void create() {
        this.spriteBatch = new SpriteBatch();
        this.bitmapFont = new BitmapFont();
        this.bitmapFont.getData().setScale(DEFAULT_FONT_SCALE);
        this.hudShapeRenderer = new ShapeRenderer();

        this.worldCamera = new OrthographicCamera();
        this.worldCamera.setToOrtho(false, GRID_WIDTH, GRID_HEIGHT);
        this.worldCamera.position.set(GRID_WIDTH * 0.5f, GRID_HEIGHT * 0.5f, 0f);
        this.worldCamera.update();

        this.hudCamera = new OrthographicCamera();
        this.hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.hudCamera.update();

        this.terrainGridRenderer = new TerrainGridRenderer();
        this.terrainGeneratorPreset = TerrainGeneratorPreset.ROLLING_PLAINS;
        this.terrainGeneratorSettings = TerrainGeneratorSettings.fromPreset(terrainGeneratorPreset);
        this.selectedControlParameter = TerrainControlParameter.RELIEF_AMPLITUDE;
        this.terrainInteractionMode = TerrainInteractionMode.PROCEDURAL;
        this.sculptTool = SculptTool.RAISE;
        this.terrainSculptLayer = new TerrainSculptLayer(LAB_GRID_SIZE);
        this.heightColorMode = HeightColorMode.TOPOGRAPHIC;
        this.gridOverlayMode = GridOverlayMode.LIGHT;
        this.gridStepIndex = 2;
        this.showPipelineSnapshots = false;
        this.snapshotIndex = 0;
        this.currentSeed = INITIAL_SEED;
        this.autoApplyGenerationEnabled = true;
        this.statusPanelVisible = true;
        this.selectedCellX = GRID_WIDTH / 2;
        this.selectedCellY = GRID_HEIGHT / 2;
        this.brushRadiusInCells = DEFAULT_BRUSH_RADIUS;
        this.brushStrength = DEFAULT_BRUSH_STRENGTH;
        this.flattenTargetHeight = DEFAULT_FLATTEN_TARGET_HEIGHT;
        this.visibleTerrainDirty = true;
        this.terrainControlOverlay = new TerrainControlOverlay(createControlOverlayListener());

        installInputProcessor();
        regenerateTerrain(currentSeed);
        terrainControlOverlay.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void dispose() {
        terrainControlOverlay.dispose();
        terrainGridRenderer.dispose();
        hudShapeRenderer.dispose();
        bitmapFont.dispose();
        spriteBatch.dispose();
    }

    @Override
    public void render() {
        handleInput(Gdx.graphics.getDeltaTime());
        rebuildVisibleTerrainIfNeeded();

        ScreenUtils.clear(0.07f, 0.08f, 0.10f, 1f);

        worldCamera.update();
        Texture renderedGridTexture = terrainGridRenderer.texture();
        spriteBatch.setProjectionMatrix(worldCamera.combined);
        spriteBatch.begin();
        spriteBatch.draw(renderedGridTexture, 0f, 0f, GRID_WIDTH, GRID_HEIGHT);
        spriteBatch.end();

        terrainGridRenderer.renderGrid(worldCamera, GRID_WIDTH, GRID_HEIGHT, gridOverlayMode, gridStep());
        if (cursorInsideTerrain) {
            terrainGridRenderer.renderSelection(worldCamera, selectedCellX, selectedCellY);
            if (terrainInteractionMode == TerrainInteractionMode.SCULPT) {
                terrainGridRenderer.renderBrush(worldCamera, selectedCellX + 0.5f, selectedCellY + 0.5f, brushRadiusInCells);
            }
        }

        drawHud();

        terrainControlOverlay.synchronize(
                terrainGeneratorPreset,
                terrainGeneratorSettings,
                heightColorMode,
                gridOverlayMode,
                gridStep(),
                terrainInteractionMode,
                sculptTool,
                brushRadiusInCells,
                brushStrength,
                flattenTargetHeight,
                autoApplyGenerationEnabled,
                currentSeed,
                statusPanelVisible
        );
        terrainControlOverlay.act(Gdx.graphics.getDeltaTime());
        terrainControlOverlay.draw();
    }

    @Override
    public void resize(int width, int height) {
        hudCamera.setToOrtho(false, width, height);
        hudCamera.update();
        terrainControlOverlay.resize(width, height);
    }

    private void installInputProcessor() {
        InputAdapter worldInputAdapter = new InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                boolean pointerOverUi = terrainControlOverlay.isPointerOverUi(Gdx.input.getX(), Gdx.input.getY());
                if (pointerOverUi) {
                    return false;
                }

                boolean acceleratedAdjustment = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
                        || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);

                if (terrainInteractionMode == TerrainInteractionMode.SCULPT) {
                    if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
                        float strengthStep = acceleratedAdjustment ? BRUSH_STRENGTH_ACCELERATED_STEP : BRUSH_STRENGTH_STEP;
                        brushStrength = clamp(brushStrength - amountY * strengthStep, BRUSH_STRENGTH_MIN, BRUSH_STRENGTH_MAX);
                    } else {
                        float radiusStep = acceleratedAdjustment ? BRUSH_RADIUS_ACCELERATED_STEP : BRUSH_RADIUS_STEP;
                        brushRadiusInCells = clamp(brushRadiusInCells - amountY * radiusStep, BRUSH_RADIUS_MIN, BRUSH_RADIUS_MAX);
                    }
                    visibleTerrainDirty = true;
                } else {
                    float zoomDirection = amountY > 0f ? ZOOM_STEP : -ZOOM_STEP;
                    worldCamera.zoom = clamp(worldCamera.zoom + zoomDirection, MIN_CAMERA_ZOOM, MAX_CAMERA_ZOOM);
                }
                return true;
            }
        };

        InputMultiplexer inputMultiplexer = new InputMultiplexer(terrainControlOverlay.stage(), worldInputAdapter);
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    private void handleInput(float deltaTime) {
        handleOverlayToggleInput();

        boolean pointerOverUi = terrainControlOverlay.isPointerOverUi(Gdx.input.getX(), Gdx.input.getY());
        if (!pointerOverUi) {
            updateSelectedCellFromCursor();
        }

        boolean uiHasKeyboardFocus = terrainControlOverlay.hasKeyboardFocus();
        if (!uiHasKeyboardFocus) {
            handleCameraInput(deltaTime);
            handleDisplayInput();
            handleGeneratorInput();
        }
        if (!pointerOverUi) {
            handleSculptInput();
        }
        clampCameraToTerrainBounds();
    }

    private void handleOverlayToggleInput() {
        if (Gdx.input.isKeyJustPressed(GENERATOR_PANEL_TOGGLE_KEY)) {
            terrainControlOverlay.toggleGeneratorPanel();
        }
        if (Gdx.input.isKeyJustPressed(SCULPT_PANEL_TOGGLE_KEY)) {
            terrainControlOverlay.toggleSculptPanel();
        }
        if (Gdx.input.isKeyJustPressed(STATUS_PANEL_TOGGLE_KEY)) {
            statusPanelVisible = !statusPanelVisible;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            terrainControlOverlay.hideWindows();
        }
    }

    private void updateSelectedCellFromCursor() {
        cursorWorldPosition.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
        worldCamera.unproject(cursorWorldPosition);

        int candidateCellX = (int) Math.floor(cursorWorldPosition.x);
        int candidateCellY = (int) Math.floor(cursorWorldPosition.y);
        cursorInsideTerrain = proceduralTerrainGrid != null && proceduralTerrainGrid.contains(candidateCellX, candidateCellY);
        if (cursorInsideTerrain) {
            selectedCellX = candidateCellX;
            selectedCellY = candidateCellY;
        }
    }

    private void handleCameraInput(float deltaTime) {
        float moveDistance = CAMERA_MOVE_SPEED * deltaTime * worldCamera.zoom;

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            worldCamera.position.x -= moveDistance;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            worldCamera.position.x += moveDistance;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            worldCamera.position.y += moveDistance;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            worldCamera.position.y -= moveDistance;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            worldCamera.zoom = clamp(worldCamera.zoom + ZOOM_STEP, MIN_CAMERA_ZOOM, MAX_CAMERA_ZOOM);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            worldCamera.zoom = clamp(worldCamera.zoom - ZOOM_STEP, MIN_CAMERA_ZOOM, MAX_CAMERA_ZOOM);
        }
    }

    private void handleDisplayInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            heightColorMode = heightColorMode.next();
            visibleTerrainDirty = true;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.G)) {
            gridOverlayMode = gridOverlayMode.next();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            gridStepIndex = (gridStepIndex + 1) % GRID_STEP_OPTIONS.length;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            showPipelineSnapshots = false;
            visibleTerrainDirty = true;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) && terrainInteractionMode == TerrainInteractionMode.PROCEDURAL) {
            if (!pipelineSnapshots.isEmpty()) {
                showPipelineSnapshots = true;
                snapshotIndex = (snapshotIndex + 1) % pipelineSnapshots.size();
                visibleTerrainDirty = true;
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            terrainInteractionMode = terrainInteractionMode.next();
            if (terrainInteractionMode == TerrainInteractionMode.SCULPT) {
                showPipelineSnapshots = false;
            }
            visibleTerrainDirty = true;
        }
    }

    private void handleGeneratorInput() {
        boolean acceleratedAdjustment = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
                || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selectedControlParameter = selectedControlParameter.previous();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedControlParameter = selectedControlParameter.next();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            selectedControlParameter.adjust(terrainGeneratorSettings, -1, acceleratedAdjustment);
            terrainGeneratorPreset = TerrainGeneratorPreset.CUSTOM;
            if (autoApplyGenerationEnabled) {
                regenerateTerrain(currentSeed);
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            selectedControlParameter.adjust(terrainGeneratorSettings, 1, acceleratedAdjustment);
            terrainGeneratorPreset = TerrainGeneratorPreset.CUSTOM;
            if (autoApplyGenerationEnabled) {
                regenerateTerrain(currentSeed);
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            terrainGeneratorPreset = terrainGeneratorPreset.nextSelectable();
            terrainGeneratorSettings = TerrainGeneratorSettings.fromPreset(terrainGeneratorPreset);
            regenerateTerrain(currentSeed);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            currentSeed += 1L;
            regenerateTerrain(currentSeed);
        }
    }

    private void handleSculptInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            sculptTool = sculptTool.next();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
            terrainSculptLayer.clear();
            visibleTerrainDirty = true;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F) && cursorInsideTerrain) {
            flattenTargetHeight = composedFinalTerrain().getHeight(selectedCellX, selectedCellY);
        }
        if (terrainInteractionMode != TerrainInteractionMode.SCULPT || !cursorInsideTerrain) {
            return;
        }

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            terrainSculptLayer.applyBrush(
                    proceduralTerrainGrid,
                    sculptTool,
                    selectedCellX,
                    selectedCellY,
                    brushRadiusInCells,
                    brushStrength,
                    flattenTargetHeight
            );
            visibleTerrainDirty = true;
        }
    }

    private void regenerateTerrain(long seed) {
        TerrainLabSession terrainLabSession = ProceduralTerrainGenerator.createSession(LAB_GRID_SIZE, seed, terrainGeneratorSettings);
        this.proceduralTerrainGrid = terrainLabSession.finalTerrainGrid();
        this.pipelineSnapshots = terrainLabSession.pipelineSnapshots();
        this.pipelineStepNames = terrainLabSession.pipelineStepNames();
        this.snapshotIndex = pipelineSnapshots.isEmpty() ? 0 : pipelineSnapshots.size() - 1;
        this.visibleTerrainDirty = true;
    }

    private void rebuildVisibleTerrainIfNeeded() {
        if (!visibleTerrainDirty) {
            return;
        }

        TerrainGrid visibleTerrainGrid = visibleTerrainGrid();
        float displayAbsoluteElevation = displayAbsoluteElevation();
        terrainGridRenderer.rebuild(visibleTerrainGrid, heightColorMode, displayAbsoluteElevation);
        visibleTerrainDirty = false;
    }

    private TerrainGrid visibleTerrainGrid() {
        if (showPipelineSnapshots && !pipelineSnapshots.isEmpty()) {
            return pipelineSnapshots.get(snapshotIndex);
        }

        this.sculptedTerrainGrid = composedFinalTerrain();
        return sculptedTerrainGrid;
    }

    private TerrainGrid composedFinalTerrain() {
        return terrainSculptLayer.composeFinalTerrain(proceduralTerrainGrid);
    }

    private float displayAbsoluteElevation() {
        float configuredReliefAmplitude = terrainGeneratorSettings.reliefAmplitude();
        return Math.max(configuredReliefAmplitude, MINIMUM_DISPLAY_ABSOLUTE_ELEVATION);
    }

    private void clampCameraToTerrainBounds() {
        float halfViewportWidth = worldCamera.viewportWidth * worldCamera.zoom * 0.5f;
        float halfViewportHeight = worldCamera.viewportHeight * worldCamera.zoom * 0.5f;

        float minimumCameraX = halfViewportWidth;
        float maximumCameraX = GRID_WIDTH - halfViewportWidth;
        float minimumCameraY = halfViewportHeight;
        float maximumCameraY = GRID_HEIGHT - halfViewportHeight;

        if (minimumCameraX > maximumCameraX) {
            worldCamera.position.x = GRID_WIDTH * 0.5f;
        } else {
            worldCamera.position.x = clamp(worldCamera.position.x, minimumCameraX, maximumCameraX);
        }
        if (minimumCameraY > maximumCameraY) {
            worldCamera.position.y = GRID_HEIGHT * 0.5f;
        } else {
            worldCamera.position.y = clamp(worldCamera.position.y, minimumCameraY, maximumCameraY);
        }
    }

    private TerrainControlOverlay.Listener createControlOverlayListener() {
        return new TerrainControlOverlay.Listener() {
            @Override
            public void onPresetSelected(TerrainGeneratorPreset preset) {
                applyPreset(preset);
            }

            @Override
            public void onApplyGeneratorRequested() {
                regenerateTerrain(currentSeed);
            }

            @Override
            public void onSeedApplied(long seed) {
                currentSeed = seed;
                regenerateTerrain(currentSeed);
            }

            @Override
            public void onAdvanceSeedRequested() {
                currentSeed += 1L;
                regenerateTerrain(currentSeed);
            }

            @Override
            public void onAutoApplyGenerationChanged(boolean autoApplyGenerationEnabled) {
                TerrainLabApplication.this.autoApplyGenerationEnabled = autoApplyGenerationEnabled;
            }

            @Override
            public void onGeneratorParameterChanged(TerrainControlParameter terrainControlParameter, float value) {
                terrainControlParameter.setValue(terrainGeneratorSettings, value);
                terrainGeneratorPreset = TerrainGeneratorPreset.CUSTOM;
                if (autoApplyGenerationEnabled) {
                    regenerateTerrain(currentSeed);
                }
            }

            @Override
            public void onHeightColorModeChanged(HeightColorMode newHeightColorMode) {
                heightColorMode = newHeightColorMode;
                visibleTerrainDirty = true;
            }

            @Override
            public void onGridOverlayModeChanged(GridOverlayMode newGridOverlayMode) {
                gridOverlayMode = newGridOverlayMode;
            }

            @Override
            public void onGridStepChanged(int gridStepInCells) {
                gridStepIndex = gridStepIndex(gridStepInCells);
            }

            @Override
            public void onInteractionModeChanged(TerrainInteractionMode newTerrainInteractionMode) {
                terrainInteractionMode = newTerrainInteractionMode;
                if (terrainInteractionMode == TerrainInteractionMode.SCULPT) {
                    showPipelineSnapshots = false;
                }
                visibleTerrainDirty = true;
            }

            @Override
            public void onSculptToolChanged(SculptTool newSculptTool) {
                sculptTool = newSculptTool;
            }

            @Override
            public void onBrushRadiusChanged(float newBrushRadiusInCells) {
                brushRadiusInCells = clamp(newBrushRadiusInCells, BRUSH_RADIUS_MIN, BRUSH_RADIUS_MAX);
            }

            @Override
            public void onBrushStrengthChanged(float newBrushStrength) {
                brushStrength = clamp(newBrushStrength, BRUSH_STRENGTH_MIN, BRUSH_STRENGTH_MAX);
            }

            @Override
            public void onFlattenTargetChanged(float newFlattenTargetHeight) {
                float minimumSupportedElevation = TerrainGeneratorSettings.minimumSupportedElevation();
                float maximumSupportedElevation = TerrainGeneratorSettings.maximumSupportedElevation();
                flattenTargetHeight = clamp(newFlattenTargetHeight, minimumSupportedElevation, maximumSupportedElevation);
            }

            @Override
            public void onClearSculptLayerRequested() {
                terrainSculptLayer.clear();
                visibleTerrainDirty = true;
            }

            @Override
            public void onStatusPanelToggleRequested() {
                statusPanelVisible = !statusPanelVisible;
            }
        };
    }

    private void applyPreset(TerrainGeneratorPreset preset) {
        if (preset == TerrainGeneratorPreset.CUSTOM) {
            terrainGeneratorPreset = TerrainGeneratorPreset.CUSTOM;
            return;
        }

        terrainGeneratorPreset = preset;
        terrainGeneratorSettings = TerrainGeneratorSettings.fromPreset(preset);
        regenerateTerrain(currentSeed);
    }

    private int gridStepIndex(int gridStepInCells) {
        for (int optionIndex = 0; optionIndex < GRID_STEP_OPTIONS.length; optionIndex++) {
            if (GRID_STEP_OPTIONS[optionIndex] == gridStepInCells) {
                return optionIndex;
            }
        }
        return gridStepIndex;
    }

    private void drawHud() {
        if (!statusPanelVisible) {
            return;
        }

        TerrainGrid visibleTerrainGrid = visibleTerrainGrid();
        TerrainStatistics terrainStatistics = TerrainStatistics.from(visibleTerrainGrid);
        float selectedHeight = cursorInsideTerrain ? visibleTerrainGrid.getHeight(selectedCellX, selectedCellY) : 0f;

        drawStatusPanelBackground();

        spriteBatch.setProjectionMatrix(hudCamera.combined);
        spriteBatch.begin();
        drawStatusPanel(terrainStatistics, selectedHeight);
        spriteBatch.end();
    }

    private void drawStatusPanelBackground() {
        float panelX = STATUS_PANEL_CORNER_MARGIN;
        float panelY = hudCamera.viewportHeight - STATUS_PANEL_HEIGHT - STATUS_PANEL_CORNER_MARGIN;

        hudShapeRenderer.setProjectionMatrix(hudCamera.combined);
        hudShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        hudShapeRenderer.setColor(STATUS_PANEL_BACKGROUND_COLOR);
        hudShapeRenderer.rect(panelX, panelY, STATUS_PANEL_WIDTH, STATUS_PANEL_HEIGHT);
        hudShapeRenderer.end();

        hudShapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        hudShapeRenderer.setColor(STATUS_PANEL_BORDER_COLOR);
        hudShapeRenderer.rect(panelX, panelY, STATUS_PANEL_WIDTH, STATUS_PANEL_HEIGHT);
        hudShapeRenderer.end();
    }

    private void drawStatusPanel(TerrainStatistics terrainStatistics, float selectedHeight) {
        float startX = STATUS_PANEL_CORNER_MARGIN + STATUS_PANEL_PADDING;
        float startY = hudCamera.viewportHeight - STATUS_PANEL_CORNER_MARGIN - STATUS_PANEL_TITLE_OFFSET;

        bitmapFont.draw(spriteBatch, "Status", startX, startY);
        bitmapFont.draw(spriteBatch, "Seed: " + currentSeed, startX, startY - STATUS_PANEL_LINE_HEIGHT * 1f);
        bitmapFont.draw(spriteBatch, "Mode: " + terrainInteractionMode.displayName(), startX, startY - STATUS_PANEL_LINE_HEIGHT * 2f);
        bitmapFont.draw(spriteBatch, "Preset: " + terrainGeneratorPreset.displayName(), startX, startY - STATUS_PANEL_LINE_HEIGHT * 3f);
        bitmapFont.draw(spriteBatch, "Auto apply: " + (autoApplyGenerationEnabled ? "on" : "off"), startX, startY - STATUS_PANEL_LINE_HEIGHT * 4f);
        bitmapFont.draw(spriteBatch, "Palette: " + heightColorMode.displayName(), startX, startY - STATUS_PANEL_LINE_HEIGHT * 5f);
        bitmapFont.draw(spriteBatch, "Grid: " + gridOverlayMode.displayName() + ", step=" + gridStep(), startX, startY - STATUS_PANEL_LINE_HEIGHT * 6f);
        bitmapFont.draw(spriteBatch, "View: " + currentViewLabel(), startX, startY - STATUS_PANEL_LINE_HEIGHT * 7f);
        bitmapFont.draw(spriteBatch,
                "Range: %.3f .. %.3f".formatted(
                        terrainStatistics.heightRange().minimum(),
                        terrainStatistics.heightRange().maximum()
                ),
                startX,
                startY - STATUS_PANEL_LINE_HEIGHT * 8f
        );
        bitmapFont.draw(spriteBatch, selectedCellCoordinatesLabel(), startX, startY - STATUS_PANEL_LINE_HEIGHT * 9f);
        bitmapFont.draw(spriteBatch, selectedHeightLabel(selectedHeight), startX, startY - STATUS_PANEL_LINE_HEIGHT * 10f);
        bitmapFont.draw(spriteBatch, controlPanelHintLabel(), startX, startY - STATUS_PANEL_LINE_HEIGHT * 11f);
    }

    private String controlPanelHintLabel() {
        return "F1 generator, F2 status, F3 sculpt, Esc hides windows";
    }

    private String currentViewLabel() {
        if (!showPipelineSnapshots || pipelineSnapshots.isEmpty()) {
            return "final";
        }

        String pipelineStepName = pipelineStepNames.get(snapshotIndex);
        int pipelineStepNumber = snapshotIndex + 1;
        return "pipeline " + pipelineStepNumber + " (" + pipelineStepName + ")";
    }

    private String selectedCellCoordinatesLabel() {
        if (!cursorInsideTerrain) {
            return "Cell: outside terrain";
        }
        return "Cell: x=%d, y=%d".formatted(selectedCellX, selectedCellY);
    }

    private String selectedHeightLabel(float selectedHeight) {
        if (!cursorInsideTerrain) {
            return "Height: n/a";
        }
        return "Height: %.3f".formatted(selectedHeight);
    }

    private int gridStep() {
        return GRID_STEP_OPTIONS[gridStepIndex];
    }

    private float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
