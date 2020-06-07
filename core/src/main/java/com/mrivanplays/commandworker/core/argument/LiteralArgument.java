package com.mrivanplays.commandworker.core.argument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
  public static LiteralArgument literal(String name) {
    return new LiteralArgument(name);
  }

  private final String name;
  private List<Argument> children;

  private boolean shouldExecuteCommand = false;

  private LiteralArgument(String name) {
    this.name = name;
    this.children = new ArrayList<>();
  }

  @Override
  public String getName() {
    return name;
  }

  /**
   * Adds a child to this argument.
   *
   * @param other child
   * @return this instance for chaining
   */
  public LiteralArgument then(Argument other) {
    children.add(other);
    return this;
  }

  /**
   * @return this instance for chaining
   * @see Argument#shouldExecuteCommand()
   */
  public LiteralArgument markShouldExecuteCommand() {
    this.shouldExecuteCommand = true;
    return this;
  }

  @Override
  public boolean shouldExecuteCommand() {
    return shouldExecuteCommand;
  }

  @Override
  public List<Argument> getChildren() {
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
