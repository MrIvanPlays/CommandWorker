package com.mrivanplays.commandworker.core;

import java.util.function.Predicate;

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
    this.commandStructure = command.createCommandStructure();
  }

  /**
   * Returns whenever the specified sender has permission to execute this command.
   *
   * @param sender sender
   * @return <code>true</code> if has, <code>false</code> otherwise
   */
  public boolean hasPermission(S sender) {
    return permissionChecker.test(sender);
  }

  /**
   * Returns the permission check {@link Predicate}.
   *
   * @return permission checker
   */
  public Predicate<S> getPermissionCheckFunction() {
    return permissionChecker;
  }

  /**
   * Returns the cached {@link LiteralNode} of the represented command, which is representing the
   * command structure.
   *
   * @return command structure node
   */
  public LiteralNode getCommandStructure() {
    return commandStructure;
  }

  /**
   * Returns the aliases (names) of the registered command.
   *
   * @return aliases
   */
  public String[] getAliases() {
    return aliases;
  }

  /**
   * Returns the {@link Command}, held by this RegisteredCommand
   *
   * @return command
   */
  public Command<S> getCommand() {
    return command;
  }
}
