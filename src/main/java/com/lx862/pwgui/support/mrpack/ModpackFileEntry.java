package com.lx862.pwgui.support.mrpack;

import com.google.gson.annotations.SerializedName;

import java.net.URI;
import java.util.Arrays;
import java.util.Objects;

public class ModpackFileEntry {
    /**
     * See https://support.modrinth.com/en/articles/8802351-modrinth-modpack-format-mrpack
     */
    public static final String[] SAFE_DOWNLOAD_DOMAIN = {
            "cdn.modrinth.com",
            "github.com",
            "raw.githubusercontent.com",
            "gitlab.com"
    };

    public String path;
    public Hashes hashes;
    public Environment env;
    public URI[] downloads;
    public int fileSize;

    public void validate() {
        if(hashes == null || hashes.sha1 == null || hashes.sha512 == null) throw new IllegalArgumentException("Incomplete hash file entry!");
        if(hashes.sha1.length() != 40) throw new IllegalArgumentException("SHA1 hash must be 40 digit long!");
        if(hashes.sha512.length() != 128) throw new IllegalArgumentException("SHA512 hash must be 128 digit long!");
        if(path.contains("..") || path.matches("^([A-Z]:/|[A-Z]:\\\\|\\\\|/).*")) throw new IllegalArgumentException("Invalid path specified!");
        if(Arrays.stream(downloads).anyMatch(e -> !Objects.equals("https", e.getScheme()))) throw new IllegalArgumentException("Non HTTPS url are not allowed!");
        if(fileSize < 0) throw  new IllegalArgumentException("fileSize must not be less than 0!");
    }

    public static class Hashes {
        public String sha1;
        public String sha512;
    }

    public static class Environment {
        public SupportType client;
        public SupportType server;

        public enum SupportType {
            @SerializedName("required")
            REQUIRED,
            @SerializedName("optional")
            OPTIONAL,
            @SerializedName("unsupported")
            UNSUPPORTED
        }
    }

    public boolean safeDownloadDomain() {
        return Arrays.stream(downloads).allMatch(domain -> Arrays.stream(SAFE_DOWNLOAD_DOMAIN).anyMatch(domain.toString()::contains));
    }
}
