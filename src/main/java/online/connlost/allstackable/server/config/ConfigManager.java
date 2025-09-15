package online.connlost.allstackable.server.config;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import online.connlost.allstackable.util.ItemsHelper;
import online.connlost.allstackable.util.NetworkHelper;
import net.fabricmc.loader.api.FabricLoader;
import online.connlost.allstackable.AllStackable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;

public final class ConfigManager {
    private static ConfigManager cm;
    public File configFile;
    private File globalConfigFile;
    private final Gson gson;
    private final ItemsHelper itemsHelper;
    public ArrayList<LinkedHashMap<String, Integer>> configList;
    private ArrayList<LinkedHashMap<String, Integer>> globalConfigList;

    @Deprecated
    public static boolean allowItemShulkerStack = true;

    public static ConfigManager getConfigManager() {
        if (cm == null) {
            cm = new ConfigManager();
        }
        return cm;
    }

    private ConfigManager() {
        initConfigList();
        gson = new Gson();
        globalConfigFile = FabricLoader.getInstance().getConfigDir().resolve("allstackable-global-config.json").toFile();
        itemsHelper = ItemsHelper.getItemsHelper();
    }

    public LinkedHashMap<String, Integer> defaultRules(boolean global) {
        LinkedHashMap<String, Integer> defaultRules = new LinkedHashMap<>();
        if (global) {
            defaultRules.put("applyGlobalConfigToAllNewGames", 0);
        }
        defaultRules.put("permissionLevel", 4);
        defaultRules.put("stackEmptyShulkerBoxOnly", 0);
        defaultRules.put("vanillaHopperBehavior", 0);
        defaultRules.put("vanillaComparatorBehavior", 0);
        return defaultRules;
    }

    private void initConfigList() {
        LinkedHashMap<String, Integer> itemsMap = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> rulesMap = defaultRules(false);
        configList = new ArrayList<>();
        configList.add(itemsMap);
        configList.add(rulesMap);
    }

    public int getRuleSetting(String str) {
        if (configList.get(1).containsKey(str)) {
            return configList.get(1).get(str);
        } else {
            AllStackable.LOGGER.error("[All Stackable] No such rule key");
            return -1;
        }
    }

    public void setRulesMap(LinkedHashMap<String, Integer> newRules) {
        if (this.configList == null) {
            this.configList = new ArrayList<>();
            this.configList.add(new LinkedHashMap<>());
            this.configList.add(new LinkedHashMap<>());
        }
        this.configList.set(1, newRules);
    }

    public byte[] getSerializedConfig() {
        return SerializationUtils.serialize((Serializable) configList);
    }

    public void passConfigFile(File f) {
        this.configFile = f;
        AllStackable.LOGGER.info("[All Stackable] Config file set to: {}", f.getAbsolutePath());
    }

    public void setupConfig() {
        if (this.configFile == null) {
            AllStackable.LOGGER.warn("[All Stackable] configFile is null! Using global fallback");
            this.configFile = globalConfigFile;
        }

        loadConfig();
        itemsHelper.setCountByConfig(this.configList.get(0).entrySet(), true);
        AllStackable.LOGGER.info("[All Stackable] Config Loaded");
    }

    public void sendConfigToPlayer() {
        NetworkHelper.sentConfigToAll();
    }

    public boolean restoreBackup() {
        if (configFile == null || configFile.getParentFile() == null) {
            AllStackable.LOGGER.error("[All Stackable] Cannot restore backup - invalid config path");
            return false;
        }

        File bk = configFile.getParentFile().toPath().resolve("allstackable-config.json.bk").toFile();
        if (bk.exists()) {
            try (FileReader reader = new FileReader(bk)) {
                ArrayList<LinkedHashMap<String, Integer>> tmp = gson.fromJson(reader,
                        new TypeToken<ArrayList<LinkedHashMap<String, Integer>>>() {}.getType());

                if (tmp == null || tmp.size() != 2) {
                    bk.delete();
                    AllStackable.LOGGER.error("[All Stackable] Corrupted backup detected, removed.");
                    return false;
                }
                configList = tmp;
            } catch (IOException e) {
                AllStackable.LOGGER.error("[All Stackable] Failed to parse backup file", e);
                return false;
            }
            this.writeConfig(this.configFile, this.configList);
            this.setupConfig();
            AllStackable.LOGGER.info("[All Stackable] Backup config restored!");
            return true;
        }
        return false;
    }

