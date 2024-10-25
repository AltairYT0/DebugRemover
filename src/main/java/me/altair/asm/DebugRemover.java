package me.altair.asm;

import me.altair.cfg.Configuration;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.*;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.jar.*;

public class DebugRemover {

    public static void removeDebugInfo(File jarFile) throws IOException {
        Configuration configuration = new Configuration();
        JarFile originalJar = new JarFile(jarFile);
        String outputFileName = jarFile.getAbsolutePath().replace(".jar", "-nodebug.jar");
        File outputFile = new File(outputFileName);
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(outputFile));
        Enumeration<JarEntry> entries = originalJar.entries();

        int localVariablesRemoved = 0;
        int lineNumbersRemoved = 0;
        int sourceFilesRemoved = 0;

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            InputStream entryInputStream = originalJar.getInputStream(entry);

            if (entry.getName().endsWith(".class")) {
                ClassReader classReader = new ClassReader(entryInputStream);
                ClassNode classNode = new ClassNode();
                classReader.accept(classNode, 0);

                if (configuration.isRemoveSourceFile()) {
                    classNode.sourceFile = getUnreadableCharacter();
                    sourceFilesRemoved++;
                }

                for (MethodNode method : classNode.methods) {
                    if (configuration.isRemoveLocalVariables() && method.localVariables != null) {
                        localVariablesRemoved += method.localVariables.size();
                        method.localVariables.clear();
                    }

                    if (configuration.isRemoveLineNumbers() && method.instructions != null) {
                        Iterator<?> instructionIterator = method.instructions.iterator();
                        while (instructionIterator.hasNext()) {
                            Object instruction = instructionIterator.next();
                            if (instruction instanceof LineNumberNode) {
                                instructionIterator.remove();
                                lineNumbersRemoved++;
                            }
                        }
                    }
                }

                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                classNode.accept(classWriter);
                byte[] modifiedClassBytes = classWriter.toByteArray();
                JarEntry modifiedEntry = new JarEntry(entry.getName());
                jarOutputStream.putNextEntry(modifiedEntry);
                jarOutputStream.write(modifiedClassBytes);
                jarOutputStream.closeEntry();
            } else {
                jarOutputStream.putNextEntry(new JarEntry(entry.getName()));
                entryInputStream.transferTo(jarOutputStream);
                jarOutputStream.closeEntry();
            }
            entryInputStream.close();
        }

        originalJar.close();
        jarOutputStream.close();

        System.out.println("Debug information removed. New file created at: " + outputFile.getAbsolutePath());
        System.out.println("Total source files removed: " + (configuration.isRemoveSourceFile() ? sourceFilesRemoved : "Disabled"));
        System.out.println("Total local variables removed: " + (configuration.isRemoveLocalVariables() ? localVariablesRemoved : "Disabled"));
        System.out.println("Total line numbers removed: " + (configuration.isRemoveLineNumbers() ? lineNumbersRemoved : "Disabled"));
    }

    public static String getUnreadableCharacter() {
        return new String(new char[1]);
    }
}
