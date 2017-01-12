/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codename1.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

/**
 *
 * @author shannah
 */
public class EclipseWrapper {
    private final File eclipse;
    private static final String eclipseRepository = "http://www.codenameone.com/files/eclipse/site.xml";
    
    public EclipseWrapper(File eclipseLocation) {
        eclipse = eclipseLocation;
    }
    
    public void installCodenameOnePlugin() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(eclipse.getAbsolutePath(), "-clean",
                "-purgeHistory",
                "-application",
                "org.eclipse.equinox.p2.director",
                "-noSplash",
                "-repository",
                eclipseRepository,
                "-installIUs",
                "CodenameOneFeature.feature.group,CodenameOneFeature.feature.jar,CodenameOnePlugin"
        );
        pb.inheritIO();
        Process p = pb.start();
        p.waitFor();
    }
    
    public boolean isCodenameOnePluginInstalled() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(eclipse.getAbsolutePath(), "-clean",
                "-purgeHistory",
                "-application",
                "org.eclipse.equinox.p2.director",
                "-noSplash",
                "-listInstalledRoots"
        );
        
        Process process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder builder = new StringBuilder();
        String line = null;
        while ( (line = reader.readLine()) != null) {
        builder.append(line);
        builder.append(System.getProperty("line.separator"));
        }
        String result = builder.toString();
        return result.contains("CodenameOneFeature");
    }
    
    public void openProject(File project) throws InterruptedException, IOException {
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(eclipse.getAbsolutePath(), 
                "-nosplash",
                "-application", "org.eclipse.cdt.managedbuilder.core.headlessbuild",
                "-import", project.getAbsolutePath());
        pb.inheritIO();
        Process p = pb.start();
        p.waitFor();
    }
    
    
}
