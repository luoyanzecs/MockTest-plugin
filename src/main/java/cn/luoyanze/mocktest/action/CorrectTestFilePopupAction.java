package cn.luoyanze.mocktest.action;

import cn.luoyanze.mocktest.parser.model.JavaFileV3;
import cn.luoyanze.mocktest.parser.model.TestSourceMap;
import cn.luoyanze.mocktest.parser.visit.SourceVisitorAdapter;
import cn.luoyanze.mocktest.parser.visit.TestPrepareVisitAdapter;
import cn.luoyanze.mocktest.parser.visit.TestVisitorAdapter;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.components.ComponentManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiFile;
import com.intellij.serviceContainer.ComponentManagerImpl;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CorrectTestFilePopupAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
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
            return;
        }

        //ServiceManager
        //Messages.showMessageDialog();
        // 目录的路径
        String folderPath = data.getContainingDirectory().toString().split(":")[1];
        // 文件内容
        String fileText = editor.getDocument().getText();
        // Java文件名
        String classname = data.getName().substring(0, data.getName().lastIndexOf("."));

        TestSourceMap testSourceMap = new TestSourceMap();
        CompilationUnit testParser = null;
        try {
            testParser = StaticJavaParser.parse(Paths.get(folderPath, data.getName()));
            new TestPrepareVisitAdapter().visit(testParser, testSourceMap);

            String sourcePath = folderPath.split(Paths.get("src/test").toString())[0]
                    + Paths.get("src/main/java/")
                    + testSourceMap.getSource().toString().replaceAll("\\.", File.pathSeparator) + ".java";

            System.out.println(sourcePath);
            if (Files.exists(Paths.get(sourcePath))) {
                String ctx = Files.readString(Paths.get(sourcePath));

                JavaFileV3 prepareForTestGenFile = new JavaFileV3();
                new SourceVisitorAdapter().visit(StaticJavaParser.parse(ctx), prepareForTestGenFile);
                new TestVisitorAdapter().visit(testParser, prepareForTestGenFile);
                editor.getDocument().setText(testParser.toString());
            } else {
                // 只支持Java文件
                Notifications.Bus.notify(new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "MockTest", "只支持JAVA文件类型", NotificationType.ERROR));
            }

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
