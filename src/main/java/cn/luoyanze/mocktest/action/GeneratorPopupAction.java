package cn.luoyanze.mocktest.action;

import cn.luoyanze.mocktest.parser.model.JavaFileV3;
import cn.luoyanze.mocktest.service.TemplateService;
import cn.luoyanze.mocktest.parser.visit.SourceVisitorAdapter;
import cn.luoyanze.mocktest.parser.visit.TestVisitorAdapter;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class GeneratorPopupAction extends AnAction {


    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            PsiFile data = e.getData(LangDataKeys.PSI_FILE);

            if (data == null) {
                return;
            }

            if (!data.getFileType().getName().equals("JAVA")) {
                // 只支持Java文件
                Notifications.Bus.notify(new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "MockTest", "只支持JAVA文件类型", NotificationType.INFORMATION));
                return;
            }

            Editor editor = e.getData(LangDataKeys.EDITOR);

            if (editor == null) {
                Notifications.Bus.notify(new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "MockTest", "无法获取编辑器", NotificationType.ERROR));
                return;
            }

            Module module = ModuleUtil.findModuleForFile(data);
            String moduleRootPath = ModuleRootManager.getInstance(module).getContentRoots()[0].getPath();

            // 目录的路径
            String folderPath = data.getContainingDirectory().toString().split(":")[1];
            // 文件内容
            String fileText = editor.getDocument().getText();
            // Java文件名
            String classname = data.getName().substring(0, data.getName().lastIndexOf("."));

            JavaFileV3 prepareForTestGenFile = new JavaFileV3();
            new SourceVisitorAdapter().visit(StaticJavaParser.parse(fileText), prepareForTestGenFile);

            String testDirPath = folderPath.replace("/src/main", "/src/test");

            Path dirPath = Paths.get(testDirPath);
            // 包路径不存在
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            // 包路径存在，检查是否有测试文件。
            File testDictionary = new File(testDirPath);

            File file = Arrays.stream(testDictionary.listFiles())
                    .filter(File::isFile)
                    .filter(it -> it.getName().endsWith(".java") && it.getName().toLowerCase().contains(classname.toLowerCase()))
                    .findFirst().orElse(null);

            if (file == null) {
                String testClassname = classname + "Test";
                prepareForTestGenFile.setTestFilenameForGenerate(testClassname);
                String mockTestTemplate = TemplateService.generateTemplate(prepareForTestGenFile);
                Files.write(Paths.get(testDirPath, testClassname + ".java"), mockTestTemplate.getBytes(), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            } else {
                FileInputStream fileInputStream = new FileInputStream(file);
                String testContet = new String(fileInputStream.readAllBytes());
                CompilationUnit testParser = StaticJavaParser.parse(testContet);
                new TestVisitorAdapter().visit(testParser, prepareForTestGenFile);
                Files.writeString(Paths.get(file.getAbsolutePath()), testParser.toString(), StandardOpenOption.WRITE);
            }

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}




