/*
 *
 *  Copyright 2016 Netflix, Inc.
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
package com.netflix.genie.web.tasks.leader;

import com.netflix.genie.common.external.dtos.v4.CommandStatus;
import com.netflix.genie.common.internal.jobs.JobConstants;
import com.netflix.genie.web.data.services.ApplicationPersistenceService;
import com.netflix.genie.web.data.services.ClusterPersistenceService;
import com.netflix.genie.web.data.services.CommandPersistenceService;
import com.netflix.genie.web.data.services.DataServices;
import com.netflix.genie.web.data.services.FilePersistenceService;
import com.netflix.genie.web.data.services.JobPersistenceService;
import com.netflix.genie.web.data.services.TagPersistenceService;
import com.netflix.genie.web.properties.DatabaseCleanupProperties;
import com.netflix.genie.web.tasks.GenieTaskScheduleType;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.CronTrigger;

import java.time.Instant;
import java.util.Calendar;
import java.util.EnumSet;

/**
 * Unit tests for {@link DatabaseCleanupTask}.
 *
 * @author tgianos
 * @since 3.0.0
 */
class DatabaseCleanupTaskTest {

    private DatabaseCleanupProperties cleanupProperties;
    private DatabaseCleanupProperties.ApplicationDatabaseCleanupProperties applicationCleanupProperties;
    private DatabaseCleanupProperties.ClusterDatabaseCleanupProperties clusterCleanupProperties;
    private DatabaseCleanupProperties.CommandDatabaseCleanupProperties commandCleanupProperties;
    private DatabaseCleanupProperties.CommandDeactivationDatabaseCleanupProperties commandDeactivationProperties;
    private DatabaseCleanupProperties.FileDatabaseCleanupProperties fileCleanupProperties;
    private DatabaseCleanupProperties.JobDatabaseCleanupProperties jobCleanupProperties;
    private DatabaseCleanupProperties.TagDatabaseCleanupProperties tagCleanupProperties;
    private MockEnvironment environment;
    private ApplicationPersistenceService applicationPersistenceService;
    private CommandPersistenceService commandPersistenceService;
    private JobPersistenceService jobPersistenceService;
    private ClusterPersistenceService clusterPersistenceService;
    private FilePersistenceService filePersistenceService;
    private TagPersistenceService tagPersistenceService;
    private DatabaseCleanupTask task;

