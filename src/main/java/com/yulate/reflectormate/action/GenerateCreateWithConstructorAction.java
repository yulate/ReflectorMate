package com.yulate.reflectormate.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.yulate.reflectormate.util.ImportUtils;

public class GenerateCreateWithConstructorAction extends AnAction {

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
                    "java.lang.reflect.Constructor",
                    "java.lang.reflect.InvocationTargetException",
                    "sun.reflect.ReflectionFactory"
            };
            ImportUtils.addImports((PsiJavaFile) psiFile, imports);
        });

        // 插入代码块并提交文档
        WriteCommandAction.runWriteCommandAction(project, () -> {
            int offset = editor.getCaretModel().getOffset();
            String codeToInsert = getCreateWithConstructorMethod();
            document.insertString(offset, codeToInsert);
            psiDocumentManager.commitDocument(document); // 提交文档更改

            // 自动格式化代码
            PsiFile psiFileAfterInsert = psiFile;
            psiDocumentManager.doPostponedOperationsAndUnblockDocument(document); // 确保文档解除阻塞
            CodeStyleManager.getInstance(project).reformat(psiFileAfterInsert);
        });
    }

    private String getCreateWithConstructorMethod() {
        return "public static <T> T createWithConstructor(Class<T> classToInstantiate,\n" +
                "                                              Class<?> constructorDeclaringClass,\n" +
                "                                              Class<?>[] constructorParameterTypes,\n" +
                "                                              Object[] constructorArguments)\n" +
                "        throws NoSuchMethodException,\n" +
                "        InstantiationException,\n" +
                "        IllegalAccessException,\n" +
                "        InvocationTargetException {\n\n" +
                "    Constructor<?> declaredConstructor = constructorDeclaringClass.getDeclaredConstructor(constructorParameterTypes);\n" +
                "    declaredConstructor.setAccessible(true);\n\n" +
                "    Constructor<T> specializedConstructor =\n" +
                "            (Constructor<T>) ReflectionFactory.getReflectionFactory()\n" +
                "                    .newConstructorForSerialization(classToInstantiate, declaredConstructor);\n" +
                "    specializedConstructor.setAccessible(true);\n\n" +
                "    return specializedConstructor.newInstance(constructorArguments);\n" +
                "}\n\n";
    }
}
