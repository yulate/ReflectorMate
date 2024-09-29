package com.yulate.reflectormate.core.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.xml.XmlFile;
import com.yulate.reflectormate.util.ImportUtils;
import org.jetbrains.idea.maven.dom.MavenDomUtil;
import org.jetbrains.idea.maven.dom.model.MavenDomDependencies;
import org.jetbrains.idea.maven.dom.model.MavenDomDependency;
import org.jetbrains.idea.maven.dom.model.MavenDomProjectModel;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import com.yulate.reflectormate.util.MenuConfigLoader.MavenDependency;
import com.intellij.codeInsight.actions.OptimizeImportsProcessor;

import java.util.List;

public class DynamicMenuAction extends AnAction {
    private final String code;  // 要插入的代码片段
    private final List<String> imports;  // 需要添加的import语句列表
    private final List<MavenDependency> mavenDependencies;  // 需要添加的Maven依赖列表

    // 构造函数，接受菜单项的名称、要插入的代码、import语句列表和Maven依赖列表
    public DynamicMenuAction(String name, String code, List<String> imports, List<MavenDependency> mavenDependencies) {
        super(name);
        this.code = code;
        this.imports = imports;
        this.mavenDependencies = mavenDependencies;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        Editor editor = e.getData(CommonDataKeys.EDITOR);

        // 确保项目、编辑器和文件是Java文件
        if (project == null || editor == null || !(psiFile instanceof PsiJavaFile)) {
            return;
        }

        PsiJavaFile javaFile = (PsiJavaFile) psiFile;

        // 添加Maven依赖到pom.xml
        addMavenDependencies(project, mavenDependencies, psiFile);

        // 插入import语句和代码片段
        insertImportsAndCode(project, editor, javaFile, imports, code);

        // 格式化代码并优化导入
        ApplicationManager.getApplication().invokeLater(() -> optimizeImportsAndReformatCode(project, javaFile));
    }

    // 向pom.xml文件添加Maven依赖
    private void addMavenDependencies(Project project, List<MavenDependency> dependencies, PsiFile psiFile) {
        if (dependencies == null || dependencies.isEmpty()) {
            return;
        }

        MavenProjectsManager mavenProjectsManager = MavenProjectsManager.getInstance(project);
        List<VirtualFile> pomFiles = mavenProjectsManager.getProjectsFiles();

        if (pomFiles.isEmpty()) {
            throw new RuntimeException("No pom.xml file found in the project");
        }

        VirtualFile pomFile = pomFiles.get(0);
        PsiFile pomPsiFile = PsiManager.getInstance(project).findFile(pomFile);

        if (!(pomPsiFile instanceof XmlFile)) {
            throw new RuntimeException("The pom.xml file could not be found or is not a valid XML file");
        }

        XmlFile xmlFile = (XmlFile) pomPsiFile;
        MavenDomProjectModel model = MavenDomUtil.getMavenDomProjectModel(project, xmlFile.getVirtualFile());

        if (model == null) {
            throw new RuntimeException("Failed to obtain MavenDomProjectModel for the pom.xml file");
        }

        WriteCommandAction.runWriteCommandAction(project, () -> {
            MavenDomDependencies domDependencies = model.getDependencies();
            for (MavenDependency dependency : dependencies) {
                boolean dependencyExists = domDependencies.getDependencies().stream()
                        .anyMatch(existingDependency ->
                                dependency.getGroupId().equals(existingDependency.getGroupId().getStringValue()) &&
                                        dependency.getArtifactId().equals(existingDependency.getArtifactId().getStringValue()) &&
                                        (dependency.getVersion() == null || dependency.getVersion().equals(existingDependency.getVersion().getStringValue()))
                        );

                if (dependencyExists) {
                    continue;
                }

                // 添加Maven依赖
                MavenDomDependency domDependency = domDependencies.addDependency();
                domDependency.getGroupId().setStringValue(dependency.getGroupId());
                domDependency.getArtifactId().setStringValue(dependency.getArtifactId());
                domDependency.getVersion().setStringValue(dependency.getVersion());
            }
        });

        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                System.out.println("进行maven刷新");
                MavenProjectsManager mavenProjectsManagers = MavenProjectsManager.getInstance(project);
                mavenProjectsManagers.forceUpdateAllProjectsOrFindAllAvailablePomFiles();
            } catch (Exception ex) {
                throw new RuntimeException("Failed to resolve Maven folders", ex);
            }
        });
    }


    // 插入import语句和代码片段到Java文件中
    private void insertImportsAndCode(Project project, Editor editor, PsiJavaFile javaFile, List<String> imports, String code) {
        if (imports == null || code == null) {
            return;
        }

        // Step 1: 插入代码
        WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
            Document document = editor.getDocument();

            documentManager.doPostponedOperationsAndUnblockDocument(document); // 确保之前的操作完成

            // 插入代码
            document.insertString(editor.getCaretModel().getOffset(), code);
            documentManager.commitDocument(document); // 提交文档更改

            documentManager.doPostponedOperationsAndUnblockDocument(document); // 确保插入代码后操作完成
        });

        // Step 2: 插入import语句
        WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
            Document document = editor.getDocument();

            documentManager.doPostponedOperationsAndUnblockDocument(document); // 确保之前的操作完成

            // 插入import语句
            ImportUtils.addImportsByString(document, imports.toArray(new String[0]), project);
            documentManager.commitDocument(document); // 提交文档更改

            documentManager.doPostponedOperationsAndUnblockDocument(document); // 确保插入import后操作完成
        });
    }

    // 优化imports并格式化代码
    private void optimizeImportsAndReformatCode(Project project, PsiJavaFile javaFile) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            // 优化 imports
            new OptimizeImportsProcessor(project, javaFile).run();

            // 格式化代码
            CodeStyleManager.getInstance(project).reformat(javaFile);
        });
    }

}
