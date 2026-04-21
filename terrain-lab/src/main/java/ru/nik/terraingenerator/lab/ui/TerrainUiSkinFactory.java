package ru.nik.terraingenerator.lab.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * Фабрика минималистичного скина для панели управления лаборатории.
 *
 * <p>Скин строится полностью в коде на основе одноцветной текстуры, чтобы не тащить
 * в проект внешние UI-ассеты раньше времени. Позже этот слой можно заменить на skin,
 * собранный в Skin Composer, VisUI или другом редакторе.</p>
 */
public final class TerrainUiSkinFactory {

    private static final int SINGLE_PIXEL_TEXTURE_SIZE = 1;
    private static final int CURSOR_MIN_WIDTH = 2;
    private static final int CURSOR_MIN_HEIGHT = 24;
    private static final int BUTTON_MIN_WIDTH = 18;
    private static final int BUTTON_MIN_HEIGHT = 18;
    private static final int TEXT_FIELD_MIN_WIDTH = 16;
    private static final int TEXT_FIELD_MIN_HEIGHT = 24;
    private static final int SLIDER_BACKGROUND_MIN_HEIGHT = 8;
    private static final int SLIDER_KNOB_MIN_WIDTH = 12;
    private static final int SLIDER_KNOB_MIN_HEIGHT = 22;
    private static final int LIST_SELECTION_MIN_HEIGHT = 20;
    private static final int CHECKBOX_BOX_SIZE = 18;

    private static final float UI_FONT_SCALE = 0.90f;

    private static final Color BACKGROUND_PANEL_COLOR = new Color(0.10f, 0.12f, 0.15f, 0.96f);
    private static final Color BACKGROUND_SECTION_COLOR = new Color(0.16f, 0.19f, 0.23f, 0.98f);
    private static final Color BACKGROUND_HOVER_COLOR = new Color(0.22f, 0.26f, 0.31f, 0.98f);
    private static final Color BACKGROUND_ACTIVE_COLOR = new Color(0.28f, 0.34f, 0.40f, 1.00f);
    private static final Color ACCENT_COLOR = new Color(0.93f, 0.72f, 0.23f, 1.00f);
    private static final Color TEXT_PRIMARY_COLOR = new Color(0.96f, 0.97f, 0.98f, 1.00f);
    private static final Color TEXT_MUTED_COLOR = new Color(0.72f, 0.77f, 0.82f, 1.00f);
    private static final Color TEXT_SELECTION_COLOR = new Color(0.24f, 0.30f, 0.36f, 1.00f);
    private static final Color LIST_SELECTION_COLOR = new Color(0.31f, 0.39f, 0.46f, 1.00f);

    private TerrainUiSkinFactory() {
    }

    /**
     * Создаёт новый экземпляр скина для панели управления.
     *
     * @return готовый skin со стилями стандартных виджетов Scene2D.UI
     */
    public static Skin create() {
        Skin skin = new Skin();

        Texture baseTexture = createBaseTexture();
        BitmapFont uiFont = createUiFont();

        skin.add("white", baseTexture);
        skin.add("default-font", uiFont);

        Drawable panelDrawable = tintedDrawable(skin, BACKGROUND_PANEL_COLOR, BUTTON_MIN_WIDTH, BUTTON_MIN_HEIGHT);
        Drawable sectionDrawable = tintedDrawable(skin, BACKGROUND_SECTION_COLOR, BUTTON_MIN_WIDTH, BUTTON_MIN_HEIGHT);
        Drawable hoverDrawable = tintedDrawable(skin, BACKGROUND_HOVER_COLOR, BUTTON_MIN_WIDTH, BUTTON_MIN_HEIGHT);
        Drawable activeDrawable = tintedDrawable(skin, BACKGROUND_ACTIVE_COLOR, BUTTON_MIN_WIDTH, BUTTON_MIN_HEIGHT);
        Drawable accentDrawable = tintedDrawable(skin, ACCENT_COLOR, BUTTON_MIN_WIDTH, BUTTON_MIN_HEIGHT);
        Drawable cursorDrawable = tintedDrawable(skin, ACCENT_COLOR, CURSOR_MIN_WIDTH, CURSOR_MIN_HEIGHT);
        Drawable textSelectionDrawable = tintedDrawable(skin, TEXT_SELECTION_COLOR, TEXT_FIELD_MIN_WIDTH, TEXT_FIELD_MIN_HEIGHT);
        Drawable listSelectionDrawable = tintedDrawable(skin, LIST_SELECTION_COLOR, TEXT_FIELD_MIN_WIDTH, LIST_SELECTION_MIN_HEIGHT);
        Drawable sliderBackgroundDrawable = tintedDrawable(skin, BACKGROUND_SECTION_COLOR, BUTTON_MIN_WIDTH, SLIDER_BACKGROUND_MIN_HEIGHT);
        Drawable sliderKnobDrawable = tintedDrawable(skin, ACCENT_COLOR, SLIDER_KNOB_MIN_WIDTH, SLIDER_KNOB_MIN_HEIGHT);
        Drawable checkboxOffDrawable = tintedDrawable(skin, BACKGROUND_SECTION_COLOR, CHECKBOX_BOX_SIZE, CHECKBOX_BOX_SIZE);
        Drawable checkboxOnDrawable = tintedDrawable(skin, ACCENT_COLOR, CHECKBOX_BOX_SIZE, CHECKBOX_BOX_SIZE);

        skin.add("panel-background", panelDrawable);
        skin.add("section-background", sectionDrawable);
        skin.add("hover-background", hoverDrawable);
        skin.add("active-background", activeDrawable);
        skin.add("accent-background", accentDrawable);
        skin.add("cursor-drawable", cursorDrawable);
        skin.add("text-selection", textSelectionDrawable);
        skin.add("list-selection", listSelectionDrawable);
        skin.add("slider-background", sliderBackgroundDrawable);
        skin.add("slider-knob", sliderKnobDrawable);
        skin.add("checkbox-off", checkboxOffDrawable);
        skin.add("checkbox-on", checkboxOnDrawable);

        createLabelStyle(skin, uiFont);
        createWindowStyle(skin, uiFont, panelDrawable);
        createTextButtonStyle(skin, uiFont, sectionDrawable, hoverDrawable, activeDrawable);
        createTextFieldStyle(skin, uiFont, sectionDrawable, activeDrawable, cursorDrawable, textSelectionDrawable);
        ScrollPane.ScrollPaneStyle scrollPaneStyle = createScrollPaneStyle(skin, sectionDrawable, hoverDrawable, accentDrawable);
        List.ListStyle listStyle = createListStyle(uiFont, listSelectionDrawable);
        createSelectBoxStyle(skin, uiFont, sectionDrawable, hoverDrawable, listStyle, scrollPaneStyle);
        createSliderStyle(skin, sliderBackgroundDrawable, sliderKnobDrawable);
        createCheckBoxStyle(skin, uiFont, checkboxOffDrawable, checkboxOnDrawable);

        skin.add("section-title-color", TEXT_MUTED_COLOR);
        return skin;
    }

