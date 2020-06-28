package com.mrivanplays.commandworker.bukkit.internal;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mrivanplays.commandworker.core.Command;
import com.mrivanplays.commandworker.core.LiteralNode;
import com.mrivanplays.commandworker.core.RegisteredCommand;
import com.mrivanplays.commandworker.core.argument.RequiredArgument;
import com.mrivanplays.commandworker.core.argument.parser.ArgumentHolder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BukkitBridgeCommandTest {

  private DummySender sender;
  private Command<CommandSender> dummyCommand;
  private RegisteredCommand<CommandSender> registeredDummy;
  private BukkitBridgeCommand bridgeCommand;

  @Before
  public void init() {
    sender = new DummySender();
    dummyCommand =
        new Command<CommandSender>() {
          @Override
          public boolean execute(CommandSender sender, String label, ArgumentHolder args)
              throws CommandSyntaxException {
            String player = args.getRawRequiredArgument("player");
            if (player.equalsIgnoreCase("DummyPlayer")) {
              throw new SimpleCommandExceptionType(new LiteralMessage("Error: Player not found"))
                  .create();
            }
            sender.sendMessage("player name: " + player);
            sender.sendMessage("message: " + args.getRawRequiredArgument("message"));
            return true;
          }

          @Override
          public LiteralNode createCommandStructure() {
            return LiteralNode.node()
                .argument(
                    RequiredArgument.argument("player", StringArgumentType.word())
                        .markShouldNotExecuteCommand()
                        .then(
                            RequiredArgument.argument("integer", IntegerArgumentType.integer())
                                .markShouldNotExecuteCommand()
                                .then(
                                    RequiredArgument.argument(
                                        "message", StringArgumentType.greedyString()))));
          }
        };
    registeredDummy =
        new RegisteredCommand<>(new String[] {"test"}, dummyCommand, (sender) -> true);
    bridgeCommand = new BukkitBridgeCommand(registeredDummy, new String[] {"test"});
  }

  @After
  public void terminate() {
    sender = null;
    dummyCommand = null;
    registeredDummy = null;
    bridgeCommand = null;
  }

  @Test
  public void testWithDummyPlayer() {
    bridgeCommand.execute(sender, "test", new String[]{"DummyPlayer", "1", "a"});

    List<String> caughtMessages = sender.getCaughtMessages();
    Assert.assertEquals(1, caughtMessages.size());
    Assert.assertEquals("§cError: Player not found", caughtMessages.get(0));
  }

  @Test
  public void testWithOtherPlayer() {
    bridgeCommand.execute(sender, "test", new String[] {"MrIvanPlays", "0", "hello"});

    List<String> caughtMessages = sender.getCaughtMessages();
    Assert.assertEquals(2, caughtMessages.size());
    Assert.assertEquals("player name: MrIvanPlays", caughtMessages.get(0));
    Assert.assertEquals("message: hello", caughtMessages.get(1));
  }

  @Test
  public void testNoArgs() {
    bridgeCommand.execute(sender, "test", new String[0]);

    List<String> caughtMessages = sender.getCaughtMessages();
    Assert.assertEquals(1, caughtMessages.size());
    Assert.assertEquals("§cUnknown command", caughtMessages.get(0));
  }

  @Test
  public void testOnlyPlayer() {
    String[] args = new String[] {"MrIvanPlays"};
    bridgeCommand.execute(sender, "test", args);

    List<String> caughtMessages = sender.getCaughtMessages();
    Assert.assertEquals(1, caughtMessages.size());
    Assert.assertEquals("§cUnknown command", caughtMessages.get(0));
  }

  private static final class DummySender implements CommandSender {

    private List<String> caughtMessages = new ArrayList<>();

    @Override
    public void sendMessage(String s) {
      caughtMessages.add(s);
    }

    public List<String> getCaughtMessages() {
      return caughtMessages;
    }

    @Override
    public void sendMessage(String[] strings) {
      for (String s : strings) {
        sendMessage(s);
      }
    }

    @Override
    public Server getServer() {
      return null;
    }

    @Override
    public String getName() {
      return "DummySender";
    }

    @Override
    public boolean isPermissionSet(String s) {
      return true;
    }

    @Override
    public boolean isPermissionSet(Permission permission) {
      return true;
    }

    @Override
    public boolean hasPermission(String s) {
      return true;
    }

    @Override
    public boolean hasPermission(Permission permission) {
      return true;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b) {
      return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
      return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b, int i) {
      return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int i) {
      return null;
    }

    @Override
    public void removeAttachment(PermissionAttachment permissionAttachment) {}

    @Override
    public void recalculatePermissions() {}

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
      return Collections.emptySet();
    }

    @Override
    public boolean isOp() {
      return true;
    }

    @Override
    public void setOp(boolean b) {}
  }
}
