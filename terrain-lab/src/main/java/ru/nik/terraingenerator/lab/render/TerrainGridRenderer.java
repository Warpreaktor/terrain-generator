package ru.nik.terraingenerator.lab.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import ru.nik.terraingenerator.core.grid.TerrainGrid;

/**
 * Рендерит сетку высот сверху в виде текстуры и простых оверлеев.
 */
public final class TerrainGridRenderer {

    private static final float SELECTION_ALPHA = 0.95f;
    private static final float BRUSH_ALPHA = 0.75f;
    private static final float BRUSH_SEGMENT_COUNT = 48f;
    private static final int PIXMAP_VERTICAL_STEP = 1;
    private static final Color GRID_COLOR = new Color(1f, 1f, 1f, 1f);
    private static final Color SELECTION_COLOR = new Color(1f, 0.2f, 0.2f, SELECTION_ALPHA);
    private static final Color BRUSH_COLOR = new Color(1f, 0.75f, 0.15f, BRUSH_ALPHA);

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private Texture texture;

    /**
     * Полностью перестраивает текстуру карты по текущей сетке.
     *
     * <p>Важно: Pixmap хранит пиксели с началом координат в верхнем левом углу,
     * а world-координаты лаборатории живут в нижнем левом углу. Поэтому при переносе
     * высот в текстуру вертикальная ось должна быть инвертирована, иначе курсор и
     * кисть будут рисоваться в одном месте, а изменение рельефа визуально проявляться
     * зеркально по оси Y.</p>
     *
     * @param terrainGrid сетка высот
     * @param heightColorMode цветовой режим
     * @param displayMaximumAbsoluteElevation стабильный диапазон отображения относительно нуля
     */
    public void rebuild(
            TerrainGrid terrainGrid,
            HeightColorMode heightColorMode,
            float displayMaximumAbsoluteElevation
    ) {
        disposeTexture();

        int terrainWidth = terrainGrid.width();
        int terrainHeight = terrainGrid.height();
        int lastPixmapRowIndex = terrainHeight - PIXMAP_VERTICAL_STEP;
        float safeDisplayMaximumAbsoluteElevation = Math.max(displayMaximumAbsoluteElevation, 0.0001f);

        Pixmap pixmap = new Pixmap(terrainWidth, terrainHeight, Pixmap.Format.RGBA8888);

        for (int terrainY = 0; terrainY < terrainHeight; terrainY++) {
            int pixmapY = lastPixmapRowIndex - terrainY;

            for (int terrainX = 0; terrainX < terrainWidth; terrainX++) {
                float cellElevation = terrainGrid.getHeight(terrainX, terrainY);
                float normalizedHeight = normalizeSignedElevation(cellElevation, safeDisplayMaximumAbsoluteElevation);
                Color cellColor = TerrainColorResolver.resolve(normalizedHeight, heightColorMode);
                pixmap.setColor(cellColor);
                pixmap.drawPixel(terrainX, pixmapY);
            }
        }

        texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        pixmap.dispose();
    }

    /**
     * Нормализует signed-высоту так, чтобы ноль всегда отображался как середина
     * цветовой шкалы, отрицательные значения попадали ниже середины, а положительные
     * выше неё.
     *
     * <p>В отличие от прошлой версии здесь используется не текущий диапазон карты,
     * а стабильный диапазон отображения. Благодаря этому локальная правка кистью не
     * "перекрашивает" весь остальной мир из-за одной глубокой точки.</p>
     *
     * @param elevation signed-высота клетки
     * @param maximumAbsoluteElevation максимальное абсолютное отклонение высоты от нуля
     * @return значение в диапазоне от 0 до 1, где {@code 0.5} соответствует нулю
     */
    private float normalizeSignedElevation(float elevation, float maximumAbsoluteElevation) {
        float normalizedOffset = elevation / (maximumAbsoluteElevation * 2f);
        float signedMidpoint = 0.5f;
        float normalizedHeight = signedMidpoint + normalizedOffset;
        return Math.max(0f, Math.min(1f, normalizedHeight));
    }

    /**
     * Рисует оверлей в виде сетки поверх карты.
     *
     * @param worldCamera активная мировая камера
     * @param gridWidth ширина карты
     * @param gridHeight высота карты
     * @param gridOverlayMode режим отображения сетки
     * @param gridStep шаг визуальной сетки
     */
    public void renderGrid(
            OrthographicCamera worldCamera,
            int gridWidth,
            int gridHeight,
            GridOverlayMode gridOverlayMode,
            int gridStep
    ) {
        if (gridOverlayMode == GridOverlayMode.OFF) {
            return;
        }

        shapeRenderer.setProjectionMatrix(worldCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(GRID_COLOR.r, GRID_COLOR.g, GRID_COLOR.b, gridOverlayMode.alpha());

        int normalizedGridStep = Math.max(1, gridStep);

        for (int x = 0; x <= gridWidth; x += normalizedGridStep) {
            shapeRenderer.line(x, 0f, x, gridHeight);
        }
        if (gridWidth % normalizedGridStep != 0) {
            shapeRenderer.line(gridWidth, 0f, gridWidth, gridHeight);
        }

        for (int y = 0; y <= gridHeight; y += normalizedGridStep) {
            shapeRenderer.line(0f, y, gridWidth, y);
        }
        if (gridHeight % normalizedGridStep != 0) {
            shapeRenderer.line(0f, gridHeight, gridWidth, gridHeight);
        }

        shapeRenderer.end();
    }

    /**
     * Подсвечивает выбранную клетку.
     *
     * @param worldCamera активная мировая камера
     * @param selectedCellX координата клетки по оси X
     * @param selectedCellY координата клетки по оси Y
     */
    public void renderSelection(OrthographicCamera worldCamera, int selectedCellX, int selectedCellY) {
        shapeRenderer.setProjectionMatrix(worldCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(SELECTION_COLOR);
        shapeRenderer.rect(selectedCellX, selectedCellY, 1f, 1f);
        shapeRenderer.end();
    }

    /**
     * Рисует контур текущей кисти поверх карты.
     *
     * @param worldCamera активная мировая камера
     * @param centerX координата центра кисти по оси X
     * @param centerY координата центра кисти по оси Y
     * @param radiusInCells радиус кисти в клетках
     */
    public void renderBrush(OrthographicCamera worldCamera, float centerX, float centerY, float radiusInCells) {
        shapeRenderer.setProjectionMatrix(worldCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(BRUSH_COLOR);
        shapeRenderer.circle(centerX, centerY, radiusInCells, (int) BRUSH_SEGMENT_COUNT);
        shapeRenderer.end();
    }

    /**
     * Возвращает текущую текстуру карты.
     *
     * @return текстура карты
     */
    public Texture texture() {
        if (texture == null) {
            throw new IllegalStateException("Текстура ещё не создана.");
        }
        return texture;
    }

    /**
     * Освобождает все графические ресурсы.
     */
    public void dispose() {
        disposeTexture();
        shapeRenderer.dispose();
    }

    /**
     * Освобождает текущую текстуру, если она существует.
     */
    private void disposeTexture() {
        if (texture != null) {
            texture.dispose();
            texture = null;
        }
    }
}
