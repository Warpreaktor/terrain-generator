package ru.nik.terraingenerator.lab;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

/**
 * Точка входа desktop-приложения для визуального просмотра рельефной сетки.
 */
public final class TerrainLabMain {

    private static final int WINDOW_WIDTH = 1440;
    private static final int WINDOW_HEIGHT = 960;
    private static final int FOREGROUND_FPS = 60;

    private TerrainLabMain() {
    }

    /**
     * Запускает лабораторию визуального просмотра terrain-сетки.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("terrain-generator");
        configuration.setWindowedMode(WINDOW_WIDTH, WINDOW_HEIGHT);
        configuration.setForegroundFPS(FOREGROUND_FPS);
        configuration.useVsync(true);

        new Lwjgl3Application(new TerrainLabApplication(), configuration);
    }
}
