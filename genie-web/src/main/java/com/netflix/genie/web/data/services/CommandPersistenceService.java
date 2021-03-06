/*
 *
 *  Copyright 2015 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.netflix.genie.web.data.services;

import com.github.fge.jsonpatch.JsonPatch;
import com.netflix.genie.common.exceptions.GenieException;
import com.netflix.genie.common.exceptions.GenieNotFoundException;
import com.netflix.genie.common.external.dtos.v4.Application;
import com.netflix.genie.common.external.dtos.v4.Cluster;
import com.netflix.genie.common.external.dtos.v4.ClusterStatus;
import com.netflix.genie.common.external.dtos.v4.Command;
import com.netflix.genie.common.external.dtos.v4.CommandRequest;
import com.netflix.genie.common.external.dtos.v4.CommandStatus;
import com.netflix.genie.common.external.dtos.v4.Criterion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * Abstraction layer to encapsulate CommandConfig functionality.<br>
 * Classes implementing this abstraction layer must be thread-safe.
 *
 * @author amsharma
 * @author tgianos
 */
@Validated
public interface CommandPersistenceService {

    /**
     * Create new command configuration.
     *
     * @param request encapsulates the command configuration information to
     *                create. Not null. Valid.
     * @return The id of the command created
     * @throws GenieException if there is an error
     */
    String createCommand(
        @NotNull(message = "No command entered. Unable to create.")
        @Valid CommandRequest request
    ) throws GenieException;

    /**
     * Gets command configuration for given id.
     *
     * @param id unique id for command configuration to get. Not null/empty.
     * @return The command configuration
     * @throws GenieException if there is an error
     */
    Command getCommand(
        @NotBlank(message = "No id entered unable to get.") String id
    ) throws GenieException;

    /**
     * Get command configurations for given filter criteria.
     *
     * @param name     Name of command config
     * @param user     The name of the user who created the configuration
     * @param statuses The status of the commands to get. Can be null.
     * @param tags     tags allocated to this command
     * @param page     The page of results to get
     * @return All the commands matching the specified criteria
     */
    Page<Command> getCommands(
        @Nullable String name,
        @Nullable String user,
        @Nullable Set<CommandStatus> statuses,
        @Nullable Set<String> tags,
        Pageable page
    );

    /**
     * Update command configuration.
     *
     * @param id            The id of the command configuration to update. Not null or
     *                      empty.
     * @param updateCommand contains the information to update the command with
     * @throws GenieException if there is an error
     */
    void updateCommand(
        @NotBlank(message = "No id entered. Unable to update.") String id,
        @NotNull(message = "No command information entered. Unable to update.")
        @Valid Command updateCommand
    ) throws GenieException;

    /**
     * Patch a command with the given json patch.
     *
     * @param id    The id of the command to update
     * @param patch The json patch to use to update the given command
     * @throws GenieException if there is an error
     */
    void patchCommand(@NotBlank String id, @NotNull JsonPatch patch) throws GenieException;

    /**
     * Delete all commands from database.
     *
     * @throws GenieException if there is an error
     */
    void deleteAllCommands() throws GenieException;

    /**
     * Delete a command configuration from database.
     *
     * @param id unique if of the command configuration to delete
     * @throws GenieException if there is an error
     */
    void deleteCommand(
        @NotBlank(message = "No id entered. Unable to delete.") String id
    ) throws GenieException;

    /**
     * Add a configuration files to the command.
     *
     * @param id      The id of the command to add the configuration file to. Not
     *                null/empty/blank.
     * @param configs The configuration files to add. Not null/empty.
     * @throws GenieException if there is an error
     */
    void addConfigsForCommand(
        @NotBlank(message = "No command id entered. Unable to add configurations.") String id,
        @NotEmpty(message = "No configuration files entered. Unable to add.") Set<String> configs
    ) throws GenieException;

    /**
     * Get the set of configuration files associated with the command with given
     * id.
     *
     * @param id The id of the command to get the configuration files for. Not
     *           null/empty/blank.
     * @return The set of configuration files as paths
     * @throws GenieException if there is an error
     */
    Set<String> getConfigsForCommand(
        @NotBlank(message = "No command id entered. Unable to get configs.") String id
    ) throws GenieException;

