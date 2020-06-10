package com.mrivanplays.commandworker.core.argument.parser;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mrivanplays.commandworker.core.LiteralNode;
import com.mrivanplays.commandworker.core.argument.Argument;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/** Represents a holder of arguments. */
public final class ArgumentHolder {

  private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER = new HashMap<>();

  static {
    PRIMITIVE_TO_WRAPPER.put(boolean.class, Boolean.class);
    PRIMITIVE_TO_WRAPPER.put(byte.class, Byte.class);
    PRIMITIVE_TO_WRAPPER.put(short.class, Short.class);
    PRIMITIVE_TO_WRAPPER.put(char.class, Character.class);
    PRIMITIVE_TO_WRAPPER.put(int.class, Integer.class);
    PRIMITIVE_TO_WRAPPER.put(long.class, Long.class);
    PRIMITIVE_TO_WRAPPER.put(float.class, Float.class);
    PRIMITIVE_TO_WRAPPER.put(double.class, Double.class);
  }

  private final String input;
  private StringReader reader;
  private Map<String, ArgumentData> argumentDataHolder;

  public ArgumentHolder(String input, LiteralNode commandStructure) {
    this.input = input;
    this.reader = new StringReader(input);
    reader.setCursor(0);
    this.argumentDataHolder = new HashMap<>();

    this.handleArguments(commandStructure.getArguments());
  }

  private void handleArguments(List<Argument> arguments) {
    if (arguments.isEmpty()) {
      return;
    }
    Map<Argument, Integer> newArgs = getRequiredArgs(arguments, -1);
    if (input.isEmpty()) {
      newArgs.forEach(
          (argument, pos) ->
              argumentDataHolder.put(
                  argument.getName(),
                  ArgumentData.newArgumentData(null, argument, null, null, null)));
      return;
    }
    Set<Entry<Argument, Integer>> entrySet =
        newArgs.entrySet().stream()
            .sorted(Entry.comparingByValue())
            .collect(Collectors.toCollection(LinkedHashSet::new));
    int index = 0;
    for (Map.Entry<Argument, Integer> entry : entrySet) {
      Argument argument = entry.getKey();
      int argumentIndex = entry.getValue();
      reader.skipWhitespace();
      int start = reader.getCursor();
      if (argument.getArgumentType() == null) {
        // we're at minecraft argument type, and brigadier is not supported.
        if (index == (entrySet.size() - 1)) { // last argument
          String raw = reader.getRemaining();
          int splitIndex = raw.split(" ").length - 1;
          IndexRange range;
          if (argumentIndex == splitIndex) {
            range = new IndexRange(argumentIndex);
          } else {
            range = new IndexRange(argumentIndex, argumentIndex + splitIndex);
          }
          argumentDataHolder.put(
              argument.getName(), ArgumentData.newArgumentData(range, argument, null, raw, null));
        } else {
          String raw = reader.readUnquotedString();
          reader.skipWhitespace();
          IndexRange range = new IndexRange(argumentIndex);
          argumentDataHolder.put(
              argument.getName(), ArgumentData.newArgumentData(range, argument, null, raw, null));
        }
        continue;
      }

      Object parsed;
      try {
        parsed = argument.getArgumentType().parse(reader);
      } catch (CommandSyntaxException e) {
        argumentDataHolder.put(
            argument.getName(), ArgumentData.newArgumentData(null, argument, null, null, e));
        continue;
      }
      int end = reader.getCursor();
      reader.skipWhitespace();

      String raw = input.substring(start, end);
      int splitIndex;
      if ((raw.length() == 1 && raw.charAt(0) == ' ') || raw.isEmpty()) {
        argumentDataHolder.put(
            argument.getName(), ArgumentData.newArgumentData(null, argument, null, null, null));
        continue;
      } else {
        splitIndex = raw.split(" ").length - 1;
      }
      IndexRange range;
      if (argumentIndex == splitIndex) {
        range = new IndexRange(argumentIndex);
      } else {
        range = new IndexRange(argumentIndex, argumentIndex + splitIndex);
      }
      argumentDataHolder.put(
          argument.getName(), ArgumentData.newArgumentData(range, argument, parsed, raw, null));
      index++;
    }
  }