    /**
     * Setup for the tests.
     */
    @BeforeEach
    void setup() {
        this.cleanupProperties = Mockito.mock(DatabaseCleanupProperties.class);
        this.applicationCleanupProperties
            = Mockito.mock(DatabaseCleanupProperties.ApplicationDatabaseCleanupProperties.class);
        Mockito.when(this.cleanupProperties.getApplicationCleanup()).thenReturn(this.applicationCleanupProperties);
        this.clusterCleanupProperties = Mockito.mock(DatabaseCleanupProperties.ClusterDatabaseCleanupProperties.class);
        Mockito.when(this.cleanupProperties.getClusterCleanup()).thenReturn(this.clusterCleanupProperties);
        this.commandCleanupProperties = Mockito.mock(DatabaseCleanupProperties.CommandDatabaseCleanupProperties.class);
        Mockito.when(this.cleanupProperties.getCommandCleanup()).thenReturn(this.commandCleanupProperties);
        this.commandDeactivationProperties
            = Mockito.mock(DatabaseCleanupProperties.CommandDeactivationDatabaseCleanupProperties.class);
        Mockito.when(this.cleanupProperties.getCommandDeactivation()).thenReturn(this.commandDeactivationProperties);
        this.fileCleanupProperties = Mockito.mock(DatabaseCleanupProperties.FileDatabaseCleanupProperties.class);
        Mockito.when(this.cleanupProperties.getFileCleanup()).thenReturn(this.fileCleanupProperties);
        this.jobCleanupProperties = Mockito.mock(DatabaseCleanupProperties.JobDatabaseCleanupProperties.class);
        Mockito.when(this.cleanupProperties.getJobCleanup()).thenReturn(this.jobCleanupProperties);
        this.tagCleanupProperties = Mockito.mock(DatabaseCleanupProperties.TagDatabaseCleanupProperties.class);
        Mockito.when(this.cleanupProperties.getTagCleanup()).thenReturn(this.tagCleanupProperties);
        this.environment = new MockEnvironment();
        this.applicationPersistenceService = Mockito.mock(ApplicationPersistenceService.class);
        this.commandPersistenceService = Mockito.mock(CommandPersistenceService.class);
        this.jobPersistenceService = Mockito.mock(JobPersistenceService.class);
        this.clusterPersistenceService = Mockito.mock(ClusterPersistenceService.class);
        this.filePersistenceService = Mockito.mock(FilePersistenceService.class);
        this.tagPersistenceService = Mockito.mock(TagPersistenceService.class);
        final DataServices dataServices = Mockito.mock(DataServices.class);
        Mockito.when(dataServices.getApplicationPersistenceService()).thenReturn(this.applicationPersistenceService);
        Mockito.when(dataServices.getCommandPersistenceService()).thenReturn(this.commandPersistenceService);
        Mockito.when(dataServices.getClusterPersistenceService()).thenReturn(this.clusterPersistenceService);
        Mockito.when(dataServices.getJobPersistenceService()).thenReturn(this.jobPersistenceService);
        Mockito.when(dataServices.getFilePersistenceService()).thenReturn(this.filePersistenceService);
        Mockito.when(dataServices.getTagPersistenceService()).thenReturn(this.tagPersistenceService);
        this.task = new DatabaseCleanupTask(
            this.cleanupProperties,
            this.environment,
            dataServices,
            new SimpleMeterRegistry()
        );
    }

    /**
     * Make sure the schedule type returns the correct thing.
     */
    @Test
    void canGetScheduleType() {
        Assertions.assertThat(this.task.getScheduleType()).isEqualTo(GenieTaskScheduleType.TRIGGER);
    }

    /**
     * Make sure the trigger returned is accurate.
     */
    @Test
    void canGetTrigger() {
        final String expression = "0 0 1 * * *";
        this.environment.setProperty(DatabaseCleanupProperties.EXPRESSION_PROPERTY, expression);
        Mockito.when(this.cleanupProperties.getExpression()).thenReturn("0 0 0 * * *");
        final Trigger trigger = this.task.getTrigger();
        if (trigger instanceof CronTrigger) {
            final CronTrigger cronTrigger = (CronTrigger) trigger;
            Assertions.assertThat(cronTrigger.getExpression()).isEqualTo(expression);
        } else {
            Assertions.fail("Trigger was not of expected type: " + CronTrigger.class.getName());
        }
    }

