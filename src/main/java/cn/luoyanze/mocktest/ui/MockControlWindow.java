package cn.luoyanze.mocktest.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @Author luoyanze[luoyanzeze@icloud.com]
 * @Date 2022/8/8 6:00 PM
 */


public class MockControlWindow {
    private JButton refineAllJavaTest;
    private JButton replaceAllLogger;
    private JPanel ParentPanel;

    public MockControlWindow(@NotNull Project project, @NotNull ToolWindow toolWindow) {

        String projectPath = project.getBasePath();

        refineAllJavaTest.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        replaceAllLogger.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

    }

    public JPanel getParentPanel() {
        return ParentPanel;
    }

}