    /**
     * Update the set of configuration files associated with the command with
     * given id.
     *
     * @param id      The id of the command to update the configuration files for.
     *                Not null/empty/blank.
     * @param configs The configuration files to replace existing configurations
     *                with. Not null/empty.
     * @throws GenieException if there is an error
     */
    void updateConfigsForCommand(
        @NotBlank(message = "No command id entered. Unable to update configurations.") String id,
        @NotEmpty(message = "No configs entered. Unable to update.") Set<String> configs
    ) throws GenieException;

    /**
     * Remove all configuration files from the command.
     *
     * @param id The id of the command to remove the configuration file from.
     *           Not null/empty/blank.
     * @throws GenieException if there is an error
     */
    void removeAllConfigsForCommand(
        @NotBlank(message = "No command id entered. Unable to remove configs.") String id
    ) throws GenieException;

    /**
     * Remove a configuration file from the command.
     *
     * @param id     The id of the command to remove the configuration file from.
     *               Not null/empty/blank.
     * @param config The configuration file to remove. Not null/empty/blank.
     * @throws GenieException if there is an error
     */
    void removeConfigForCommand(
        @NotBlank(message = "No command id entered. Unable to remove configuration.") String id,
        @NotBlank(message = "No config entered. Unable to remove.") String config
    ) throws GenieException;

    /**
     * Add dependency files to the command.
     *
     * @param id           The id of the command to add the dependency file to. Not
     *                     null/empty/blank.
     * @param dependencies The dependency files to add. Not null.
     * @throws GenieException if there is an error
     */
    void addDependenciesForCommand(
        @NotBlank(message = "No command id entered. Unable to add dependencies.") String id,
        @NotEmpty(message = "No dependencies entered. Unable to add dependencies.") Set<String> dependencies
    ) throws GenieException;

    /**
     * Get the set of dependency files associated with the command with given id.
     *
     * @param id The id of the command to get the dependency files for. Not
     *           null/empty/blank.
     * @return The set of dependency files as paths
     * @throws GenieException if there is an error
     */
    Set<String> getDependenciesForCommand(
        @NotBlank(message = "No command id entered. Unable to get dependencies.") String id
    ) throws GenieException;

    /**
     * Update the set of dependency files associated with the command with given
     * id.
     *
     * @param id           The id of the command to update the dependency files for. Not
     *                     null/empty/blank.
     * @param dependencies The dependency files to replace existing dependencies with. Not null/empty.
     * @throws GenieException if there is an error
     */
    void updateDependenciesForCommand(
        @NotBlank(message = "No command id entered. Unable to update dependencies.") String id,
        @NotNull(message = "No dependencies entered. Unable to update.") Set<String> dependencies
    ) throws GenieException;

    /**
     * Remove all dependency files from the command.
     *
     * @param id The id of the command to remove the configuration file
     *           from. Not null/empty/blank.
     * @throws GenieException if there is an error
     */
    void removeAllDependenciesForCommand(
        @NotBlank(message = "No command id entered. Unable to remove dependencies.") String id
    ) throws GenieException;

    /**
     * Remove a dependency file from the command.
     *
     * @param id         The id of the command to remove the dependency file from. Not
     *                   null/empty/blank.
     * @param dependency The dependency file to remove. Not null/empty/blank.
     * @throws GenieException if there is an error
     */
    void removeDependencyForCommand(
        @NotBlank(message = "No command id entered. Unable to remove dependency.") String id,
        @NotBlank(message = "No dependency entered. Unable to remove dependency.") String dependency
    ) throws GenieException;

    /**
     * Add tags to the command.
     *
     * @param id   The id of the command to add the tags to. Not
     *             null/empty/blank.
     * @param tags The tags to add. Not null/empty.
     * @throws GenieException if there is an error
     */
    void addTagsForCommand(
        @NotBlank(message = "No command id entered. Unable to add tags.") String id,
        @NotEmpty(message = "No tags entered. Unable to add.") Set<String> tags
    ) throws GenieException;

    /**
     * Get the set of tags associated with the command with given
     * id.
     *
     * @param id The id of the command to get the tags for. Not
     *           null/empty/blank.
     * @return The set of tags as paths
     * @throws GenieException if there is an error
     */
    Set<String> getTagsForCommand(
        @NotBlank(message = "No command id sent. Cannot retrieve tags.") String id
    ) throws GenieException;

