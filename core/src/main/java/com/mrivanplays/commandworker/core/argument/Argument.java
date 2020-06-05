package com.mrivanplays.commandworker.core.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import java.util.function.Consumer;

/**
 * Represents wrapped argument, which would get wrapped in a brigadier one later when command is
 * being registered.
 */
public interface Argument {

  /**
   * Returns the name of the argument. Usually the completion you see if the argument is literal
   *
   * @return name
   */
  String getName();

  /**
   * Returns the brigadier argument type, if the argument is required.
   *
   * <p>A literal argument does not have a dedicated argument type, and this will return null if the
   * argument is literal.
   *
   * @param <T> the type held by the argument type.
   * @return argument type
   */
  default <T> ArgumentType<T> getArgumentType() {
    return null;
  }

  /**
   * Returns whether or not this argument is literal.
   *
   * @return <code>true</code> if literal, <code>false</code> otherwise
   */
  default boolean isLiteral() {
    return getArgumentType() == null;
  }

  /**
   * Returns the {@link SuggestionsBuilder} consumer, which is able to modify the suggestions for
   * this argument. By default, this returns null.
   *
   * <p><i>We do not suggest using suggestions builder to add your suggestions. Instead, use literal
   * arguments with combination with a required one</i>
   *
   * @return suggestions consumer
   */
  default Consumer<SuggestionsBuilder> getSuggestionsConsumer() {
    return null;
  }

  /**
   * Returns whether or not this argument should call the base command's execute method when the
   * argument ends up being the last typed argument. This marking is being ignored if brigadier
   * isn't supported for the minecraft version your plugin is being ran on.
   *
   * @return <code>true</code> if should have execute, <code>false</code> otherwise
   */
  boolean shouldExecuteCommand();

  /**
   * Returns unmodifiable copy of the children this argument is holding.
   *
   * @return children
   */
  List<Argument> getChildren();
}
