package com.udacity.catpoint.service;

import com.udacity.catpoint.data.AlarmStatus;
import com.udacity.catpoint.data.ArmingStatus;
import com.udacity.catpoint.data.SecurityRepository;
import com.udacity.catpoint.data.Sensor;
import com.udacity.catpoint.data.SensorType;
import com.udacity.catpoint.imageservice.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {
    @Mock
    private SecurityRepository securityRepository;

    @Mock
    private ImageService imageService;

    private SecurityService securityService;

    @BeforeEach
    void setUp() {
        securityService = new SecurityService(securityRepository, imageService);
    }

    private Sensor buildSensor(String label, boolean activeState) {
        Sensor sensor = new Sensor(label, SensorType.DOOR);
        sensor.setActive(activeState);
        return sensor;
    }
    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_HOME", "ARMED_AWAY"})
    void sensorActivatedWhenArmed_setsPendingState(ArmingStatus armingStatus) {

        Sensor sensor = buildSensor("Front Door", false);

        when(securityRepository.getArmingStatus()).thenReturn(armingStatus);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);

        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }
    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_HOME", "ARMED_AWAY"})
    void activeSensorDuringPendingState_triggersAlarm(ArmingStatus armingStatus) {

        Sensor sensor = buildSensor("Front Door", false);

        when(securityRepository.getArmingStatus()).thenReturn(armingStatus);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    void inactiveSensorsWhilePending_changesToNoAlarm() {

        Sensor sensor = buildSensor("Front Door", true);

        Set<Sensor> sensors = Set.of(sensor);

        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        when(securityRepository.getSensors()).thenReturn(sensors);

        securityService.changeSensorActivationStatus(sensor, false);

        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    void pendingAlarmWithAnotherActiveSensor_keepsPendingState() {

        Sensor sensor1 = buildSensor("Front Door", true);
        Sensor sensor2 = buildSensor("Back Window", true);

        Set<Sensor> sensors = new HashSet<>(Set.of(sensor1, sensor2));

        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        when(securityRepository.getSensors()).thenReturn(sensors);

        securityService.changeSensorActivationStatus(sensor1, false);

        verify(securityRepository, never()).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    void sensorActivationDuringAlarm_doesNotChangeState() {

        Sensor sensor = buildSensor("Front Door", false);

        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);

        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository, never()).setAlarmStatus(any());
    }

    @Test
    void sensorDeactivationDuringAlarm_doesNotChangeState() {

        Sensor sensor = buildSensor("Front Door", true);

        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);

        securityService.changeSensorActivationStatus(sensor, false);

        verify(securityRepository, never()).setAlarmStatus(any());
    }

    @Test
    void reactivatingActiveSensorWhilePending_setsAlarm() {

        Sensor sensor = buildSensor("Front Door", true);

        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    void deactivatingInactiveSensor_keepsAlarmUnchanged() {

        Sensor sensor = buildSensor("Front Door", false);

        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);

        securityService.changeSensorActivationStatus(sensor, false);

        verify(securityRepository, never()).setAlarmStatus(any());
    }

    @Test
    void catDetectedWhileHomeArmed_setsAlarmState() {

        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);

        securityService.processImage(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    void noCatDetectedAndSensorsInactive_setsNoAlarm() {

        Set<Sensor> sensors = Set.of(buildSensor("Front Door", false));

        when(securityRepository.getSensors()).thenReturn(sensors);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(false);

        securityService.processImage(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));

        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    void noCatDetectedButSensorsActive_keepsCurrentAlarmState() {

        Set<Sensor> sensors = Set.of(buildSensor("Front Door", true));

        when(securityRepository.getSensors()).thenReturn(sensors);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(false);

        securityService.processImage(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));

        verify(securityRepository, never()).setAlarmStatus(any());
    }

    @Test
    void disarmingSystem_setsNoAlarmState() {

        securityService.setArmingStatus(ArmingStatus.DISARMED);

        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_HOME", "ARMED_AWAY"})
    void armingSystem_resetsSensorsToInactive(ArmingStatus armingStatus) {

        Sensor sensor1 = buildSensor("Front Door", true);
        Sensor sensor2 = buildSensor("Back Window", true);

        Set<Sensor> sensors = new HashSet<>(Set.of(sensor1, sensor2));

        when(securityRepository.getSensors()).thenReturn(sensors);

        securityService.setArmingStatus(armingStatus);

        assertFalse(sensor1.getActive(), "Sensor should reset to inactive");
        assertFalse(sensor2.getActive(), "Sensor should reset to inactive");
    }

    @Test
    void armedHomeWhileCatAlreadyPresent_setsAlarm() {

        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);

        securityService.processImage(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));

        when(securityRepository.getSensors()).thenReturn(new HashSet<>());

        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }
}