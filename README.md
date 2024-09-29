# ReflectorMate

ReflectorMate 是一款方便快捷的 idea 插件，快速生成反序列化中常用的方法，比如setFieldValue、createTemplatesImpl等

### 功能演示：
- **自动生成反射方法：** 快速生成用于设置和获取字段值的反射方法，减少手动编码的工作量。

![功能演示](/img/功能演示.gif "功能演示")

### 添加新的选项
ReflectorMate 支持通过json动态的添加可用的选项，只需要在json中进行配置即可轻松快速的添加。如果您添加了新的菜单可以向本项目提出pr或者issues进行贡献，感谢您的帮助。

每个菜单项包括代码片段、导入语句和Maven依赖。主要结构如下：

- menuItems: 包含一个或多个菜单项，每个项都可以包含子项。 
- name: 菜单项的名称，显示在菜单中。 
- code: 要插入的Java代码片段。 
- imports: 代码片段所需的Java导入语句列表。 
- mavenDependencies: Maven依赖列表，如果代码需要外部库支持，可以在这里添加。

演示添加新的菜单

1. 添加一级菜单
```json
{
  "menuItems": [
    {
      "name": "Sample Menu 1",
      "code": "System.out.println(\"Hello World!\");",
      "imports": [
        "java.lang.System"
      ],
      "mavenDependencies": []
    }
  ]
}
```
menuItems中添加了一个名为"Sample Menu 1"的菜单项，它将在菜单中显示为一级菜单。
2. 添加二级菜单
```json
{
  "menuItems": [
    {
      "name": "Parent Menu",
      "subItems": [
        {
          "name": "Sub Menu 1",
          "code": "System.out.println(\"Hello from Sub Menu 1!\");",
          "imports": [
            "java.lang.System"
          ],
          "mavenDependencies": []
        }
      ]
    }
  ]
}
```
Parent Menu是一个一级菜单，subItems包含一个二级菜单Sub Menu 1。
3. 添加三级菜单
```json
{
  "menuItems": [
    {
      "name": "Parent Menu",
      "subItems": [
        {
          "name": "Sub Menu 1",
          "subItems": [
            {
              "name": "Sub-Sub Menu 1",
              "code": "System.out.println(\"Hello from Sub-Sub Menu 1!\");",
              "imports": [
                "java.lang.System"
              ],
              "mavenDependencies": []
            }
          ]
        }
      ]
    }
  ]
}
```
Parent Menu是一级菜单，Sub Menu 1是二级菜单，Sub-Sub Menu 1是三级菜单。

总览:

- 每个menuItems对象表示一级菜单。
- 每个subItems表示该菜单项的子菜单，可以嵌套形成多级菜单。
- name定义菜单项名称，code是要插入的Java代码，imports是所需的Java导入包，mavenDependencies是所需的Maven依赖。