package com.yulate.reflectormate.util;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;

public class ImportUtils {

    /**
     * 根据psi进行插入import语句，findClass 在有maven操作的时候有可能会出现找不到类的情况
     *
     * @param javaFile
     * @param imports
     */
    public static void addImports(PsiJavaFile javaFile, String[] imports) {
        PsiElementFactory elementFactory = PsiElementFactory.getInstance(javaFile.getProject());
        JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(javaFile.getProject());

        for (String importClass : imports) {
            if (!isAlreadyImported(javaFile, importClass)) {
                PsiClass psiClass = javaPsiFacade.findClass(importClass, javaFile.getResolveScope());
                if (psiClass != null) {
                    PsiImportStatement importStatement = elementFactory.createImportStatement(psiClass);
                    javaFile.getImportList().add(importStatement);
                }
            }
        }
    }

    /**
     * 字符串形式插入 import 信息
     *
     * @param document
     * @param imports
     * @param project
     */
    public static void addImportsByString(Document document, String[] imports, Project project) {
        StringBuilder importText = new StringBuilder();
        for (String importClass : imports) {
            importText.append("import ").append(importClass).append(";\n");
        }

        WriteCommandAction.runWriteCommandAction(project, () -> {
            String currentText = document.getText();
            int insertOffset = 0;

            // 如果有package语句，定位到package语句之后的位置
            if (currentText.startsWith("package")) {
                int packageEnd = currentText.indexOf(";");
                if (packageEnd != -1) {
                    insertOffset = packageEnd + 2; // 插入点应在package语句结束后的下一行
                }
            }

            // 在插入点插入导入语句
            document.insertString(insertOffset, importText.toString());

            // 提交文档更改
            PsiDocumentManager.getInstance(project).commitDocument(document);
        });
    }


    private static boolean isAlreadyImported(PsiJavaFile javaFile, String importClass) {
        PsiImportStatement[] importStatements = javaFile.getImportList().getImportStatements();
        for (PsiImportStatement statement : importStatements) {
            if (statement.getQualifiedName().equals(importClass)) {
                return true;
            }
        }
        return false;
    }
}
