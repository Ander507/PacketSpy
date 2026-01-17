package org.ander507.packetspy.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateJigsawC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateStructureBlockC2SPacket;

@Environment(EnvType.CLIENT)
public class PacketSpyHandler extends ChannelDuplexHandler {
    private static final Gson GSON = new Gson();

    // Deobfuscator integration
    private static Object mappingsInstance;
    private static Method mapClassMethod;
    private static Method mapFieldMethod;
    private static boolean deobfInitAttempted = false;

    private static void initDeobfuscator() {
        if (deobfInitAttempted) return;
        deobfInitAttempted = true;
        try {
            Class<?> modClass = Class.forName("dev.booky.stackdeobf.StackDeobfMod");
            Field mappingsField = modClass.getDeclaredField("mappings");
            mappingsField.setAccessible(true);
            if (Modifier.isStatic(mappingsField.getModifiers())) {
                mappingsInstance = mappingsField.get(null);
            }

            if (mappingsInstance != null) {
                // Try specific methods first (StackDeobf 1.4.3+)
                try {
                    Class<?> clazz = mappingsInstance.getClass();
                    mapFieldMethod = clazz.getMethod("remapFields", String.class);
                    mapClassMethod = clazz.getMethod("remapClasses", String.class);
                } catch (NoSuchMethodException e) {
                    // Fallback to signature search
                    for (Method m : mappingsInstance.getClass().getMethods()) {
                        if (m.getReturnType() == String.class) {
                            Class<?>[] params = m.getParameterTypes();
                            if (params.length == 3 && params[0] == String.class && params[1] == String.class && params[2] == String.class) {
                                // Likely (owner, name, desc) -> mappedName
                                mapFieldMethod = m;
                            } else if (params.length == 1 && params[0] == String.class) {
                                // Likely (name) -> mappedName
                                mapClassMethod = m;
                            }
                        }
                    }
                }
            }
        } catch (Throwable t) {
            PacketspyClient.LOGGER.warn("Failed to init StackDeobf: " + t);
        }
    }

    private static String deobfuscateClassName(String className) {
        initDeobfuscator();
        if (mapClassMethod != null && mappingsInstance != null) {
            try {
                String result = (String) mapClassMethod.invoke(mappingsInstance, className.replace('.', '/'));
                if (result != null) return result.replace('/', '.');
            } catch (Exception e) {}
        }
        return className;
    }

    private static String deobfuscateFieldName(Class<?> owner, String name, Class<?> type) {
        initDeobfuscator();
        if (mapFieldMethod != null && mappingsInstance != null) {
            try {
                // Check parameter count to decide how to call
                if (mapFieldMethod.getParameterCount() == 1) {
                     String result = (String) mapFieldMethod.invoke(mappingsInstance, name);
                     if (result != null) return result;
                } else {
                    // Legacy 3-arg support
                    String ownerName = owner.getName().replace('.', '/');
                    String desc = getDescriptor(type);
                    String result = (String) mapFieldMethod.invoke(mappingsInstance, ownerName, name, desc);
                    if (result != null) return result;
                }
            } catch (Exception e) {}
        }
        return name;
    }

    private static String getDescriptor(Class<?> cl) {
        if (cl.isPrimitive()) {
            if (cl == Integer.TYPE) return "I";
            if (cl == Void.TYPE) return "V";
            if (cl == Boolean.TYPE) return "Z";
            if (cl == Byte.TYPE) return "B";
            if (cl == Character.TYPE) return "C";
            if (cl == Short.TYPE) return "S";
            if (cl == Double.TYPE) return "D";
            if (cl == Float.TYPE) return "F";
            if (cl == Long.TYPE) return "J";
        }
        if (cl.isArray()) {
            return "[" + getDescriptor(cl.getComponentType());
        }
        return "L" + cl.getName().replace('.', '/') + ";";
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Packet) {
            this.sendPacket("IN", (Packet<?>)msg);
        }

