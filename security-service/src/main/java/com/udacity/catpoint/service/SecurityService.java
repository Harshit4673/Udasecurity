package com.udacity.catpoint.service;

import com.udacity.catpoint.application.StatusListener;
import com.udacity.catpoint.data.AlarmStatus;
import com.udacity.catpoint.data.ArmingStatus;
import com.udacity.catpoint.data.SecurityRepository;
import com.udacity.catpoint.data.Sensor;
import com.udacity.catpoint.imageservice.ImageService;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

/**
 * Service that receives information about changes to the security system.
 * Responsible for forwarding updates to the repository and making decisions
 * about changing the current system state.
 *
 * This class contains the primary business logic for the security application
 * and is the main target for unit testing.
 */
public class SecurityService {

    private final ImageService imageService;
    private final SecurityRepository securityRepository;
    private final Set<StatusListener> statusListeners = new HashSet<>();
    private boolean catDetected = false;

    public SecurityService(SecurityRepository securityRepository, ImageService imageService) {
        this.securityRepository = securityRepository;
        this.imageService = imageService;
    }

    /**
     * Sets the arming status for the security system.
     * Updating the arming state may also affect the alarm status.
     *
     * @param armingStatus current arming mode
     */
    public void setArmingStatus(ArmingStatus armingStatus) {

        if (armingStatus == ArmingStatus.DISARMED) {

            setAlarmStatus(AlarmStatus.NO_ALARM);

        } else {

            new HashSet<>(securityRepository.getSensors())
                    .forEach(sensor -> {
                        sensor.setActive(false);
                        securityRepository.updateSensor(sensor);
                    });
        }

        securityRepository.setArmingStatus(armingStatus);

        if (armingStatus == ArmingStatus.ARMED_HOME && catDetected) {
            setAlarmStatus(AlarmStatus.ALARM);
        }
    }

    /**
     * Updates the alarm state depending on whether a cat is currently detected.
     *
     * @param catVisible true if a cat is detected in the image
     */
    private void updateCatDetectionStatus(Boolean catVisible) {

        catDetected = catVisible;

        if (catVisible && getArmingStatus() == ArmingStatus.ARMED_HOME) {

            setAlarmStatus(AlarmStatus.ALARM);

        } else if (!catVisible) {

            boolean sensorStillActive = securityRepository.getSensors().stream()
                    .anyMatch(Sensor::getActive);

            if (!sensorStillActive) {
                setAlarmStatus(AlarmStatus.NO_ALARM);
            }
        }

        statusListeners.forEach(listener -> listener.catDetected(catVisible));
    }
    /**
     * Registers a StatusListener for receiving system updates.
     *
     * @param statusListener listener to register
     */
    public void addStatusListener(StatusListener statusListener) {
        statusListeners.add(statusListener);
    }

    public void removeStatusListener(StatusListener statusListener) {
        statusListeners.remove(statusListener);
    }

    /**
     * Updates the alarm status and notifies listeners.
     *
     * @param status new alarm state
     */
    public void setAlarmStatus(AlarmStatus status) {

        securityRepository.setAlarmStatus(status);

        statusListeners.forEach(listener -> listener.notify(status));
    }

    /**
     * Updates the alarm state when a sensor becomes active.
     */
    private void handleSensorActivated() {

        if (securityRepository.getArmingStatus() == ArmingStatus.DISARMED) {
            return;
        }

        switch (securityRepository.getAlarmStatus()) {

            case NO_ALARM ->
                    setAlarmStatus(AlarmStatus.PENDING_ALARM);

            case PENDING_ALARM ->
                    setAlarmStatus(AlarmStatus.ALARM);

        }   
    }

    /**
     * Updates the alarm state when a sensor becomes inactive.
     * Returns to NO_ALARM only if all sensors are inactive while
     * the system is in PENDING_ALARM state.
     */
    private void handleSensorDeactivated() {

        if (securityRepository.getAlarmStatus() == AlarmStatus.PENDING_ALARM) {

            boolean activeSensorPresent = securityRepository.getSensors().stream()
                    .anyMatch(Sensor::getActive);

            if (!activeSensorPresent) {
                setAlarmStatus(AlarmStatus.NO_ALARM);
            }
        }
    }

    /**
     * Updates the activation status of a sensor and adjusts alarm status
     * whenever required.
     *
     * @param sensor sensor being updated
     * @param active current active state
     */
    public void changeSensorActivationStatus(Sensor sensor, Boolean active) {

        AlarmStatus currentAlarm = getAlarmStatus();

        boolean wasPreviouslyActive = sensor.getActive();

        sensor.setActive(active);

        securityRepository.updateSensor(sensor);

        if (currentAlarm == AlarmStatus.ALARM) {
            return;
        }

        if (wasPreviouslyActive && active
                && currentAlarm == AlarmStatus.PENDING_ALARM) {

            setAlarmStatus(AlarmStatus.ALARM);

        } else if (!wasPreviouslyActive && active) {

            handleSensorActivated();

        } else if (wasPreviouslyActive && !active) {

            handleSensorDeactivated();
        }
    }

    /**
     * Sends an image for analysis using the ImageService implementation.
     * The alarm state may change depending on cat detection results.
     *
     * @param cameraFrame current camera image
     */
    public void processImage(BufferedImage cameraFrame) {

        updateCatDetectionStatus(
                imageService.imageContainsCat(cameraFrame, 50.0f)
        );
    }

    public AlarmStatus getAlarmStatus() {
        return securityRepository.getAlarmStatus();
    }

    public Set<Sensor> getSensors() {
        return securityRepository.getSensors();
    }

    public void addSensor(Sensor sensor) {
        securityRepository.addSensor(sensor);
    }

    public void removeSensor(Sensor sensor) {
        securityRepository.removeSensor(sensor);
    }

    public ArmingStatus getArmingStatus() {
        return securityRepository.getArmingStatus();
    }
}