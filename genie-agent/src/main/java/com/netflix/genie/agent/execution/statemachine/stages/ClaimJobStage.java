/*
 *
 *  Copyright 2020 Netflix, Inc.
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
package com.netflix.genie.agent.execution.statemachine.stages;

import com.netflix.genie.agent.cli.UserConsole;
import com.netflix.genie.agent.execution.exceptions.JobReservationException;
import com.netflix.genie.agent.execution.services.AgentJobService;
import com.netflix.genie.agent.execution.statemachine.ExecutionStage;
import com.netflix.genie.agent.execution.statemachine.FatalTransitionException;
import com.netflix.genie.agent.execution.statemachine.States;
import com.netflix.genie.agent.execution.statemachine.RetryableTransitionException;
import com.netflix.genie.agent.execution.statemachine.ExecutionContext;
import com.netflix.genie.common.dto.JobStatusMessages;
import com.netflix.genie.common.external.dtos.v4.AgentClientMetadata;
import com.netflix.genie.common.external.dtos.v4.JobStatus;
import com.netflix.genie.common.internal.exceptions.unchecked.GenieRuntimeException;
import lombok.extern.slf4j.Slf4j;

/**
 * Claim the job, so no other agent can execute it.
 *
 * @author mprimi
 * @since 4.0.0
 */
@Slf4j
public class ClaimJobStage extends ExecutionStage {
    private final AgentJobService agentJobService;

    /**
     * Constructor.
     *
     * @param agentJobService agent job service
     */
    public ClaimJobStage(final AgentJobService agentJobService) {
        super(States.CLAIM_JOB);
        this.agentJobService = agentJobService;
    }

    @Override
    protected void attemptTransition(
        final ExecutionContext executionContext
    ) throws RetryableTransitionException, FatalTransitionException {

        final String jobId = executionContext.getReservedJobId();
        final AgentClientMetadata agentClientMetadata = executionContext.getAgentClientMetadata();

        assert jobId != null;
        assert agentClientMetadata != null;

        log.info("Claiming job");

        try {
            this.agentJobService.claimJob(jobId, agentClientMetadata);
        } catch (final GenieRuntimeException e) {
            throw createRetryableException(e);
        } catch (final JobReservationException e) {
            throw createFatalException(e);
        }

        UserConsole.getLogger().info("Successfully claimed job: {}", jobId);

        // Update context
        executionContext.setCurrentJobStatus(JobStatus.CLAIMED);
        executionContext.setClaimedJobId(jobId);
        executionContext.setNextJobStatus(JobStatus.INIT);
        executionContext.setNextJobStatusMessage(JobStatusMessages.JOB_INITIALIZING);
    }
}
