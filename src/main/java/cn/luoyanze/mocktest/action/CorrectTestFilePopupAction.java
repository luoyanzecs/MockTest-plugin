package cn.luoyanze.mocktest.action;

import cn.luoyanze.mocktest.parser.model.SimpleJavaSource;
import cn.luoyanze.mocktest.parser.model.TestSourceMap;
import cn.luoyanze.mocktest.parser.visit.*;
import cn.luoyanze.mocktest.service.RefineAllTestService;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class CorrectTestFilePopupAction extends AnAction {

    private static final Logger logger = LoggerFactory.getLogger(CorrectTestFilePopupAction.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);

        if (psiFile == null) {
            Notifications.Bus.notify(new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "MockTest", "无法获取文件", NotificationType.INFORMATION));
            return;
        }
        if (!psiFile.getFileType().getName().equals("JAVA")) {
            // 只支持Java文件
            Notifications.Bus.notify(new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "MockTest", "只支持JAVA文件类型", NotificationType.INFORMATION));
            return;
        }
        // 文件路径
        Path testSourcePath = Paths.get(psiFile.getOriginalFile().getVirtualFile().getPath());

        Module moduleForFile = ModuleUtil.findModuleForFile(psiFile);
        if (moduleForFile == null) {
            Notifications.Bus.notify(new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "MockTest", "无法获取maven模块", NotificationType.INFORMATION));
            return;
        }
        String moduleFilePath = moduleForFile.getModuleFilePath();
        String name = moduleForFile.getName();
        int index = moduleFilePath.lastIndexOf("/" + name + "/");

        Path modulePath = Paths.get(moduleFilePath.substring(0, index), "/" + name + "/");

        RefineAllTestService.refine(testSourcePath, modulePath);
    }
}
