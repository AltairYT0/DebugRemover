package me.altair;

import me.altair.asm.DebugRemover;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java -jar DebugRemover.jar <jarfile_path>");
            return;
        }

        String jarPath = args[0];
        File jarFile = new File(jarPath);

        if (!jarFile.exists() || !jarFile.getName().endsWith(".jar")) {
            System.out.println("Error: Provided path is not a valid JAR file.");
            return;
        }

        try {
            if (verifyJarFile(jarFile)) {
                DebugRemover.removeDebugInfo(jarFile);
            } else {
                System.out.println("Error: The file is not a valid JAR file.");
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    private static boolean verifyJarFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] signature = new byte[4];
            if (fis.read(signature) != 4) {
                return false;
            }

            return (signature[0] == 0x50 && signature[1] == 0x4B &&
                    signature[2] == 0x03 && signature[3] == 0x04);
        }
    }
}
