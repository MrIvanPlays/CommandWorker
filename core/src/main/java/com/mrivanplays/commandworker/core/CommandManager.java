package com.mrivanplays.commandworker.core;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
  default void register(Command<S> command, String... aliases) {
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
  void register(Command<S> command, Predicate<S> permissionCheck, String... aliases);

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
  List<RegisteredCommand<S>> getRegisteredCommands();

  /**
   * Returns whenever the aliases are free in the specified command list.
   *
   * @param commands commands
   * @param aliases aliases
   * @return <code>true</code> if aliases free, <code>false</code> otherwise
   */
  default boolean aliasesFree(List<RegisteredCommand<S>> commands, String[] aliases) {
    return commands.stream()
        .noneMatch(
            command ->
                Arrays.stream(aliases)
                    .anyMatch(
                        alias ->
                            Arrays.stream(command.getAliases()).anyMatch(alias::equalsIgnoreCase)));
  }

  /**
   * Creates the aliases with the fallback prefix bukkit is creating normally, returns all the
   * aliases with which command x would get registered on bukkit.
   *
   * @param fallbackPrefix prefix
   * @param aliases normally specified aliases
   * @return all the aliases
   */
  static String[] getAliases(String fallbackPrefix, String... aliases) {
    Objects.requireNonNull(fallbackPrefix, "fallbackPrefix");
    Objects.requireNonNull(aliases, "aliases");

    return Arrays.stream(aliases)
        .flatMap(alias -> Stream.of(alias, fallbackPrefix.toLowerCase() + ":" + alias))
        .distinct()
        .toArray(String[]::new);
  }
}
