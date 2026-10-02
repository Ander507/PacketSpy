package org.ander507.packetspy.client;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Turns obfuscated names (class_1234, field_5678, comp_9012) back into readable Yarn names.
 * The table is generated at build time (see generateNameTable in build.gradle) and only exists in
 * jars for obfuscated Minecraft versions; on 26.x everything already has its real name.
 */
@Environment(EnvType.CLIENT)
final class NameTable {
    private static final Pattern OBFUSCATED = Pattern.compile("(net\\.minecraft\\.)?class_(\\d+)|\\b(field|comp)_(\\d+)\\b");

    private static final Map<Integer, String> CLASSES = new HashMap<>();
    private static final Map<Integer, String> FIELDS = new HashMap<>();
    private static final Map<Integer, String> COMPONENTS = new HashMap<>();

    static {
        try (InputStream in = NameTable.class.getResourceAsStream("/packetspy/names.tsv.gz")) {
            if (in != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(in), StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\t", 3);
                    if (parts.length != 3) continue;
                    Map<Integer, String> target = switch (parts[0]) {
                        case "c" -> CLASSES;
                        case "f" -> FIELDS;
                        case "r" -> COMPONENTS;
                        default -> null;
                    };
                    if (target != null) {
                        target.put(Integer.parseInt(parts[1]), parts[2]);
                    }
                }
                PacketspyClient.LOGGER.info("PacketSpy loaded {} class and {} field names", CLASSES.size(), FIELDS.size() + COMPONENTS.size());
            }
        } catch (Exception e) {
            PacketspyClient.LOGGER.warn("PacketSpy could not load its name table, names will stay obfuscated", e);
        }
    }

    private NameTable() {
    }

    /** Remaps every obfuscated class/field name inside a piece of text, e.g. a toString() result. */
    static String remap(String text) {
        if (CLASSES.isEmpty() || (!text.contains("class_") && !text.contains("field_") && !text.contains("comp_"))) {
            return text;
        }
        Matcher m = OBFUSCATED.matcher(text);
        StringBuilder out = new StringBuilder(text.length() + 32);
        while (m.find()) {
            String replacement = null;
            if (m.group(2) != null) {
                String name = CLASSES.get(Integer.parseInt(m.group(2)));
                // "net.minecraft.class_1" is a full name, a bare "class_1" (records, inner classes) is a simple one
                if (name != null) {
                    replacement = m.group(1) != null ? name : simpleName(name);
                }
            } else {
                Map<Integer, String> table = m.group(3).equals("field") ? FIELDS : COMPONENTS;
                replacement = table.get(Integer.parseInt(m.group(4)));
            }
            m.appendReplacement(out, Matcher.quoteReplacement(replacement != null ? replacement : m.group()));
        }
        m.appendTail(out);
        return out.toString();
    }

    static String className(Class<?> type) {
        return remap(type.getName());
    }

    static String simpleClassName(Class<?> type) {
        return simpleName(className(type));
    }

    private static String simpleName(String name) {
        return name.substring(Math.max(name.lastIndexOf('.'), name.lastIndexOf('$')) + 1);
    }
}