    public ArrayList<LinkedHashMap<String, Integer>> loadConfig() {
        // Critical null safety check
        if (this.configFile == null) {
            AllStackable.LOGGER.warn("[All Stackable] configFile is null in loadConfig! Using global fallback");
            this.configFile = globalConfigFile;
        }

        this.tryReadGlobalConfig();

        if (this.configFile.exists()) {
            try (FileReader reader = new FileReader(this.configFile)) {
                configList = gson.fromJson(reader,
                        new TypeToken<ArrayList<LinkedHashMap<String, Integer>>>() {}.getType());

                if (configList == null || configList.size() != 2) {
                    safeDeleteConfig();
                    AllStackable.LOGGER.error("[All Stackable] Corrupted config detected, reset.");
                    return loadConfig(); // Safe recursion with null checks
                }
                configList.set(1, makeRulesUpdated(configList.get(1), false));
            } catch (IOException e) {
                AllStackable.LOGGER.error("[All Stackable] Failed to parse config", e);
                createDefaultConfig();
            }
        } else {
            handleMissingConfig();
        }

        return configList;
    }

    private void safeDeleteConfig() {
        try {
            if (configFile != null && configFile.exists()) {
                configFile.delete();
            }
        } catch (SecurityException e) {
            AllStackable.LOGGER.error("[All Stackable] Failed to delete corrupted config", e);
        }
    }

    private void handleMissingConfig() {
        if (configFile != null) {
            String path = configFile.getAbsolutePath();
            String oldPath = path.replace("allstackable-config.json", "all_stackable.json");
            File oldFile = new File(oldPath);

            if (oldFile.exists()) {
                convertOldConfig(oldFile);
                return;
            }
        }

        if (!tryApplyGlobalToLocalConfig(false)) {
            createDefaultConfig();
        }
    }

    private void convertOldConfig(File oldFile) {
        AllStackable.LOGGER.info("[All Stackable] Converting old config format");
        this.initConfigList();
        try (FileReader reader = new FileReader(oldFile)) {
            LinkedHashMap<String, Integer> tmp = gson.fromJson(reader,
                    new TypeToken<LinkedHashMap<String, Integer>>() {}.getType());

            if (tmp == null) {
                oldFile.delete();
                AllStackable.LOGGER.error("[All Stackable] Corrupted old config detected, removed.");
                createDefaultConfig();
            } else {
                configList.set(0, tmp);
                oldFile.delete();
                writeConfig(this.configFile, this.configList);
            }
        } catch (IOException e) {
            AllStackable.LOGGER.error("[All Stackable] Failed to parse old config", e);
            createDefaultConfig();
        }
    }

    private void createDefaultConfig() {
        this.initConfigList();
        if (configFile != null) {
            writeConfig(this.configFile, this.configList);
        } else {
            AllStackable.LOGGER.error("[All Stackable] Cannot create default config - null path");
        }
    }

    public void writeConfig(File configFile, ArrayList<LinkedHashMap<String, Integer>> configData) {
        if (configFile == null) {
            AllStackable.LOGGER.error("[All Stackable] Cannot write config - null file");
            return;
        }

        File dir = configFile.getParentFile();
        if (dir == null) {
            AllStackable.LOGGER.error("[All Stackable] Invalid config path");
            return;
        }

        if (!dir.exists() && !dir.mkdirs()) {
            AllStackable.LOGGER.error("[All Stackable] Failed to create config directory");
            return;
        }

        if (configFile.exists()) {
            File bk = new File(configFile.getAbsolutePath() + ".bk");
            try {
                FileUtils.copyFile(configFile, bk);
            } catch (IOException e) {
                AllStackable.LOGGER.error("[All Stackable] Failed to backup config", e);
            }
        }

        try (FileWriter writer = new FileWriter(configFile)) {
            gson.toJson(configData, writer);
        } catch (IOException e) {
            AllStackable.LOGGER.error("[All Stackable] Failed to save config", e);
        }
    }

    public ArrayList<LinkedHashMap<String, Integer>> syncConfig() {
        if (configList != null && itemsHelper != null) {
            configList.set(0, itemsHelper.getNewConfigMap());
            writeConfig(this.configFile, this.configList);
            NetworkHelper.sentConfigToAll();
        }
        return configList;
    }

    public void resetAllItems() {
        if (configList != null && !configList.isEmpty()) {
            configList.get(0).clear();
            writeConfig(this.configFile, this.configList);
            NetworkHelper.sentConfigToAll();
        }
    }

