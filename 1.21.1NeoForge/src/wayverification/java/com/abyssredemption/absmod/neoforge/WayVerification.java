package com.abyssredemption.absmod.neoforge;

import com.abyssredemption.absmod.cat.CatLife;
import com.abyssredemption.absmod.spirit.*;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = "absmod", dist = Dist.CLIENT)
public final class WayVerification {
    private boolean opening;
    private final boolean readback = Boolean.getBoolean("absmod.wayReadback");
    private int ticks, index;
    private ItemStack original;
    private final List<String> items = new ArrayList<>();
    private final String world = System.getProperty("absmod.wayWorld");

    public WayVerification() {
        require(world.matches("absmod-upgrade-test-[0-9]+"), "disposable copy required");
        for (String path : List.of("blossom_bow", "blossom_staff", "dark_blossom_bow", "dark_blossom_staff")) {
            for (int n = 1; n <= (path.startsWith("dark") ? 8 : 9); n++) items.add(path + (n == 1 ? "" : "_stage_" + n));
        }
        items.add("cherry_blossom_lantern");
        NeoForge.EVENT_BUS.addListener(this::tick);
    }

    private void tick(ClientTickEvent.Post event) {
        var mc = Minecraft.getInstance();
        if (!opening && mc.screen instanceof TitleScreen) {
            opening = true;
            mc.options.pauseOnLostFocus = false;
            mc.createWorldOpenFlows().openWorld(world, () -> { throw new AssertionError("Missing old world copy"); });
        }
        var server = mc.getSingleplayerServer();
        if (mc.player == null || server == null) return;
        int t = ++ticks;
        require(t <= 650, "quest completion timeout");
        if (t < 40) return;
        if (t == 40 && !readback) BlossomModelVerification.verify(mc);
        server.submit(() -> {
            ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
            var ftb = ServerQuestFile.INSTANCE;
            if (t == 40) {
                var store = BladeSoulStore.forInitialization(server);
                var inventory = player.getInventory();
                int count = 0;
                for (int i = 0; i < inventory.getContainerSize(); i++) {
                    var sword = inventory.getItem(i);
                    if (sword.is(BuiltInRegistries.ITEM.get(id("meow_blade_stage_9")))) {
                        var record = BladeSoulService.requireRecord(store, sword);
                        require(record.mirrorCleared() && record.affection().orElseThrow() == 250 &&
                                sword.getHoverName().getString().equals("Moonlit original") &&
                                ItemStack.matches(sword, originalNinthBlade(player)) &&
                                sword.getEnchantments().getLevel(player.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                                        .getOrThrow(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING)) == 3, "legacy bound sword");
                        count++;
                    }
                }
                require(count == 1 && !BladeSoulService.requireRecord(store, inventory.getItem(8)).mirrorCleared(), "distinct VIII and IX");
                var life = CatLife.store(player);
                for (int x : List.of(4, 6)) {
                    var home = life.at(GlobalPos.of(player.level().dimension(), new BlockPos(x, 90, 0)));
                    require(home != null && life.visit(player.getUUID(), home.cat().orElseThrow()).spot().equals(home.spot()), "legacy cat home");
                }
                var team = ftb.getTeamData(player).orElseThrow();
                for (long quest : new long[]{0x1D4D2E8F48E7AA03L, 0x1846776214F76AB8L, 0x79F2310DDDF5539AL,
                        0x0CE33A72B13EAAD6L, 0x19EF437F13F31165L, 0x4ACE82CA4A3C572BL}) {
                    require(team.isCompleted(ftb.getQuest(quest)), "retained FTB progress");
                }
                require(ftb.getChapter(0x1C4E810187B7DDC2L).getQuests().size() == 111, "111 FTB entries");
                if (readback) {
                    for (String name : items) require(team.isCompleted(ftb.getQuest(questId(name))), "saved quest " + name);
                    checkCache(player, false);
                    index = items.size() + 1;
                    System.out.println("UPGRADE_WORLD_REOPEN_PASS");
                    return;
                }
                try { UpgradeFailureVerification.verify(player); }
                catch (java.io.IOException ex) { throw new RuntimeException(ex); }
                System.out.println("UPGRADE_LEGACY_GAME_PASS");
                BlossomWeaponVerification.verify(player);
            } else if (t == 70) {
                BlossomWeaponVerification.verifyImpact(player);
                original = player.getInventory().getItem(5).copy();
            } else if (t > 70 && index < items.size()) {
                String name = items.get(index++);
                player.getInventory().setItem(5, new ItemStack(BuiltInRegistries.ITEM.get(id(name))));
                player.inventoryMenu.broadcastChanges();
            } else if (index == items.size() && t > 125) {
                for (String name : items) {
                    var a = server.getAdvancements().get(id("progression/" + name));
                    require(player.getAdvancements().getOrStartProgress(a).isDone(), name);
                    long q = questId(name);
                    if (!ftb.getTeamData(player).orElseThrow().isCompleted(ftb.getQuest(q))) return;
                }
                player.getInventory().setItem(5, original);
                checkCache(player, true);
                index++;
                System.out.println("BLOSSOM_35_FTB_GAME_PASS");
            }
        }).join();
        if (index > items.size()) mc.stop();
    }

