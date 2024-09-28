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

public class GenerateSerializeAction extends AnAction {

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
                    "java.io.ByteArrayOutputStream",
                    "java.io.IOException",
                    "java.io.ObjectOutputStream",
                    "java.util.Base64"
            };
            ImportUtils.addImports((PsiJavaFile) psiFile, imports);

            Document document = editor.getDocument();
            int offset = editor.getCaretModel().getOffset();
            String codeToInsert = getSerializeMethod();
            document.insertString(offset, codeToInsert);

            // 提交文档更改
            psiDocumentManager.commitDocument(document);

            // 自动格式化代码
            CodeStyleManager.getInstance(project).reformatText(psiFile, offset, offset + codeToInsert.length());
        });
    }

    private String getSerializeMethod() {
        return "public static String serialize(Object object) throws IOException {\n" +
                "       ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();\n" +
                "       ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);\n" +
                "       objectOutputStream.writeObject(object);\n" +
                "       return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());\n" +
                "    }\n\n";
    }
}
