package com.mrivanplays.commandworker.bukkit.registry;

import org.bukkit.Bukkit;

public class CmdRegistryHandler {

  private static CmdRegistry instance;

  public static boolean isSupported() {
    if (instance == null) {
      getRegistry();
    }
    return instance != null;
  }

  public static CmdRegistry getRegistry() {
    if (instance != null) {
      return instance;
    }
    String version =
        Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
    switch (version) {
      case "v1_13_R1":
        instance = new CmdRegistry1_13_R1();
        break;
      case "v1_13_R2":
        instance = new CmdRegistry1_13_R2();
        break;
      case "v1_14_R1":
        instance = new CmdRegistry1_14_R1();
        break;
      case "v1_15_R1":
        instance = new CmdRegistry1_15_R1();
        break;
    }
    return instance;
  }
}
