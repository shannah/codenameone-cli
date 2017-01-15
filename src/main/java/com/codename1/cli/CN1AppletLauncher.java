/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codename1.cli;

import com.codename1.io.JSONParser;
import com.codename1.templatebrowser.TemplateBrowser;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author shannah
 */
public class CN1AppletLauncher {
    private String jarUrl;
    private String mainClass;
    private boolean blockAndWait=false;
    
    
    public CN1AppletLauncher(String jarUrl, String mainClass) {
        this.jarUrl = jarUrl;
        this.mainClass = mainClass;
    }
    
    public static CN1AppletLauncher parseURL(String url) {
        String[] parts = url.split("#");
        return new CN1AppletLauncher(parts[0], parts[1]);
    }
    
    public void setBlockAndWait(boolean blockAndWait) {
        this.blockAndWait = blockAndWait;
    }
    
    public void start() {
        try {
                File tmpDir = new File(System.getProperty("user.home"), ".codenameone-cli");
                if (!tmpDir.exists()) {
                    tmpDir.mkdir();
                }
                String jdeployBasedir = System.getProperty("jdeploy.base");
                double version = 0;
                if (jdeployBasedir != null) {
                    File basedir = new File(jdeployBasedir);
                    File packageJson =  new File(basedir.getParentFile(), "package.json");
                    if (packageJson.exists()) {
                        JSONParser p = new JSONParser();
                        
                        try {
                            Map<String,Object> packageJsonMap = p.parseJSON(new InputStreamReader(new FileInputStream(packageJson), "UTF-8"));
                            String packageVersion = (String)packageJsonMap.get("version");
                            if (packageVersion != null) {
                                String[] parts = packageVersion.split("\\.");
                                double place = 1.0;
                                for (String part : parts) {
                                    version += (place * Integer.parseInt(part));
                                    place /= 100;
                                }
                            }
                            System.out.println("Version is "+packageVersion);
                        } catch (UnsupportedEncodingException ex) {
                            Logger.getLogger(TemplateBrowser.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            Logger.getLogger(TemplateBrowser.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                
                File versionFile = new File(tmpDir, "version.txt");
                double cacheVersion = 0;
                if (versionFile.exists()) {
                    try {
                        String versionStr = FileUtils.readFileToString(versionFile);
                        cacheVersion = Double.parseDouble(versionStr.trim());
                    } catch (IOException ex) {
                        Logger.getLogger(TemplateBrowser.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                File javaSE = new File(tmpDir, "JavaSE.jar");
                if (!javaSE.exists() || cacheVersion < version) {
                    
                    try {
                        FileUtils.copyInputStreamToFile(CodenameOneCLI.class.getResourceAsStream("JavaSE.jar"), javaSE);
                        FileUtils.writeStringToFile(versionFile, String.valueOf(version));
                        
                    } catch (IOException ex) {
                        Logger.getLogger(TemplateBrowser.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                String jarPath = jarUrl;
                if (jarUrl.startsWith("http:") || jarUrl.startsWith("https:")) {
                    File tmpJar = File.createTempFile("cn1-tmp-jar", ".jar");
                    URL url = new URL(jarUrl);
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    conn.setInstanceFollowRedirects(true);
                    FileUtils.copyInputStreamToFile(conn.getInputStream(), tmpJar);
                    tmpJar.deleteOnExit();
                    jarPath = tmpJar.getAbsolutePath();
                    
                    
                }
                
                ProcessBuilder pb = new ProcessBuilder();
                pb.command("java", "-cp", javaSE.getAbsolutePath() + File.pathSeparator + jarPath, "com.codename1.impl.javase.Simulator", mainClass);
                pb.directory(new File(jarPath).getParentFile());
                pb.inheritIO();
                Process p = pb.start();
                if (blockAndWait) {
                    p.waitFor();
                }
                //p.waitFor();
            } catch (IOException ex) {
                Logger.getLogger(TemplateBrowser.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
            Logger.getLogger(CN1AppletLauncher.class.getName()).log(Level.SEVERE, null, ex);
        }// catch (InterruptedException ex) {
            
    }
}