    /**
     * Update the set of tags associated with the command with
     * given id.
     *
     * @param id   The id of the command to update the tags for.
     *             Not null/empty/blank.
     * @param tags The tags to replace existing tags
     *             with. Not null/empty.
     * @throws GenieException if there is an error
     */
    void updateTagsForCommand(
        @NotBlank(message = "No command id entered. Unable to update tags.") String id,
        @NotEmpty(message = "No tags entered. Unable to update.") Set<String> tags
    ) throws GenieException;

    /**
     * Remove all tags from the command.
     *
     * @param id The id of the command to remove the tags from.
     *           Not null/empty/blank.
     * @throws GenieException if there is an error
     */
    void removeAllTagsForCommand(
        @NotBlank(message = "No command id entered. Unable to remove tags.") String id
    ) throws GenieException;

    /**
     * Remove a tag from the command.
     *
     * @param id  The id of the command to remove the tag from. Not
     *            null/empty/blank.
     * @param tag The tag to remove. Not null/empty/blank.
     * @throws GenieException if there is an error
     */
    void removeTagForCommand(
        @NotBlank(message = "No command id entered. Unable to remove tag.") String id,
        @NotBlank(message = "No tag entered. Unable to remove.") String tag
    ) throws GenieException;

    /**
     * Add applications for the command.
     *
     * @param id             The id of the command to add the application file to. Not
     *                       null/empty/blank.
     * @param applicationIds The ids of the applications to add. Not null.
     * @throws GenieException if there is an error
     */
    void addApplicationsForCommand(
        @NotBlank(message = "No command id entered. Unable to add applications.") String id,
        @NotEmpty(message = "No application ids entered. Unable to add applications.") List<String> applicationIds
    ) throws GenieException;

    /**
     * Set the applications for the command.
     *
     * @param id             The id of the command to add the application file to. Not
     *                       null/empty/blank.
     * @param applicationIds The ids of the applications to set. Not null.
     * @throws GenieException if there is an error
     */
    void setApplicationsForCommand(
        @NotBlank(message = "No command id entered. Unable to set applications.") String id,
        @NotNull(message = "No application ids entered. Unable to set applications.") List<String> applicationIds
    ) throws GenieException;

    /**
     * Get the applications for a given command.
     *
     * @param id The id of the command to get the application for. Not
     *           null/empty/blank.
     * @return The applications or exception if none exist.
     * @throws GenieException if there is an error
     */
    List<Application> getApplicationsForCommand(
        @NotBlank(message = "No command id entered. Unable to get applications.") String id
    ) throws GenieException;

    /**
     * Remove the applications from the command.
     *
     * @param id The id of the command to remove the application from. Not
     *           null/empty/blank.
     * @throws GenieException if there is an error
     */
    void removeApplicationsForCommand(
        @NotBlank(message = "No command id entered. Unable to remove applications.") String id
    ) throws GenieException;

    /**
     * Remove the application from the command.
     *
     * @param id    The id of the command to remove the application from. Not null/empty/blank.
     * @param appId The id of the application to remove. Not null/empty/blank
     * @throws GenieException if there is an error
     */
    void removeApplicationForCommand(
        @NotBlank(message = "No command id entered. Unable to remove application.") String id,
        @NotBlank(message = "No application id entered. Unable to remove application.") String appId
    ) throws GenieException;

    /**
     * Get all the clusters the command with given id is associated with.
     *
     * @param id       The id of the command to get the clusters for.
     * @param statuses The status of the clusters returned
     * @return The clusters the command is available on.
     * @throws GenieException if there is an error
     */
    Set<Cluster> getClustersForCommand(
        @NotBlank(message = "No command id entered. Unable to get clusters.") String id,
        @Nullable Set<ClusterStatus> statuses
    ) throws GenieException;

    /**
     * For the given command {@literal id} return the Cluster {@link Criterion} in priority order that is currently
     * associated with this command if any.
     *
     * @param id The id of the command to get the criteria for
     * @return The cluster criteria in priority order
     * @throws GenieNotFoundException If no command with {@literal id} exists
     */
    List<Criterion> getClusterCriteriaForCommand(String id) throws GenieNotFoundException;

    /**
     * Add a new {@link Criterion} to the existing list of cluster criteria for the command identified by {@literal id}.
     * This new criterion will be the lowest priority criterion.
     *
     * @param id        The id of the command to add to
     * @param criterion The new {@link Criterion} to add
     * @throws GenieNotFoundException If no command with {@literal id} exists
     */
    void addClusterCriterionForCommand(String id, @Valid Criterion criterion) throws GenieNotFoundException;

