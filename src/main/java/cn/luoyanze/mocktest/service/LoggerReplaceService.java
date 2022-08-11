package cn.luoyanze.mocktest.service;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.NotNull;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author luoyanze[luoyanzeze@icloud.com]
 * @Date 2022/8/10 11:31 PM
 */


public class LoggerReplaceService {

    private static final String LOGGER_REG = "import[^;\n\\(\\)]*?\\.Logger(?=[;|\\s|\n])";
    private static final String LOGGERFACTORY_REG = "import[^;\n]*?\\.LoggerFactory(?=[;|\\s|\n])";
    private final ActionEvent actionEvent;
    private final Project project;
    private final ToolWindow toolWindow;


    public LoggerReplaceService(ActionEvent e, @NotNull Project project, @NotNull ToolWindow toolWindow) {
        this.actionEvent = e;
        this.project = project;
        this.toolWindow = toolWindow;
    }

    public void startReplace() {
        Module[] modules = ModuleManager.getInstance(project).getModules();
        Set<String> startPoints = Arrays.stream(modules)
                .map(ModuleUtil::getModuleDirPath)
                .map(dir -> Set.of(dir + "/src/main/java", dir + "/src/main/test"))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        startPoints.forEach(this::replaceSingele);
    }

    private void replaceSingele(String path) {
        try{
             Files.walk(Paths.get(path), FileVisitOption.FOLLOW_LINKS)
                    .filter(it -> !Files.isDirectory(it, LinkOption.NOFOLLOW_LINKS))
                    .filter(it -> it.toString().endsWith("java") || it.toString().endsWith("kt"))
                    .forEach(this::loggerhandler);
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    private void loggerhandler(Path path) {
        try {
            String before = Files.readString(path);
            String after = before.replaceAll(LOGGERFACTORY_REG, "import org.slf4j.LoggerFactory")
                    .replaceAll(LOGGER_REG, "import org.slf4j.Logger");

            if (before.equals(after)) {
                return;
            }
            Files.writeString(path, after, StandardOpenOption.WRITE);

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}











