package com.mrivanplays.commandworker.core;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a command which is registered.
 *
 * @param <S> sender type
 */
public final class RegisteredCommand<S> {

  private final String[] aliases;
  private final Command<S> command;

  private final Predicate<S> permissionChecker;

  private final LiteralNode commandStructure;

  public RegisteredCommand(String[] aliases, Command<S> command, Predicate<S> permissionChecker) {
    this.aliases = aliases;
    this.command = command;
    this.permissionChecker = permissionChecker;
    this.commandStructure =
        Objects.requireNonNull(
            command.createCommandStructure(), "command structure null for " + aliases[0]);
  }

  /**
   * Returns whenever the specified sender has permission to execute this command.
   *
   * @param sender sender
   * @return <code>true</code> if has, <code>false</code> otherwise
   */
  public boolean hasPermission(@NotNull S sender) {
    Objects.requireNonNull(sender, "sender");
    return permissionChecker.test(sender);
  }

  /**
   * Returns the permission check {@link Predicate}.
   *
   * @return permission checker
   */
  @NotNull
  public Predicate<S> getPermissionCheckFunction() {
    return permissionChecker;
  }

  /**
   * Returns the cached {@link LiteralNode} of the represented command, which is representing the
   * command structure.
   *
   * @return command structure node
   */
  @NotNull
  public LiteralNode getCommandStructure() {
    return commandStructure;
  }

  /**
   * Returns the aliases (names) of the registered command.
   *
   * @return aliases
   */
  @NotNull
  public String[] getAliases() {
    return aliases;
  }

  /**
   * Returns the {@link Command}, held by this RegisteredCommand
   *
   * @return command
   */
  @NotNull
  public Command<S> getCommand() {
    return command;
  }

  @Override
  public String toString() {
    return "RegisteredCommand(command="
        + command
        + ", aliases="
        + Arrays.deepToString(aliases)
        + ", permissionCheckFunction="
        + permissionChecker
        + ", commandStructure="
        + commandStructure
        + ")";
  }
}
