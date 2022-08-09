package cn.luoyanze.mocktest.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

/**
 * @Author luoyanze[luoyanzeze@icloud.com]
 * @Date 2022/8/8 6:06 PM
 */


public class MockControlWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        MockControlWindow mockControlWindow = new MockControlWindow(project, toolWindow);
        ContentFactory instance = ContentFactory.SERVICE.getInstance();
        Content content = instance.createContent(mockControlWindow.getParentPanel(), "", false);
        toolWindow.getContentManager().addContent(content);
    }
}
