/*
 * Copyright (c) 2024 sovity GmbH
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       sovity GmbH - initial API and implementation
 *
 */

package de.sovity.edc.e2e;

import de.sovity.edc.client.EdcClient;
import de.sovity.edc.extension.e2e.connector.remotes.api_wrapper.E2eTestScenario;
import de.sovity.edc.extension.e2e.junit.CeE2eTestExtension;
import de.sovity.edc.extension.e2e.junit.utils.Consumer;
import de.sovity.edc.extension.e2e.junit.utils.Provider;
import de.sovity.edc.extension.utils.junit.DisabledOnGithub;
import de.sovity.edc.utils.config.CeConfigProps;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockserver.integration.ClientAndServer;

import static de.sovity.edc.e2e.AlwaysTrueMigrationTest.testTransfer;

class AlwaysTrueMigrationReversedTest {

    @RegisterExtension
    private static final CeE2eTestExtension E2E_TEST_EXTENSION = CeE2eTestExtension.builder()
        .additionalModule(":launchers:connectors:sovity-dev")
        .consumerConfigCustomizer(config -> config.property(
            CeConfigProps.EDC_FLYWAY_ADDITIONAL_MIGRATION_LOCATIONS,
            "classpath:db/additional-test-data/always-true-policy-legacy"
        ))
        .providerConfigCustomizer(config -> config.property(
            CeConfigProps.EDC_FLYWAY_ADDITIONAL_MIGRATION_LOCATIONS,
            "classpath:db/additional-test-data/always-true-policy-migrated"
        ))
        .build();

    @Test
    @DisabledOnGithub
    void test_migrated_policy_working_test_legacy_policy_working(
        E2eTestScenario scenario,
        ClientAndServer mockServer,
        @Provider EdcClient providerClient,
        @Consumer EdcClient consumerClient
    ) {
        // assert correct policies
        testTransfer(scenario, mockServer, consumerClient);
    }
}
