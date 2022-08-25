package net.insprill.cjm

import co.aikar.commands.PaperCommandManager
import net.insprill.cjm.command.CjmCommand
import net.insprill.cjm.command.CommandCompletion
import net.insprill.cjm.command.CommandContext
import net.insprill.cjm.compatibility.Dependency
import net.insprill.cjm.compatibility.hook.HookManager
import net.insprill.cjm.compatibility.hook.PluginHook
import net.insprill.cjm.listener.JoinEvent
import net.insprill.cjm.listener.QuitEvent
import net.insprill.cjm.listener.WorldChangeEvent
import net.insprill.cjm.message.MessageSender
import net.insprill.cjm.message.types.ActionbarMessage
import net.insprill.cjm.message.types.ChatMessage
import net.insprill.cjm.message.types.SoundMessage
import net.insprill.cjm.message.types.TitleMessage
import net.insprill.xenlib.XenLib
import net.insprill.xenlib.files.YamlFile
import org.bstats.bukkit.Metrics
import org.bstats.charts.SimplePie
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class CustomJoinMessages : JavaPlugin() {

    lateinit var messageSender: MessageSender
    lateinit var hookManager: HookManager

    override fun onEnable() {
        val metrics = Metrics(this, 6346)
        metrics.addCustomChart(SimplePie("worldBasedMessages") {
            YamlFile.CONFIG.getBoolean("World-Based-Messages.Enabled").toString()
        })

        XenLib.init(this)

        val pluginHooks = getPluginHooks()
        hookManager = HookManager(pluginHooks)

        registerListeners()

        val messageTypes = listOf(
            ActionbarMessage(this),
            ChatMessage(),
            SoundMessage(this),
            TitleMessage()
        )

        for (msg in messageTypes) {
            metrics.addCustomChart(SimplePie("message_type_" + msg.name) { msg.isEnabled.toString() })
        }

        messageSender = MessageSender(this, messageTypes)
        messageSender.setupPermissions()

        // Commands
        val manager = PaperCommandManager(this)
        @Suppress("DEPRECATION")
        manager.enableUnstableAPI("help")
        CommandContext(this).register(manager)
        CommandCompletion(this).register(manager)
        manager.registerCommand(CjmCommand(this))
    }

    private fun getPluginHooks(): List<PluginHook> {
        val hooks = ArrayList<PluginHook>()
        for (dependency in Dependency.values()) {
            if (!dependency.isEnabled)
                continue
            val hook = dependency.pluginHookClass?.getConstructor(javaClass)?.newInstance(this) ?: continue
            hooks.add(hook)
        }
        return Collections.unmodifiableList(hooks)
    }

    private fun registerListeners() {
        var registeredAuthListener = false
        for (listener in hookManager.authHooks) {
            if (!YamlFile.CONFIG.getBoolean("Addons.Auth.Wait-For-Login"))
                continue
            Bukkit.getPluginManager().registerEvents(listener, this)
            registeredAuthListener = true
        }
        if (!registeredAuthListener) {
            Bukkit.getPluginManager().registerEvents(JoinEvent(this), this)
        }

        Bukkit.getPluginManager().registerEvents(QuitEvent(this), this)
        Bukkit.getPluginManager().registerEvents(WorldChangeEvent(this), this)

        for (listener in hookManager.vanishHooks.filterIsInstance<Listener>()) {
            if (!YamlFile.CONFIG.getBoolean("Addons.Vanish.Fake-Messages.Enabled"))
                continue
            Bukkit.getPluginManager().registerEvents(listener, this)
        }
    }

}