    /**
     * Make sure the run method passes in the expected date.
     */
    @Test
    void canRun() {
        final int days = 5;
        final int negativeDays = -1 * days;
        final int pageSize = 10;
        final int maxDeleted = 10_000;

        Mockito.when(this.jobCleanupProperties.getRetention()).thenReturn(days).thenReturn(negativeDays);
        Mockito.when(this.jobCleanupProperties.getPageSize()).thenReturn(pageSize);
        Mockito.when(this.jobCleanupProperties.getMaxDeletedPerTransaction()).thenReturn(maxDeleted);

        Mockito.when(this.commandDeactivationProperties.getCommandCreationThreshold()).thenReturn(60);
        Mockito.when(this.commandDeactivationProperties.getJobCreationThreshold()).thenReturn(30);
        final ArgumentCaptor<Instant> argument = ArgumentCaptor.forClass(Instant.class);

        final long deletedCount1 = 6L;
        final long deletedCount2 = 18L;
        final long deletedCount3 = 2L;
        Mockito
            .when(
                this.jobPersistenceService.deleteBatchOfJobsCreatedBeforeDate(
                    Mockito.any(Instant.class),
                    Mockito.anyInt(),
                    Mockito.anyInt()
                )
            )
            .thenReturn(deletedCount1)
            .thenReturn(0L)
            .thenReturn(deletedCount2)
            .thenReturn(deletedCount3)
            .thenReturn(0L);

        Mockito.when(this.clusterPersistenceService.deleteTerminatedClusters()).thenReturn(1L, 2L);
        Mockito.when(this.filePersistenceService.deleteUnusedFiles(Mockito.any(Instant.class))).thenReturn(3L, 4L);
        Mockito.when(this.tagPersistenceService.deleteUnusedTags(Mockito.any(Instant.class))).thenReturn(5L, 6L);
        Mockito
            .when(this.applicationPersistenceService.deleteUnusedApplicationsCreatedBefore(Mockito.any(Instant.class)))
            .thenReturn(11, 117);
        Mockito
            .when(
                this.commandPersistenceService.updateStatusForUnusedCommands(
                    Mockito.eq(CommandStatus.INACTIVE),
                    Mockito.any(Instant.class),
                    Mockito.eq(EnumSet.of(CommandStatus.DEPRECATED, CommandStatus.ACTIVE)),
                    Mockito.any(Instant.class)
                )
            )
            .thenReturn(50, 242);
        Mockito
            .when(
                this.commandPersistenceService.deleteUnusedCommands(
                    Mockito.eq(EnumSet.of(CommandStatus.INACTIVE)),
                    Mockito.any(Instant.class)
                )
            )
            .thenReturn(11, 81);

        // The multiple calendar instances are to protect against running this test when the day flips
        final Calendar before = Calendar.getInstance(JobConstants.UTC);
        this.task.run();
        this.task.run();
        final Calendar after = Calendar.getInstance(JobConstants.UTC);

        if (before.get(Calendar.DAY_OF_YEAR) == after.get(Calendar.DAY_OF_YEAR)) {
            Mockito
                .verify(this.jobPersistenceService, Mockito.times(5))
                .deleteBatchOfJobsCreatedBeforeDate(argument.capture(), Mockito.eq(maxDeleted), Mockito.eq(pageSize));
            final Calendar date = Calendar.getInstance(JobConstants.UTC);
            date.set(Calendar.HOUR_OF_DAY, 0);
            date.set(Calendar.MINUTE, 0);
            date.set(Calendar.SECOND, 0);
            date.set(Calendar.MILLISECOND, 0);
            date.add(Calendar.DAY_OF_YEAR, negativeDays);
            Assertions.assertThat(argument.getAllValues().get(0).toEpochMilli()).isEqualTo(date.getTime().getTime());
            Assertions.assertThat(argument.getAllValues().get(1).toEpochMilli()).isEqualTo(date.getTime().getTime());
            Mockito.verify(this.clusterPersistenceService, Mockito.times(2)).deleteTerminatedClusters();
            Mockito
                .verify(this.filePersistenceService, Mockito.times(2))
                .deleteUnusedFiles(Mockito.any(Instant.class));
            Mockito
                .verify(this.tagPersistenceService, Mockito.times(2))
                .deleteUnusedTags(Mockito.any(Instant.class));
            Mockito
                .verify(this.applicationPersistenceService, Mockito.times(2))
                .deleteUnusedApplicationsCreatedBefore(Mockito.any(Instant.class));
            Mockito
                .verify(this.commandPersistenceService, Mockito.times(2))
                .deleteUnusedCommands(Mockito.eq(EnumSet.of(CommandStatus.INACTIVE)), Mockito.any(Instant.class));
            Mockito
                .verify(this.commandPersistenceService, Mockito.times(2))
                .updateStatusForUnusedCommands(
                    Mockito.eq(CommandStatus.INACTIVE),
                    Mockito.any(Instant.class),
                    Mockito.eq(EnumSet.of(CommandStatus.DEPRECATED, CommandStatus.ACTIVE)),
                    Mockito.any(Instant.class)
                );
        }
    }

