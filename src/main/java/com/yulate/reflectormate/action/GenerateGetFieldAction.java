package com.yulate.reflectormate.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.yulate.reflectormate.util.ImportUtils;
import com.intellij.psi.codeStyle.CodeStyleManager;

public class GenerateGetFieldAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        Editor editor = event.getData(com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR);
        PsiFile psiFile = event.getData(com.intellij.openapi.actionSystem.CommonDataKeys.PSI_FILE);

        if (editor == null || project == null || psiFile == null || !(psiFile instanceof PsiJavaFile)) return;

        Document document = editor.getDocument();
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);

        // 确保文档操作已经完成并解除阻塞
        psiDocumentManager.doPostponedOperationsAndUnblockDocument(document);
        psiDocumentManager.commitDocument(document);

        // 添加 import 语句
        WriteCommandAction.runWriteCommandAction(project, () -> {
            String[] imports = {
                    "java.lang.reflect.Field",
                    "java.lang.NoSuchFieldException"
            };
            ImportUtils.addImports((PsiJavaFile) psiFile, imports);
        });

        // 插入代码块
        WriteCommandAction.runWriteCommandAction(project, () -> {
            int offset = editor.getCaretModel().getOffset();
            String codeToInsert = getGetFieldMethod();
            document.insertString(offset, codeToInsert);
            psiDocumentManager.commitDocument(document); // 提交文档更改

            // 自动格式化代码
            PsiFile psiFileAfterInsert = psiFile;
            CodeStyleManager.getInstance(project).reformat(psiFileAfterInsert);
        });
    }

    private String getGetFieldMethod() {
        return "public static Field getField(Class<?> clazz, String fieldName) {\n" +
                "    Field field = null;\n\n" +
                "    try {\n" +
                "        field = clazz.getDeclaredField(fieldName);\n" +
                "        field.setAccessible(true);\n" +
                "    } catch (NoSuchFieldException e) {\n" +
                "        if (clazz.getSuperclass() != null) {\n" +
                "            field = getField(clazz.getSuperclass(), fieldName);\n" +
                "        }\n" +
                "    }\n\n" +
                "    return field;\n" +
                "}\n\n";
    }
}
