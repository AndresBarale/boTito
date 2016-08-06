package com.botito.run.test;

import junit.framework.TestCase;

import org.junit.Test;

public class RunBotitoTest extends TestCase {
	
	@Test
	public void testName() throws Exception {
		RunBotito runboTito = RunBotito.getInstance();
		runboTito.setPathCSV("/home/andres/.wine/drive_c/Archivos de programa/Ava MetaTrader/tester/files/");
		runboTito.readFiles();
	}


}
