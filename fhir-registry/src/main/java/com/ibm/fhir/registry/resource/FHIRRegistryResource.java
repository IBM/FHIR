/*
 * (C) Copyright IBM Corp. 2019, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.registry.resource;

import java.util.Objects;

import com.ibm.fhir.model.resource.Resource;

/**
 * An abstract base class that contains the metadata for a definitional resource (e.g. StructureDefinition)
 */
public abstract class FHIRRegistryResource implements Comparable<FHIRRegistryResource> {
    protected final Class<? extends Resource> resourceType;
    protected final String id;
    protected final String url;
    protected final Version version;
    protected final String kind;
    protected final String type;

    public FHIRRegistryResource(
            Class<? extends Resource> resourceType,
            String id,
            String url,
            Version version,
            String kind,
            String type) {
        this.resourceType = Objects.requireNonNull(resourceType);
        this.id = id;
        this.url = Objects.requireNonNull(url);
        this.version = Objects.requireNonNull(version);
        this.kind = kind;
        this.type = type;
    }

    public Class<? extends Resource> getResourceType() {
        return resourceType;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public Version getVersion() {
        return version;
    }

    public String getKind() {
        return kind;
    }

    public String getType() {
        return type;
    }

    public abstract Resource getResource();

    public <T extends FHIRRegistryResource> boolean is(Class<T> registryResourceType) {
        return registryResourceType.isInstance(this);
    }

    public <T extends FHIRRegistryResource> T as(Class<T> registryResourceType) {
        return registryResourceType.cast(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FHIRRegistryResource other = (FHIRRegistryResource) obj;
        return Objects.equals(url, other.url) && Objects.equals(version, other.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, version);
    }

    @Override
    public int compareTo(FHIRRegistryResource other) {
        int result = url.compareTo(other.url);
        if (result == 0) {
            return version.compareTo(other.version);
        }
        return result;
    }

    /**
     * Represents a version that can either be lexical or follow the Semantic Versioning format
     */
    public static class Version implements Comparable<Version> {
        public enum CompareMode { SEMVER, LEXICAL };

        private final String version;
        private final Integer major;
        private final Integer minor;
        private final Integer patch;
        private final CompareMode mode;

        private Version(String version) {
            this.version = version;
            major = minor = patch = null;
            mode = CompareMode.LEXICAL;
        }

        private Version(String version, Integer major, Integer minor, Integer patch) {
            this.version = version;
            this.major = major;
            this.minor = minor;
            this.patch = patch;
            this.mode = CompareMode.SEMVER;
        }

        public int major() {
            return major;
        }

        public int minor() {
            return minor;
        }

        public int patch() {
            return patch;
        }

        public static Version from(String version) {
            String[] tokens = version.split("\\.");
            if (tokens.length < 1 || tokens.length > 3) {
                return new Version(version);
            }
            try {
                Integer major = Integer.parseInt(tokens[0]);
                Integer minor = (tokens.length >= 2) ? Integer.parseInt(tokens[1]) : 0;
                Integer patch = (tokens.length == 3) ? Integer.parseInt(tokens[2]) : 0;
                return new Version(version, major, minor, patch);
            } catch (Exception e) {
                return new Version(version);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Version other = (Version) obj;
            if (CompareMode.LEXICAL.equals(mode) || CompareMode.LEXICAL.equals(other.mode)) {
                return Objects.equals(version, other.version);
            } else {
                return Objects.equals(major, other.major) && Objects.equals(minor, other.minor) && Objects.equals(patch, other.patch);
            }
        }

        @Override
        public int hashCode() {
            if (CompareMode.LEXICAL.equals(mode)) {
                return Objects.hash(version);
            } else {
                return Objects.hash(major, minor, patch);
            }
        }

        @Override
        public String toString() {
            return version;
        }

        @Override
        public int compareTo(Version version) {
            if (CompareMode.LEXICAL.equals(mode) || CompareMode.LEXICAL.equals(version.mode)) {
                return this.version.compareTo(version.version);
            } else {
                int result = major.compareTo(version.major);
                if (result == 0) {
                    result = minor.compareTo(version.minor);
                    if (result == 0) {
                        return patch.compareTo(version.patch);
                    }
                    return result;
                }
                return result;
            }
        }
    }
}