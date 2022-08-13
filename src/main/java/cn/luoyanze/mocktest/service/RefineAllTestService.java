package cn.luoyanze.mocktest.service;

import cn.luoyanze.mocktest.parser.model.SimpleJavaSource;
import cn.luoyanze.mocktest.parser.model.TestSourceMap;
import cn.luoyanze.mocktest.parser.visit.JavaSourceVisitorAdapter;
import cn.luoyanze.mocktest.parser.visit.JavaTestWithSourceVisitorAdapter;
import cn.luoyanze.mocktest.parser.visit.PreviousTestVisitAdapter;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author luoyanze[luoyanzeze@icloud.com]
 * @Date 2022/8/11 12:42 AM
 */


public class RefineAllTestService {

    private static final Logger logger = LoggerFactory.getLogger(RefineAllTestService.class);
    private final ActionEvent actionEvent;
    private final Project project;
    private final ToolWindow toolWindow;


    public RefineAllTestService(ActionEvent e, @NotNull Project project, @NotNull ToolWindow toolWindow) {
        this.actionEvent = e;
        this.project = project;
        this.toolWindow = toolWindow;
    }

    public void startAllRefine() {
        Module[] modules = ModuleManager.getInstance(project).getModules();
        Set<Path> startPoints = Arrays.stream(modules)
                .map(ModuleUtil::getModuleDirPath)
                .map(Paths::get)
                .map(dir -> dir.resolve("src/main/test"))
                .collect(Collectors.toSet());

        startPoints.forEach(this::refineSingle);
    }

    private void refineSingle(Path path) {
        try{
            Files.walk(path, FileVisitOption.FOLLOW_LINKS)
                    .filter(it -> !Files.isDirectory(it, LinkOption.NOFOLLOW_LINKS))
                    .filter(it -> it.toString().endsWith("java"))
                    .forEach(it -> refine(it, path));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void refine(Path testPath, Path modulePath) {
        try {
            TestSourceMap testSourceMap = new TestSourceMap();
            CompilationUnit testParser = StaticJavaParser.parse(testPath);

            new PreviousTestVisitAdapter().visit(testParser, testSourceMap);
            if (!testSourceMap.isRunWithAnnotated()) {
                Notifications.Bus.notify(new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "MockTest", "该测试类未被RunWith注解", NotificationType.ERROR));
                return;
            }

            Path sourcePath = modulePath
                    .resolve(Paths.get("src/main/java"))
                    .resolve(Paths.get(testSourceMap.getSource().toString().replaceAll("\\.", File.pathSeparator) + ".java"));

            if (Files.exists(sourcePath)) {
                // 解析Java source文件
                SimpleJavaSource simpleJavaSource = new SimpleJavaSource();
                new JavaSourceVisitorAdapter().visit(StaticJavaParser.parse(sourcePath), simpleJavaSource);

                new JavaTestWithSourceVisitorAdapter().visit(testParser, simpleJavaSource);

                Files.write(testPath, testParser.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE);

            } else {
                // 只支持Java文件
                Notifications.Bus.notify(new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "MockTest", "只支持JAVA文件类型", NotificationType.ERROR));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Notifications.Bus.notify(new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "MockTest", e.getMessage(), NotificationType.INFORMATION));

        }

    }

}
