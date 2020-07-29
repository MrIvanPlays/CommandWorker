package com.mrivanplays.commandworker.core;

import com.mojang.brigadier.suggestion.IntegerSuggestion;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mrivanplays.commandworker.core.argument.Argument;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Represents a literal node, holder of the base sub command completions. Usage example:
 *
 * <pre>
 *     LiteralNode node = LiteralNode.node().argument(LiteralArgument.literal("literal").then(LiteralArgument.literal("other")));
 * </pre>
 */
public final class LiteralNode {

  // LEGACY COMPLETIONS START
  private static final Function<Suggestion, String> SUGGESTION_MAPPER =
      (suggestion) -> {
        if (suggestion instanceof IntegerSuggestion) {
          return Integer.toString(((IntegerSuggestion) suggestion).getValue());
        } else {
          return suggestion.getText();
        }
      };
  private static final BiFunction<String, List<Argument>, List<String>> TO_SUGGESTIONS =
      (lastArg, arguments) -> {
        List<String> ret = new ArrayList<>();
        for (Argument arg : arguments) {
          if (arg.isLiteral()) {
            if (arg.getName().startsWith(lastArg)) {
              ret.add(arg.getName());
            }
          } else {
            SuggestionsBuilder builder = new SuggestionsBuilder(lastArg, 0);
            if (arg.getSuggestionsConsumer() != null) {
              arg.getSuggestionsConsumer().accept(builder);
            } else {
              if (arg.getArgumentType() == null) {
                // why we check here? obviously a required argument's argument type should not be
                // null! well, apparently, the minecraft argument types would be null if this is
                // even
                // called! and that's why we have to have an exception here
                continue;
              }
              CompletableFuture<Suggestions> suggestionsFuture =
                  arg.getArgumentType().listSuggestions(null, builder);
              List<Suggestion> suggestionsList = suggestionsFuture.join().getList();
              if (suggestionsList.isEmpty()) {
                continue;
              }
              for (Suggestion suggestion : suggestionsList) {
                String suggestionString = SUGGESTION_MAPPER.apply(suggestion);
                if (suggestionString.startsWith(lastArg)) {
                  ret.add(SUGGESTION_MAPPER.apply(suggestion));
                }
              }
              continue;
            }
            List<Suggestion> suggestionsList = builder.build().getList();
            if (suggestionsList.isEmpty()) {
              continue;
            }
            for (Suggestion suggestion : suggestionsList) {
              String suggestionString = SUGGESTION_MAPPER.apply(suggestion);
              if (suggestionString.startsWith(lastArg)) {
                ret.add(SUGGESTION_MAPPER.apply(suggestion));
              }
            }
          }
        }
        return ret;
      };
  // LEGACY COMPLETIONS END

  /**
   * Creates a new {@link LiteralNode}
   *
   * @return literal node.
   */
  public static LiteralNode node() {
    return new LiteralNode();
  }

  private List<Argument> arguments;
  private Map<Integer, StringBuilder> usageBuilders;

  private boolean shouldExecuteCommand = false;

  private LiteralNode() {
    this.arguments = new ArrayList<>();
    this.usageBuilders = new HashMap<>();
  }

  /**
   * Adds argument to the base sub commands.
   *
   * @param argument argument
   * @return this instance for chaining
   * @see Argument
   */
  public LiteralNode argument(Argument argument) {
    arguments.add(argument);
    return this;
  }

  /**
   * @return this instance for chaining
   * @see #shouldExecuteCommand()
   */
  public LiteralNode markShouldExecuteCommand() {
    this.shouldExecuteCommand = true;
    return this;
  }

  /**
   * Returns unmodifiable copy of the base {@link Argument Arguments} with their children.
   *
   * @return arguments
   */
  public List<Argument> getArguments() {
    return Collections.unmodifiableList(arguments);
  }

  /**
   * Returns whether the node should call the base command's execute method if the command was
   * called with no arguments. By default, this returns false. The return value is being ignored if
   * there are no base arguments specified in this literal node.
   *
   * @return <code>true</code> if should execute command, <code>false</code> otherwise.
   */
  public boolean shouldExecuteCommand() {
    return shouldExecuteCommand;
  }

