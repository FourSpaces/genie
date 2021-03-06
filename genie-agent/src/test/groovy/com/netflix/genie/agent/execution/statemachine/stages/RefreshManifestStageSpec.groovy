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
package com.netflix.genie.agent.execution.statemachine.stages

import com.netflix.genie.agent.execution.services.AgentFileStreamService
import com.netflix.genie.agent.execution.statemachine.ExecutionContext
import com.netflix.genie.agent.execution.statemachine.ExecutionStage
import spock.lang.Specification

class RefreshManifestStageSpec extends Specification {
    AgentFileStreamService agentFileService
    ExecutionStage stage
    ExecutionContext executionContext

    void setup() {
        this.agentFileService = Mock(AgentFileStreamService)
        this.executionContext = Mock(ExecutionContext)
        this.stage = new RefreshManifestStage(agentFileService)
    }

    def "AttemptTransition"() {
        when:
        this.stage.attemptTransition(executionContext)

        then:
        1 * agentFileService.forceServerSync()
    }
}
