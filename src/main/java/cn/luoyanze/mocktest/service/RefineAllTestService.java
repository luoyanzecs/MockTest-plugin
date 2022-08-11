package cn.luoyanze.mocktest.service;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.NotNull;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author luoyanze[luoyanzeze@icloud.com]
 * @Date 2022/8/11 12:42 AM
 */


public class RefineAllTestService {

    private final ActionEvent actionEvent;
    private final Project project;
    private final ToolWindow toolWindow;


    public RefineAllTestService(ActionEvent e, @NotNull Project project, @NotNull ToolWindow toolWindow) {
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

    }

}