        super.channelRead(ctx, msg);
    }

    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof Packet) {
            this.sendPacket("OUT", (Packet<?>)msg);
        }

        super.write(ctx, msg, promise);
    }

    private void sendPacket(String direction, Packet<?> packet) {
        try {
            String packetName = packet.getClass().getSimpleName();

            try {
                 packetName = packet.getPacketType().id().toString();

                if (packetName.startsWith("minecraft:")) {
                    packetName = packetName.substring(10);
                }
            } catch (Throwable var14) {
                // Ignore errors determining packet name from type
            }

            if (PacketSpyConfig.silencedPackets.contains(packetName) || PacketSpyConfig.silencedPackets.contains(packet.getClass().getSimpleName()) || PacketSpyConfig.silencedPackets.contains(packet.getClass().getName())) {
                return;
            }

            if (!PacketSpyConfig.logPlayerMovementPackets) {
                if (packetName.contains("PlayerMove") || packetName.contains("move_player")) {
                    return;
                }
            }

            JsonObject json = new JsonObject();
            json.addProperty("type", "packet");
            json.addProperty("direction", direction);
            json.addProperty("name", packetName);
            json.addProperty("class", deobfuscateClassName(packet.getClass().getName()));

             // Use reflection to get field data instead of just toString()
            try {
                // Use the robust string representation requested
                 json.add("data", getPacketData(packet));
            } catch (Exception e) {
                json.addProperty("data", "Error serializing: " + e.getMessage());
            }

            PacketspyClient.broadcast(GSON.toJson(json));
        } catch (Throwable e) {
             String errorMsg = "PacketSpy Error serializing packet: " + e.toString();
             PacketspyClient.LOGGER.error(errorMsg);
             PacketspyClient.broadcast("{\"error\": \"" + errorMsg.replace("\"", "'") + "\"}");
        }
    }

    private JsonObject getPacketData(Packet<?> packet) {
        // Explicit handling for common packets to avoid obfuscated field names
        if (packet instanceof ClickSlotC2SPacket) return getClickSlotData((ClickSlotC2SPacket) packet);
        if (packet instanceof PlayerMoveC2SPacket) return getPlayerMoveData((PlayerMoveC2SPacket) packet);
        if (packet instanceof UpdateSelectedSlotC2SPacket) return getUpdateSelectedSlotData((UpdateSelectedSlotC2SPacket) packet);
        if (packet instanceof PlayerInteractBlockC2SPacket) return getPlayerInteractBlockData((PlayerInteractBlockC2SPacket) packet);
        if (packet instanceof PlayerInteractEntityC2SPacket) return getPlayerInteractEntityData((PlayerInteractEntityC2SPacket) packet);
        if (packet instanceof PlayerInteractItemC2SPacket) return getPlayerInteractItemData((PlayerInteractItemC2SPacket) packet);
        if (packet instanceof PlayerActionC2SPacket) return getPlayerActionData((PlayerActionC2SPacket) packet);
        if (packet instanceof UpdateSignC2SPacket) return getUpdateSignData((UpdateSignC2SPacket) packet);

        JsonObject json = new JsonObject();
        Class<?> clazz = packet.getClass();

        // Loop through class hierarchy
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                // Skip statics
                if (Modifier.isStatic(field.getModifiers())) continue;

                try {
                    // Force access to private fields (the "field_xxxx" ones)
                    field.setAccessible(true);

                    // Get the value
                    Object value = field.get(packet);
                    String fieldName = deobfuscateFieldName(clazz, field.getName(), field.getType());
                    String key = fieldName;

                    // Handle duplicate keys from hierarchy
                    if (json.has(key)) {
                        key = fieldName + " (" + clazz.getSimpleName() + ")";
                    }

                    // Add field type info to the key for clarity?
                    // User asked "where is click slot", seeing "slot (int)" helps.
                    // But changing key changes contract.
                    // Let's keep key as name, but maybe value format?
                    // "36 (int)"?
                    // Actually, JSON types help. But field_123 implies nothing.
                    // Let's construct a cleaner object.

                    if (value == null) {
                        json.add(key, com.google.gson.JsonNull.INSTANCE);
                    } else if (value instanceof Number) {
                        // Special handling for cursor slot / outside inventory clicks
                        if (value instanceof Integer && ((Integer)value) == -999) {
                            json.addProperty(key, "-999 (Outside Inventory)");
                        } else {
                            json.addProperty(key, (Number) value);
                        }
                    } else if (value instanceof Boolean) {
                        json.addProperty(key, (Boolean) value);
                    } else if (value instanceof String) {
                        json.addProperty(key, (String) value);
                    } else if (value instanceof java.util.Map) {
                        // Maps (often Int2ObjectMap etc) needs special handling or just stringify
                        json.addProperty(key, String.valueOf(value));
                    } else if (value instanceof java.util.Collection) {
                        json.addProperty(key, String.valueOf(value));
                    } else {
                         // Default to string representation for complex types
                         try {
                            json.addProperty(key, String.valueOf(value));
                         } catch (Throwable t) {
                            json.addProperty(key, "<Error calling toString(): " + t.getClass().getSimpleName() + ">");
                         }
                    }

                } catch (Exception e) {
                    json.addProperty(field.getName() + "_error", e.toString());
                }
            }
            clazz = clazz.getSuperclass();
        }

        return json;
    }

    private JsonObject getClickSlotData(ClickSlotC2SPacket p) {
        JsonObject json = new JsonObject();
        json.addProperty("syncId", p.getSyncId());
        json.addProperty("revision", p.getRevision());
        json.addProperty("slot", p.getSlot());
        json.addProperty("button", p.getButton());
        json.addProperty("actionType", String.valueOf(p.getActionType()));
        json.addProperty("stack", String.valueOf(p.getStack()));
        json.addProperty("modifiedStacks", String.valueOf(p.getModifiedStacks()));
        return json;
    }

    private JsonObject getPlayerMoveData(PlayerMoveC2SPacket p) {
        JsonObject json = new JsonObject();
        json.addProperty("onGround", p.isOnGround());
        if (p.changesPosition()) {
            json.addProperty("x", p.getX(0));
            json.addProperty("y", p.getY(0));
            json.addProperty("z", p.getZ(0));
        }
        if (p.changesLook()) {
             json.addProperty("yaw", p.getYaw(0));
             json.addProperty("pitch", p.getPitch(0));
        }
        return json;
    }

    private JsonObject getUpdateSelectedSlotData(UpdateSelectedSlotC2SPacket p) {
        JsonObject json = new JsonObject();
        json.addProperty("selectedSlot", p.getSelectedSlot());
        return json;
    }

    private JsonObject getPlayerInteractBlockData(PlayerInteractBlockC2SPacket p) {
        JsonObject json = new JsonObject();
        json.addProperty("hand", String.valueOf(p.getHand()));
        json.add("blockHitResult", GSON.toJsonTree(p.getBlockHitResult())); // Uses toString or internal structure
        try {
            json.addProperty("sequence", p.getSequence());
        } catch (Throwable t) {
            // Ignore if method missing (older versions)
        }
        return json;
    }

    private JsonObject getPlayerInteractEntityData(PlayerInteractEntityC2SPacket p) {
        JsonObject json = new JsonObject();
        // Use reflection to find fields by type to be robust against obfuscation
        for (Field f : p.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers())) continue;
            f.setAccessible(true);
            try {
                if (f.getType() == int.class) {
                    json.addProperty("entityId", f.getInt(p));
                } else if (f.getType() == boolean.class) {
                    json.addProperty("sneaking", f.getBoolean(p));
                } else {
                    // Assume it's the type/action handler
                    Object val = f.get(p);
                    json.addProperty("type", String.valueOf(val));
                }
            } catch (Exception e) {}
        }
        return json;
    }

    private JsonObject getPlayerInteractItemData(PlayerInteractItemC2SPacket p) {
        JsonObject json = new JsonObject();
        json.addProperty("hand", String.valueOf(p.getHand()));
        try {
             json.addProperty("sequence", p.getSequence());
        } catch (Throwable t) {}
        return json;
    }

    private JsonObject getPlayerActionData(PlayerActionC2SPacket p) {
        JsonObject json = new JsonObject();
        json.addProperty("action", String.valueOf(p.getAction()));
        json.addProperty("pos", String.valueOf(p.getPos()));
        json.addProperty("direction", String.valueOf(p.getDirection()));
        try {
             json.addProperty("sequence", p.getSequence());
        } catch (Throwable t) {}
        return json;
    }

    private JsonObject getUpdateSignData(UpdateSignC2SPacket p) {
        JsonObject json = new JsonObject();
        json.addProperty("pos", String.valueOf(p.getPos()));
        json.addProperty("isFront", p.isFront());
        json.addProperty("text", String.valueOf(p.getText()));
        return json;
    }

}
