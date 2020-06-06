![license](https://img.shields.io/github/license/MrIvanPlays/CommandWorker.svg?style=for-the-badge)
![issues](https://img.shields.io/github/issues/MrIvanPlays/CommandWorker.svg?style=for-the-badge)
![api version](https://img.shields.io/maven-metadata/v?color=%20blue&label=latest%20version&metadataUrl=https%3A%2F%2Frepo.mrivanplays.com%2Frepository%2Fivan%2Fcom%2Fmrivanplays%2Fcommandworker-core%2Fmaven-metadata.xml&style=for-the-badge)
[![support](https://img.shields.io/discord/493674712334073878.svg?colorB=Blue&logo=discord&label=Support&style=for-the-badge)](https://mrivanplays.com/discord)

# CommandWorker
A command framework using brigadier for minecraft

Allows you to create such commands: (please keep in mind this is just an example)
![image](https://img.mrivanplays.com/hYARA3qRri.gif)

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
public class TellMeCommand implements BukkitCommand {

    @Override
    public boolean execute(CommandSender sender, String label, ArgumentHolder args) {
        Player player = Bukkit.getPlayer(args.getRawRequiredArgument("player"));
        if (player == null) {
            sender.sendMessage("Error: Player not online.");
            return true;
        }
        player.sendMessage(args.getRawRequiredArgument("message"));
        // you could also use args.getRequiredArgument(String name, Class<?> argumentType),
        // but for this example it's not necessary to use it
        return true;
    }

    @Override
    public LiteralNode createCommandStructure() {
        return LiteralNode.node()
                .argument(RequiredArgument.argument("player", StringArgumentType.word())
                        .markShouldNotExecuteCommand() // this makes the argument required
                        .suggests(builder -> {
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                builder.suggest(player.getName());
                            }
                        })
                        .then(RequiredArgument.argument("message", StringArgumentType.greedyString()))
                );
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
        <!-- Platforms: bukkit-core -->
        <artifactId>commandworker-(platform)</artifactId> <!-- Replace platform -->
        <version>VERSION</version> <!-- Replace with latest version -->
        <scope>compile</scope>
    </dependency>
```

# License
MIT, you can go kaboom
