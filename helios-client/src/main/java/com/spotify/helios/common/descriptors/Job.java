/*
 * Copyright (c) 2014 Spotify AB.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.helios.common.descriptors;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.io.BaseEncoding;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.spotify.helios.common.Json;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;
import static com.spotify.helios.common.Hash.sha1digest;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

/**
 * Represents a Helios job.
 *
 * An sample expression of it in JSON might be:
 * <pre>
 * {
 *   "command" : [ "server", "serverconfig.yaml" ],
 *   "env" : {
 *     "JVM_ARGS" : "-Ddw.feature.randomFeatureFlagEnabled=true"
 *   },
 *   "expires" : null,
 *   "id" : "myservice:0.5:3539b7bc2235d53f79e6e8511942bbeaa8816265",
 *   "image" : "myregistry:80/janedoe/myservice:0.5-98c6ff4",
 *   "ports" : {
 *     "http" : {
 *       "externalPort" : 8060,
 *       "internalPort" : 8080,
 *       "protocol" : "tcp"
 *     },
 *     "http-admin" : {
 *       "externalPort" : 8061,
 *       "internalPort" : 8081,
 *       "protocol" : "tcp"
 *     }
 *   },
 *   "registration" : {
 *     "service/http" : {
 *       "ports" : {
 *         "http" : { }
 *       }
 *     }
 *   },
 *   "registrationDomain" : "",
 *   "volumes" : {
 *     "/path/to/mysecretconfig.yaml:ro:ro" : "/path/to/mysecretconfig.yaml"
 *   }
 * }
 * </pre>
 */
public class Job extends Descriptor implements Comparable<Job> {

  public static final Map<String, String> EMPTY_ENV = emptyMap();
  public static final Map<String, PortMapping> EMPTY_PORTS = emptyMap();
  public static final List<String> EMPTY_COMMAND = emptyList();
  public static final Map<ServiceEndpoint, ServicePorts> EMPTY_REGISTRATION = emptyMap();
  public static final Integer EMPTY_GRACE_PERIOD = null;
  public static final Map<String, String> EMPTY_VOLUMES = emptyMap();
  public static final String EMPTY_MOUNT = "";
  public static final Date EMPTY_EXPIRES = null;
  public static final String EMPTY_REGISTRATION_DOMAIN = "";
  public static final String EMPTY_CREATING_USER = null;

  private final JobId id;
  private final String image;
  private final List<String> command;
  private final Map<String, String> env;
  private final Map<String, PortMapping> ports;
  private final Map<ServiceEndpoint, ServicePorts> registration;
  private final Integer gracePeriod;
  private final Map<String, String> volumes;
  private final Date expires;
  private final String registrationDomain;
  private final String creatingUser;

  /**
   * Create a Job.
   *
   * @param id The id if the job.
   * @param image The docker image to use.
   * @param command The command to pass to the container.
   * @param env Environment variables to set
   * @param ports The ports you wish to expose from the container.
   * @param registration Configuration information for the discovery service (if applicable)
   * @param gracePeriod How long to let the container run after deregistering with the discovery
   *    service.  If nothing is configured in registration, this option is ignored.
   * @param volumes Docker volumes to mount.
   * @param expires If set, a timestamp at which the job and any deployments will be removed.
   * @param registrationDomain If set, override the default domain in which discovery service
   *    registration occurs.  What is allowed here will vary based upon the discovery service
   *    plugin used.
   */
  public Job(@JsonProperty("id") final JobId id,
             @JsonProperty("image") final String image,
             @JsonProperty("command") @Nullable final List<String> command,
             @JsonProperty("env") @Nullable final Map<String, String> env,
             @JsonProperty("ports") @Nullable final Map<String, PortMapping> ports,
             @JsonProperty("registration") @Nullable
                 final Map<ServiceEndpoint, ServicePorts> registration,
             @JsonProperty("gracePeriod") @Nullable final Integer gracePeriod,
             @JsonProperty("volumes") @Nullable final Map<String, String> volumes,
             @JsonProperty("expires") @Nullable final Date expires,
             @JsonProperty("registrationDomain") @Nullable String registrationDomain,
             @JsonProperty("creatingUser") @Nullable String creatingUser) {
    this.id = id;
    this.image = image;

    // Optional
    this.command = Optional.fromNullable(command).or(EMPTY_COMMAND);
    this.env = Optional.fromNullable(env).or(EMPTY_ENV);
    this.ports = Optional.fromNullable(ports).or(EMPTY_PORTS);
    this.registration = Optional.fromNullable(registration).or(EMPTY_REGISTRATION);
    this.gracePeriod = Optional.fromNullable(gracePeriod).orNull();
    this.volumes = Optional.fromNullable(volumes).or(EMPTY_VOLUMES);
    this.expires = expires;
    this.registrationDomain = Optional.fromNullable(registrationDomain)
        .or(EMPTY_REGISTRATION_DOMAIN);
    this.creatingUser = Optional.fromNullable(creatingUser).orNull();
  }

