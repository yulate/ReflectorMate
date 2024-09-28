package com.yulate.reflectormate.core;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.yulate.reflectormate.core.action.DynamicMenuAction;
import com.yulate.reflectormate.util.MenuConfigLoader;

import java.io.IOException;
import java.util.List;

public class PluginInitializer {

    /**
     * 初始化插件菜单，基于 JSON 配置文件动态生成菜单项和子菜单项。
     *
     * @param jsonConfig JSON 配置文件的路径或内容
     */
    public static void initializeMenu(String jsonConfig) {
        List<MenuConfigLoader.MenuItem> menuItems;
        try {
            // 从 JSON 配置中加载菜单项
            menuItems = MenuConfigLoader.loadMenuItems(jsonConfig);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load menu items from JSON configuration", e);
        }

        // 获取菜单组实例
        DefaultActionGroup actionGroup = (DefaultActionGroup) ActionManager.getInstance().getAction("com.example.ReflectorMate.GenerateReflectionMethods");

        // 遍历并创建菜单项
        for (MenuConfigLoader.MenuItem item : menuItems) {
            actionGroup.add(createActionGroup(item));
        }
    }

    /**
     * 创建菜单组或菜单项，根据是否存在子菜单项递归创建。
     *
     * @param item 菜单项配置
     * @return 创建的 AnAction 对象
     */
    private static AnAction createActionGroup(MenuConfigLoader.MenuItem item) {
        if (item.getSubItems() != null && !item.getSubItems().isEmpty()) {
            // 如果有子菜单项，创建一个新的 ActionGroup
            DefaultActionGroup subGroup = new DefaultActionGroup(item.getName(), true);
            for (MenuConfigLoader.MenuItem subItem : item.getSubItems()) {
                subGroup.add(createActionGroup(subItem));
            }
            return subGroup;
        } else {
            // 否则创建一个普通的 DynamicMenuAction
            return new DynamicMenuAction(item.getName(), item.getCode(), item.getImports(), item.getMavenDependencies());
        }
    }
}
