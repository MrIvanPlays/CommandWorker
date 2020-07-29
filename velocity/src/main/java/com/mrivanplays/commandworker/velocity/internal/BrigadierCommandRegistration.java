package com.mrivanplays.commandworker.velocity.internal;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mrivanplays.commandworker.core.Command;
import com.mrivanplays.commandworker.core.LiteralNode;
import com.mrivanplays.commandworker.core.RegisteredCommand;
import com.mrivanplays.commandworker.core.argument.Argument;
import com.mrivanplays.commandworker.core.argument.parser.ArgumentHolder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import java.util.List;
import java.util.function.Predicate;

public class BrigadierCommandRegistration implements CommandRegistration {

  @Override
  public void register(ProxyServer proxy, RegisteredCommand<CommandSource> command) {
    for (String alias : command.getAliases()) {
      LiteralArgumentBuilder<CommandSource> builder =
          LiteralArgumentBuilder.<CommandSource>literal(alias)
              .requires(command.getPermissionCheckFunction());

      LiteralNode node = command.getCommandStructure();
      if (node.shouldExecuteCommand()) {
        builder.executes(getBrigadierCommand(command.getCommand(), alias, node));
      }

      if (node.getArguments().isEmpty()) {
        proxy.getCommandManager().register(new BrigadierCommand(builder.build()));
        continue;
      }

      LiteralArgumentBuilder<CommandSource> filledBuilder =
          (LiteralArgumentBuilder<CommandSource>)
              handleArguments(
                  command.getCommand(),
                  alias,
                  node.getArguments(),
                  command.getPermissionCheckFunction(),
                  node,
                  builder);

      proxy.getCommandManager().register(new BrigadierCommand(filledBuilder.build()));
    }
  }

  private com.mojang.brigadier.Command<CommandSource> getBrigadierCommand(
      Command<CommandSource> command, String alias, LiteralNode structure) {
    return context ->
        command.execute(
                context.getSource(),
                alias,
                new ArgumentHolder(context.getInput().replace(alias + " ", ""), structure))
            ? 1
            : 0;
  }

  private ArgumentBuilder<CommandSource, ?> handleArguments(
      Command<CommandSource> command,
      String alias,
      List<Argument> arguments,
      Predicate<CommandSource> permissionCheck,
      LiteralNode commandStructure,
      ArgumentBuilder<CommandSource, ?> builder) {
    if (arguments.isEmpty()) {
      return builder;
    }
    for (Argument argument : arguments) {
      builder.then(
          handleArguments(
              command,
              alias,
              argument.getChildren(),
              permissionCheck,
              commandStructure,
              argument.isLiteral()
                  ? getLiteral(command, alias, argument, permissionCheck, commandStructure)
                  : getRequired(command, alias, argument, permissionCheck, commandStructure)));
    }
    return builder;
  }

  private LiteralArgumentBuilder<CommandSource> getLiteral(
      Command<CommandSource> command,
      String commandAlias,
      Argument argument,
      Predicate<CommandSource> permissionCheck,
      LiteralNode commandStructure) {
    LiteralArgumentBuilder<CommandSource> builder =
        LiteralArgumentBuilder.literal(argument.getName());
    builder.requires(permissionCheck);
    if (argument.shouldExecuteCommand()) {
      builder.executes(getBrigadierCommand(command, commandAlias, commandStructure));
    }
    return builder;
  }

  private RequiredArgumentBuilder<CommandSource, Object> getRequired(
      Command<CommandSource> command,
      String commandAlias,
      Argument argument,
      Predicate<CommandSource> permissionCheck,
      LiteralNode commandStructure) {
    RequiredArgumentBuilder<CommandSource, Object> required =
        RequiredArgumentBuilder.argument(argument.getName(), argument.getArgumentType());
    required.requires(permissionCheck);
    if (argument.shouldExecuteCommand()) {
      required.executes(getBrigadierCommand(command, commandAlias, commandStructure));
    }
    if (argument.getSuggestionsConsumer() != null) {
      required.suggests(
          (context, builder) -> {
            argument.getSuggestionsConsumer().accept(builder);
            return builder.buildFuture();
          });
    }
    return required;
  }
}
