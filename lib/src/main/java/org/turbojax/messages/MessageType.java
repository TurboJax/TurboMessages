package org.turbojax.messages;

import org.jspecify.annotations.NullMarked;

import java.util.Collection;
import java.util.Set;

/**
 * Holds components of a translatable message found in language files.
 *
 * @param key The key the message is stored under.
 * @param placeholders The placeholders the message uses.
 */
@NullMarked
public record MessageType(String key, Set<String> placeholders) {
    /**
     * Creates a MessageType with no placeholders.
     * @param key The key the message is stored under.
     */
    public MessageType(String key) {
        this(key, Set.of());
    }

    /**
     * Creates a MessageType with no placeholders.
     * @param key The key the message is stored under.
     * @param placeholders The placeholders the message uses.
     */
    public MessageType(String key, String... placeholders) {
        this(key, Set.of(placeholders));
    }

    /**
     * Creates a MessageType with no placeholders.
     * @param key The key the message is stored under.
     * @param placeholders The placeholders the message uses.
     */
    public MessageType(String key, Collection<String> placeholders) {
        this(key, Set.copyOf(placeholders));
    }
}