  private Job(final JobId id, final Builder.Parameters p) {
    this.id = id;
    this.image = p.image;
    this.command = ImmutableList.copyOf(checkNotNull(p.command, "command"));
    this.env = ImmutableMap.copyOf(checkNotNull(p.env, "env"));
    this.ports = ImmutableMap.copyOf(checkNotNull(p.ports, "ports"));
    this.registration = ImmutableMap.copyOf(checkNotNull(p.registration, "registration"));
    this.gracePeriod = p.gracePeriod;
    this.volumes = ImmutableMap.copyOf(checkNotNull(p.volumes, "volumes"));
    this.expires = p.expires;
    this.registrationDomain = Optional.fromNullable(p.registrationDomain)
        .or(EMPTY_REGISTRATION_DOMAIN);
    this.creatingUser = p.creatingUser;
  }

  public JobId getId() {
    return id;
  }

  public String getImage() {
    return image;
  }

  public List<String> getCommand() {
    return command;
  }

  public Map<String, String> getEnv() {
    return env;
  }

  public Map<String, PortMapping> getPorts() {
    return ports;
  }

  public Map<ServiceEndpoint, ServicePorts> getRegistration() {
    return registration;
  }

  public String getRegistrationDomain() {
    return registrationDomain;
  }

  public Integer getGracePeriod() {
    return gracePeriod;
  }

  public Map<String, String> getVolumes() {
    return volumes;
  }

  public Date getExpires() {
    return expires;
  }

