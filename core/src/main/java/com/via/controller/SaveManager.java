package com.via.controller;

import com.via.model.GameState;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SaveManager {

    private static final String SAVE_DIR = "saves";
    private static final String SAVE_EXTENSION = ".via";

    public static void saveGame(GameState gameState, String saveName) throws IOException {
        Files.createDirectories(Paths.get(SAVE_DIR));
        String filePath = SAVE_DIR + File.separator + saveName + SAVE_EXTENSION;
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
            out.writeObject(gameState);
        }
    }

    public static GameState loadGame(String saveName) throws IOException, ClassNotFoundException {
        String filePath = SAVE_DIR + File.separator + saveName + SAVE_EXTENSION;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath))) {
            return (GameState) in.readObject();
        }
    }

    public static String[] listSaves() {
        File saveDir = new File(SAVE_DIR);
        if (!saveDir.exists()) return new String[0];
        String[] files = saveDir.list((dir, name) -> name.endsWith(SAVE_EXTENSION));
        if (files == null) return new String[0];
        for (int i = 0; i < files.length; i++) {
            files[i] = files[i].replace(SAVE_EXTENSION, "");
        }
        return files;
    }
}
