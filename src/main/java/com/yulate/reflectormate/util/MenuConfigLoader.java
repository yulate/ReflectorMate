package com.yulate.reflectormate.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

public class MenuConfigLoader {

    public static List<MenuItem> loadMenuItems(String jsonConfig) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        MenuConfig config = mapper.readValue(jsonConfig, MenuConfig.class);
        return config.getMenuItems();
    }

    public static class MenuConfig {
        private List<MenuItem> menuItems;

        public List<MenuItem> getMenuItems() {
            return menuItems;
        }

        public void setMenuItems(List<MenuItem> menuItems) {
            this.menuItems = menuItems;
        }
    }

    public static class MenuItem {
        private String name;
        private String code;
        private List<String> imports;
        private List<MenuItem> subItems;
        private List<MavenDependency> mavenDependencies;  // 添加 Maven 依赖信息

        // Getters and setters

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public List<String> getImports() {
            return imports;
        }

        public void setImports(List<String> imports) {
            this.imports = imports;
        }

        public List<MenuItem> getSubItems() {
            return subItems;
        }

        public void setSubItems(List<MenuItem> subItems) {
            this.subItems = subItems;
        }

        public List<MavenDependency> getMavenDependencies() {
            return mavenDependencies;
        }

        public void setMavenDependencies(List<MavenDependency> mavenDependencies) {
            this.mavenDependencies = mavenDependencies;
        }
    }

    public static class MavenDependency {
        private String groupId;
        private String artifactId;
        private String version;

        // Getters and setters

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public void setArtifactId(String artifactId) {
            this.artifactId = artifactId;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }
}