    private ItemStack originalNinthBlade(ServerPlayer player) {
        var dir = player.server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
                .toAbsolutePath().normalize().resolveSibling("absmod-upgrade-backups");
        try (var archives = java.nio.file.Files.newDirectoryStream(dir, world + "-*.zip")) {
            var iterator = archives.iterator();
            var file = iterator.next();
            require(!iterator.hasNext(), "one backup for both launches");
            try (var zip = new java.util.zip.ZipFile(file.toFile());
                    var in = zip.getInputStream(zip.getEntry(world + "/level.dat"))) {
                var root = net.minecraft.nbt.NbtIo.readCompressed(in, net.minecraft.nbt.NbtAccounter.unlimitedHeap());
                var contents = root.getCompound("Data").getCompound("Player")
                        .getList("Inventory", net.minecraft.nbt.Tag.TAG_COMPOUND);
                for (int i = 0; i < contents.size(); i++) {
                    var saved = contents.getCompound(i);
                    if (saved.getString("id").equals("absmod:meow_blade_stage_9"))
                        return ItemStack.parseOptional(player.registryAccess(), saved);
                }
                throw new AssertionError("Original IX blade missing in legacy snapshot");
            }
        } catch (java.io.IOException ex) { throw new IllegalStateException(ex); }
    }

    private void checkCache(ServerPlayer player, boolean write) {
        var level = player.serverLevel();
        for (int i = 0; i < items.size(); i++) {
            var pos = new BlockPos(4 + 2 * (i / 27), 110, 0);
            if (write && i % 27 == 0) {
                require(level.isEmptyBlock(pos), "unused fixture space");
                level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.BARREL.defaultBlockState());
            }
            var box = (net.minecraft.world.Container)level.getBlockEntity(pos);
            var item = BuiltInRegistries.ITEM.get(id(items.get(i)));
            if (write) {
                var stack = new ItemStack(item);
                if (stack.isDamageableItem()) stack.setDamageValue(17);
                box.setItem(i % 27, stack);
                box.setChanged();
            } else {
                var saved = box.getItem(i % 27);
                require(saved.is(item) && saved.getCount() == 1 && (!saved.isDamageableItem() || saved.getDamageValue() == 17),
                        "saved new equipment: " + items.get(i));
            }
        }
    }

    private static long questId(String name) {
        try {
            byte[] hash = java.security.MessageDigest.getInstance("SHA-256").digest(("absmod/ways/" + name).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return Long.parseLong("3" + java.util.HexFormat.of().formatHex(hash).substring(0, 15), 16);
        } catch (java.security.NoSuchAlgorithmException ex) { throw new AssertionError(ex); }
    }

    private static ResourceLocation id(String name) { return ResourceLocation.fromNamespaceAndPath("absmod", name); }
    private static void require(boolean ok, String what) { if (!ok) throw new AssertionError(what); }
}