    public void updateGlobalConfig(boolean updateStackableList, boolean allowAutoApply) {
        if (globalConfigList == null || configList == null) return;

        if (updateStackableList) {
            this.globalConfigList.get(0).clear();
            for (Map.Entry<String, Integer> entry : this.configList.get(0).entrySet()) {
                this.globalConfigList.get(0).put(entry.getKey(), entry.getValue());
            }
            this.globalConfigList.set(1, makeRulesUpdated(this.configList.get(1), true));
        }

        if (allowAutoApply) {
            this.globalConfigList.get(1).put("applyGlobalConfigToAllNewGames", 1);
        }

        this.writeConfig(this.globalConfigFile, this.globalConfigList);
        AllStackable.LOGGER.info("[All Stackable] Global Config Updated");
    }

    private void tryReadGlobalConfig() {
        if (this.globalConfigFile.exists()) {
            try (FileReader reader = new FileReader(this.globalConfigFile)) {
                this.globalConfigList = gson.fromJson(reader,
                        new TypeToken<ArrayList<LinkedHashMap<String, Integer>>>() {}.getType());

                if (globalConfigList == null || globalConfigList.size() != 2) {
                    safeDeleteGlobalConfig();
                    AllStackable.LOGGER.error("[All Stackable] Corrupted global config detected, reset.");
                    tryReadGlobalConfig();
                    return;
                }

                this.globalConfigList.set(1, makeRulesUpdated(this.globalConfigList.get(1), true));
                this.writeConfig(this.globalConfigFile, this.globalConfigList);

            } catch (IOException e) {
                AllStackable.LOGGER.error("[All Stackable] Failed to parse global config", e);
            }
        } else {
            createDefaultGlobalConfig();
        }
    }

    private void safeDeleteGlobalConfig() {
        try {
            if (globalConfigFile.exists()) {
                globalConfigFile.delete();
            }
        } catch (SecurityException e) {
            AllStackable.LOGGER.error("[All Stackable] Failed to delete corrupted global config", e);
        }
    }

    private void createDefaultGlobalConfig() {
        this.globalConfigList = new ArrayList<>();
        this.globalConfigList.add(new LinkedHashMap<>());
        this.globalConfigList.add(this.defaultRules(true));
        this.writeConfig(this.globalConfigFile, this.globalConfigList);
        AllStackable.LOGGER.info("[All Stackable] New global config created");
    }

    private boolean tryApplyGlobalToLocalConfig(boolean forced) {
        if (globalConfigList == null || globalConfigList.size() < 2) return false;

        Integer applySetting = globalConfigList.get(1).get("applyGlobalConfigToAllNewGames");
        if (applySetting == null) applySetting = 0;

        if (applySetting == 1 || forced) {
            LinkedHashMap<String, Integer> itemsMap = new LinkedHashMap<>(globalConfigList.get(0));
            LinkedHashMap<String, Integer> rulesMap = makeRulesUpdated(globalConfigList.get(1), false);

            configList = new ArrayList<>();
            configList.add(itemsMap);
            configList.add(rulesMap);
            return true;
        }
        return false;
    }

    private LinkedHashMap<String, Integer> makeRulesUpdated(
            LinkedHashMap<String, Integer> currentMap,
            boolean global
    ) {
        LinkedHashMap<String, Integer> rulesMap = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> defaultRules = defaultRules(global);

        for (Map.Entry<String, Integer> entry : defaultRules.entrySet()) {
            String key = entry.getKey();
            if (currentMap.containsKey(key)) {
                rulesMap.put(key, currentMap.get(key));
            } else {
                if (global && "applyGlobalConfigToAllNewGames".equals(key)) {
                    if (globalConfigList != null &&
                            globalConfigList.size() > 1 &&
                            globalConfigList.get(1).containsKey(key)) {
                        rulesMap.put(key, globalConfigList.get(1).get(key));
                        continue;
                    }
                }
                rulesMap.put(key, entry.getValue());
                AllStackable.LOGGER.info("[All Stackable] Added new rule: {}", key);
            }
        }
        return rulesMap;
    }

    public void applyGlobalToLocal() {
        if (tryApplyGlobalToLocalConfig(true)) {
            writeConfig(this.configFile, this.configList);
            itemsHelper.setCountByConfig(this.configList.get(0).entrySet(), true);
            NetworkHelper.sentConfigToAll();
            AllStackable.LOGGER.info("[All Stackable] Applied global config");
        }
    }
}