    /**
     * Создаёт одноцветную текстуру-основу для drawables.
     */
    private static Texture createBaseTexture() {
        Pixmap pixmap = new Pixmap(SINGLE_PIXEL_TEXTURE_SIZE, SINGLE_PIXEL_TEXTURE_SIZE, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /**
     * Создаёт bitmap-font для панели.
     */
    private static BitmapFont createUiFont() {
        BitmapFont uiFont = new BitmapFont();
        uiFont.getData().setScale(UI_FONT_SCALE);
        return uiFont;
    }

    /**
     * Создаёт стиль обычных label-ов.
     */
    private static void createLabelStyle(Skin skin, BitmapFont uiFont) {
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = uiFont;
        labelStyle.fontColor = TEXT_PRIMARY_COLOR;
        skin.add("default", labelStyle);

        Label.LabelStyle sectionTitleStyle = new Label.LabelStyle();
        sectionTitleStyle.font = uiFont;
        sectionTitleStyle.fontColor = TEXT_MUTED_COLOR;
        skin.add("section-title", sectionTitleStyle);
    }

    /**
     * Создаёт стиль окна.
     */
    private static void createWindowStyle(Skin skin, BitmapFont uiFont, Drawable backgroundDrawable) {
        Window.WindowStyle windowStyle = new Window.WindowStyle();
        windowStyle.titleFont = uiFont;
        windowStyle.titleFontColor = TEXT_PRIMARY_COLOR;
        windowStyle.background = backgroundDrawable;
        skin.add("default", windowStyle);
    }

    /**
     * Создаёт стиль кнопок.
     */
    private static void createTextButtonStyle(
            Skin skin,
            BitmapFont uiFont,
            Drawable upDrawable,
            Drawable overDrawable,
            Drawable downDrawable
    ) {
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = uiFont;
        textButtonStyle.fontColor = TEXT_PRIMARY_COLOR;
        textButtonStyle.overFontColor = TEXT_PRIMARY_COLOR;
        textButtonStyle.downFontColor = TEXT_PRIMARY_COLOR;
        textButtonStyle.checkedFontColor = TEXT_PRIMARY_COLOR;
        textButtonStyle.up = upDrawable;
        textButtonStyle.over = overDrawable;
        textButtonStyle.down = downDrawable;
        textButtonStyle.checked = downDrawable;
        skin.add("default", textButtonStyle);
    }

    /**
     * Создаёт стиль текстовых полей.
     */
    private static void createTextFieldStyle(
            Skin skin,
            BitmapFont uiFont,
            Drawable backgroundDrawable,
            Drawable focusedDrawable,
            Drawable cursorDrawable,
            Drawable selectionDrawable
    ) {
        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
        textFieldStyle.font = uiFont;
        textFieldStyle.fontColor = TEXT_PRIMARY_COLOR;
        textFieldStyle.messageFont = uiFont;
        textFieldStyle.messageFontColor = TEXT_MUTED_COLOR;
        textFieldStyle.cursor = cursorDrawable;
        textFieldStyle.selection = selectionDrawable;
        textFieldStyle.background = backgroundDrawable;
        textFieldStyle.focusedBackground = focusedDrawable;
        textFieldStyle.disabledBackground = backgroundDrawable;
        skin.add("default", textFieldStyle);
    }

    /**
     * Создаёт стиль ScrollPane.
     */
    private static ScrollPane.ScrollPaneStyle createScrollPaneStyle(
            Skin skin,
            Drawable backgroundDrawable,
            Drawable trackDrawable,
            Drawable knobDrawable
    ) {
        ScrollPane.ScrollPaneStyle scrollPaneStyle = new ScrollPane.ScrollPaneStyle();
        scrollPaneStyle.background = backgroundDrawable;
        scrollPaneStyle.hScroll = trackDrawable;
        scrollPaneStyle.vScroll = trackDrawable;
        scrollPaneStyle.hScrollKnob = knobDrawable;
        scrollPaneStyle.vScrollKnob = knobDrawable;
        skin.add("default", scrollPaneStyle);
        return scrollPaneStyle;
    }

    /**
     * Создаёт стиль списка для SelectBox.
     */
    private static List.ListStyle createListStyle(BitmapFont uiFont, Drawable selectionDrawable) {
        List.ListStyle listStyle = new List.ListStyle();
        listStyle.font = uiFont;
        listStyle.fontColorSelected = TEXT_PRIMARY_COLOR;
        listStyle.fontColorUnselected = TEXT_PRIMARY_COLOR;
        listStyle.selection = selectionDrawable;
        return listStyle;
    }

    /**
     * Создаёт стиль выпадающего списка.
     */
    private static void createSelectBoxStyle(
            Skin skin,
            BitmapFont uiFont,
            Drawable backgroundDrawable,
            Drawable backgroundOverDrawable,
            List.ListStyle listStyle,
            ScrollPane.ScrollPaneStyle scrollPaneStyle
    ) {
        SelectBox.SelectBoxStyle selectBoxStyle = new SelectBox.SelectBoxStyle();
        selectBoxStyle.font = uiFont;
        selectBoxStyle.fontColor = TEXT_PRIMARY_COLOR;
        selectBoxStyle.background = backgroundDrawable;
        selectBoxStyle.backgroundOver = backgroundOverDrawable;
        selectBoxStyle.listStyle = listStyle;
        selectBoxStyle.scrollStyle = scrollPaneStyle;
        skin.add("default", selectBoxStyle);
    }

    /**
     * Создаёт стиль слайдера.
     */
    private static void createSliderStyle(Skin skin, Drawable backgroundDrawable, Drawable knobDrawable) {
        Slider.SliderStyle sliderStyle = new Slider.SliderStyle();
        sliderStyle.background = backgroundDrawable;
        sliderStyle.knob = knobDrawable;
        skin.add("default-horizontal", sliderStyle);
        skin.add("default-vertical", sliderStyle);
        skin.add("default", sliderStyle);
    }

    /**
     * Создаёт стиль чекбокса.
     */
    private static void createCheckBoxStyle(
            Skin skin,
            BitmapFont uiFont,
            Drawable checkboxOffDrawable,
            Drawable checkboxOnDrawable
    ) {
        CheckBox.CheckBoxStyle checkBoxStyle = new CheckBox.CheckBoxStyle();
        checkBoxStyle.font = uiFont;
        checkBoxStyle.fontColor = TEXT_PRIMARY_COLOR;
        checkBoxStyle.checkboxOff = checkboxOffDrawable;
        checkBoxStyle.checkboxOver = checkboxOffDrawable;
        checkBoxStyle.checkboxOn = checkboxOnDrawable;
        checkBoxStyle.checkboxOnOver = checkboxOnDrawable;
        skin.add("default", checkBoxStyle);
    }

    /**
     * Создаёт tinted drawable из white-текстуры skin-а.
     */
    private static Drawable tintedDrawable(
            Skin skin,
            Color color,
            float minimumWidth,
            float minimumHeight
    ) {
        TextureRegion baseRegion = skin.getRegion("white");
        return tintedDrawableFromRegion(baseRegion, color, minimumWidth, minimumHeight);
    }

    /**
     * Создаёт tinted drawable из базового texture region.
     */
    private static Drawable tintedDrawableFromRegion(
            TextureRegion baseRegion,
            Color color,
            float minimumWidth,
            float minimumHeight
    ) {
        SpriteDrawable drawable = (SpriteDrawable) new TextureRegionDrawable(baseRegion).tint(color);
        drawable.setMinWidth(minimumWidth);
        drawable.setMinHeight(minimumHeight);
        return drawable;
    }
}
