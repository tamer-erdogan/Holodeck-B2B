package org.holodeckb2b.common.testhelpers.events;

import org.holodeckb2b.interfaces.eventprocessing.IMessageProcessingEvent;
import org.holodeckb2b.interfaces.eventprocessing.IMessageProcessingEventHandler;

/**
 * Created at 16:37 15.07.17
 *
 * @author Timur Shakuov (t.shakuov at gmail.com)
 */
class TestEventHandler implements IMessageProcessingEventHandler {

    @Override
    public void handleEvent(IMessageProcessingEvent event)
            throws IllegalArgumentException {

    }
}
