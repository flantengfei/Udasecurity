package com.udacity.security.service;

import com.udacity.imageservice.FakeImageService;
import com.udacity.security.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {

    public SecurityService securityService;

    @Mock
    public FakeImageService imageService;

    @Mock
    public SecurityRepository securityRepository;

    @Mock
    public Sensor sensor;

    @BeforeEach
    public void setUp() {
        securityService = new SecurityService(securityRepository, imageService);
    }

    @Test
    @Order(1)
    @DisplayName(
        "1. If alarm is armed and a sensor becomes activated, put the system into pending alarm status.")
    public void alarmArmed_sensorActivated_alarmStatusIsPending() {
        when(sensor.getActive()).thenReturn(false);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        securityService.changeSensorActivationStatus(sensor, true);
        verify(securityRepository, times(1)).setAlarmStatus(eq(AlarmStatus.PENDING_ALARM));
    }

    @Test
    @Order(2)
    @DisplayName(
        "2. If alarm is armed and a sensor becomes activated and the system is already pending alarm, " +
        "set the alarm status to alarm.")
    public void alarmArmed_sensorActivated_alarmStatusPending_alarmIsActive() {
        when(sensor.getActive()).thenReturn(false);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor, true);
        verify(securityRepository, times(1)).setAlarmStatus(eq(AlarmStatus.ALARM));
    }

    @Test
    @Order(3)
    @DisplayName("3. If pending alarm and all sensors are inactive, return to no alarm state.")
    public void pendingAlarm_sensorInactive_noAlarmIsActive() {
        when(sensor.getActive()).thenReturn(true);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor, false);
        verify(securityRepository, times(1)).setAlarmStatus(eq(AlarmStatus.NO_ALARM));
    }

    @Test
    @Order(4)
    @DisplayName("4. If alarm is active, change in sensor state should not affect the alarm state.")
    public void alarmActive_sensorChange_noAffectOnAlarmState() {
        when(sensor.getActive()).thenReturn(true);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        securityService.changeSensorActivationStatus(sensor, false);
        verify(securityRepository, times(0)).setAlarmStatus(any());
    }

    @Test
    @Order(5)
    @DisplayName("5. If a sensor is activated while already active and the system is in pending state, " +
        "change it to alarm state.")
    public void pendingAlarm_activeSensorActivated_alarmIsActive() {
        when(sensor.getActive()).thenReturn(true);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor, true);
        verify(securityRepository, times(1)).setAlarmStatus(eq(AlarmStatus.ALARM));
    }

    @Test
    @Order(6)
    @DisplayName("6. If a sensor is deactivated while already inactive, make no changes to the alarm state.")
    public void deactiveSensorDeactivated_alarmNoChange() {
        when(sensor.getActive()).thenReturn(false);
        securityService.changeSensorActivationStatus(sensor, false);
        verify(securityRepository, times(0)).setAlarmStatus(any());
    }

    @Test
    @Order(7)
    @DisplayName("7. If the image service identifies an image containing " +
        "a cat while the system is armed-home, put the system into alarm status.")
    public void armedHome_catDetected_alarmIsActive() {
        when(imageService.imageContainsCat(null, 50.0f)).thenReturn(true);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        securityService.processImage(null);
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    @Order(8)
    @DisplayName("8. If the image service identifies an image that does not contain a cat, " +
        "change the status to no alarm as long as the sensors are not active.")
    public void catNotDetected_alarmIsNotActive() {
        when(imageService.imageContainsCat(null, 50.0f)).thenReturn(false);
        securityService.processImage(null);
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    @Order(9)
    @DisplayName("9. If the system is disarmed, set the status to no alarm.")
    public void systemDisarmed_alarmStatusIsNoAlarm() {
        securityService.setArmingStatus(ArmingStatus.DISARMED);
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    @Order(10)
    @DisplayName("10. If the system is armed, reset all sensors to inactive.")
    public void systemArmed_setAllSensorsInactive() {
        when(securityRepository.getSensors()).thenReturn(new HashSet<>());
        securityService = spy(securityService);
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        verify(securityService).deactivateAllSensors();
    }

    @Test
    @Order(10)
    @DisplayName("11. If the system is armed-home while the camera shows a cat, " +
        "set the alarm status to alarm.")
    public void systemArmedHome_CatDetected_alarmIsActive() {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(null, 50.0f)).thenReturn(true);
        securityService.processImage(null);
        verify(securityRepository, times(1)).setAlarmStatus(eq(AlarmStatus.ALARM));
    }
}
