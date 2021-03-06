/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.android.apps.mytracks.services.sensors;

import android.content.Context;
import android.util.Log;

import com.google.android.apps.mytracks.services.sensors.ant.AntSensorManager;
import com.google.android.apps.mytracks.services.sensors.ble.BluetoothLeManager;
import com.google.android.apps.mytracks.services.sensors.ble.CscSensor;
import com.google.android.apps.mytracks.services.sensors.ble.HrmSensor;
import com.google.android.apps.mytracks.util.AnalyticsUtils;
import com.google.android.apps.mytracks.util.PreferencesUtils;

import br.com.bioscada.apps.mytracks.R;

/**
 * A factory of {@link SensorManager}.
 *
 * @author Sandor Dornbush
 */
public class SensorManagerFactory {

  private static SensorManager systemSensorManager = null;
  private static SensorManager tempSensorManager = null;

  private SensorManagerFactory() {}

  /**
   * Gets the system sensor manager.
   *
   * @param context the context
   */
  public static SensorManager getSystemSensorManager(Context context) {
    releaseTempSensorManager();
    releaseSystemSensorManager();
    systemSensorManager = getSensorManager(context, true);
    if (systemSensorManager != null) {
      systemSensorManager.startSensor();
    }
    return systemSensorManager;
  }

  /**
   * Releases the system sensor manager.
   */
  public static void releaseSystemSensorManager() {
    if (systemSensorManager != null) {
      systemSensorManager.stopSensor();
    }
    systemSensorManager = null;
  }

  /**
   * Gets the temp sensor manager.
   *
   * @param context
   */
  public static SensorManager getTempSensorManager(Context context) {
    releaseTempSensorManager();
    if (systemSensorManager != null) {
      return null;
    }
    tempSensorManager = getSensorManager(context, false);
    if (tempSensorManager != null) {
      tempSensorManager.startSensor();
    }
    return tempSensorManager;
  }

  /**
   * Releases the temp sensor manager.
   */
  public static void releaseTempSensorManager() {
    if (tempSensorManager != null) {
      tempSensorManager.stopSensor();
    }
    tempSensorManager = null;
  }

  /**
   * Gets the sensor manager.
   *
   * @param context the context
   */
  private static SensorManager getSensorManager(Context context, boolean sendPageViews) {
    String sensorType = PreferencesUtils.getString(
        context, R.string.sensor_type_key, PreferencesUtils.SENSOR_TYPE_DEFAULT);

    if (sensorType.equals(context.getString(R.string.sensor_type_value_ant))) {
      if (sendPageViews) {
        AnalyticsUtils.sendPageViews(context, AnalyticsUtils.SENSOR_ANT);
      }
      return new AntSensorManager(context);
    } 
    else if (sensorType.equals(context.getString(R.string.sensor_type_value_zephyr))) {
      if (sendPageViews) {
        AnalyticsUtils.sendPageViews(context, AnalyticsUtils.SENSOR_ZEPHYR);
      }
      return new ZephyrSensorManager(context);
    } 
    else if (sensorType.equals(context.getString(R.string.sensor_type_value_neurosky))) {
      if (sendPageViews) {
        AnalyticsUtils.sendPageViews(context, AnalyticsUtils.SENSOR_NEUROSKY);
      }
      return new NeuroskySensorManager(context);
    } 
    else if (sensorType.equals(context.getString(R.string.sensor_type_value_polar))) {
      if (sendPageViews) {
        AnalyticsUtils.sendPageViews(context, AnalyticsUtils.SENSOR_POLAR);
      }

      return new PolarSensorManager(context); 
    } else if (sensorType.equals(context.getString(R.string.sensor_type_value_ble))) {
      if (sendPageViews) {
        AnalyticsUtils.sendPageViews(context, AnalyticsUtils.SENSOR_BLE);
      }
      //We only support one device for now, try HRM first
      String addr;
      addr = PreferencesUtils.getString(
          context, R.string.ble_hrm_sensor_id_key, PreferencesUtils.BLUETOOTH_SENSOR_DEFAULT);
      
      if (addr != null && !addr.trim().equals(""))
      {
        return new BluetoothLeManager(context, addr, HrmSensor.INSTANCE);
      }
      //Next try CSC
      addr = PreferencesUtils.getString(
          context, R.string.ble_csc_sensor_id_key, PreferencesUtils.BLUETOOTH_SENSOR_DEFAULT);
      
      if (addr != null && !addr.trim().equals(""))
      {
        return new BluetoothLeManager(context, addr, CscSensor.INSTANCE);
      }
      return null; 
    }


    return null;
  }
}
