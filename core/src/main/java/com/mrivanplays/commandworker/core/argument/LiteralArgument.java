package com.mrivanplays.commandworker.core.argument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Represents literal argument. This is a wrapper for brigadier's LiteralArgumentBuilder, wrapped
 * for easier use.
 */
public final class LiteralArgument implements Argument {

  /**
   * Creates a new {@link LiteralArgument}
   *
   * @param name completion value you see ingame.
   * @return literal argument
   */
  @NotNull
  public static LiteralArgument literal(@NotNull String name) {
    return new LiteralArgument(name);
  }

  private final String name;
  private List<Argument> children;

  private boolean shouldExecuteCommand = false;

  private LiteralArgument(@NotNull String name) {
    this.name = Objects.requireNonNull(name, "name");
    this.children = new ArrayList<>();
  }

  @Override
  public @NotNull String getName() {
    return name;
  }

  /**
   * Adds a child to this argument.
   *
   * @param other child
   * @return this instance for chaining
   */
  @NotNull
  public LiteralArgument then(@NotNull Argument other) {
    Objects.requireNonNull(other, "other");
    children.add(other);
    return this;
  }

  @NotNull
  public LiteralArgument then(@NotNull Argument... other) {
    Objects.requireNonNull(other, "other");
    // We're not using Collection#addAll because this way we save memory
    //noinspection ManualArrayToCollectionCopy
    for (Argument arg : other) {
      //noinspection UseBulkOperation
      children.add(arg);
    }
    return this;
  }

  @NotNull
  public LiteralArgument then(@NotNull Iterable<Argument> iterable) {
    Objects.requireNonNull(iterable, "iterable");
    for (Argument arg : iterable) {
      children.add(arg);
    }
    return this;
  }

  /**
   * @return this instance for chaining
   * @see Argument#shouldExecuteCommand()
   */
  @NotNull
  public LiteralArgument markShouldExecuteCommand() {
    this.shouldExecuteCommand = true;
    return this;
  }

  @Override
  public boolean shouldExecuteCommand() {
    return shouldExecuteCommand;
  }

  @Override
  public @NotNull List<Argument> getChildren() {
    return Collections.unmodifiableList(children);
  }

  @Override
  public String toString() {
    return "LiteralArgument(name="
        + name
        + ", shouldExecuteCommand="
        + shouldExecuteCommand
        + ", children="
        + children
        + ")";
  }
}
