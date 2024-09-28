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
import com.intellij.psi.xml.XmlFile;
import com.yulate.reflectormate.util.ImportUtils;
import org.jetbrains.idea.maven.buildtool.MavenImportSpec;
import org.jetbrains.idea.maven.dom.MavenDomUtil;
import org.jetbrains.idea.maven.dom.model.MavenDomDependencies;
import org.jetbrains.idea.maven.dom.model.MavenDomDependency;
import org.jetbrains.idea.maven.dom.model.MavenDomProjectModel;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import com.yulate.reflectormate.util.MenuConfigLoader.MavenDependency;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.codeInsight.actions.OptimizeImportsProcessor;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class DynamicMenuAction extends AnAction {
    private final String code;
    private final List<String> imports;
    private final List<MavenDependency> mavenDependencies;

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

        if (project == null || psiFile == null || editor == null || !(psiFile instanceof PsiJavaFile)) {
            return;
        }

        PsiJavaFile javaFile = (PsiJavaFile) psiFile;

        // 添加 Maven 依赖到 pom.xml
        addMavenDependencies(project, mavenDependencies, psiFile);

        // 插入导入语句和代码
        insertImportsAndCode(project, editor, javaFile, imports, code);

        // 格式化代码并优化导入
        ApplicationManager.getApplication().invokeLater(() -> optimizeImportsAndReformatCode(project, javaFile));
    }

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

                MavenDomDependency domDependency = domDependencies.addDependency();
                domDependency.getGroupId().setStringValue(dependency.getGroupId());
                domDependency.getArtifactId().setStringValue(dependency.getArtifactId());
                domDependency.getVersion().setStringValue(dependency.getVersion());
            }
        });

        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                Method method = MavenProjectsManager.class.getDeclaredMethod("doScheduleUpdateProjects", Collection.class, MavenImportSpec.class);
                method.setAccessible(true);
                MavenImportSpec importSpec = new MavenImportSpec(true, true, true);
                method.invoke(mavenProjectsManager, Collections.emptyList(), importSpec);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to invoke doScheduleUpdateProjects method via reflection", ex);
            }
        });
    }

    private void insertImportsAndCode(Project project, Editor editor, PsiJavaFile javaFile, List<String> imports, String code) {
        if (imports == null || code == null) {
            return;
        }

        // Step 1: 解锁文档，确保之前的操作已完成
        PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
        Document document = editor.getDocument();
        documentManager.doPostponedOperationsAndUnblockDocument(document);

        // Step 2: 使用 ImportUtils 插入 import 语句
        WriteCommandAction.runWriteCommandAction(project, () -> {
            ImportUtils.addImports(javaFile, imports.toArray(new String[0]));

            // 提交文档更改
            documentManager.commitDocument(document);
        });

        // Step 3: 插入代码（使用 invokeLater 延迟执行）
        ApplicationManager.getApplication().invokeLater(() -> {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                int offset = editor.getCaretModel().getOffset();
                document.insertString(offset, code);
                documentManager.commitDocument(document);
            });

            // Step 4: 优化导入并格式化代码
            optimizeImportsAndReformatCode(project, javaFile);
        });
    }


    private void optimizeImportsAndReformatCode(Project project, PsiJavaFile javaFile) {
        CommandProcessor.getInstance().runUndoTransparentAction(() -> {
            new OptimizeImportsProcessor(project, javaFile).run();
        });
    }
}
