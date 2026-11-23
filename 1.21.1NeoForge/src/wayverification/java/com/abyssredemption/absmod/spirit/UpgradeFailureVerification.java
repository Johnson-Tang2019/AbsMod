package com.abyssredemption.absmod.spirit;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import net.minecraft.server.level.ServerPlayer;

/** Isolated failures and retries using the same registry-aware path as world startup. */
public final class UpgradeFailureVerification {
    private UpgradeFailureVerification() {}

    public static void verify(ServerPlayer player) throws IOException {
        Path base = Files.createTempDirectory(Path.of(".").toAbsolutePath(), "upgrade-verification-");
        Path world = Files.createDirectory(base.resolve("fixture"));
        Path data = Files.createDirectory(world.resolve("data"));
        Path stamp = data.resolve("absmod_save_version.txt");
        Path cats = data.resolve("absmod_cat_life.dat");
        byte[] broken = {4, 1, 8};
        Files.write(cats, broken);
        expectRefusal(world, player);
        if (Files.exists(stamp) || !Arrays.equals(broken, Files.readAllBytes(cats))) throw new AssertionError("Malformed ledger overwritten");
        Files.delete(cats);
        Files.writeString(stamp, "200\n99.0.0\n");
        expectRefusal(world, player);
        if (!Files.readString(stamp).equals("200\n99.0.0\n")) throw new AssertionError("Newer marker overwritten");
        Files.writeString(stamp, "1\n0.6.0+1.21.1\n");
        prepare(world, player);
        Path backups = base.resolve("absmod-upgrade-backups");
        long count;
        try (var files = Files.list(backups)) { count = files.count(); }
        prepare(world, player);
        try (var files = Files.list(backups)) {
            if (count != 2 || files.count() != count) throw new AssertionError("Unexpected backup count");
        }
        System.out.println("UPGRADE_REFUSAL_RETRY_PASS");
    }

    private static void prepare(Path world, ServerPlayer player) {
        String release = "0.7.0+1.21.1";
        if (WorldUpgrade.snapshotIfNeeded(world, release)) WorldUpgrade.finish(world, player.registryAccess(), release);
    }

    private static void expectRefusal(Path world, ServerPlayer player) {
        try { prepare(world, player); }
        catch (IllegalStateException expected) { return; }
        throw new AssertionError("Unsafe save accepted");
    }
}
