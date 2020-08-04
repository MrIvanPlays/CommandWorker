package com.mrivanplays.commandworker.bukkit.registry;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mrivanplays.commandworker.core.Command;
import com.mrivanplays.commandworker.core.LiteralNode;
import com.mrivanplays.commandworker.core.RegisteredCommand;
import com.mrivanplays.commandworker.core.argument.Argument;
import com.mrivanplays.commandworker.core.argument.parser.ArgumentHolder;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.server.v1_16_R1.CommandListenerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R1.CraftServer;

public class CmdRegistry1_16_R1 implements CmdRegistry {

  @Override
  public void register(RegisteredCommand<CommandSender> command) {
    CommandDispatcher<CommandListenerWrapper> dispatcher =
        ((CraftServer) Bukkit.getServer()).getServer().vanillaCommandDispatcher.a();
    for (String alias : command.getAliases()) {
      LiteralArgumentBuilder<CommandListenerWrapper> builder =
          LiteralArgumentBuilder.<CommandListenerWrapper>literal(alias)
              .requires(getBrigadierRequires(command.getPermissionCheckFunction()));

      LiteralNode node = command.getCommandStructure();
      if (node.shouldExecuteCommand()) {
        builder.executes(getBrigadierCommand(command.getCommand(), alias, node));
      }

      if (node.getArguments().isEmpty()) {
        dispatcher.register(
            builder.executes(getBrigadierCommand(command.getCommand(), alias, node)));
        continue;
      }

      LiteralArgumentBuilder<CommandListenerWrapper> filledBuilder =
          (LiteralArgumentBuilder<CommandListenerWrapper>)
              handleArguments(
                  command.getCommand(),
                  alias,
                  node.getArguments(),
                  command.getPermissionCheckFunction(),
                  node,
                  builder);

      dispatcher.register(filledBuilder);
    }
  }

  private com.mojang.brigadier.Command<CommandListenerWrapper> getBrigadierCommand(
      Command<CommandSender> command, String alias, LiteralNode structure) {
    return context ->
        command.execute(
                context.getSource().getBukkitSender(),
                alias,
                new ArgumentHolder(context.getInput().replace(alias + " ", ""), structure))
            ? 1
            : 0;
  }

  private Predicate<CommandListenerWrapper> getBrigadierRequires(
      Predicate<CommandSender> requires) {
    return (wrapper) -> requires.test(wrapper.getBukkitSender());
  }

  private ArgumentBuilder<CommandListenerWrapper, ?> handleArguments(
      Command<CommandSender> command,
      String alias,
      List<Argument> arguments,
      Predicate<CommandSender> permissionCheck,
      LiteralNode commandStructure,
      ArgumentBuilder<CommandListenerWrapper, ?> builder) {
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

  private LiteralArgumentBuilder<CommandListenerWrapper> getLiteral(
      Command<CommandSender> command,
      String commandAlias,
      Argument argument,
      Predicate<CommandSender> permissionCheck,
      LiteralNode commandStructure) {
    LiteralArgumentBuilder<CommandListenerWrapper> builder =
        LiteralArgumentBuilder.literal(argument.getName());
    builder.requires(getBrigadierRequires(permissionCheck));
    if (argument.shouldExecuteCommand()) {
      builder.executes(getBrigadierCommand(command, commandAlias, commandStructure));
    }
    return builder;
  }

  private RequiredArgumentBuilder<CommandListenerWrapper, Object> getRequired(
      Command<CommandSender> command,
      String commandAlias,
      Argument argument,
      Predicate<CommandSender> permissionCheck,
      LiteralNode commandStructure) {
    RequiredArgumentBuilder<CommandListenerWrapper, Object> required =
        RequiredArgumentBuilder.argument(argument.getName(), argument.getArgumentType());
    required.requires(getBrigadierRequires(permissionCheck));
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
