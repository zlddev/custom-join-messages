package net.insprill.cjm.message.types

import de.leonhard.storage.SimplixBuilder
import de.leonhard.storage.internal.FlatFile
import de.leonhard.storage.internal.settings.ConfigSettings
import de.leonhard.storage.internal.settings.DataType
import net.insprill.cjm.CustomJoinMessages
import net.insprill.cjm.message.MessageVisibility
import org.bukkit.entity.Player
import java.nio.file.Path

abstract class MessageType(plugin: CustomJoinMessages) {

    /**
     * @return The name of the message type.
     */
    abstract val name: String

    /**
     * @return The primary key that all messages are under.
     */
    abstract val key: String

    /**
     * @return The [FlatFile] associated with the message type.
     */
    val config: FlatFile by lazy {
        SimplixBuilder.fromPath(Path.of("${plugin.dataFolder}/messages/$name.yml"))
            .addInputStreamFromResource("messages/$name.yml")
            .setConfigSettings(ConfigSettings.PRESERVE_COMMENTS)
            .setDataType(DataType.SORTED)
            .reloadCallback { plugin.messageSender.reloadPermissions(it) }
            .createYaml()
    }

    /**
     * @return Whether this MessageType is enabled.
     */
    val isEnabled: Boolean
        get() = config.getBoolean("Enabled")

    /**
     * Called when a message should be sent.
     *
     * @param primaryPlayer Player joining/ leaving.
     * @param players       Everyone who should receive the message.
     * @param rootPath      Root path of the chosen message.
     * @param chosenPath    Full path to the chosen message.
     * @param visibility    The visibility of the message.
     */
    abstract fun handle(
        primaryPlayer: Player,
        players: List<Player>,
        rootPath: String?,
        chosenPath: String,
        visibility: MessageVisibility
    )

}
