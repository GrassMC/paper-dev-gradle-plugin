import io.github.grassmc.paperdev.dsl.Dependency
import io.github.grassmc.paperdev.dsl.PaperPluginYml
import io.github.grassmc.paperdev.dsl.PermissionDefault

plugins {
    id("io.github.grassmc.paper-dev")
}

pluginYml {
    name = "with-configuration"
    description = "A plugin that demonstrates the paper-dev plugin"
    authors = listOf("GrassMC")
    contributors = listOf("TozyDev")
    apiVersion = PaperPluginYml.ApiVersion.V1_20
    load = PaperPluginYml.PluginLoadOrder.STARTUP
    permissions {
        create("with-configuration.command") {
            description = "Allows access to the /with-configuration command"
            default = PermissionDefault.OP
            children = mapOf("with-configuration.command.child1" to true, "with-configuration.command.child2" to false)
        }
        create("with-configuration.event")
    }
    dependencies {
        server {
            create("Plugin1") {
                load = Dependency.LoadOrder.BEFORE
                required = true
            }
            create("Plugin2")
        }
    }
}