  /**
   * Returns {@link Optional} value of the argument with the name specified.
   *
   * @param name name
   * @return argument if present, empty optional else.
   */
  public Optional<Argument> getArgumentByName(String name) {
    return getArgumentByName0(arguments, name);
  }

  private Optional<Argument> getArgumentByName0(List<Argument> args, String name) {
    Argument first = null;
    for (Argument arg : args) {
      if (arg.getName().toLowerCase().equalsIgnoreCase(name.toLowerCase())) {
        first = arg;
        break;
      }
    }
    if (first != null) {
      return Optional.of(first);
    } else {
      for (Argument arg : args) {
        if (arg.getChildren().isEmpty()) {
          continue;
        }
        Optional<Argument> argumentOptional = getArgumentByName0(arg.getChildren(), name);
        if (argumentOptional.isPresent()) {
          return argumentOptional;
        }
      }
    }
    return Optional.empty();
  }

  /**
   * Returns unmodifiable list of all the arguments, held by this index.
   *
   * @param index the index for which you need the arguments
   * @return arguments by index
   */
  public List<Argument> getArgumentsByIndex(int index) {
    return getArgumentsByIndex0(arguments, index);
  }

  private List<Argument> getArgumentsByIndex0(List<Argument> args, int index) {
    if (index == 0) {
      return Collections.unmodifiableList(args);
    }
    if (index == 1) {
      List<Argument> ret = new ArrayList<>();
      for (Argument arg : args) {
        ret.addAll(arg.getChildren());
      }
      return Collections.unmodifiableList(ret);
    }
    List<Argument> childrenCombined = new ArrayList<>();
    for (Argument arg : args) {
      childrenCombined.addAll(arg.getChildren());
    }
    return getArgumentsByIndex0(childrenCombined, index - 1);
  }

  /**
   * Builds the argument usage.
   *
   * @return argument usage, or if no base arguments - empty string
   */
  public String buildUsage() {
    if (arguments.isEmpty()) {
      return "";
    }
    StringBuilder builder = new StringBuilder();
    buildUsageFor(arguments, builder, true, -1);
    return builder.toString();
  }

  /**
   * Builds full command usage for the alias specified
   *
   * @param alias command alias for which you want full command usage
   * @return command usage
   */
  public String buildUsage(String alias) {
    String argumentUsage = buildUsage();
    return "/" + alias + " " + argumentUsage;
  }

  private void buildUsageFor(
      List<Argument> args, StringBuilder appendTo, boolean initial, int currentArg) {
    if (!args.isEmpty()) {
      if (!initial) {
        appendTo.append(' ');
      }
    }
    currentArg++;
    for (int i = 0, len = args.size(); i < len; i++) {
      boolean lastArg = (i + 1) == len;
      Argument arg = args.get(i);
      if (arg.isLiteral()) {
        appendTo.append(arg.getName());
      } else {
        if (arg.shouldExecuteCommand()) {
          appendTo.append('[');
        }
        appendTo.append('<').append(arg.getName()).append('>');
        if (arg.shouldExecuteCommand()) {
          appendTo.append(']');
        }
      }
      if (!lastArg) {
        appendTo.append("|");
      }

      StringBuilder child = usageBuilders.computeIfAbsent(currentArg, StringBuilder::new);
      buildUsageFor(arg.getChildren(), child, false, currentArg + 1);
      usageBuilders.replace(currentArg, child);
      if (lastArg) {
        appendTo.append(child);
      }
    }
  }

  /**
   * Returns a unmodifiable string list, which converts brigadier's suggestions to string list
   * suggestions based on the arguments specified.
   *
   * @param args args for which you need string list completion
   * @return string list completion
   */
  public List<String> completeToStringList(String[] args) {
    if (arguments.isEmpty() || args.length == 0) {
      return Collections.emptyList();
    }
    List<Argument> argsList = getArgumentsByIndex(args.length - 1);
    if (argsList.isEmpty()) {
      return Collections.emptyList();
    }
    String lastArg = args[args.length - 1];
    return Collections.unmodifiableList(TO_SUGGESTIONS.apply(lastArg, argsList));
  }

  @Override
  public String toString() {
    return "LiteralNode(shouldExecuteCommand="
        + shouldExecuteCommand
        + ", arguments="
        + arguments
        + ")";
  }
}
