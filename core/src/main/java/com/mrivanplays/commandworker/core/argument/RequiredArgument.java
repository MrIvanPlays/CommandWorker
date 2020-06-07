package com.mrivanplays.commandworker.core.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Represents required argument. This is a wrapper for brigadier's RequiredArgumentType, wrapped for
 * easier use.
 *
 * @param <V> type of the argument type.
 */
public final class RequiredArgument<V> implements Argument {

  /**
   * Creates a new {@link RequiredArgument}
   *
   * @param name completion value you see ingame
   * @param type type
   * @param <T> type of the argument type
   * @return required argument
   */
  public static <T> RequiredArgument<T> argument(String name, ArgumentType<T> type) {
    return new RequiredArgument<>(name, type);
  }

  private final String name;
  private final ArgumentType<V> type;
  private List<Argument> children;

  private boolean shouldExecuteCommand = true;

  private Consumer<SuggestionsBuilder> suggestionsConsumer;

  private RequiredArgument(String name, ArgumentType<V> type) {
    this.name = name;
    this.type = type;
    this.children = new ArrayList<>();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> ArgumentType<T> getArgumentType() {
    return (ArgumentType<T>) type;
  }

  /**
   * Adds a child to this required argument.
   *
   * @param child child
   * @return this instance for chaining
   */
  public RequiredArgument<V> then(Argument child) {
    children.add(child);
    return this;
  }

  /**
   * Sets the {@link SuggestionsBuilder} modifications to get applied later.
   *
   * <p><i>We do not suggest using suggestions builder to add your suggestions. Instead, use literal
   * arguments with combination with a required one</i>
   *
   * @param suggestionsConsumer suggestions consumer
   * @return this instance for chaining
   */
  public RequiredArgument<V> suggests(Consumer<SuggestionsBuilder> suggestionsConsumer) {
    this.suggestionsConsumer = suggestionsConsumer;
    return this;
  }

  /**
   * @return this instance for chaining
   * @see Argument#shouldExecuteCommand()
   */
  public RequiredArgument<V> markShouldNotExecuteCommand() {
    this.shouldExecuteCommand = false;
    return this;
  }

  @Override
  public Consumer<SuggestionsBuilder> getSuggestionsConsumer() {
    return suggestionsConsumer;
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
    return "RequiredArgument(name="
        + name
        + ", shouldExecuteCommand="
        + shouldExecuteCommand
        + ", argumentType="
        + type
        + ", children="
        + children
        + ")";
  }
}
