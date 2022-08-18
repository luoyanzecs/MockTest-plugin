package cn.luoyanze.mocktest.group;

import com.intellij.openapi.actionSystem.*;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @Author luoyanze[luoyanzeze@icloud.com]
 * @Date 2022/8/9 9:28 PM
 */


public class MockTestEditorGroup extends ActionGroup {

    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        if (e == null) {
            return new AnAction[0];
        }
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);

        if (psiFile == null || !psiFile.getFileType().getName().equals("JAVA")) {
            return new AnAction[0];
        }

        // 文件路径
        Path path = Paths.get(psiFile.getOriginalFile().getVirtualFile().getPath());
        if (path.toString().contains(Paths.get("/src/main").toString())) {
            return new AnAction[] { ActionManager.getInstance().getAction("GeneratorPopupAction") };
        } else if (path.toString().contains(Paths.get("/src/test").toString())){
            return new AnAction[] { ActionManager.getInstance().getAction("CorrectTestFilePopupAction") };
        } else {
            return new AnAction[0];
        }
    }
}
