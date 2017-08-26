import java.io.IOException;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import main.CoreController;

/**
 * ファンコントローラ用ドライバ。
 * 使用プラグイン:RXTX,JSONIC
 * @author oilyoil
 */
public class Driver {

	public static void main(String[] args) {
		CoreController controller;
		try {
			controller = CoreController.getInstance();
			controller.start();
		} catch (PortInUseException | NoSuchPortException | UnsupportedCommOperationException
				| InterruptedException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

}
