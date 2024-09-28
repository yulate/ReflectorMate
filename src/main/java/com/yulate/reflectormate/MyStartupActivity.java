package com.yulate.reflectormate;

import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.project.Project;
import com.yulate.reflectormate.core.PluginInitializer;
import com.yulate.reflectormate.core.config.ConfigLoader;
import org.jetbrains.annotations.NotNull;

public class MyStartupActivity implements StartupActivity.DumbAware {

    @Override
    public void runActivity(@NotNull Project project) {
        String jsonConfig = ConfigLoader.loadJsonConfig();
        if (jsonConfig != null) {
            PluginInitializer.initializeMenu(jsonConfig);
        } else {
            System.err.println("Failed to load JSON configuration.");
        }
    }
}