  public String getCreatingUser() {
    return creatingUser;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  @Override
  public int compareTo(final Job o) {
    return id.compareTo(o.getId());
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final Job job = (Job) o;

    if (command != null ? !command.equals(job.command) : job.command != null) {
      return false;
    }
    if (env != null ? !env.equals(job.env) : job.env != null) {
      return false;
    }
    if (expires != null ? !expires.equals(job.expires) : job.expires != null) {
      return false;
    }
    if (id != null ? !id.equals(job.id) : job.id != null) {
      return false;
    }
    if (image != null ? !image.equals(job.image) : job.image != null) {
      return false;
    }
    if (ports != null ? !ports.equals(job.ports) : job.ports != null) {
      return false;
    }
    if (registration != null ? !registration.equals(job.registration) : job.registration != null) {
      return false;
    }
    if (registrationDomain != null
        ? !registrationDomain.equals(job.registrationDomain)
        : job.registrationDomain != null) {
      return false;
    }
    if (gracePeriod != null ? !gracePeriod.equals(job.gracePeriod) : job.gracePeriod != null) {
      return false;
    }
    if (volumes != null ? !volumes.equals(job.volumes) : job.volumes != null) {
      return false;
    }
    if (creatingUser != null ? !creatingUser.equals(job.creatingUser) : job.creatingUser != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (image != null ? image.hashCode() : 0);
    result = 31 * result + (expires != null ? expires.hashCode() : 0);
    result = 31 * result + (command != null ? command.hashCode() : 0);
    result = 31 * result + (env != null ? env.hashCode() : 0);
    result = 31 * result + (ports != null ? ports.hashCode() : 0);
    result = 31 * result + (registration != null ? registration.hashCode() : 0);
    result = 31 * result + (registrationDomain != null ? registrationDomain.hashCode() : 0);
    result = 31 * result + (gracePeriod != null ? gracePeriod.hashCode() : 0);
    result = 31 * result + (volumes != null ? volumes.hashCode() : 0);
    result = 31 * result + (creatingUser != null ? creatingUser.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("id", id)
        .add("image", image)
        .add("command", command)
        .add("env", env)
        .add("ports", ports)
        .add("registration", registration)
        .add("gracePeriod", gracePeriod)
        .add("expires", expires)
        .add("registrationDomain", registrationDomain)
        .add("creatingUser", creatingUser)
        .toString();
  }

  public Builder toBuilder() {
    final Builder builder = newBuilder();

    if (id != null) {
      builder.setName(id.getName())
          .setVersion(id.getVersion());
    }

    return builder.setImage(image)
        .setCommand(command)
        .setEnv(env)
        .setPorts(ports)
        .setRegistration(registration)
        .setGracePeriod(gracePeriod)
        .setVolumes(volumes)
        .setExpires(expires)
        .setRegistrationDomain(registrationDomain)
        .setCreatingUser(creatingUser);
  }

  public static class Builder implements Cloneable {

    private final Parameters p;
    private String hash;

    private Builder() {
      this.p = new Parameters();
    }

    public Builder(final String hash, final Parameters parameters) {
      this.hash = hash;
      this.p = parameters;
    }

    private static class Parameters implements Cloneable {

      public String registrationDomain;
      public String name;
      public String version;
      public String image;
      public List<String> command;
      public Map<String, String> env;
      public Map<String, PortMapping> ports;
      public Map<ServiceEndpoint, ServicePorts> registration;
      public Integer gracePeriod;
      public Map<String, String> volumes;
      public Date expires;
      public String creatingUser;

      private Parameters() {
        this.command = EMPTY_COMMAND;
        this.env = Maps.newHashMap(EMPTY_ENV);
        this.ports = Maps.newHashMap(EMPTY_PORTS);
        this.registration = Maps.newHashMap(EMPTY_REGISTRATION);
        this.gracePeriod = EMPTY_GRACE_PERIOD;
        this.volumes = Maps.newHashMap(EMPTY_VOLUMES);
        this.registrationDomain = EMPTY_REGISTRATION_DOMAIN;
        this.creatingUser = EMPTY_CREATING_USER;
      }

      private Parameters(final Parameters p) {
        this.name = p.name;
        this.version = p.version;
        this.image = p.image;
        this.command = ImmutableList.copyOf(p.command);
        this.env = Maps.newHashMap(p.env);
        this.ports = Maps.newHashMap(p.ports);
        this.registration = Maps.newHashMap(p.registration);
        this.gracePeriod = p.gracePeriod;
        this.volumes = Maps.newHashMap(p.volumes);
        this.expires = p.expires;
        this.registrationDomain = p.registrationDomain;
        this.creatingUser = p.creatingUser;
      }
    }

    public Builder setRegistrationDomain(final String domain) {
      this.p.registrationDomain = domain;
      return this;
    }

    public Builder setCreatingUser(final String creatingUser) {
      this.p.creatingUser = creatingUser;
      return this;
    }

    public Builder setHash(final String hash) {
      this.hash = hash;
      return this;
    }

    public Builder setName(final String name) {
      p.name = name;
      return this;
    }

    public Builder setVersion(final String version) {
      p.version = version;
      return this;
    }

    public Builder setImage(final String image) {
      p.image = image;
      return this;
    }

    public Builder setCommand(final List<String> command) {
      p.command = ImmutableList.copyOf(command);
      return this;
    }

    public Builder setEnv(final Map<String, String> env) {
      p.env = Maps.newHashMap(env);
      return this;
    }

    public Builder addEnv(final String key, final String value) {
      p.env.put(key, value);
      return this;
    }

    public Builder setPorts(final Map<String, PortMapping> ports) {
      p.ports = Maps.newHashMap(ports);
      return this;
    }

    public Builder addPort(final String name, final PortMapping port) {
      p.ports.put(name, port);
      return this;
    }

    public Builder setRegistration(final Map<ServiceEndpoint, ServicePorts> registration) {
      p.registration = Maps.newHashMap(registration);
      return this;
    }

    public Builder addRegistration(final ServiceEndpoint endpoint, final ServicePorts ports) {
      p.registration.put(endpoint, ports);
      return this;
    }

    public Builder setGracePeriod(final Integer gracePeriod) {
      p.gracePeriod = gracePeriod;
      return this;
    }

    public Builder setVolumes(final Map<String, String> volumes) {
      p.volumes = Maps.newHashMap(volumes);
      return this;
    }

    public Builder addVolume(final String path) {
      p.volumes.put(path, EMPTY_MOUNT);
      return this;
    }

    public Builder addVolume(final String path, final String source) {
      p.volumes.put(path, source);
      return this;
    }

    public Builder setExpires(final Date expires) {
      p.expires = expires;
      return this;
    }

    public String getName() {
      return p.name;
    }

    public String getVersion() {
      return p.version;
    }

    public String getImage() {
      return p.image;
    }

    public List<String> getCommand() {
      return p.command;
    }

    public Map<String, String> getEnv() {
      return ImmutableMap.copyOf(p.env);
    }

    public Map<String, PortMapping> getPorts() {
      return ImmutableMap.copyOf(p.ports);
    }

    public Map<ServiceEndpoint, ServicePorts> getRegistration() {
      return ImmutableMap.copyOf(p.registration);
    }

    public String getRegistrationDomain() {
      return p.registrationDomain;
    }

    public Integer getGracePeriod() {
      return p.gracePeriod;
    }

    public Map<String, String> getVolumes() {
      return ImmutableMap.copyOf(p.volumes);
    }

    public Date getExpires() {
      return p.expires;
    }

    public String getCreatingUser() {
      return p.creatingUser;
    }

    @SuppressWarnings({"CloneDoesntDeclareCloneNotSupportedException", "CloneDoesntCallSuperClone"})
    @Override
    public Builder clone() {
      return new Builder(hash, new Parameters(p));
    }

    public Job build() {
      final String configHash;
      try {
        configHash = hex(Json.sha1digest(p));
      } catch (IOException e) {
        throw propagate(e);
      }

      final String hash;
      if (!Strings.isNullOrEmpty(this.hash)) {
        hash = this.hash;
      } else {
        if (p.name != null && p.version != null) {
          final String input = String.format("%s:%s:%s", p.name, p.version, configHash);
          hash = hex(sha1digest(input.getBytes(UTF_8)));
        } else {
          hash = null;
        }
      }

      final JobId id = new JobId(p.name, p.version, hash);

      return new Job(id, p);
    }

    private String hex(final byte[] bytes) {
      return BaseEncoding.base16().lowerCase().encode(bytes);
    }
  }
}
