/*
 *  Copyright (c) 2024 sovity GmbH
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

package de.sovity.edc.extension.contacttermination;

import de.sovity.edc.ext.db.jooq.Tables;
import de.sovity.edc.extension.contacttermination.query.ContractAgreementIsTerminatedQuery;
import de.sovity.edc.extension.contacttermination.query.ContractAgreementTerminationDetailsQuery;
import de.sovity.edc.extension.contacttermination.query.TerminateContractQuery;
import de.sovity.edc.extension.db.directaccess.DslContextFactory;
import de.sovity.edc.extension.messenger.SovityMessenger;
import de.sovity.edc.extension.messenger.SovityMessengerRegistry;
import de.sovity.edc.utils.config.CeConfigProps;
import lombok.val;
import org.eclipse.edc.connector.controlplane.transfer.spi.observe.TransferProcessObservable;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provides;
import org.eclipse.edc.spi.agent.ParticipantAgentService;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;


@Provides(ContractAgreementTerminationService.class)
public class ContractTerminationExtension implements ServiceExtension {

    @Inject
    private DslContextFactory dslContextFactory;

    @Inject
    private Monitor monitor;

    @Inject
    private SovityMessenger sovityMessenger;

    @Inject
    private SovityMessengerRegistry messengerRegistry;

    @Inject
    private TransferProcessObservable observable;

    @Inject
    private ParticipantAgentService participantAgentService;

    @Override
    public void initialize(ServiceExtensionContext context) {
        val terminationService = setupTerminationService(context);
        setupMessenger(terminationService);
        setupTransferPrevention();
    }

    private ContractAgreementTerminationService setupTerminationService(ServiceExtensionContext context) {

        val config = context.getConfig();
        val contractAgreementTerminationDetailsQuery = new ContractAgreementTerminationDetailsQuery();
        val terminateContractQuery = new TerminateContractQuery();

        val terminationService = new ContractAgreementTerminationService(
            sovityMessenger,
            contractAgreementTerminationDetailsQuery,
            terminateContractQuery,
            monitor,
            CeConfigProps.EDC_PARTICIPANT_ID.getStringOrEmpty(config)
        );

        context.registerService(ContractAgreementTerminationService.class, terminationService);

        return terminationService;
    }

    private void setupMessenger(ContractAgreementTerminationService terminationService) {
        messengerRegistry.register(
            ContractTerminationMessage.class,
            (claims, termination) ->
                dslContextFactory.transactionResult(dsl ->
                    terminationService.terminateAgreementAsCounterparty(
                        dsl,
                        participantAgentService.createFor(claims).getIdentity(),
                        buildTerminationRequest(termination))));
    }

    private static ContractTerminationParam buildTerminationRequest(ContractTerminationMessage message) {
        return new ContractTerminationParam(
            message.getContractAgreementId(),
            message.getDetail(),
            message.getReason()
        );
    }

    private void setupTransferPrevention() {
        observable.registerListener(
            new TransferProcessBlocker(
                dslContextFactory,
                new ContractAgreementIsTerminatedQuery()));
    }
}
