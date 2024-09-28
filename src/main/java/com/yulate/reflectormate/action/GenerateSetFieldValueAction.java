package com.yulate.reflectormate.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.yulate.reflectormate.util.ImportUtils;

public class GenerateSetFieldValueAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        Editor editor = event.getData(com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR);
        PsiFile psiFile = event.getData(com.intellij.openapi.actionSystem.CommonDataKeys.PSI_FILE);

        if (editor == null || project == null || psiFile == null || !(psiFile instanceof PsiJavaFile)) return;

        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);

        WriteCommandAction.runWriteCommandAction(project, () -> {
            // 添加 import 语句
            String[] imports = {
                    "java.lang.reflect.Field",
                    "java.lang.NoSuchFieldException",
                    "java.lang.IllegalAccessException"
            };
            ImportUtils.addImports((PsiJavaFile) psiFile, imports);

            Document document = editor.getDocument();
            int offset = editor.getCaretModel().getOffset();
            String codeToInsert = getSetFieldValueMethod();
            document.insertString(offset, codeToInsert);

            // 提交文档更改
            psiDocumentManager.commitDocument(document);

            // 自动格式化代码
            CodeStyleManager.getInstance(project).reformatText(psiFile, offset, offset + codeToInsert.length());
        });
    }

    private String getSetFieldValueMethod() {
        return "public static void setFieldValue(Object object, String field, Object arg) throws NoSuchFieldException, IllegalAccessException {\n" +
                "       Field f = object.getClass().getDeclaredField(field);\n" +
                "       f.setAccessible(true);\n" +
                "       f.set(object, arg);\n" +
                "    }\n\n";
    }
}