    /**
     * Make sure the run method throws when an error is encountered.
     */
    @Test
    void cantRun() {
        final int days = 5;
        final int negativeDays = -1 * days;
        final int pageSize = 10;
        final int maxDeleted = 10_000;

        Mockito.when(this.jobCleanupProperties.getRetention()).thenReturn(days).thenReturn(negativeDays);
        Mockito.when(this.jobCleanupProperties.getPageSize()).thenReturn(pageSize);
        Mockito.when(this.jobCleanupProperties.getMaxDeletedPerTransaction()).thenReturn(maxDeleted);

        Mockito
            .when(
                this.jobPersistenceService.deleteBatchOfJobsCreatedBeforeDate(
                    Mockito.any(Instant.class),
                    Mockito.anyInt(),
                    Mockito.anyInt()
                )
            )
            .thenThrow(new RuntimeException("test"));

        Assertions.assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> this.task.run());
    }

    /**
     * Make sure individual cleanup sub-tasks are skipped according to properties.
     */
    @Test
    void skipAll() {
        this.environment.setProperty(
            DatabaseCleanupProperties.ApplicationDatabaseCleanupProperties.SKIP_PROPERTY,
            "true"
        );
        this.environment.setProperty(DatabaseCleanupProperties.CommandDatabaseCleanupProperties.SKIP_PROPERTY, "true");
        this.environment.setProperty(
            DatabaseCleanupProperties.CommandDeactivationDatabaseCleanupProperties.SKIP_PROPERTY,
            "true"
        );
        this.environment.setProperty(DatabaseCleanupProperties.ClusterDatabaseCleanupProperties.SKIP_PROPERTY, "true");
        this.environment.setProperty(DatabaseCleanupProperties.FileDatabaseCleanupProperties.SKIP_PROPERTY, "true");
        this.environment.setProperty(DatabaseCleanupProperties.JobDatabaseCleanupProperties.SKIP_PROPERTY, "true");
        this.environment.setProperty(DatabaseCleanupProperties.TagDatabaseCleanupProperties.SKIP_PROPERTY, "true");
        Mockito.when(this.applicationCleanupProperties.isSkip()).thenReturn(false);
        Mockito.when(this.commandCleanupProperties.isSkip()).thenReturn(false);
        Mockito.when(this.commandDeactivationProperties.isSkip()).thenReturn(false);
        Mockito.when(this.clusterCleanupProperties.isSkip()).thenReturn(false);
        Mockito.when(this.fileCleanupProperties.isSkip()).thenReturn(false);
        Mockito.when(this.tagCleanupProperties.isSkip()).thenReturn(false);
        Mockito.when(this.jobCleanupProperties.isSkip()).thenReturn(false);

        this.task.run();

        Mockito
            .verify(this.applicationPersistenceService, Mockito.never())
            .deleteUnusedApplicationsCreatedBefore(Mockito.any(Instant.class));
        Mockito
            .verify(this.commandPersistenceService, Mockito.never())
            .deleteUnusedCommands(Mockito.anySet(), Mockito.any(Instant.class));
        Mockito
            .verify(this.commandPersistenceService, Mockito.never())
            .updateStatusForUnusedCommands(
                Mockito.any(CommandStatus.class),
                Mockito.any(Instant.class),
                Mockito.anySet(),
                Mockito.any(Instant.class)
            );
        Mockito
            .verify(this.jobPersistenceService, Mockito.never())
            .deleteBatchOfJobsCreatedBeforeDate(
                Mockito.any(Instant.class),
                Mockito.anyInt(),
                Mockito.anyInt()
            );
        Mockito
            .verify(this.clusterPersistenceService, Mockito.never())
            .deleteTerminatedClusters();
        Mockito
            .verify(this.filePersistenceService, Mockito.never())
            .deleteUnusedFiles(Mockito.any(Instant.class));
        Mockito
            .verify(this.tagPersistenceService, Mockito.never())
            .deleteUnusedTags(Mockito.any(Instant.class));
    }
}
