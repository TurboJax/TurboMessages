package org.turbojax.messages;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Collections;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Handles configs and utility methods for TurboMessages.
 * USe this to set up the Message api before you use it.
 */
public class MessageManager {
    private static File langDir = new File("config/turbo-messages/lang");
    private static Locale defaultLocale = Locale.US;

    private MessageManager() {}

    /**
     * Gets the location of the plugin/mod's language folder.
     * For example: `plugins/Infuse/lang` or `config/infuse/lang`
     * Defaults to `config/turbo-messages/lang`.  Make sure to change <i>before</i> loading any language files.
     * @return A {@link File} pointing to the lang folder.
     */
    public static File getLangDir() {
        return langDir;
    }

    /**
     * Changes the directory where language files are found.
     * @param langDir The new directory where language files are found.
     */
    public static void setLangDir(File langDir) {
        MessageManager.langDir = langDir;
    }

    /**
     * Gets the default locale.
     * Defaults to {@link Locale#US}.
     * @return The default {@link Locale}.
     */
    public static Locale getDefaultLocale() {
        return defaultLocale;
    }

    /**
     * Changes the default locale.
     * @param defaultLocale The new default locale.
     */
    public static void setDefaultLocale(Locale defaultLocale) {
        MessageManager.defaultLocale = defaultLocale;
    }

    /** Loads the language configs from `lang/base`. */
    public static void loadBaseFiles() {
        // Checking that the lang/base folder exists
        URI uri;
        try {
            var url = MessageManager.class.getClassLoader().getResource("lang/base");
            if (url == null) {
                Message.LOGGER.error("Failed to locate base localization files at `lang/base`");
                return;
            }
            uri = url.toURI();
        } catch (URISyntaxException e) {
            Message.LOGGER.error("Failed to locate base localization files at `lang/base`", e);
            return;
        }

        // Making sure we're reading a jar
        if (!uri.getScheme().equals("jar")) {
            Message.LOGGER.error("URI not pointing to a jar file.  Cannot load lang configs.");
            return;
        }

        // Extracting the lang files from the jar
        try (FileSystem jarFS = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
            Path langPath = jarFS.getPath("/lang/base");

            Stream<Path> fs = Files.walk(langPath, 1);
            fs.forEach(f -> {
                if (Files.isDirectory(f)) return;

                try {
                    Path dest = langDir.toPath().resolve("base/" + langPath.relativize(f));

                    Files.createDirectories(dest.getParent());
                    Files.copy(f, dest, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    Message.LOGGER.error("Failed to copy lang file", e);
                }
            });

            fs.close();
        } catch (IOException e) {
            Message.LOGGER.error("Failed to open the jar filesystem", e);
        }
    }
}
