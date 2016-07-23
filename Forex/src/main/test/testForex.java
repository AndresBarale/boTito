import com.botito.run.RunBotito;


public class testForex {
	

	
	public static void main(String[] args) {
		RunBotito runBotito = new RunBotito();
		runBotito.setPathCSV("/home/andres/.wine/drive_c/Archivos de programa/Ava MetaTrader/MQL4/Files/");
		runBotito.readFiles();
	}

}
