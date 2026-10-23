package org.turbojax.messages;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * Represents a message that can be sent to players.
 * It can understand minimessage formatting and has some basic placeholder support.
 */
@NullMarked
public class Message implements ComponentLike {
    /** The minimessage parser.  Public for others to use. */
    public static final MiniMessage mm = MiniMessage.miniMessage();

    /** The internal Logger for TurboMessages. */
    @ApiStatus.Internal
    public static final Logger LOGGER = LoggerFactory.getLogger("TurboMessages");

    /**
     * Creates a new Message from a minimessage string.
     *
     * @param message A minimessage string.
     * @return The new {@link Message}
     */
    public static Message text(String message) {
        return new Message(message, Set.of());
    }

    /**
     * Creates a new message from a minimessage string.
     *
     * @param message A minimessage string.
     * @param placeholders Placeholders recognized by the message.
     * @return The new {@link Message}
     */
    public static Message text(String message, Set<String> placeholders) {
        return new Message(message, placeholders);
    }

    /**
     * Creates a new Message from a translatable message type.<br>
     * Translates to the default locale.
     *
     * @param key The key of a translatable message.
     * @return The new {@link Message}.  If no message was found for the given type in the default locale, it returns an empty message.
     */
    public static Message translatable(String key) {
        return translatable(new MessageType(key));
    }

    /**
     * Creates a new Message from a translatable message type.
     *
     * @param key The key of a translatable message.
     * @param locale The locale to use for the message.
     * @return The new {@link Message}.  If no message was found for the given type and locale, it returns an empty message.
     */
    public static Message translatable(String key, Locale locale) {
        return translatable(new MessageType(key), locale);
    }

    /**
     * Creates a new Message from a translatable message type.
     *
     * @param key The key of a translatable message.
     * @param locale The locale to use for the message.
     * @param placeholders The placeholders the message uses.
     * @return The new {@link Message}.  If no message was found for the given type and locale, it returns an empty message.
     */
    public static Message translatable(String key, Locale locale, Set<String> placeholders) {
        return translatable(new MessageType(key, placeholders), locale);
    }

    /**
     * Creates a new Message from a translatable message type.<br>
     * Translates to the default locale.
     *
     * @param type The message type.
     * @return The new {@link Message}.  If no message was found for the given type in the default locale, it returns an empty message.
     */
    public static Message translatable(MessageType type) {
        return translatable(type, MessageManager.getDefaultLocale());
    }

    /**
     * Creates a new Message from a translatable message type.
     *
     * @param type The message type.
     * @param locale The locale to use for the message.
     * @return The new {@link Message}.  If no message was found for the given type and locale, it returns an empty message.
     */
    public static Message translatable(MessageType type, Locale locale) {
        File baseFile = new File(MessageManager.getLangDir(), "base/" + locale + ".yml");
        File overrideFile = new File(MessageManager.getLangDir(), locale + ".yml");

        String msg = null;

        // Searching the overrides file if it exists
        if (overrideFile.exists()) {
            msg = readMsgFromLocaleFile(type.key(), locale, overrideFile);
        }

        // If the msg was not overridden, get the default value
        if (msg == null) {
            msg = readMsgFromLocaleFile(type.key(), locale, baseFile);
        }

        // If the msg was still not found, check the default locale or return an empty message
        if (msg == null) {
            LOGGER.error("Could not translate the key '{}' in the '{}' locale.", type.key(), locale);
            if (locale.equals(MessageManager.getDefaultLocale())) return new Message("");
            LOGGER.error("Falling back to the default locale.");
            return translatable(type, MessageManager.getDefaultLocale());
        }

        return new Message(msg, type.placeholders());
    }

