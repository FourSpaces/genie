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

import com.netflix.genie.agent.execution.services.AgentFileStreamService;
import com.netflix.genie.agent.execution.statemachine.ExecutionContext;
import com.netflix.genie.agent.execution.statemachine.ExecutionStage;
import com.netflix.genie.agent.execution.statemachine.FatalTransitionException;
import com.netflix.genie.agent.execution.statemachine.RetryableTransitionException;
import com.netflix.genie.agent.execution.statemachine.States;
import lombok.extern.slf4j.Slf4j;

/**
 * Triggers a manual refresh of the cached files manifest.
 *
 * @author mprimi
 * @since 4.0.0
 */
@Slf4j
public class RefreshManifestStage extends ExecutionStage {
    private final AgentFileStreamService agentFileStreamService;

    /**
     * Constructor.
     *
     * @param agentFileStreamService agent file stream service
     */
    public RefreshManifestStage(final AgentFileStreamService agentFileStreamService) {
        super(States.FORCE_FILE_MANIFEST_REFRESH);
        this.agentFileStreamService = agentFileStreamService;
    }

    @Override
    protected void attemptTransition(
        final ExecutionContext executionContext
    ) throws RetryableTransitionException, FatalTransitionException {

        this.agentFileStreamService.forceServerSync();

    }
}
