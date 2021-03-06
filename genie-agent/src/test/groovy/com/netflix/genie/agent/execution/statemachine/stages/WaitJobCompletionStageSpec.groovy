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

import com.netflix.genie.agent.execution.process.JobProcessManager
import com.netflix.genie.agent.execution.process.JobProcessResult
import com.netflix.genie.agent.execution.statemachine.ExecutionContext
import com.netflix.genie.agent.execution.statemachine.ExecutionStage
import com.netflix.genie.agent.execution.statemachine.FatalTransitionException
import com.netflix.genie.common.external.dtos.v4.JobStatus
import spock.lang.Specification

class WaitJobCompletionStageSpec extends Specification {
    ExecutionStage stage
    ExecutionContext executionContext
    JobProcessManager jobProcessManager
    JobProcessResult jobProcessResult

    void setup() {
        this.jobProcessResult = Mock(JobProcessResult)
        this.jobProcessManager = Mock(JobProcessManager)
        this.executionContext = Mock(ExecutionContext)
        this.stage = new WaitJobCompletionStage(jobProcessManager)
    }

    def "AttemptTransition -- not launched"() {
        when:
        stage.attemptTransition(executionContext)

        then:
        1 * executionContext.isJobLaunched() >> false
        0 * jobProcessManager.waitFor()
    }

    def "AttemptTransition -- success"() {
        when:
        stage.attemptTransition(executionContext)

        then:
        1 * executionContext.isJobLaunched() >> true
        1 * jobProcessManager.waitFor() >> jobProcessResult
        1 * executionContext.setJobProcessResult(jobProcessResult)
        1 * jobProcessResult.getFinalStatus() >> JobStatus.KILLED
    }

    def "AttemptTransition -- error"() {
        setup:
        InterruptedException interruptedException = Mock(InterruptedException)

        when:
        stage.attemptTransition(executionContext)

        then:
        1 * executionContext.isJobLaunched() >> true
        1 * jobProcessManager.waitFor() >> { throw interruptedException }
        def e = thrown(FatalTransitionException)
        e.getCause() == interruptedException
        0 * executionContext.setJobProcessResult(jobProcessResult)
        0 * jobProcessResult.getFinalStatus() >> JobStatus.KILLED
    }
}
