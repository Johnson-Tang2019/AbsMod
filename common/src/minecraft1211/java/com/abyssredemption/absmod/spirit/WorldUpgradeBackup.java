package com.abyssredemption.absmod.spirit;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.*;
import java.util.*;
import java.util.zip.*;

/** A complete sibling archive. Only a closed, reread archive is given a .zip name. */
final class WorldUpgradeBackup {
    private WorldUpgradeBackup() {}

    static Path create(Path world) throws IOException {
        Path directory = world.resolveSibling("absmod-upgrade-backups");
        if (directory.startsWith(world)) throw new IOException("Backup destination inside world: " + world);
        Files.createDirectories(directory);
        if (directory.toRealPath().startsWith(world.toRealPath())) throw new IOException("Backup destination inside world");
        Path scratch = Files.createTempFile(directory, "upgrade-", ".pending");
        try {
            Map<String, byte[]> hashes = new HashMap<>();
            try (var zip = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(scratch)))) {
                Files.walkFileTree(world, new SimpleFileVisitor<>() {
                    @Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attributes) throws IOException {
                        String name = world.getFileName() + "/" + world.relativize(dir).toString().replace(File.separatorChar, '/');
                        if (!name.endsWith("/")) name += "/";
                        zip.putNextEntry(new ZipEntry(name));
                        zip.closeEntry();
                        hashes.put(name, sha256().digest());
                        return FileVisitResult.CONTINUE;
                    }
                    @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                        if (file.equals(world.resolve("session.lock"))) return FileVisitResult.CONTINUE;
                        if (!attributes.isRegularFile()) throw new IOException("Cannot archive special file: " + file);
                        String name = world.getFileName() + "/" + world.relativize(file).toString().replace(File.separatorChar, '/');
                        var entry = new ZipEntry(name);
                        entry.setLastModifiedTime(attributes.lastModifiedTime());
                        zip.putNextEntry(entry);
                        MessageDigest hash = sha256();
                        try (var input = new DigestInputStream(Files.newInputStream(file), hash)) {
                            input.transferTo(zip);
                        }
                        zip.closeEntry();
                        hashes.put(name, hash.digest());
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
            try (var channel = FileChannel.open(scratch, StandardOpenOption.WRITE)) { channel.force(true); }
            try (var zip = new ZipFile(scratch.toFile())) {
                if (zip.size() != hashes.size()) throw new IOException("Incomplete world backup");
                for (var expected : hashes.entrySet()) {
                    var entry = zip.getEntry(expected.getKey());
                    if (entry == null) throw new IOException("Missing backup entry: " + expected.getKey());
                    var digest = sha256();
                    try (var stream = new DigestInputStream(zip.getInputStream(entry), digest)) {
                        stream.transferTo(OutputStream.nullOutputStream());
                    }
                    if (!MessageDigest.isEqual(expected.getValue(), digest.digest())) {
                        throw new IOException("Backup verification failed: " + expected.getKey());
                    }
                }
            }
            Path target = directory.resolve(world.getFileName() + "-" + UUID.randomUUID() + ".zip");
            return Files.move(scratch, target, StandardCopyOption.ATOMIC_MOVE);
        } finally {
            Files.deleteIfExists(scratch);
        }
    }

    private static MessageDigest sha256() {
        try { return MessageDigest.getInstance("SHA-256"); }
        catch (NoSuchAlgorithmException ex) { throw new AssertionError(ex); }
    }
}
