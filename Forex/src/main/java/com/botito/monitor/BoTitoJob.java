package com.botito.monitor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.botito.run.RunBotito;

public class BoTitoJob implements Job {

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {

        // do something
    	RunBotito runBoTito = RunBotito.getInstance();
    	runBoTito.setPathCSV(BoTitoPath.getPath());
    	runBoTito.readFiles();


	}

	

	private void readProperties() {
		try {
		   
			/**Creamos un Objeto de tipo Properties*/
		    Properties propiedades = new Properties();
		    
		    /**Cargamos el archivo desde la ruta especificada*/
		    propiedades
		     .load(new FileInputStream(
		       "D:/HENAO/codejavu/Workspace/PruebaProperties/src/properties/archivo.properties"));
		 
		    /**Obtenemos los parametros definidos en el archivo*/
		    String nombre = propiedades.getProperty("nombre");
		    String pagina = propiedades.getProperty("pagina");
		 
		    /**Imprimimos los valores*/
		    System.out.println("Nombre: "+nombre + "\n" +"Pagina: "+ pagina);
		    ////home/andres/.wine/drive_c/Archivos de programa/Ava MetaTrader/MQL4
	    
	   } catch (FileNotFoundException e) {
		   System.out.println("Error, El archivo no exite");
	   } catch (IOException e) {
		   System.out.println("Error, No se puede leer el archivo");
	   }
	}

}
