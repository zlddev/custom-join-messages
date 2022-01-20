package net.insprill.cjm.handlers;

import lombok.Getter;
import net.insprill.xenlib.files.YamlFile;
import net.insprill.xenlib.logging.Logger;

import java.util.Random;

public class RandomHandler {

    @Getter
    private static final Random random = new Random();

    /**
     * Gets a random config key under the path provided.
     *
     * @param config Config to get the key from.
     * @param path   Parent path of the key to get.
     * @return A random config key under the parent key provided.
     */
    public static String getRandomKey(YamlFile config, String path) {
        if (config.getConfigSection(path) == null) {
            Logger.severe("\"" + path + "\" in messages/" + config.getFile().getName() + " has no keys. Perhaps the messages indentation is wrong?");
            return null;
        }
        int amount = config.getConfigSection(path).getKeys(false).size();
        int selectedMessage = RandomHandler.getRandom().nextInt(amount) + 1;
        return path + "." + selectedMessage;
    }

}