    /**
     * Add a new {@link Criterion} to the existing list of cluster criteria for the command identified by {@literal id}.
     * The {@literal priority} is the place in the list this new criterion should be placed. A value of {@literal 0}
     * indicates it should be placed at the front of the list with the highest possible priority. {@literal 1} would be
     * second in the list etc. If {@literal priority} is {@literal >} the current size of the cluster criteria list
     * this new criterion will be placed at the end as the lowest priority item.
     *
     * @param id        The id of the command to add to
     * @param criterion The new {@link Criterion} to add
     * @param priority  The place in the existing cluster criteria list this new criterion should be placed. Min 0.
     * @throws GenieNotFoundException If no command with {@literal id} exists
     */
    void addClusterCriterionForCommand(
        String id,
        @Valid Criterion criterion,
        @Min(0) int priority
    ) throws GenieNotFoundException;

    /**
     * For the command identified by {@literal id} reset the entire list of cluster criteria to match the contents of
     * {@literal clusterCriteria}.
     *
     * @param id              The id of the command to set the cluster criteria for
     * @param clusterCriteria The priority list of {@link Criterion} to set
     * @throws GenieNotFoundException If no command with {@literal id} exists
     */
    void setClusterCriteriaForCommand(String id, List<@Valid Criterion> clusterCriteria) throws GenieNotFoundException;

    /**
     * Remove the {@link Criterion} with the given {@literal priority} from the current list of cluster criteria
     * associated with the command identified by {@literal id}. A value of {@literal 0} for {@literal priority}
     * will result in the first element in the list being removed, {@literal 1} the second element and so on.
     *
     * @param id       The id of the command to remove the criterion from
     * @param priority The priority of the criterion to remove
     * @throws GenieNotFoundException If no command with {@literal id} exists
     */
    void removeClusterCriterionForCommand(String id, @Min(0) int priority) throws GenieNotFoundException;

    /**
     * Remove all the {@link Criterion} currently associated with the command identified by {@literal id}.
     *
     * @param id The id of the command to remove the criteria from
     * @throws GenieNotFoundException If no command with {@literal id} exists
     */
    void removeAllClusterCriteriaForCommand(String id) throws GenieNotFoundException;

    /**
     * Find all the {@link Command}'s that match the given {@link Criterion}.
     *
     * @param criterion        The {@link Criterion} supplied that each command needs to completely match to be returned
     * @param addDefaultStatus {@literal true} if a default status should be added to the supplied criterion if a
     *                         status isn't already present
     * @return All the {@link Command}'s which matched the {@link Criterion}
     */
    Set<Command> findCommandsMatchingCriterion(@Valid Criterion criterion, boolean addDefaultStatus);

    /**
     * Update the status of a command to the {@literal desiredStatus} if its status is in {@literal currentStatuses},
     * it was created before {@literal commandCreatedThreshold} and it hasn't been used in any job that was created
     * in the Genie system after {@literal jobCreatedThreshold}.
     *
     * @param desiredStatus           The new status the matching commands should have
     * @param commandCreatedThreshold The instant in time which a command must have been created before to be
     *                                considered for update. Exclusive
     * @param currentStatuses         The set of current statuses a command must have to be considered for update
     * @param jobCreatedThreshold     The instant in time after which a command must not have been used in a Genie job
     *                                for it to be considered for update. Inclusive.
     * @return The number of commands whose statuses were updated to {@literal desiredStatus}
     */
    int updateStatusForUnusedCommands(
        CommandStatus desiredStatus,
        Instant commandCreatedThreshold,
        Set<CommandStatus> currentStatuses,
        Instant jobCreatedThreshold
    );

    /**
     * Bulk delete commands from the database where their status is in {@literal deleteStatuses} they were created
     * before {@literal commandCreatedThreshold} and they aren't attached to any jobs still in the database.
     *
     * @param deleteStatuses          The set of statuses a command must be in in order to be considered for deletion
     * @param commandCreatedThreshold The instant in time a command must have been created before to be considered for
     *                                deletion. Exclusive.
     * @return The number of commands that were deleted
     */
    int deleteUnusedCommands(Set<CommandStatus> deleteStatuses, Instant commandCreatedThreshold);
}
