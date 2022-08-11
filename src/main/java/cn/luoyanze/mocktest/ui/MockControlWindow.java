package cn.luoyanze.mocktest.ui;

import cn.luoyanze.mocktest.service.LoggerReplaceService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @Author luoyanze[luoyanzeze@icloud.com]
 * @Date 2022/8/8 6:00 PM
 */


public class MockControlWindow {
    private JButton refineAllJavaTest;
    private JButton replaceAllLogger;
    private JPanel ParentPanel;

    public MockControlWindow(@NotNull Project project, @NotNull ToolWindow toolWindow) {

        final String projectPath = project.getBasePath();

        refineAllJavaTest.addActionListener(e -> {

        });

        replaceAllLogger.addActionListener(e -> {
            new LoggerReplaceService(e, project, toolWindow).startReplace();
        });

    }

    public JPanel getParentPanel() {
        return ParentPanel;
    }

}
