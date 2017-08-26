package xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

public class SettingReader {
	private static final String REGEX_FAN_SPEED = "^(0|12[0-7]|1[0-1][0-9]|[1-9][0-9])$";
	private static final String DEFAULT_FAN1 = "DefaultSpeedFan1";
	private static final String DEFAULT_FAN2 = "DefaultSpeedFan2";
	private static final String DEFAULT_FAN3 = "DefaultSpeedFan3";
	private static final String DEFAULT_FAN4 = "DefaultSpeedFan4";
	private static final String MONITOR_INTERVAL = "MonitorInterval";
	private static final String COM_PORT = "ArduinoSerialPortName";
	private static final String CPU_CORE = "CPUCore";
	private static final String CPU_NAME = "CPUName";
	private static final String GPU_NAME = "GPUName";
	private static final String OHM_PORT = "OpenHardwareMonitorPort";
	private static final String FAN1_TEMP = "Fan1TargetTemperature";
	private static final String FAN1_SPEED = "Fan1TargetSpeed";
	private static final String FAN2_TEMP = "Fan2TargetTemperature";
	private static final String FAN2_SPEED = "Fan3TargetSpeed";
	private static final String FAN3_TEMP = "Fan3TargetTemperature";
	private static final String FAN3_SPEED = "Fan3TargetSpeed";
	private static final String FAN4_TEMP = "Fan4TargetTemperature";
	private static final String FAN4_SPEED = "Fan4TargetSpeed";

	Properties option;
	Settings setting;
	private final String FILE_PATH;

	public SettingReader(String filePath) throws FileNotFoundException, IOException{
		this.FILE_PATH = filePath;
		try( FileInputStream configFile = new FileInputStream( new File(this.FILE_PATH) ) ){
			option = new Properties();
			option.loadFromXML(configFile);
		}
	}

