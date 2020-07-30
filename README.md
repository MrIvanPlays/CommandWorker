![license](https://img.shields.io/github/license/MrIvanPlays/CommandWorker.svg?style=for-the-badge)
![issues](https://img.shields.io/github/issues/MrIvanPlays/CommandWorker.svg?style=for-the-badge)
![api version](https://img.shields.io/maven-metadata/v?color=%20blue&label=latest%20version&metadataUrl=https%3A%2F%2Frepo.mrivanplays.com%2Frepository%2Fivan%2Fcom%2Fmrivanplays%2Fcommandworker-core%2Fmaven-metadata.xml&style=for-the-badge)
[![support](https://img.shields.io/discord/493674712334073878.svg?colorB=Blue&logo=discord&label=Support&style=for-the-badge)](https://mrivanplays.com/discord)

# CommandWorker
A command framework using brigadier for minecraft

Allows you to create such commands: (please keep in mind this is just an example)
![image](https://img.mrivanplays.com/UIXBLjtUr9.gif)

Documentation:
- [bukkit](https://mrivanplays.com/javadocs/commandworker/bukkit/) - for bukkit specific things 
- [core](https://mrivanplays.com/javadocs/commandworker/core/) - for most of the framework

# Why choose CommandWorker over other
First of all CommandWorker's code base is abstract, meaning that in future CommandWorker would not
only support Bukkit and its forks, but other minecraft server software, including proxies.

Second, CommandWorker registers commands by the standard way, intended for the server software
it is being implemented on, IF brigadier isn't supported on the minecraft version your plugin is ran
on.

# Can I have example
Sure, but keep in mind these examples are for the bukkit platform. You have to find it yourself if 
you want for other platforms :)

```java
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static com.mrivanplays.commandworker.core.LiteralNode.node;
import static com.mrivanplays.commandworker.core.argument.RequiredArgument.argument;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mrivanplays.commandworker.bukkit.BukkitCommand;
import com.mrivanplays.commandworker.core.LiteralNode;
import com.mrivanplays.commandworker.core.argument.parser.ArgumentHolder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TellMeCommand implements BukkitCommand {

  @Override
  public boolean execute(CommandSender sender, String label, ArgumentHolder args)
      throws CommandSyntaxException {
    String playerName = args.getRequiredArgument("player", String.class);
    Player player = Bukkit.getPlayer(playerName);
    if (player == null) {
      throw new SimpleCommandExceptionType(new LiteralMessage("Error: Player not online")).create();
    }
    String message = args.getRequiredArgument("message", String.class);
    player.sendMessage(sender.getName() + " sent you message: " + message);
    return false;
  }

  @Override
  public LiteralNode createCommandStructure() {
    return node()
        .argument(
            argument("player", word())
                .markShouldNotExecuteCommand() // this makes the argument required
                .suggests(
                    builder -> {
                      String currentArgument = builder.getRemaining().toLowerCase();
                      for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.getName().toLowerCase().startsWith(currentArgument)) {
                          builder.suggest(player.getName());
                        }
                      }
                    })
                .then(argument("message", greedyString())));
  }
}

```

Registering command:
```java
BukkitCommandManager commandManager = new BukkitCommandManager(yourPluginInstance);
commandManager.register(new TellMeCommand(), sender -> sender.hasPermission("myplugin.tellme"), "tellme", "metell");
```

More things can be found at the javadocs [here](https://mrivanplays.com/javadocs/)

# Installation

Maven:
```xml

    <build>
        <plugins>
            <plugin>
                <version>3.7.0</version>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>1.8</source>
                    <target>1.8</target>
                    <compilerArgs>
                        <arg>-parameters</arg>
                    </compilerArgs>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.1.1</version>
                <configuration>
                    <relocations>
                        <relocation>
                            <pattern>com.mrivanplays.commandworker</pattern>
                            <shadedPattern>[YOUR PLUGIN PACKAGE].commandworker</shadedPattern> <!-- Replace this -->
                        </relocation>
                    </relocations>
                </configuration>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

    <repositories>
        <repository>
            <id>ivan</id>
            <url>https://repo.mrivanplays.com/repository/ivan/</url>
        </repository>
    </repositories>

    <dependency>
        <groupId>com.mrivanplays</groupId>
        <!-- Platforms: bukkit-core, velocity -->
        <artifactId>commandworker-(platform)</artifactId> <!-- Replace platform -->
        <version>VERSION</version> <!-- Replace with latest version -->
        <scope>compile</scope>
    </dependency>
```

# License
MIT, you can go kaboom