    /**
     * Reads a message from a locale file.
     *
     * @param key    The key the message is stored under.
     * @param locale The locale the message is stored in.
     * @param file   The file the message is stored in.
     * @return The message string, or null if it could not be found.
     */
    @Nullable
    private static String readMsgFromLocaleFile(String key, Locale locale, File file) {
        Yaml yaml = new Yaml();
        try (FileReader reader = new FileReader(file)) {
            // Loading the yaml file into an object
            Object msg = yaml.load(reader);
            String[] parts = key.split("\\.");

            // Iterating through the key to find the message
            for (String part : parts) {
                try {
                    // Kiiiinda messy but it is all handled fine.
                    // noinspection unchecked
                    msg = ((Map<String, Object>) msg).get(part);
                } catch (ClassCastException | NullPointerException e) {
                    return null;
                }
            }

            // Returning the resulting message
            if (msg instanceof String) {
                return (String) msg;
            } else if (msg instanceof List<?> list) {
                return String.join("\n", list.stream().map(String::valueOf).toList());
            }

            return null;
        } catch (FileNotFoundException e) {
            LOGGER.error("Could not find base language file for locale {}", locale, e);
            return null;
        } catch (IOException e) {
            LOGGER.error("Failed to read base language file for locale {}", locale, e);
            return null;
        }
    }

    // Message instance data
    private String message;
    private final Set<String> placeholders;

    private Message(String message) {
        this.message = message;
        this.placeholders = Set.of();
    }

    private Message(String message, Set<String> placeholders) {
        this.message = message;
        this.placeholders = new HashSet<>(Set.copyOf(placeholders));
    }

    /**
     * Applies a placeholder to the message.
     *
     * @param placeholder The placeholder to set.
     * @param value The {@link Message} value to apply
     */
    public void applyPlaceholder(String placeholder, Message value) {
        applyPlaceholder(placeholder, value.toString());
    }

    /**
     * Applies a placeholder to the message.
     *
     * @param placeholder The placeholder to set.
     * @param value The {@link Component} value to apply
     */
    public void applyPlaceholder(String placeholder, Component value) {
        applyPlaceholder(placeholder, mm.serialize(value));
    }

    /**
     * Applies a placeholder to the message.
     *
     * @param placeholder The placeholder to set.
     * @param value The value to apply.
     */
    public void applyPlaceholder(String placeholder, Object value) {
        placeholders.remove(placeholder);

        this.message = message.replace("%" + placeholder + "%", String.valueOf(value));
    }

    /**
     * Converts the message to a list of components, split by newlines.
     *
     * @return The list of components.
     * @throws IllegalStateException If not all placeholders have been registered.
     */
    public List<Component> toComponentList() {
        if (!placeholders.isEmpty()) {
            throw new IllegalStateException("Not all placeholders have been registered.");
        }

        return Stream.of(message.split("\n"))
                .map(e -> "<i:false>" + e)
                .map(mm::deserialize).toList();
    }

    /**
     * Converts the message to a single component.
     *
     * @return The {@link Component}.
     * @throws IllegalStateException If not all placeholders have been registered.
     */
    public Component asComponent() {
        if (!placeholders.isEmpty()) {
            throw new IllegalStateException("Not all placeholders have been registered.");
        }

        return mm.deserialize("<i:false>" + message);
    }

    /**
     * Gets the string representation of the message.
     *
     * @return The message.
     * @throws IllegalStateException If not all placeholders have been registered.
     */
    public String toString() {
        if (!placeholders.isEmpty()) {
            throw new IllegalStateException("Not all placeholders have been registered.");
        }

        return message;
    }

    /**
     * Gets the plain string representation of the message.
     * Strips the message of formatting tags and returns it.
     *
     * @return The plain message.
     * @throws IllegalStateException If not all placeholders have been registered.
     */
    public String toPlainString() {
        if (!placeholders.isEmpty()) {
            throw new IllegalStateException("Not all placeholders have been registered.");
        }

        return mm.stripTags(message);
    }

    /**
     * Converts the message to a list of string, split by newlines.
     *
     * @return The list of strings.
     * @throws IllegalStateException If not all placeholders have been registered.
     */
    public List<String> toStringList() {
        if (!placeholders.isEmpty()) {
            throw new IllegalStateException("Not all placeholders have been registered.");
        }

        return List.of(mm.stripTags(message).split("\n"));
    }
}