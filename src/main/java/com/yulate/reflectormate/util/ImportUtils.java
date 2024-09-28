package com.yulate.reflectormate.util;

import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiImportStatement;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;

public class ImportUtils {

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
