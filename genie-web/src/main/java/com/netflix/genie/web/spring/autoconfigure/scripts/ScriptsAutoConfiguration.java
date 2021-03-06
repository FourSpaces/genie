/*
 *
 *  Copyright 2019 Netflix, Inc.
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
package com.netflix.genie.web.spring.autoconfigure.scripts;

import com.netflix.genie.common.external.util.GenieObjectMapper;
import com.netflix.genie.web.properties.ClusterSelectorScriptProperties;
import com.netflix.genie.web.properties.CommandSelectorManagedScriptProperties;
import com.netflix.genie.web.properties.ExecutionModeFilterScriptProperties;
import com.netflix.genie.web.properties.ScriptManagerProperties;
import com.netflix.genie.web.scripts.ClusterSelectorScript;
import com.netflix.genie.web.scripts.CommandSelectorManagedScript;
import com.netflix.genie.web.scripts.ExecutionModeFilterScript;
import com.netflix.genie.web.scripts.ManagedScript;
import com.netflix.genie.web.scripts.ScriptManager;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.TaskScheduler;

import javax.script.ScriptEngineManager;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Configuration for script extensions.
 *
 * @author mprimi
 * @since 4.0.0
 */
@Configuration
@EnableConfigurationProperties(
    {
        ClusterSelectorScriptProperties.class,
        CommandSelectorManagedScriptProperties.class,
        ExecutionModeFilterScriptProperties.class,
        ScriptManagerProperties.class,
    }
)
public class ScriptsAutoConfiguration {

    /**
     * Create a {@link ScriptManager} unless one exists.
     *
     * @param scriptManagerProperties properties
     * @param taskScheduler           task scheduler
     * @param resourceLoader          resource loader
     * @param meterRegistry           meter registry
     * @return a {@link ScriptManager}
     */
    @Bean
    @ConditionalOnMissingBean(ScriptManager.class)
    ScriptManager scriptManager(
        final ScriptManagerProperties scriptManagerProperties,
        @Qualifier("genieTaskScheduler") final TaskScheduler taskScheduler,
        final ResourceLoader resourceLoader,
        final MeterRegistry meterRegistry
    ) {
        return new ScriptManager(
            scriptManagerProperties,
            taskScheduler,
            Executors.newCachedThreadPool(),
            new ScriptEngineManager(),
            resourceLoader,
            meterRegistry
        );
    }

    /**
     * Create a {@link SmartInitializingSingleton} that "warms" known scripts so they're ready for execution on first
     * invocation.
     *
     * @param managedScripts the managed scripts, if any exist in context
     * @return A {@link ManagedScriptPreLoader} that runs after the application context is ready
     */
    @Bean
    public ManagedScriptPreLoader managedScriptPreLoader(final List<ManagedScript> managedScripts) {
        return new ManagedScriptPreLoader(managedScripts);
    }

    /**
     * Create a {@link ClusterSelectorScript} unless one exists.
     *
     * @param scriptManager    script manager
     * @param scriptProperties script properties
     * @param meterRegistry    meter registry
     * @return a {@link ClusterSelectorScript}
     */
    @Bean
    @ConditionalOnMissingBean(ClusterSelectorScript.class)
    @ConditionalOnProperty(value = ClusterSelectorScriptProperties.SOURCE_PROPERTY)
    ClusterSelectorScript clusterSelectorScript(
        final ScriptManager scriptManager,
        final ClusterSelectorScriptProperties scriptProperties,
        final MeterRegistry meterRegistry
    ) {
        return new ClusterSelectorScript(
            scriptManager,
            scriptProperties,
            GenieObjectMapper.getMapper(),
            meterRegistry
        );
    }

    /**
     * Create a {@link CommandSelectorManagedScript}  if necessary and one doesn't already exist.
     *
     * @param scriptManager                          script manager
     * @param commandSelectorManagedScriptProperties script properties
     * @param meterRegistry                          meter registry
     * @return a {@link CommandSelectorManagedScript}
     */
    @Bean
    @ConditionalOnMissingBean(CommandSelectorManagedScript.class)
    @ConditionalOnProperty(value = CommandSelectorManagedScriptProperties.SOURCE_PROPERTY)
    CommandSelectorManagedScript commandSelectormanagedScript(
        final ScriptManager scriptManager,
        final CommandSelectorManagedScriptProperties commandSelectorManagedScriptProperties,
        final MeterRegistry meterRegistry
    ) {
        return new CommandSelectorManagedScript(
            scriptManager,
            commandSelectorManagedScriptProperties,
            meterRegistry
        );
    }

    /**
     * Create a {@link ExecutionModeFilterScript}, unless one exists.
     *
     * @param scriptManager    script manager
     * @param scriptProperties script properties
     * @param meterRegistry    meter registry
     * @return a {@link ExecutionModeFilterScript}
     */
    @Bean
    @ConditionalOnMissingBean(ExecutionModeFilterScript.class)
    @ConditionalOnProperty(value = ExecutionModeFilterScriptProperties.SOURCE_PROPERTY)
    ExecutionModeFilterScript executionModeFilterScript(
        final ScriptManager scriptManager,
        final ExecutionModeFilterScriptProperties scriptProperties,
        final MeterRegistry meterRegistry
    ) {
        return new ExecutionModeFilterScript(
            scriptManager,
            scriptProperties,
            GenieObjectMapper.getMapper(),
            meterRegistry
        );
    }

    /**
     * A {@link SmartInitializingSingleton} that warms up the existing script beans so they are ready for execution.
     */
    static final class ManagedScriptPreLoader implements SmartInitializingSingleton {
        private List<ManagedScript> managedScripts;

        private ManagedScriptPreLoader(final List<ManagedScript> managedScripts) {
            this.managedScripts = managedScripts;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void afterSingletonsInstantiated() {
            this.managedScripts.forEach(ManagedScript::warmUp);
        }
    }
}
