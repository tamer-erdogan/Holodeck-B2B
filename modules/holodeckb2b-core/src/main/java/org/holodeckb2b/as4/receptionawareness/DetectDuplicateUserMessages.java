/**
 * Copyright (C) 2014 The Holodeck B2B Team, Sander Fieten
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.holodeckb2b.as4.receptionawareness;

import org.apache.axis2.description.HandlerDescription;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.holodeckb2b.common.handler.AbstractUserMessageHandler;
import org.holodeckb2b.common.handler.MessageProcessingContext;
import org.holodeckb2b.interfaces.as4.pmode.IAS4Leg;
import org.holodeckb2b.interfaces.as4.pmode.IReceptionAwareness;
import org.holodeckb2b.interfaces.core.HolodeckB2BCoreInterface;
import org.holodeckb2b.interfaces.persistency.PersistenceException;
import org.holodeckb2b.interfaces.persistency.entities.IUserMessageEntity;
import org.holodeckb2b.interfaces.pmode.ILeg;
import org.holodeckb2b.interfaces.pmode.IPMode;
import org.holodeckb2b.interfaces.processingmodel.ProcessingState;
import org.holodeckb2b.module.HolodeckB2BCore;

/**
 * Is the <i>IN_FLOW</i> handler responsible for detecting and when requested eliminating duplicate <i>user messages</i>.
 * This functionality is part of the <i>Reception Awareness</i> feature specified in the AS4 profile (see section 3.2).
 * <p>The detection of duplicates is done by checking for an existing {@link IUserMessageEntity} with the same
 * <code>MessageId</code> and which is in {@link ProcessingState#DELIVERED} state. This means the detection window
 * is determined by the time messages stay in the message log.
 * <p>How a duplicate should be handled is configured by the P-Mode parameter <b>ReceptionAwareness.DuplicateDetection.Eliminate</b>.
 * When set to <code>true</code> the duplicate will not be processed and its processing state set to {@link ProcessingState#DUPLICATE}.
 * Because the duplicate may be a retry due to a missing Receipt signal a new Receipt will be sent as response. This is
 * done by marking in the message context this message unit as delivered.
 *
 * @author Sander Fieten (sander at holodeck-b2b.org)
 */
public class DetectDuplicateUserMessages extends AbstractUserMessageHandler {

    /**
     * Duplicates will always be logged to a special log. Using the logging
     * configuration users can decide if this logging should be enabled and
     * how duplicates should be logged.
     */
    private static Log     duplicateLog;

    @Override
	public void init(HandlerDescription handlerdesc) {
    	super.init(handlerdesc);
    	duplicateLog = LogFactory.getLog("org.holodeckb2b.msgproc.duplicates." + handledMsgProtocol);
    }
    
    @Override
    protected InvocationResponse doProcessing(final IUserMessageEntity um, final MessageProcessingContext procCtx,
    										  final Log log) throws PersistenceException {
        // First determine if duplicate check must be executed for this UserMessage
        //
        boolean detectDups = false;
        log.trace("Check if duplicate check must be executed");

        // Get P-Mode configuration
        final IPMode pmode = HolodeckB2BCoreInterface.getPModeSet().get(um.getPModeId());
        if (pmode == null) {
            // The P-Mode configurations has changed and does not include this P-Mode anymore, assume no receipt
            // is needed
            log.error("P-Mode " + um.getPModeId() + " not found in current P-Mode set!"
                        + "Unable to determine if receipt is needed for message [msgId=" + um.getMessageId() + "]");
            return InvocationResponse.CONTINUE;
        }
        // Currently we only support one-way MEPs so the leg is always the first one
        final ILeg leg = pmode.getLeg(um.getLeg() != null ? um.getLeg() : ILeg.Label.REQUEST);

        // Duplicate detection is part of the AS4 Reception Awareness feature which can only be configured on a leg
        // of type ILegAS4, so check type
        if (!(leg instanceof IAS4Leg))
            // Not an AS4 leg, so no duplicate detection
            detectDups = false;
        else {
            // Get configuration of Reception Awareness feature
            final IReceptionAwareness raConfig = ((IAS4Leg) leg).getReceptionAwareness();
            if (raConfig != null)
                detectDups = raConfig.useDuplicateDetection();
            else
                detectDups = false;
        }

        if (!detectDups) {
            log.debug("Duplicate detection not enabled, skipping check.");
            return InvocationResponse.CONTINUE;
        } else {
            final String msgId = um.getMessageId();
            log.trace("Duplicate detection enabled. Check if this Usermessage [msgId=" + msgId
                        + "] has already been delivered");

            boolean isDuplicate = false;
            isDuplicate = HolodeckB2BCore.getQueryManager().isAlreadyProcessed(um);
            if (isDuplicate) {
                log.debug("UserMessage [msgId=" + msgId + "] has already been processed");
                // Also log in special duplicate log
                duplicateLog.info("UserMessage [msgId=" + msgId
                                            + "] is a duplicate of an already processed message");

                log.trace("Update processing state to duplicate");
                HolodeckB2BCore.getStorageManager().setProcessingState(um, ProcessingState.DUPLICATE);
                // Raise message processing event to inform other components of the duplicate message
                HolodeckB2BCore.getEventProcessor().raiseEvent(new DuplicateReceivedEvent(um));
            }

            return InvocationResponse.CONTINUE;
        }
    }
}
