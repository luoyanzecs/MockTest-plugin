package cn.luoyanze.mocktest.group;

import cn.luoyanze.mocktest.action.CorrectTestFilePopupAction;
import cn.luoyanze.mocktest.action.GeneratorPopupAction;
import com.google.common.collect.Lists;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.psi.PsiFile;
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @Author luoyanze[luoyanzeze@icloud.com]
 * @Date 2022/8/9 9:28 PM
 */


public class MockTestEditorGroup extends ActionGroup {

    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        System.out.println("------------");
        if (e == null) {
            return new AnAction[0];
        }

        PsiFile data = e.getData(LangDataKeys.PSI_FILE);

        if (data == null || !data.getFileType().getName().equals("JAVA")) {
            return new AnAction[0];
        }

        String folderPath = data.getContainingDirectory().toString().split(":")[1];

        if (folderPath.contains("/src/main")) {
            return new AnAction[] { ActionManager.getInstance().getAction("GeneratorPopupAction") };
            //return new AnAction[] { new GeneratorPopupAction() };
        } else if (folderPath.contains("/src/test")){
            return new AnAction[] { ActionManager.getInstance().getAction("CorrectTestFilePopupAction") };
        } else {
            return new AnAction[0];
        }
    }
}