  private Map<Argument, Integer> getRequiredArgs(List<Argument> arguments, int previousIndex) {
    if (arguments.isEmpty()) {
      return Collections.emptyMap();
    }
    Map<Argument, Integer> args = new HashMap<>();
    for (Argument argument : arguments) {
      int currentIndex = previousIndex + 1;
      if (!argument.isLiteral()) {
        args.put(argument, currentIndex);
      }
      if (argument.getChildren().isEmpty()) {
        continue;
      }
      args.putAll(getRequiredArgs(argument.getChildren(), currentIndex));
    }
    return args;
  }

  /**
   * Retrieves the required argument with the specified name, which should be parsed to the
   * specified type.
   *
   * @param name argument name
   * @param type argument type
   * @param <V> value type
   * @return value
   */
  public <V> V getRequiredArgument(String name, Class<V> type) throws CommandSyntaxException {
    ArgumentData argumentData = argumentDataHolder.get(name);
    if (argumentData != null) {
      if (argumentData.getCommandSyntaxException() != null) {
        throw argumentData.getCommandSyntaxException();
      }

      if (argumentData.getArgument().getArgumentType() == null) {
        throw new IllegalArgumentException(
            "Cannot parse minecraft argument type on non-brigadier supported version. \n "
                + "THIS IS NOT A BUG !!! Please use getRawRequiredArgument and PARSE THE ARGUMENT YOURSELF");
      }

      Object parsed = argumentData.getParsedValue();
      if (PRIMITIVE_TO_WRAPPER.getOrDefault(type, type).isAssignableFrom(parsed.getClass())) {
        return (V) parsed;
      } else {
        throw new IllegalArgumentException(
            "Argument '"
                + name
                + "' is defined as '"
                + parsed.getClass().getSimpleName()
                + "', not "
                + type.getSimpleName()
                + "\n THIS IS NOT A BUG. IF the argument type is a minecraft one, "
                + "please use getRawRequiredArgument and PARSE IT YOURSELF.");
      }
    }
    return null;
  }

  /**
   * Returns the last argument, for which we have a value.
   *
   * @return last argument
   */
  public Argument getLastArgument() {
    Collection<ArgumentData> argumentData = argumentDataHolder.values();
    Argument argument = null;
    for (ArgumentData data : argumentData) {
      if (isTyped(data)) {
        IndexRange indexRange = data.getIndex();
        if (indexRange != null) {
          int compared =
              indexRange.isRange() ? (indexRange.getEnd() + 1) : (indexRange.getIndex() + 1);
          if (compared == size()) {
            argument = data.getArgument();
            break;
          }
        } else if (data.getCommandSyntaxException() != null) {
          argument = data.getArgument();
          break;
        }
      }
    }
    return argument;
  }

  /**
   * Returns the {@link IndexRange} of the specified argument name, if the argument was defined in
   * the command structure.
   *
   * @param argumentName argument name
   * @return range if present
   */
  public IndexRange getArgumentIndex(String argumentName) {
    ArgumentData data = argumentDataHolder.get(argumentName);
    return data != null ? data.getIndex() : null;
  }

  /**
   * Returns whether or not the specified argument is typed (being present when the command was
   * executed).
   *
   * @param argumentName the argument's name that you want to check if has value or not.
   * @return <code>true</code> if typed, <code>false</code> otherwise
   */
  public boolean isTyped(String argumentName) {
    return isTyped(argumentDataHolder.get(argumentName));
  }

  private boolean isTyped(ArgumentData argumentData) {
    return argumentData != null
        && (argumentData.getRawValue() != null || argumentData.getCommandSyntaxException() != null);
  }

  /**
   * Returns the specified required argument's name's argument if is typed in its raw state.
   *
   * @param argumentName argument name
   * @return argument
   */
  public String getRawRequiredArgument(String argumentName) throws CommandSyntaxException {
    ArgumentData argumentData = argumentDataHolder.get(argumentName);
    if (argumentData == null) {
      return null;
    }
    if (argumentData.getCommandSyntaxException() != null) {
      throw argumentData.getCommandSyntaxException();
    }
    return argumentData.getRawValue();
  }

  /**
   * Returns the arguments length.
   *
   * @return length
   */
  public int size() {
    return (int) argumentDataHolder.values().stream().filter(this::isTyped).count();
  }

  /**
   * Returns the arguments in their raw state.
   *
   * @return raw arguments
   */
  public String[] getRawArgs() {
    return input.split(" ");
  }
}
