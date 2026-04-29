/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package sim;

import devices.api.DataAcquisitionDevice;
import devices.api.DirectConnectDXHardwareLink;
import infrastructure.logger.LogLevel;
import infrastructure.logger.Logger;
import infrastructure.subscribers.PoliceStationLink;
import infrastructure.subscribers.SecurityTeamPhoneAppAlarm;
import infrastructure.subscribers.Subscriber;
import infrastructure.subscribers.SubscriberStatus;
import service.receiver.ReceiverService;

/**
 *<h3>Hardware Link Simulation Used to Connect to Receivers</h3>
 * Simulates a real connection to a hardware link connected via Direct Connect DX
 */
public class DirectConnectDXHardwareLinkSim implements DirectConnectDXHardwareLink {
    private ReceiverService receiverService;
    private final Logger logger;

    public DirectConnectDXHardwareLinkSim(Logger logger) {
        this.logger = logger;
    }

    /**
     * @param receiverService the ReceiverService being injected via setter injection
     */
    public void setReceiverService(ReceiverService receiverService) {
        this.receiverService = receiverService;
    }

    /**
     * Links connection with randomly generated device for a given int Id
     */
    @Override
    public void enableLink(int Id) {
        receiverService.saveReceiver(createReceiver(Id));
        logger.log("A new receiver of Id " + Id + " has been enabled as a receiver", LogLevel.INFO);
    }

    @Override
    public boolean isDeviceResponsive(DataAcquisitionDevice dataAcquisitionDevice) {
        return false;
    }

    /**
     * Randomly creates a new Receiver Link. In a real system this method would pair with a real connection to another receiver device.
     */
    private Subscriber createReceiver(int Id) {
        double rand = Math.random();
        if (rand < 0.5) {
            return new PoliceStationLink(Id, SubscriberStatus.ACTIVE);
        } else {
            return new SecurityTeamPhoneAppAlarm(Id, SubscriberStatus.ACTIVE);
        }
    }
}
