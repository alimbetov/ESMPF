package com.esmpf.storage.domain;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HexFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class LocalFileStorageAdapter implements FileStoragePort {
    private final Path root;
    private final Path temp;

    LocalFileStorageAdapter(
            @Value("${esmpf.storage.local.root:./var/storage}") String root,
            @Value("${esmpf.storage.local.temp-directory:./var/storage/.tmp}") String temp
    ) throws IOException {
        this.root = Path.of(root).toAbsolutePath().normalize();
        this.temp = Path.of(temp).toAbsolutePath().normalize();
        Files.createDirectories(this.root);
        Files.createDirectories(this.temp);
        if (!this.temp.startsWith(this.root)) {
            throw new IllegalStateException("Storage temp directory must be inside storage root");
        }
    }

    @Override
    public StorageWriteResult store(StorageWriteRequest request) throws IOException {
        String key = key(request);
        Path target = resolve(key);
        Files.createDirectories(target.getParent());
        Path temporary = Files.createTempFile(temp, request.fileId() + "-", ".upload");
        MessageDigest digest = sha256();
        long written = 0;
        try (InputStream source = new BufferedInputStream(request.content());
             DigestInputStream digested = new DigestInputStream(source, digest);
             OutputStream output = new BufferedOutputStream(Files.newOutputStream(temporary, StandardOpenOption.WRITE))) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = digested.read(buffer)) >= 0) {
                written += read;
                if (written > request.maximumBytes()) throw new FileTooLargeException(request.maximumBytes());
                output.write(buffer, 0, read);
            }
        } catch (RuntimeException | IOException failure) {
            Files.deleteIfExists(temporary);
            throw failure;
        }
        try {
            Files.move(temporary, target, ATOMIC_MOVE, REPLACE_EXISTING);
        } catch (IOException atomicMoveUnsupported) {
            Files.move(temporary, target, REPLACE_EXISTING);
        }
        return new StorageWriteResult(key, written, HexFormat.of().formatHex(digest.digest()));
    }

    @Override public StoredContent open(String storageKey) throws IOException {
        Path path = resolve(storageKey);
        return new StoredContent(Files.newInputStream(path), Files.size(path));
    }

    @Override public void delete(String storageKey) throws IOException { Files.deleteIfExists(resolve(storageKey)); }
    @Override public boolean exists(String storageKey) { return Files.isRegularFile(resolve(storageKey)); }

    private String key(StorageWriteRequest request) {
        LocalDate now = LocalDate.now(ZoneOffset.UTC);
        return "tenant/%s/%04d/%02d/%02d/%s".formatted(
                request.businessId(), now.getYear(), now.getMonthValue(), now.getDayOfMonth(), request.fileId());
    }

    private Path resolve(String key) {
        Path path = root.resolve(key).normalize();
        if (!path.startsWith(root)) throw new IllegalArgumentException("Invalid storage key");
        return path;
    }

    private static MessageDigest sha256() {
        try { return MessageDigest.getInstance("SHA-256"); }
        catch (NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
    }
}

final class FileTooLargeException extends IllegalArgumentException {
    FileTooLargeException(long maximumBytes) { super("File exceeds maximum size of " + maximumBytes + " bytes"); }
}