	public Settings getSetting(boolean reOpen){
		if( setting == null || reOpen){
			setting = new Settings();

			/** デフォルトのファン速度 */
			int[] defaultFanSpeed = {60, 60, 60, 60};

			/** ファン速度設定値 */
			ArrayList<SortedMap<Integer, Integer>> fanSpeedSettings = new ArrayList<SortedMap<Integer, Integer>>();

			//デフォルト設定
			TreeMap<Integer, Integer> map = new TreeMap<Integer, Integer>();
			map.put(0, 60);

			for(int i = 0 ; i < 4 ; i++){
				fanSpeedSettings.add(map);
			}

			//COMポート名取得
			if( option.containsKey(COM_PORT) ){
				setting.setComPort(option.getProperty(COM_PORT));
			}

			//PC監視間隔取得
			if( option.containsKey(MONITOR_INTERVAL) && option.getProperty(MONITOR_INTERVAL).matches("\\d+")){
				setting.setMonitorInterval( Integer.parseInt(option.getProperty(MONITOR_INTERVAL)) );
			}

			//OpenHardwareMonitorポート取得
			if( option.containsKey(OHM_PORT) && option.getProperty(OHM_PORT).matches("\\d+") ){
				setting.setPort( option.getProperty(OHM_PORT) );
			}

			//CPU物理コア数取得
			if( option.containsKey(CPU_CORE) && option.getProperty(CPU_CORE).matches("\\d+") ){
				setting.setCpuCore( Integer.parseInt(option.getProperty(CPU_CORE)) );
			}

			//CPU名取得
			if( option.containsKey(CPU_NAME) ){
				setting.setCpuName( option.getProperty(CPU_NAME) );
			}

			//GPU名取得
			if( option.containsKey(GPU_NAME) ){
				setting.setGpuName( option.getProperty(GPU_NAME) );
			}


			//デフォルトファン速度取得
			if( option.containsKey(DEFAULT_FAN1) && option.getProperty(DEFAULT_FAN1).matches(REGEX_FAN_SPEED) ){
				defaultFanSpeed[0] = Integer.parseInt(option.getProperty(DEFAULT_FAN1));
			}

			if( option.containsKey(DEFAULT_FAN2) && option.getProperty(DEFAULT_FAN2).matches(REGEX_FAN_SPEED) ){
				defaultFanSpeed[1] = Integer.parseInt(option.getProperty(DEFAULT_FAN2));
			}

			if( option.containsKey(DEFAULT_FAN3) && option.getProperty(DEFAULT_FAN3).matches(REGEX_FAN_SPEED) ){
				defaultFanSpeed[2] = Integer.parseInt(option.getProperty(DEFAULT_FAN3));
			}

			if( option.containsKey(DEFAULT_FAN4) && option.getProperty(DEFAULT_FAN4).matches(REGEX_FAN_SPEED) ){
				defaultFanSpeed[3] = Integer.parseInt(option.getProperty(DEFAULT_FAN4));
			}

			setting.setDefaultFanSpeed(defaultFanSpeed);

			String[] speed;
			String[] temp;
			if( option.containsKey(FAN1_TEMP) && option.containsKey(FAN1_SPEED) ){
				temp = option.getProperty(FAN1_TEMP).split(",");
				speed = option.getProperty(FAN1_SPEED).split(",");
				if( temp.length == speed.length ){
					boolean isError = false;
					for(String val: temp){
						if(!val.matches("^\\d{1,3}$")){
							isError = true;
						}
					}

					for(String val: speed){
						if(!val.matches(REGEX_FAN_SPEED)){
							isError = true;
						}
					}

					if(!isError){
						TreeMap<Integer,Integer> targetFanSpeed = new TreeMap<Integer,Integer>();
						for(int i = 0 ; i < temp.length ; i++){
							targetFanSpeed.put(new Integer(temp[i]), new Integer(speed[i]));
						}

						fanSpeedSettings.set(0, targetFanSpeed);
					}
				}
			}

			if( option.containsKey(FAN2_TEMP) && option.containsKey(FAN2_SPEED) ){
				temp = option.getProperty(FAN2_TEMP).split(",");
				speed = option.getProperty(FAN2_SPEED).split(",");
				if( temp.length == speed.length ){
					boolean isError = false;
					for(String val: temp){
						if(!val.matches("^\\d{1,3}$")){
							isError = true;
						}
					}

					for(String val: speed){
						if(!val.matches(REGEX_FAN_SPEED)){
							isError = true;
						}
					}

					if(!isError){
						TreeMap<Integer,Integer> targetFanSpeed = new TreeMap<Integer,Integer>();
						for(int i = 0 ; i < temp.length ; i++){
							targetFanSpeed.put(new Integer(temp[i]), new Integer(speed[i]));
						}

						fanSpeedSettings.set(1, targetFanSpeed);
					}
				}
			}

			if( option.containsKey(FAN3_TEMP) && option.containsKey(FAN3_SPEED) ){
				temp = option.getProperty(FAN3_TEMP).split(",");
				speed = option.getProperty(FAN3_SPEED).split(",");
				if( temp.length == speed.length ){
					boolean isError = false;
					for(String val: temp){
						if(!val.matches("^\\d{1,3}$")){
							isError = true;
						}
					}

					for(String val: speed){
						if(!val.matches(REGEX_FAN_SPEED)){
							isError = true;
						}
					}

					if(!isError){
						TreeMap<Integer,Integer> targetFanSpeed = new TreeMap<Integer,Integer>();
						for(int i = 0 ; i < temp.length ; i++){
							targetFanSpeed.put(new Integer(temp[i]), new Integer(speed[i]));
						}

						fanSpeedSettings.set(2, targetFanSpeed);
					}
				}
			}

			if( option.containsKey(FAN4_TEMP) && option.containsKey(FAN4_SPEED) ){
				temp = option.getProperty(FAN4_TEMP).split(",");
				speed = option.getProperty(FAN4_SPEED).split(",");
				if( temp.length == speed.length ){
					boolean isError = false;
					for(String val: temp){
						if(!val.matches("^\\d{1,3}$")){
							isError = true;
						}
					}

					for(String val: speed){
						if(!val.matches(REGEX_FAN_SPEED)){
							isError = true;
						}
					}

					if(!isError){
						TreeMap<Integer,Integer> targetFanSpeed = new TreeMap<Integer,Integer>();
						for(int i = 0 ; i < temp.length ; i++){
							targetFanSpeed.put(new Integer(temp[i]), new Integer(speed[i]));
						}

						fanSpeedSettings.set(3, targetFanSpeed);
					}
				}
			}

			setting.setFanSpeedSettings(fanSpeedSettings);

		}

		return setting;
	}

}
