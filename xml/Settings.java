package xml;

import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

public class Settings {

	/** デフォルトのファン速度 */
	private int[] defaultFanSpeed = {80, 80, 80, 80};

	/** ファン速度設定値 */
	private ArrayList<SortedMap<Integer, Integer>> fanSpeedSettings = new ArrayList<SortedMap<Integer, Integer>>();

	/** PC監視間隔 */
	private int monitorInterval = 1;

	/** CPU物理コア数 */
	private int cpuCore = 4;

	/** CPU名 */
	private String cpuName = "3770K";

	/** GPU名 */
	private String gpuName = "GTX 1080";

	/** OpenHardwareMonitorポート番号 */
	private String port = "8085";

	/** COMポート名 */
	private String comPort = "COM3";

	protected Settings(){
		//デフォルト設定
		TreeMap<Integer, Integer> map = new TreeMap<Integer, Integer>();
		map.put(0, 60);

		for(int i = 0 ; i < 4 ; i++){
			fanSpeedSettings.add(map);
		}
	}

	/**
	 * デフォルトのファン速度を取得します。
	 * @return デフォルトのファン速度
	 */
	public int[] getDefaultFanSpeed() {
	    return defaultFanSpeed;
	}

	/**
	 * デフォルトのファン速度を設定します。
	 * @param defaultFanSpeed デフォルトのファン速度
	 */
	public void setDefaultFanSpeed(int[] defaultFanSpeed) {
	    this.defaultFanSpeed = defaultFanSpeed;
	}

	/**
	 * ファン速度設定値を取得します。
	 * @return ファン速度設定値
	 */
	public ArrayList<SortedMap<Integer,Integer>> getFanSpeedSettings() {
	    return fanSpeedSettings;
	}

	/**
	 * ファン速度設定値を設定します。
	 * @param fanSpeedSettings ファン速度設定値
	 */
	public void setFanSpeedSettings(ArrayList<SortedMap<Integer,Integer>> fanSpeedSettings) {
	    this.fanSpeedSettings = fanSpeedSettings;
	}

	/**
	 * PC監視間隔を取得します。
	 * @return PC監視間隔
	 */
	public int getMonitorInterval() {
	    return monitorInterval;
	}

	/**
	 * PC監視間隔を設定します。
	 * @param monitorInterval PC監視間隔
	 */
	public void setMonitorInterval(int monitorInterval) {
	    this.monitorInterval = monitorInterval;
	}

	/**
	 * CPU物理コア数を取得します。
	 * @return CPU物理コア数
	 */
	public int getCpuCore() {
	    return cpuCore;
	}

	/**
	 * CPU物理コア数を設定します。
	 * @param cpuCore CPU物理コア数
	 */
	public void setCpuCore(int cpuCore) {
	    this.cpuCore = cpuCore;
	}

	/**
	 * CPU名を取得します。
	 * @return CPU名
	 */
	public String getCpuName() {
	    return cpuName;
	}

	/**
	 * CPU名を設定します。
	 * @param cpuName CPU名
	 */
	public void setCpuName(String cpuName) {
	    this.cpuName = cpuName;
	}

	/**
	 * GPU名を取得します。
	 * @return GPU名
	 */
	public String getGpuName() {
	    return gpuName;
	}

	/**
	 * GPU名を設定します。
	 * @param gpuName GPU名
	 */
	public void setGpuName(String gpuName) {
	    this.gpuName = gpuName;
	}

	/**
	 * OpenHardwareMonitorポート番号を取得します。
	 * @return OpenHardwareMonitorポート番号
	 */
	public String getPort() {
	    return port;
	}

	/**
	 * OpenHardwareMonitorポート番号を設定します。
	 * @param port OpenHardwareMonitorポート番号
	 */
	public void setPort(String port) {
	    this.port = port;
	}

	/**
	 * COMポート名を取得します。
	 * @return COMポート名
	 */
	public String getComPort() {
	    return comPort;
	}

	/**
	 * COMポート名を設定します。
	 * @param comPort COMポート名
	 */
	public void setComPort(String comPort) {
	    this.comPort = comPort;
	}
}
