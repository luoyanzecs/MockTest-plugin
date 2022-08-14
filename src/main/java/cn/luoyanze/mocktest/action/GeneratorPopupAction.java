package cn.luoyanze.mocktest.action;

import cn.luoyanze.mocktest.parser.model.SimpleJavaSource;
import cn.luoyanze.mocktest.parser.visit.JavaSourceVisitorAdapter;
import cn.luoyanze.mocktest.parser.visit.JavaTestWithSourceVisitorAdapter;
import cn.luoyanze.mocktest.service.TemplateService;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Objects;

public class GeneratorPopupAction extends AnAction {

    private static final Logger logger = LoggerFactory.getLogger(GeneratorPopupAction.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
            if (psiFile == null) {
                Notifications.Bus.notify(new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "MockTest", "无法获取文件", NotificationType.INFORMATION));
                return;
            }
            if (!psiFile.getFileType().getName().equals("JAVA")) {
                Notifications.Bus.notify(new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "MockTest", "只支持JAVA文件类型", NotificationType.INFORMATION));
                return;
            }

            // 文件路径
            Path sourcePath = Paths.get(psiFile.getOriginalFile().getVirtualFile().getPath());

            // Java文件名
            String sourceName = sourcePath.getFileName().toString().split("\\.")[0];
            Path sourceDirPath = sourcePath.getParent();
            Path testDirPath = Paths.get(sourceDirPath.toString().replace(Paths.get("/src/main").toString(), Paths.get("/src/test").toString()));

            // 包路径不存在
            if (!Files.exists(testDirPath)) {
                Files.createDirectories(testDirPath);
            }

            // 包路径存在，检查是否有测试文件。
            File testDictionary = testDirPath.toFile();

            File testFile = Arrays.stream(Objects.requireNonNull(testDictionary.listFiles()))
                    .filter(File::isFile)
                    .filter(it -> it.getName().endsWith(".java") && it.getName().toLowerCase().contains(sourceName.toLowerCase()))
                    .findFirst().orElse(null);

            SimpleJavaSource simpleJavaSource = new SimpleJavaSource();
            new JavaSourceVisitorAdapter().visit(StaticJavaParser.parse(sourcePath), simpleJavaSource);

            if (testFile == null) {
                String testClassname = sourceName + "Test";
                String mockTestTemplate = TemplateService.generateTemplate(simpleJavaSource, testClassname);
                Files.write(testDirPath.resolve(testClassname + ".java"), mockTestTemplate.getBytes(), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            } else {
                CompilationUnit testParser = StaticJavaParser.parse(testFile);
                new JavaTestWithSourceVisitorAdapter().visit(testParser, simpleJavaSource);
                Files.write(testFile.getAbsoluteFile().toPath(), testParser.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE);
            }

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            Notifications.Bus.notify(new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "MockTest", ex.getMessage(), NotificationType.INFORMATION));
        }
    }
}




