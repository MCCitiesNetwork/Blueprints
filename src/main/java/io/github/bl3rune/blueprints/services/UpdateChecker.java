package io.github.bl3rune.blueprints.services;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import io.github.bl3rune.blueprints.config.GlobalConfig;
import io.github.bl3rune.blueprints.enums.SemanticLevel;

/**
 * Polls GitHub releases for new versions and reports them to the console.
 * Extracted from the plugin main class so the plugin can wire it through
 * the registry; the verbose-logging and update-level decisions still come
 * from {@link GlobalConfig}.
 */
public final class UpdateChecker {

    private static final String LATEST_RELEASE_URL =
            "https://api.github.com/repos/bl3rune/Blu3Prints-Plugin/releases/latest";
    private static final String ALL_RELEASES_URL =
            "https://api.github.com/repos/bl3rune/Blu3Prints-Plugin/releases";

    private final JavaPlugin plugin;
    private final Gson gson = new Gson();
    private final Supplier<List<String>> messageSink;

    public UpdateChecker(JavaPlugin plugin, Supplier<List<String>> messageSink) {
        this.plugin = plugin;
        this.messageSink = messageSink;
    }

    public void checkUpdate() {
        try {
            SemanticLevel semanticLevel = GlobalConfig.getUpdateLoggingLevel();
            if (semanticLevel == SemanticLevel.NONE) {
                return;
            }

            HttpURLConnection con = (HttpURLConnection) new URL(LATEST_RELEASE_URL).openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("GET");
            JsonReader reader = gson.newJsonReader(new InputStreamReader(con.getInputStream()));
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            String latestVersion = json.get("tag_name").getAsString();
            String version = plugin.getDescription().getVersion();
            String[] lv = latestVersion.split("\\.");
            String[] v = version.split("\\.");
            boolean changed = lv.length != v.length;
            switch (semanticLevel) {
                case PATCH:
                    changed = changed || (!lv[2].equals(v[2]));
                case MINOR:
                    changed = changed || (!lv[1].equals(v[1]));
                case MAJOR:
                    changed = changed || (!lv[0].equals(v[0]));
                default:
            }
            if (changed) {
                List<String> messages = messageSink.get();
                messages.clear();
                messages.add("§9[Blu3Print]§r§6 New Update Available!");
                messages.add("§9[Blu3Print]§r Current v" + version + " >> Latest v" + latestVersion);
                messages.addAll(getUpdateMessageDetails());
                messages.forEach(um -> Bukkit.getConsoleSender().sendMessage(um));
            }
        } catch (Exception ex) {
            Bukkit.getConsoleSender().sendMessage("§cCould not check for updates!");
            if (GlobalConfig.isVerboseLogging()) {
                ex.printStackTrace();
            }
        }
    }

    public List<String> getUpdateMessageDetails() {
        SemanticLevel semanticLevel = GlobalConfig.getUpdateLoggingLevel();
        String version = plugin.getDescription().getVersion();
        String[] v = version.split("\\.");
        List<String> updateMessages = new ArrayList<>();
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(ALL_RELEASES_URL).openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("GET");
            JsonReader reader = gson.newJsonReader(new InputStreamReader(con.getInputStream()));
            JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
            for (JsonElement element : jsonArray.asList()) {
                JsonObject release = element.getAsJsonObject();
                String releaseVersion = release.get("tag_name").getAsString();
                String[] rv = releaseVersion.split("\\.");
                boolean changed = rv.length != v.length;
                switch (semanticLevel) {
                    case PATCH:
                        changed = changed || (!rv[2].equals(v[2]));
                    case MINOR:
                        changed = changed || (!rv[1].equals(v[1]));
                    case MAJOR:
                        changed = changed || (!rv[0].equals(v[0]));
                    default:
                }
                if (!changed) {
                    break;
                }
                String update = release.get("name").getAsString();
                updateMessages.add("§9[Blu3Print]§r " + update);
            }
        } catch (Exception ex) {
            Bukkit.getConsoleSender().sendMessage("§cCould not check for detailed updates!");
        }
        return updateMessages;
    }
}
