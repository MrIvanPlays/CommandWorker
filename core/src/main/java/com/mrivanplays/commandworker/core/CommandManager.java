package com.mrivanplays.commandworker.core;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a manager of all the registered commands.
 *
 * @param <S> sender type
 */
public interface CommandManager<S> {

  /**
   * Registers the specified command with the identifying aliases. The registration will fallback to
   * bukkit if brigadier isn't present.
   *
   * @param command the command you want to register
   * @param aliases aliases
   */
  default void register(@NotNull Command<S> command, @NotNull String... aliases) {
    register(command, (sender) -> true, aliases);
  }

  /**
   * Registers the specified command with the identifying aliases, giving option to check for
   * permissions. The registration will fallback to bukkit if brigadier isn't present.
   *
   * @param command the command you want to register
   * @param permissionCheck permission check
   * @param aliases aliases
   */
  void register(@NotNull Command<S> command, @Nullable Predicate<S> permissionCheck, @NotNull String... aliases);

  /**
   * Returns whether or not brigadier is supported for this command manager.
   *
   * @return <code>true</code> if brigadier is supported, <code>false</code> otherwise
   */
  boolean isBrigadierSupported();

  /**
   * Returns whether or not this command manager should fallback to the implemented on platform's
   * normal registering technique when brigadier isn't supported.
   *
   * @return <code>true</code> if should fallback, <code>false</code> otherwise
   */
  boolean shouldFallback();

  /**
   * @param shouldFallback value
   * @see #shouldFallback()
   */
  void setShouldFallback(boolean shouldFallback);

  /**
   * Returns unmodifiable copy of the commands, registered and held by this command manager.
   *
   * @return registered commands
   */
  @NotNull
  List<RegisteredCommand<S>> getRegisteredCommands();

  /**
   * Returns whenever the aliases are free in the specified command list.
   *
   * @param commands commands
   * @param aliases aliases
   * @return <code>true</code> if aliases free, <code>false</code> otherwise
   */
  default boolean aliasesFree(@NotNull List<RegisteredCommand<S>> commands, @NotNull String[] aliases) {
    Objects.requireNonNull(commands, "commands");
    Objects.requireNonNull(aliases, "aliases");
    boolean ret = true;
    for (RegisteredCommand<S> command : commands) {
      for (String commandAlias : command.getAliases()) {
        for (String givenAlias : aliases) {
          if (commandAlias.equalsIgnoreCase(givenAlias)) {
            ret = false;
            break;
          }
        }
      }
    }
    return ret;
  }

  /**
   * Creates the aliases with the fallback prefix bukkit is creating normally, returns all the
   * aliases with which command x would get registered on bukkit.
   *
   * @param fallbackPrefix prefix
   * @param aliases normally specified aliases
   * @return all the aliases
   */
  @NotNull
  static String[] getAliases(@NotNull String fallbackPrefix, @NotNull String... aliases) {
    Objects.requireNonNull(fallbackPrefix, "fallbackPrefix");
    Objects.requireNonNull(aliases, "aliases");

    String[] newAliases = new String[aliases.length * 2];
    for (int i = 0, len = aliases.length; i < len; i++) {
      String alias = aliases[i];
      newAliases[i] = alias;
      newAliases[i + aliases.length] = fallbackPrefix.toLowerCase() + ":" + alias;
    }
    return newAliases;
  }
}
