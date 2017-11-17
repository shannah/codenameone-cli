/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codename1.cli;

import com.codename1.templatebrowser.TemplateBrowser;
import com.codename1.templatebrowser.TemplateBrowser.TemplateBrowserConnector;
import com.codename1.xml.Element;
import com.codename1.xml.XMLParser;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import javax.swing.JFrame;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.util.Zip4jUtil;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;


/**
 *
 * @author shannah
 */
public class CodenameOneCLI {
    
    private static String GIT="git";
    private static String ANT="ant";
    private boolean logcatVerbose;
    private boolean skipBuild; // Flag to tell it to skip the build if the build is already done.

    
    
    public static enum IDE {
        Netbeans,
        Eclipse,
        IntelliJ;
        
        public static IDE fromString(String str) {
            switch (str.toLowerCase()) {
                case "netbeans": return Netbeans;
                case "eclipse": return Eclipse;
                case "intellij": return IntelliJ;
                default: throw new RuntimeException("No IDE found with that name: "+str);
            }
        }
    }
    
    public static enum Command {
        Create
    }
    
    private final File dir;
    
    private CodenameOneCLI(File directory) {
        this.dir = directory;
    }
    
    private void createNetbeansProjectAt(File dest, String packageName, String mainClass) throws IOException {
        try {
            File zipDest = new File(dest.getAbsolutePath() + ".zip");
            InputStream is = CodenameOneCLI.class.getResourceAsStream("NetbeansProjectTemplate.zip");
            FileUtils.copyInputStreamToFile(is, zipDest);
            //ZipFile zip = new ZipFile()
            ZipFile zip = new ZipFile(zipDest);
            
            File tmp = File.createTempFile(packageName, "tmp");
            tmp.delete();
            
            zip.extractAll(tmp.getAbsolutePath());
            
            File extractedDir = new File(tmp, "NetbeansProjectTemplate");
            FileUtils.moveDirectory(extractedDir, dest);
            
            FileUtils.deleteDirectory(tmp);
            
            File cn1Settings = new File(dest, "codenameone_settings.properties");
            Properties props = new Properties();
            props.load(new FileInputStream(cn1Settings));
            props.put("codename1.packageName", packageName);
            props.put("codename1.ios.appid", "Q5GHSKAL2F."+packageName);
            props.put("codename1.mainName", mainClass);
            props.put("codename1.displayName", mainClass);
            props.put("codename1.ios.release.provision", "");
            props.put("codename1.ios.release.certificate", "");
            props.put("codename1.ios.debug.provision", "");
            props.put("codename1.windows.certificate", "");
            props.put("libVersion", "");
            props.store(new FileOutputStream(cn1Settings), "Written by CodenameOneCLI");
            
            replaceRecursive(dest, "NetbeansProjectTemplate", dest.getName());
            
            copyJarsToProject(dest);
            
            System.out.println("Netbeans project created at "+dest);
            System.out.println("You can open this project in Netbeans");
            System.out.println("NOTE: If you receive compile warnings, you may need to update the project libs first so that it is working with the latest.");
            
        } catch (ZipException ex) {
            Logger.getLogger(CodenameOneCLI.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }
    }
    
    private void replaceRecursive(File root, String key, String replacement) throws IOException {
        if (root.isFile() && (root.getName().endsWith(".xml") || root.getName().endsWith(".properties"))) {
            String contents = FileUtils.readFileToString(root, "UTF-8");
            contents = contents.replace(key, replacement);
            FileUtils.writeStringToFile(root, contents, "UTF-8");
        } else if (root.isDirectory()) {
            for (File f : root.listFiles()) {
                replaceRecursive(f, key, replacement);
            }
        }
    }
    
    private void createIntelliJProjectAt(File dest, String packageName, String mainClass) throws IOException, InterruptedException {
        try {
            File zipDest = new File(dest.getAbsolutePath() + ".zip");
            InputStream is = CodenameOneCLI.class.getResourceAsStream("IntelliJProjectTemplate.zip");
            FileUtils.copyInputStreamToFile(is, zipDest);
            //ZipFile zip = new ZipFile()
            ZipFile zip = new ZipFile(zipDest);
            
            File tmp = File.createTempFile(packageName, "tmp");
            tmp.delete();
            
            zip.extractAll(tmp.getAbsolutePath());
            
            File extractedDir = new File(tmp, "IntelliJProjectTemplate");
            FileUtils.moveDirectory(extractedDir, dest);
            
            FileUtils.deleteDirectory(tmp);
            
            File cn1Settings = new File(dest, "codenameone_settings.properties");
            Properties props = new Properties();
            props.load(new FileInputStream(cn1Settings));
            props.put("codename1.packageName", packageName);
            props.put("codename1.ios.appid", "Q5GHSKAL2F."+packageName);
            props.put("codename1.mainName", mainClass);
            props.put("codename1.displayName", mainClass);
            props.put("codename1.ios.release.provision", "");
            props.put("codename1.ios.release.certificate", "");
            props.put("codename1.ios.debug.provision", "");
            props.put("codename1.windows.certificate", "");
            props.put("libVersion", "1");
            props.store(new FileOutputStream(cn1Settings), "Written by CodenameOneCLI");
            
            replaceRecursive(dest, "IntelliJProjectTemplate", dest.getName());
            replaceRecursive(dest, "com.mycompany.myapp.MyApplication", packageName+"."+mainClass);
            
            new File(dest, "IntelliJProjectTemplate.iml").renameTo(new File(dest, dest.getName()+".iml"));
            
            copyJarsToProject(dest);
            
            System.out.println("IntelliJ project created at "+dest);
            System.out.println("You can open this project in IntelliJ IDEA");
            System.out.println("NOTE: If you receive compile warnings, you may need to update the project libs first so that it is working with the latest.");
            
        } catch (ZipException ex) {
            Logger.getLogger(CodenameOneCLI.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }
    }
    
    private void createEclipseProjectAt(File dest, String packageName, String mainClass) throws IOException, InterruptedException {
        
        EclipseWrapper eclipse = new EclipseWrapper(getEclipseLocation(null));
        if (!eclipse.isCodenameOnePluginInstalled()) {
            System.out.println("Installing Eclipse plugin...");
            installEclipsePlugin(null);
        }
        
         try {
            File zipDest = new File(dest.getAbsolutePath() + ".zip");
            InputStream is = CodenameOneCLI.class.getResourceAsStream("EclipseProjectTemplate.zip");
            FileUtils.copyInputStreamToFile(is, zipDest);
            //ZipFile zip = new ZipFile()
            ZipFile zip = new ZipFile(zipDest);
            
            File tmp = File.createTempFile(packageName, "tmp");
            tmp.delete();
            
            zip.extractAll(tmp.getAbsolutePath());
            
            File extractedDir = new File(tmp, "EclipseProjectTemplate");
            FileUtils.moveDirectory(extractedDir, dest);
            
            FileUtils.deleteDirectory(tmp);
            
            File cn1Settings = new File(dest, "codenameone_settings.properties");
            Properties props = new Properties();
            props.load(new FileInputStream(cn1Settings));
            props.put("codename1.packageName", packageName);
            props.put("codename1.ios.appid", "Q5GHSKAL2F."+packageName);
            props.put("codename1.mainName", mainClass);
            props.put("codename1.displayName", mainClass);
            props.put("codename1.ios.release.provision", "");
            props.put("codename1.ios.release.certificate", "");
            props.put("codename1.ios.debug.provision", "");
            props.put("codename1.windows.certificate", "");
            props.put("libVersion", "1");
            props.store(new FileOutputStream(cn1Settings), "Written by CodenameOneCLI");
            
            File projectFile = new File(dest, ".project");
            String projectFileContents = FileUtils.readFileToString(projectFile);
            projectFileContents = projectFileContents.replace("<name>EclipseProjectTemplate</name>", "<name>"+dest.getName()+"</name>");
            FileUtils.writeStringToFile(projectFile, projectFileContents, "UTF-8");
            
            //eclipse.openProject(dest);
            
            //clitest1/.externalToolBuilders/jar_project.launch
            
            File jarProjectLaunchFile = new File(dest, ".externalToolBuilders" + File.separator + "jar_project.launch");
            String jarProjectLaunchContents = FileUtils.readFileToString(jarProjectLaunchFile, "UTF-8");
            jarProjectLaunchContents = jarProjectLaunchContents.replace("EclipseProjectTemplate", dest.getName());
            
            String mainClassFqn = packageName + "." + mainClass;
            jarProjectLaunchContents = jarProjectLaunchContents.replace("com.mycompany.myapp.MyApplication", mainClassFqn);
            FileUtils.writeStringToFile(jarProjectLaunchFile, jarProjectLaunchContents, "UTF-8");
            
            File launchFile = new File(dest, "Simulator_EclipseProjectTemplate.launch");
            String launchContents = FileUtils.readFileToString(launchFile, "UTF-8");
            launchContents = launchContents.replace("EclipseProjectTemplate", dest.getName());
            
            launchContents = launchContents.replace("com.mycompany.myapp.MyApplication", mainClassFqn);
            FileUtils.writeStringToFile(launchFile, launchContents, "UTF-8");
            
            launchFile.renameTo(new File(dest, "Simulator_"+dest.getName()+".launch"));
            
            File buildProps = new File(dest, "build.props");
            String buildPropsContents = FileUtils.readFileToString(buildProps, "UTF-8");
            buildPropsContents = buildPropsContents.replace("EclipseProjectTemplate", dest.getName());
            FileUtils.writeStringToFile(buildProps, buildPropsContents);
            
            File buildXml = new File(dest, "build.xml");
            String buildXmlContents = FileUtils.readFileToString(buildXml, "UTF-8");
            buildXmlContents = buildXmlContents.replace("EclipseProjectTemplate", dest.getName());
            FileUtils.writeStringToFile(buildXml, buildXmlContents, "UTF-8");
            
            copyJarsToProject(dest);
            
            clean(new File(dest, "build"));
            clean(new File(dest, "bin"));
            System.out.println("Eclipse project has been created at "+dest);
            System.out.println("Open the project in Eclipse, and perform a clean build to begin.");
            System.out.println("NOTE: If you receive compile warnings, you may need to update the project libs first so that it is working with the latest.");
            
            
        } catch (ZipException ex) {
            Logger.getLogger(CodenameOneCLI.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }
    }
    
    private void copyJarsToProject(File projectDir) throws IOException {
        File zip = downloadFiles(null);
        extractJavaSEJarTo(zip, new File(projectDir, "JavaSE.jar"));
        extractCLDCJarTo(zip, new File(projectDir, "lib" + File.separator + "CLDC11.jar"));
        extractCodenameOneJarTo(zip, new File(projectDir, "lib" + File.separator + "CodenameOne.jar"));
        zip.delete();
        
        //FileUtils.copyInputStreamToFile(
        //        CodenameOneCLI.class.getResourceAsStream("JavaSE.jar"), 
        //        new File(projectDir, "JavaSE.jar"));
        
        //FileUtils.copyInputStreamToFile(
        //        CodenameOneCLI.class.getResourceAsStream("CLDC11.jar"), 
        //        new File(projectDir, "lib" + File.separator + "CLDC11.jar"));
        
        //FileUtils.copyInputStreamToFile(
        //        CodenameOneCLI.class.getResourceAsStream("CodenameOne.jar"),
        //        new File(projectDir, "lib" + File.separator + "CodenameOne.jar")
        //);
        
        FileUtils.copyInputStreamToFile(
                CodenameOneCLI.class.getResourceAsStream("CodenameOne_SRC.zip"),
                new File(projectDir, "lib" + File.separator + "CodenameOne_SRC.zip")
        );
        
        FileUtils.copyInputStreamToFile(
                CodenameOneCLI.class.getResourceAsStream("CodeNameOneBuildClient.jar"),
                new File(projectDir, "CodeNameOneBuildClient.jar")
        );
        
        FileUtils.copyInputStreamToFile(
                CodenameOneCLI.class.getResourceAsStream("theme.res"),
                new File(projectDir, "src" + File.separator + "theme.res")
        );
    }
    
    private void copyTestXml(File projectDir) throws IOException {
        File testsXml = new File(projectDir, "tests.xml");
        if (testsXml.exists()) {
            System.err.println("test.xml already exists.  Delete this file first then run install-tests to update it.");
            System.exit(1);
        }
        URL antTaskUrl = new URL("https://raw.githubusercontent.com/shannah/cn1-travis-template/master/tests.xml");
        HttpURLConnection conn = (HttpURLConnection)antTaskUrl.openConnection();
        conn.setInstanceFollowRedirects(true);
        FileUtils.copyInputStreamToFile(conn.getInputStream(), testsXml);

    }
    
    private void copyAppiumXml(File projectDir) throws IOException {
        File testsXml = new File(projectDir, "appium.xml");
        if (testsXml.exists()) {
            System.err.println("appium.xml already exists.  Delete this file first then run install-tests to update it.");
            System.exit(1);
        }
        URL antTaskUrl = new URL("https://raw.githubusercontent.com/shannah/cn1-travis-template/master/appium.xml");
        HttpURLConnection conn = (HttpURLConnection)antTaskUrl.openConnection();
        conn.setInstanceFollowRedirects(true);
        FileUtils.copyInputStreamToFile(conn.getInputStream(), testsXml);

    }
    
    private void setLibVersion(File projectDir, String version) throws IOException {
        File settings = new File(projectDir, "codenameone_settings.properties");
        Properties props = new Properties();
        props.load(new FileInputStream(settings));
        props.setProperty("codename1.arg.build.version", version);
        props.store(new FileOutputStream(settings), "Updated version to "+version);
    }
    
    private void copyJarsToProjectWithoutRes(File projectDir) throws IOException {
        File zip = downloadFiles(null);
        extractJavaSEJarTo(zip, new File(projectDir, "JavaSE.jar"));
        File lib = new File(projectDir, "lib");
        lib.mkdir();
        extractCLDCJarTo(zip, new File(projectDir, "lib" + File.separator + "CLDC11.jar"));
        extractCodenameOneJarTo(zip, new File(projectDir, "lib" + File.separator + "CodenameOne.jar"));
        zip.delete();
        //FileUtils.copyInputStreamToFile(
        //        CodenameOneCLI.class.getResourceAsStream("JavaSE.jar"), 
        //        new File(projectDir, "JavaSE.jar"));
        
        //FileUtils.copyInputStreamToFile(
        //        CodenameOneCLI.class.getResourceAsStream("CLDC11.jar"), 
        //        new File(projectDir, "lib" + File.separator + "CLDC11.jar"));
        
        //FileUtils.copyInputStreamToFile(
        //        CodenameOneCLI.class.getResourceAsStream("CodenameOne.jar"),
        //        new File(projectDir, "lib" + File.separator + "CodenameOne.jar")
        //);
        
        FileUtils.copyInputStreamToFile(
                CodenameOneCLI.class.getResourceAsStream("CodenameOne_SRC.zip"),
                new File(projectDir, "lib" + File.separator + "CodenameOne_SRC.zip")
        );
        
        FileUtils.copyInputStreamToFile(
                CodenameOneCLI.class.getResourceAsStream("CodeNameOneBuildClient.jar"),
                new File(projectDir, "CodeNameOneBuildClient.jar")
        );
        
        
    }
    
    private File getEclipseLocation(String eclipseLocation) throws IOException{
        try {
            Preferences prefs = Preferences.userNodeForPackage(CodenameOneCLI.class);
            eclipseLocation = eclipseLocation != null ? eclipseLocation : prefs.get("eclipseLocation", null);
            
            if (eclipseLocation == null || !new File(eclipseLocation).exists()) {
                ArrayList<File> matches = new ArrayList<File>();
                findEclipseInstallations(matches);
                if (!matches.isEmpty()) {
                    int selection = 0;
                    while (selection < 1 || selection > matches.size()+1) {
                        System.out.println("Please select the Eclipse installation that would like to use:");
                        int i=1;
                        for (File f : matches) {
                            System.out.println(i+") "+f.getAbsolutePath());
                            i++;
                        }
                        System.out.println(i+") Other");
                        Scanner input = new Scanner(System.in);
                        
                        selection = input.nextInt();
                        
                        if (selection == matches.size()+1) {
                            // They selected "Other"
                            System.out.println("Please enter the path to the eclipse executable:");
                            String path = input.nextLine().trim();
                            
                            File pathFile = new File(path);
                            if (!pathFile.exists()) {
                                System.err.println("File not found: "+pathFile.getAbsolutePath());
                                continue;
                            } else if (!"eclipse".equals(pathFile.getName())) {
                                // Wrong name
                                if (pathFile.getName().endsWith(".app")) {
                                    pathFile = new File(pathFile, "Contents" + File.separator + "MacOS" + File.separator + "eclipse");
                                }
                                if (!pathFile.exists()) {
                                    System.err.println("Failed to find eclipse binary at "+pathFile);
                                    continue;
                                }
                            }
                            eclipseLocation = pathFile.getAbsolutePath();
                            break;
                            
                        } else if (selection > 0 && selection < matches.size()+1){
                            
                            File selectedFile = matches.get(selection-1);
                            eclipseLocation = selectedFile.getAbsolutePath();
                            break;
                        } else {
                            System.err.println("Invalid selection.  Please select a number between 1 and "+matches.size()+1);
                        }
                    }
                    
                }
            }
            
            if (eclipseLocation == null) {
                throw new RuntimeException("No eclipse location found");
            }
            prefs.put("eclipseLocation", eclipseLocation);
            prefs.flush();
            return new File(eclipseLocation);
        } catch (BackingStoreException ex) {
            Logger.getLogger(CodenameOneCLI.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }
    
    private void installEclipsePlugin(String eclipseLocation) throws IOException, InterruptedException {
        EclipseWrapper eclipse = new EclipseWrapper(getEclipseLocation(eclipseLocation));
        eclipse.installCodenameOnePlugin();
        
    }
    
    private void findEclipseInstallations(File root, List<File> matches) {
        if ("eclipse".equals(root.getName()) && root.isFile()) {
            matches.add(root);
        } else if ( root.isDirectory()) {
            for (File child : root.listFiles()) {
                findEclipseInstallations(child, matches);
            }
        }
    }
    
    private void findEclipseInstallations(List<File> matches) {
        File[] roots = new File[]{
            new File(System.getProperty("user.home") + File.separator + "eclipse"),
            new File("C:\\Program Files"),
            new File("/Applications/eclipse")
        };
        
        for (File f : roots) {
            if (f.exists() && f.isDirectory()) {
                findEclipseInstallations(f, matches);
            }
        }
    }
    
    private void copyLibs(File srcLib, File destLib) throws IOException {
        for (File f : srcLib.listFiles()) {
            if ("CodenameOne.jar".equals(f.getName()) || "CodenameOne_SRC.jar".equals(f.getName()) || "CLDC11.jar".equals(f.getName()) || "JavaSE.jar".equals(f.getName())) {
                continue;
            } else if (f.isFile()) {
                FileUtils.copyFile(f, new File(destLib, f.getName()));
            } else if (f.isDirectory()) {
                FileUtils.copyDirectory(f, new File(destLib, f.getName()));
            }
        }
    }
    
    
    private void installTemplate(IDE ide, File projectDir, File templateDir) throws IOException {
        FileUtils.deleteDirectory(new File(projectDir, "src"));
        FileUtils.copyDirectory(new File(templateDir, "src"), new File(projectDir, "src"));
        if (new File(templateDir, "native").exists()) {
            FileUtils.deleteDirectory(new File(projectDir, "native"));
        
            FileUtils.copyDirectory(new File(templateDir, "native"), new File(projectDir, "native"));
        }
        
        if (new File(templateDir, "lib").exists()) {
            copyLibs(new File(templateDir, "lib"), new File(projectDir, "lib"));
        }
        
        if (new File(templateDir, "res").exists()) {
            FileUtils.copyDirectory(new File(templateDir, "res"), new File(projectDir, "res"));
        }
        if (new File(templateDir, "css").exists()) {
            FileUtils.copyDirectory(new File(templateDir, "css"), new File(projectDir, "css"));
        }
        
        Properties origProps = new Properties();
        origProps.load(new FileInputStream(new File(projectDir, "codenameone_settings.properties")));
        
        Properties newProps = new Properties();
        newProps.load(new FileInputStream(new File(templateDir, "codenameone_settings.properties")));
        
        String templatePackageId = newProps.getProperty("codename1.packageName");
        String projectPackageId = origProps.getProperty("codename1.packageName");
        
        String templateMain = newProps.getProperty("codename1.mainName");
        String projectMain = origProps.getProperty("codename1.mainName");
        
        if (!projectPackageId.equals(templatePackageId) || !projectMain.equals(templateMain)) {
            // The package id and or main is different than the template
            // subclass it
            String src = "package " + projectPackageId + ";\n"
                    + "import " + templatePackageId + "." + templateMain + ";\n"
                    + "public class "+projectMain+" extends "+templateMain+ "{\n" +
"    public void start() {\n" +
"        super.start();\n" +
"    }\n" +
"    \n" +
"    public void init(Object obj) {\n" +
"        super.init(obj);\n" +
"    }\n" +
"    \n" +
"    public void stop() {\n" +
"        super.stop();\n" +
"    }\n" +
"    \n" +
"    public void destroy() {\n" +
"        super.destroy();\n" +
"    }\n" +
"}";
            
            String mainPath = projectPackageId.replace('.', '/') + "/" + projectMain+".java";
            File mainFile = new File(projectDir, "src/"+mainPath);
            mainFile.getParentFile().mkdirs();
            FileUtils.writeStringToFile(mainFile, src);
        }
        
        newProps.put("codename1.packageName", projectPackageId);
        newProps.put("codename1.mainName", projectMain);
        newProps.put("codename1.ios.appid", origProps.getProperty("codename1.ios.appid"));
        newProps.put("codename1.displayName", projectMain);
        newProps.put("codename1.ios.release.provision", "");
        newProps.put("codename1.ios.release.certificate", "");
        newProps.put("codename1.ios.debug.provision", "");
        newProps.put("codename1.windows.certificate", "");
        newProps.put("libVersion", "1");
        newProps.store(new FileOutputStream(new File(projectDir, "codenameone_settings.properties")), "Written by CodenameOne CLI");
        
        
        refreshLibs(projectDir);
        
    }
    
    
    private void refreshLibs(File projectDir) {
        try {
            System.out.println("Attempting to refresh CN1Libs");
            ProcessBuilder pb = new ProcessBuilder();
            pb.inheritIO();
            pb.command("ant", "refresh-libs");
            pb.directory(projectDir);
            Process process = pb.start();
            process.waitFor();
        } catch (Exception ex) {
            System.err.println("Failed to run the refresh-libs target in the project.  If this template contained cn1libs, you may need to select Refresh CN1Libs after opening it in the IDE.");
        }
    }
    
    public void create(String[] args) throws ParseException, IOException, InterruptedException {
        
        
        
        Options opts = new Options();
        opts.addOption("i", "ide", true, "Type of project to create: Netbeans, Eclipse, or IntelliJ");
        opts.addOption("t", "template", true, "URL or path to a template to use as a base");
        opts.addOption("g", "gui", false, "Open GUI create project wizard");
        
        
        
        
        DefaultParser parser = new DefaultParser();
        CommandLine line = parser.parse(opts, args);
        args = line.getArgs();
        Preferences prefs = Preferences.userNodeForPackage(CodenameOneCLI.class);
        
        String ide = line.hasOption("i") ? line.getOptionValue("i") :
                prefs.get("ide", null);
        
        String templateUrl = line.hasOption("t") ? line.getOptionValue("t") : null;
        
        if (args.length == 2) {
            // Only the dest and mainclass are provided so they must have included package and mainclass together
            args = new String[]{
                args[0],
                args[1].substring(0, args[1].lastIndexOf('.')),
                args[1].substring(args[1].lastIndexOf('.')+1)
            };
        }
        
        if (line.hasOption("g")) {
            // We are doing the GUI option
            if (false) {
                // Use swing form
            
                CreateProjectForm wizard = new CreateProjectForm();
                Properties props = wizard.getProperties();
                if (args.length > 2) {
                    props.setProperty("mainClass", args[1] + "." + args[2]);
                } else {
                    props.setProperty("mainClass", "com.mycompany.myapp.MyApplication");
                }
                if (templateUrl != null) {
                    props.setProperty("templateUrl", templateUrl);
                }

                if (ide != null) {
                    props.setProperty("ide", ide);
                }

                final Object lock = new Object();
                final boolean[] complete = new boolean[1];

                EventQueue.invokeLater(()->{
                    JFrame frame = wizard.createCreateProjectFrame(e->{
                        complete[0] = true;
                        synchronized(lock) {
                            lock.notifyAll();
                        }
                    });
                    frame.setVisible(true);
                });

                synchronized(lock) {
                    while (!complete[0]) {
                        lock.wait();
                    }
                }

                ide = props.getProperty("ide");
                templateUrl = props.getProperty("templateUrl");
                if (templateUrl.trim().isEmpty()) {
                    templateUrl = null;
                }
                args = new String[]{
                    args.length > 0 ? args[0] : props.getProperty("mainClass").substring(props.getProperty("mainClass").lastIndexOf(".")+1).toLowerCase(),
                    props.getProperty("mainClass").substring(0, props.getProperty("mainClass").lastIndexOf(".")),
                    props.getProperty("mainClass").substring(props.getProperty("mainClass").lastIndexOf(".")+1)
                };
            } else {
                
                

                final Object lock = new Object();
                final boolean[] complete = new boolean[1];
                final boolean[] submitted = new boolean[1];
                final TemplateBrowser.Project[] proj = new TemplateBrowser.Project[1];
                String[] fargs = args;
                String fTemplateUrl = templateUrl;
                String fIde = ide;
                String dest = args.length > 0 ? args[0] : null;
                if (dest != null) {
                    dest = new File(dest).getAbsolutePath();
                }
                String fDest = dest;
                EventQueue.invokeLater(()->{
                    TemplateBrowser form = new TemplateBrowser();
                    CN1Frame frame = new CN1Frame("Create New Project", form);
                    form.setConnector(new TemplateBrowserConnector() {

                        @Override
                        public void submitForm(TemplateBrowser.Project project) {
                            synchronized(lock) {
                                complete[0] = true;
                                submitted[0] = true;
                                proj[0] = project;
                                frame.dispose();
                                lock.notifyAll();
                            }
                        }

                        @Override
                        public void cancel() {
                            synchronized (lock) {
                                complete[0] = true;
                                frame.dispose();
                                lock.notifyAll();
                            }
                        }

                        @Override
                        public void initProject(TemplateBrowser.Project project) {
                            project.destPath = fDest;
                            if (fargs.length > 2) {
                                project.mainClass = fargs[1] + "." + fargs[2];
                            } else {
                                project.mainClass = "com.mycompany.myapp.MyApplication";
                            }
                            
                            if (fTemplateUrl != null) {
                                project.templateUrl = fTemplateUrl;
                            }
                            if (fIde != null) {
                                
                            }
                            project.ide = fIde;
                        }

                        @Override
                        public void runDemo(TemplateBrowser.Template tpl) {
                            CN1AppletLauncher launcher = CN1AppletLauncher.parseURL(tpl.demoUrl);
                            launcher.setBlockAndWait(false);
                            launcher.start();
                        }
                        
                        
                    });
                    
                    
                    frame.setPreferredSize(new Dimension(prefs.getInt("frameWidth", 1024), prefs.getInt("frameHeight", 728)));
                    frame.setVisible(true);
                    
                });
                   
                
                synchronized(lock) {
                    while (!complete[0]) {
                        lock.wait();
                    }
                }
                
                if (!submitted[0]) {
                    System.err.println("Operation cancelled");
                    System.exit(1);
                }

                ide = proj[0].ide;
                templateUrl = proj[0].templateUrl;
                if (templateUrl != null && templateUrl.trim().isEmpty()) {
                    templateUrl = null;
                }
                args = new String[]{
                    proj[0].destPath != null ? proj[0].destPath : proj[0].mainClass.substring(proj[0].mainClass.lastIndexOf(".")+1).toLowerCase(),
                    proj[0].mainClass.substring(0, proj[0].mainClass.lastIndexOf(".")),
                    proj[0].mainClass.substring(proj[0].mainClass.lastIndexOf(".")+1)
                };
                
            }
        }
        
        if (args.length < 3) {
            System.err.println("create requires at least three arguments");
            printHelp(Command.Create, opts);
            System.exit(1);
        }
        
        String path = args[0];
        String packageId = args[1].trim().toLowerCase();
        String mainClassName = args[2];
        
        File destDir = path.contains("/") || path.contains("\\") ? new File(path) : new File(dir, path);
        if (destDir.exists()) {
            System.err.println("The dest path already exists: " + destDir);
            printHelp(Command.Create, opts);
            System.exit(1);
        }
        
        if (!destDir.getParentFile().exists()) {
            System.err.println("The parent directory of the destination path doesn't exist.");
            printHelp(Command.Create, opts);
            System.exit(1);
        }
        
        
        
        
        if (ide == null) {
            
            System.out.println("Generate project for which IDE?\n1) Netbeans\n2) Eclipse\n3) IntelliJ\nPlease enter number:");
            Scanner inputScanner = new Scanner(System.in);
            int selection = 0;
            while (selection < 1 || selection > 3) {
                selection = inputScanner.nextInt();
                if (selection < 1 || selection > 3) {
                    System.out.println("Invalid selection.  Must select 1, 2, or 3.  Try again? (Y/n)");
                    if ("n".equals(inputScanner.next().toLowerCase())) {
                        System.exit(1);
                    }
                }
            }
            
            switch (selection) {
                case 1:
                    ide = "Netbeans";
                    break;
                case 2:
                    ide = "Eclipse";
                    break;
                case 3:
                    ide = "IntelliJ";
                    break;
            }
            
            
        }
        prefs.put("ide", ide);
        
        switch (ide.toLowerCase()) {
            case "netbeans" :
                createNetbeansProjectAt(destDir, packageId, mainClassName);
                break;
            case "eclipse" :
                createEclipseProjectAt(destDir, packageId, mainClassName);
                break;
            case "intellij":
                createIntelliJProjectAt(destDir, packageId, mainClassName);
                break;
            default:
                throw new RuntimeException("Eclipse and IntelliJ support incomplete.");
        }
        
        
        
        if (templateUrl != null) {
            try {
                
                //String templateUrl = line.getOptionValue("t");
                System.out.println("Downloading template from "+templateUrl);
                HttpURLConnection conn = (HttpURLConnection)new URL(templateUrl).openConnection();
                conn.setInstanceFollowRedirects(true);
                File tmpTemplateZip = File.createTempFile("codenameonecli", "tmp.zip");
                FileUtils.copyInputStreamToFile(conn.getInputStream(), tmpTemplateZip);
                ZipFile zipFile = new ZipFile(tmpTemplateZip);
                
                File tmpZipDir = File.createTempFile(tmpTemplateZip.getName(), "dir");
                tmpZipDir.delete();
                System.out.println("Extacting zip file "+zipFile.getFile().getAbsolutePath());
                zipFile.extractAll(tmpZipDir.getAbsolutePath());
                
                File templateDir = findCodenameOneProject(tmpZipDir);
                if (templateDir == null) {
                    throw new RuntimeException("Failed to find codename one project in template");
                }
                System.out.println("Installing template in "+destDir);
                installTemplate(IDE.fromString(ide), destDir, templateDir);
                System.out.println("Template installation complete");
                
            } catch (ZipException ex) {
                Logger.getLogger(CodenameOneCLI.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        
    }
    
    private File findCodenameOneProject(File root) {
        List<File> results = new ArrayList<File>();
        _findCodenameOneProject(root, results);
        if (!results.isEmpty()) {
            return results.get(0);
        }
        return null;
    }
    
    private void _findCodenameOneProject(File root, List<File> results) {
        File settings = new File(root, "codenameone_settings.properties");
        if (settings.exists()) {
            results.add(root);
        } else if (root.isDirectory()) {
            for (File child : root.listFiles()) {
                _findCodenameOneProject(child, results);
            }
        }
    }
    
    public void printHelp(Command cmd, Options opts ) {
        if (cmd != null) {
            switch (cmd) {
                case Create: {
                    String header = "Usage: codenameone-cli create <dest> <package> <mainClassName> [options]\n\n"
                            + "<dest> - Destination directory where project should be created\n"
                            + "<package> - Package ID for this app.  (e.g. com.mycompany.myapp)\n"
                            + "<manClassName> - The name of the main class for the app.  E.g. MyApp\n";
                    
                    HelpFormatter formatter = new HelpFormatter();
                    formatter.printHelp("codenameone-cli", header, opts, "", true);
                    break;
                }
                default: {
                    System.err.println("TODO: help");
                }
                    
            }
            
        } else {
            System.err.println("Usage: codenameone-cli [command] \n\n"
                    + "Commands:\n"
                    + "  create - Create a new Codename One Project\n"
                    + "  settings - Open project settings for project in current directory.\n"
                    + "  css - CSS-related commands\n"
                    + "  test - Unit-test related commands\n"
                    + "  install-jars - Install latest jars into project\n"
                    + "  install-tests - Install tests.xml file with some test targets\n"
                    + "  install-appium-tests - Install appium.xml file with some appium tests defined.\n"
                    + "  ");
            
        }
    }
    
    private void clean(File file) {
        if (file.isFile() && file.getName().endsWith(".class")) {
            file.delete();
        } else {
            if (file.isDirectory()) {
                for (File f : file.listFiles()) {
                    clean(f);
                }
            }
        }
    }
    
    
    private void run() throws IOException, InterruptedException {
        
    }
    
    private void settings() throws IOException, InterruptedException {
        File settings = new File(dir, "codenameone_settings.properties");
        if (!settings.exists()) {
            System.err.println("No codenameone settings found in this directory.");
            System.exit(1);
        }
        ProcessBuilder pb = new ProcessBuilder();
        pb.command("java", 
                "-jar", new File(System.getProperty("user.home") + File.separator + ".codenameone" + File.separator + "guibuilder_1.jar").getAbsolutePath(),
                "-settings", settings.getAbsolutePath()
                );
        pb.inheritIO();
        Process p = pb.start();
        p.waitFor();
    }
    
    private void cn1test() {
        EventQueue.invokeLater(()->{
            TemplateBrowser browser = new TemplateBrowser();
            CN1Frame f = new CN1Frame("Create New Project", browser);
            f.setVisible(true);
        });
    }
    private void printCSSHelp() {
        System.out.println("Usage: cn1 css <command>\n"
                + "\n"
                + "Commands: \n"
                + " install - Installs the CSS library in this project.\n"
                + " update - Downloads latest CSS library and replaces existing one.\n"
                + "\n"
                + "Examples:\n"
                + "  $ codenameone-cli css install\n"
                + "");
        
    }
    
    private void printTestHelp(Options opts) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("cn1 test [options] [project1] [project2] ...", "Codename One Unit Test Runner", opts, "See wiki for usage instructions.  \nhttps://github.com/shannah/codenameone-cli/wiki/test");
        
        
    }
    
    
    private void prepareTest(Element test) throws IOException, InterruptedException {
        
        String path = test.getAttribute("path");
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Test missing 'path' attribute");
        }
        System.out.println("Preparing test "+path+"...");
        File testDir = new File(path);

        String repo = test.getAttribute("repo");
        if (repo == null) {
            repo = "";
        }
        if (repo.isEmpty() && !testDir.exists()) {
            throw new IllegalArgumentException("Test "+path+" missing and no repo specified");
        }
        
        if (!repo.isEmpty()) {
            if (!testDir.exists()) {
                Process p = new ProcessBuilder(GIT, "clone", repo, testDir.getAbsolutePath()).inheritIO().start();
                if (p.waitFor() != 0) {
                    throw new IOException("Failed to clone repo "+repo+" to "+testDir);
                }
            } else {
                Process p = new ProcessBuilder(GIT, "pull", "origin", "master").directory(testDir).inheritIO().start();
                if (p.waitFor() != 0) {
                    throw new IOException("Failed to update repo "+repo+" in "+testDir);
                }
            }
        }
                
    }
    
    private void runTestOnAndroid(Element test, File cn1Sources, String adbPath, String deviceName) throws IOException, InterruptedException {
        boolean testPassed = true;
        boolean testCompleted = false;
        StringBuilder sb = new StringBuilder();
        String path = test.getAttribute("path");
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Test missing 'path' attribute");
        }
        System.out.print("Running test "+path+"... ");
        File testDir = new File(path);
        if (!testDir.exists()) {
            throw new IllegalArgumentException("Testy "+testDir+" does not exist");
        }
        
        File javaSE = new File("JavaSE.jar");
        File testJavaSE = new File(testDir, "JavaSE.jar");
        File javaSEBak = new File(testJavaSE.getPath()+".bak."+System.currentTimeMillis());
        javaSEBak.deleteOnExit();
        boolean copiedJavaSE = false;
        
        File cn1Jar = new File("CodenameOne.jar");
        File testCn1Jar = new File(testDir, "lib/CodenameOne.jar");
        File cn1JarBak = new File(testCn1Jar.getPath()+".bak."+System.currentTimeMillis());
        cn1JarBak.deleteOnExit();
        boolean copiedCn1Jar = false;
        
        File cldcJar = new File("CLDC11.jar");
        File testCldcJar = new File(testDir, "lib/CLDC11.jar");
        File cldcJarBak = new File(testCldcJar.getPath()+".bak."+System.currentTimeMillis());
        cldcJarBak.deleteOnExit();
        boolean copiedCldcJar = false;
        
        File buildClientJar =  new File(testDir, "CodeNameOneBuildClient.jar");
        if (!buildClientJar.exists()) {
            FileUtils.copyInputStreamToFile(
                    CodenameOneCLI.class.getResourceAsStream("CodeNameOneBuildClient.jar"),
                    buildClientJar
            );
        }
        
        try {
            if (javaSE.exists()) {
                copiedJavaSE = true;
                if (verbose) System.out.println("Copying "+javaSE+" to "+testJavaSE);
                if (testJavaSE.exists()) FileUtils.moveFile(testJavaSE, javaSEBak);
                FileUtils.copyFile(javaSE, testJavaSE);
            }
            
            if (cn1Jar.exists()) {
                copiedCn1Jar = true;
                if (verbose) System.out.println("Copying "+cn1Jar+" to "+testCn1Jar);
                if (testCn1Jar.exists()) FileUtils.moveFile(testCn1Jar, cn1JarBak);
                FileUtils.copyFile(cn1Jar, testCn1Jar);
            }
            
            if (cldcJar.exists()) {
                copiedCldcJar = true;
                if (verbose) System.out.println("Copying "+cldcJar+" to "+testCldcJar);
                if (testCldcJar.exists()) FileUtils.moveFile(testCldcJar, cldcJarBak);
                FileUtils.copyFile(cldcJar, testCldcJar);
            }
            File tmpErrorLog = File.createTempFile("cn1_test_errors", ".log");
            tmpErrorLog.deleteOnExit();
            
            File androidSrcJar = null;
            if (cn1Sources != null) {
                
                androidSrcJar = new File(testDir, "src" + File.separator + "cn1.override.android_port_src.jar");
                if (androidSrcJar.exists()) {
                    throw new RuntimeException("Building with sources requires a temporary copy of the cn1 android sources to "+androidSrcJar+", but a file already exists at that location.  Please delete "+androidSrcJar+" and try again.");
                }
                androidSrcJar.deleteOnExit();
                createAndroidSrcJar(cn1Sources, androidSrcJar);
            }
            Process p = null;
            File resultZip = new File(testDir, "dist" + File.separator + "result.zip");
            if (!skipBuild || !resultZip.exists()) {
                try {
                    // Send to build server synchronously
                    String buildTarget = "test-for-android-device";
                    if (System.getProperty("debug") != null) {
                        buildTarget += "-debug";
                    }
                    String cn1user = System.getenv("CN1USER");
                    String cn1pass = System.getenv("CN1PASS");
                    cn1user = System.getProperty("CN1USER", cn1user);
                    cn1pass = System.getProperty("CN1PASS", cn1pass);
                    System.out.print("Sending to build server with account "+cn1user+" ... this may take a few minutes ...");
                    
                    p = new ProcessBuilder(ANT, buildTarget, 
                            "-Dautomated=true", 
                            "-Dcodename1.android.keystore="+System.getProperty("keystore", new File("Keychain.ks").getAbsolutePath()), 
                            "-Dcodename1.android.keystoreAlias="+System.getProperty("keystoreAlias", "codenameone"),
                            "-Dcodename1.android.keystorePassword="+System.getProperty("keystorePassword", "password"),
                            "-Dcodename1.arg.android.debug=true",
                            (cn1user!=null)?"-Dcn1user="+cn1user:"-Dcn1.default.user=1",
                            (cn1pass!=null)?"-Dcn1password="+cn1pass:"-Dcn1.default.password=1"
                            )
                            .directory(testDir)
                            .redirectError(tmpErrorLog)
                            .start();
                    if (p.waitFor() != 0) {
                        System.err.println("Errors occured.  Log:");
                        System.out.println(FileUtils.readFileToString(tmpErrorLog));
                        throw new RuntimeException("Test "+testDir+" failed");
                    }
                    System.out.println("Completed");
                } finally {
                    if (androidSrcJar != null) {
                        androidSrcJar.delete();
                    }
                }
            }
            
            if (!resultZip.exists()) {
                System.err.println("Synchronous android build failed.");
                throw new RuntimeException("Test "+testDir+" failed");
            }
            
            File apk = new File(testDir, "dist" + File.separator + "result.apk");
            extractFileWithExtTo(resultZip, ".apk", apk);
            
            if (!apk.exists()) {
                System.err.println("Failed to extract .apk file out of "+resultZip);
                throw new RuntimeException("Test "+testDir+" failed");
            }
            ProcessBuilder pb = null;
            // First install the APK on device
            
            if (verbose) {
                System.out.println("Installing apk on device "+deviceName);
            }
            pb = new ProcessBuilder(adbPath, "-s", deviceName, "install", "-r", apk.getAbsolutePath())
                    .directory(testDir);
            
            if (verbose) {
                pb.inheritIO();
            } else {
                pb.redirectError(tmpErrorLog);
            }
            
            p = pb.start();
            if (p.waitFor() != 0) {
                System.err.println("Failed to install apk on device "+deviceName+"  Log:");
                System.out.println(FileUtils.readFileToString(tmpErrorLog));
                throw new RuntimeException("Test "+testDir+" failed");
            }
            
            Properties cn1Settings = new Properties();
            cn1Settings.load(new FileInputStream(new File(testDir, "codenameone_settings.properties")));
            String displayName = cn1Settings.getProperty("codename1.displayName");
            if (displayName == null) {
                throw new RuntimeException("codename1.displayName property is required in the codenameone_settings.properties file for project "+testDir);
            } 
            String packageName = cn1Settings.getProperty("codename1.packageName");
            if (packageName == null) {
                throw new RuntimeException("codename1.packageName property is required in the codenameone_settings.properties file for project "+testDir);
            }
            
            System.out.println("Running test "+packageName+" on device "+deviceName);
            // Next start the unit tests activity
            if (verbose) {
                System.out.println("Starting activity on device...");
            }
            pb = new ProcessBuilder(adbPath, "-s", deviceName, 
                    "shell", "am", "start", "-a", "android.intent.action.MAIN",
                    "-n", packageName + "/.CodenameOneUnitTestExecutorStub"
                    
                );
            if (verbose) {
                pb.inheritIO();
            }
            
            p = pb.start();
            
            if (p.waitFor() != 0) {
                System.err.println("Failed to start activity "+packageName+" on device "+deviceName);
                throw new RuntimeException("Test "+testDir+" failed");
            }
            
            // Next find the PID
            long now = System.currentTimeMillis();
            long timeout = 90000l;
            String pid = null;
            
            while (pid == null && now + timeout >= System.currentTimeMillis()) {     
                // We place in a loop because it may take some time on slower systems
                // before the pid will be present.
                p = new ProcessBuilder(adbPath, "-s", deviceName, "shell", "ps")
                        .redirectError(tmpErrorLog).start();

                if (verbose) {
                    System.out.println("Looking for pid of for package "+packageName);
                }
                try (InputStream is = p.getInputStream()){
                    Scanner scanner = new Scanner(is, "UTF-8");
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine().trim();
                        if (!line.contains(packageName)) {
                            continue;
                        }
                        if (verbose) {
                            System.out.println("[adb shell ps] "+line);
                        }
                        String[] parts = line.split("\\s+");
                        //System.out.println("Parts:"+Arrays.toString(parts));
                        if (parts.length < 7) {
                            throw new RuntimeException("Problem splitting the line from ps command.  Line was "+line+", split to "+Arrays.toString(parts));
                        }
                        pid = parts[1].trim();
                    }
                }
                if (pid == null) {
                    try {
                        // If we didn't find the pid, let's sleep for 1 second
                        // before checking again.
                        Thread.sleep(1000l);
                    } catch (Throwable t) {}
                }
            }
            if (pid == null) {
                System.err.println("Failed to find pid for package "+packageName+" on device "+deviceName);
                System.err.println("Error log "+FileUtils.readFileToString(tmpErrorLog));
                throw new RuntimeException("Test "+testDir+" failed");
            }
            
            
            final String fpid = pid;
            String paddedPid = pid;
            while (paddedPid.length() < 5) {
                paddedPid = " " + paddedPid;
            }
            if (paddedPid.trim().length() == 0) {
                throw new RuntimeException("Failed to parse PID.  Found only whitespace for pid");
            }
            if (!verbose) System.out.println("Monitoring in logcat.  Use -v flag to see more verbose output");
            else System.out.println("Monitoring in logcat");
            p = new ProcessBuilder(adbPath, "-s", deviceName, "logcat").redirectError(tmpErrorLog).start();
            
            String sep = System.getProperty("line.separator");
            try (InputStream is = p.getInputStream()) {
                Scanner scanner = new Scanner(is, "UTF-8");
                Pattern failedPattern = Pattern.compile(".*Passed: (\\d+) tests\\. Failed: (\\d+) tests\\..*");
                                                            //Total 1 tests passed
                Pattern allPassedPattern = Pattern.compile(".*Total (\\d+) tests passed.*");
                Pattern pidRegex = Pattern.compile("\\b"+Pattern.quote(pid)+"\\b");
                
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    //System.out.println("LINE:");
                    //System.out.println("Looking for ("+paddedPid+")");
                    if (!logcatVerbose && !line.contains("("+paddedPid+")") && !pidRegex.matcher(line).find()) {
                        continue;
                    }
                    
                    if (line.contains("-----FINISHED TESTS-----")) {
                        p.destroyForcibly();
                        break;
                    }
                    if (!verbose) {
                        sb.append(line).append(sep);
                    } else {
                        System.out.println(line);
                    }
                    line = line.trim();
                    Matcher m = failedPattern.matcher(line);
                    Matcher m2 = allPassedPattern.matcher(line);
                    if (m.find()) {
                        testCompleted = true;
                        String numFailedStr = m.group(2);
                        if (Integer.parseInt(numFailedStr) > 0) {
                            testPassed = false;
                        }
                        passedTests += Integer.parseInt(m.group(1));
                        failedTests += Integer.parseInt(m.group(2));
                        System.out.println(line);
                    } else if (m2.find()) {
                        testCompleted = true;
                        passedTests += Integer.parseInt(m2.group(1));
                    }
                }
                
            }
            System.out.println("Finished reading logcat input stream.");
            System.out.println("Error log: "+FileUtils.readFileToString(tmpErrorLog));
            int result = p.waitFor();
            
            
        } finally {
            if (copiedJavaSE) {
                testJavaSE.delete();
                if (javaSEBak.exists()) FileUtils.moveFile(javaSEBak, testJavaSE);
            }
            if (copiedCn1Jar) {
                testCn1Jar.delete();
                if (cn1JarBak.exists()) FileUtils.moveFile(cn1JarBak, testCn1Jar);
            }
            if (copiedCldcJar) {
                testCldcJar.delete();
                if (cldcJarBak.exists()) FileUtils.moveFile(cldcJarBak, testCldcJar);
            }
        }
        if (!testCompleted) {
            System.err.println("\nTest did not complete.  Check the project structure of "+testDir+" to ensure that the 'ant test' target works.");
            if (sb.length() > 0) {
                System.err.println(sb.toString());
                throw new RuntimeException("Test "+testDir+" did not complete");
            }
        }
        if (!testPassed) {
            
            if (stopOnFail) {
                if (sb.length() > 0) {
                    System.err.println("Test "+testDir+" FAILED.  Error log:");
                    System.err.println(sb.toString());
                }
                throw new RuntimeException("Test "+testDir+" FAILED");
            } else {
                if (sb.length() > 0 && errors) {
                    System.err.println("Test "+testDir+" FAILED.  Error log:");
                    System.err.println(sb.toString());
                } else {
                    System.out.println("Test "+testDir+" FAILED.");
                }
            }
            
        } else {
            System.out.println("Test "+testDir+" PASSED.");
        }
        
    }
    
    
    
    private void runTestOnIOS(Element test, File cn1Sources, String xcrunPath, String deviceName) throws IOException, InterruptedException {
        boolean testPassed = true;
        boolean testCompleted = false;
        StringBuilder sb = new StringBuilder();
        String path = test.getAttribute("path");
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Test missing 'path' attribute");
        }
        System.out.print("Running test "+path+"... ");
        File testDir = new File(path);
        if (!testDir.exists()) {
            throw new IllegalArgumentException("Testy "+testDir+" does not exist");
        }
        
        File javaSE = new File("JavaSE.jar");
        File testJavaSE = new File(testDir, "JavaSE.jar");
        File javaSEBak = new File(testJavaSE.getPath()+".bak."+System.currentTimeMillis());
        javaSEBak.deleteOnExit();
        boolean copiedJavaSE = false;
        
        File cn1Jar = new File("CodenameOne.jar");
        File testCn1Jar = new File(testDir, "lib/CodenameOne.jar");
        File cn1JarBak = new File(testCn1Jar.getPath()+".bak."+System.currentTimeMillis());
        cn1JarBak.deleteOnExit();
        boolean copiedCn1Jar = false;
        
        File cldcJar = new File("CLDC11.jar");
        File testCldcJar = new File(testDir, "lib/CLDC11.jar");
        File cldcJarBak = new File(testCldcJar.getPath()+".bak."+System.currentTimeMillis());
        cldcJarBak.deleteOnExit();
        boolean copiedCldcJar = false;
        
        File buildClientJar =  new File(testDir, "CodeNameOneBuildClient.jar");
        if (!buildClientJar.exists()) {
            FileUtils.copyInputStreamToFile(
                    CodenameOneCLI.class.getResourceAsStream("CodeNameOneBuildClient.jar"),
                    buildClientJar
            );
        }
        
        try {
            if (javaSE.exists()) {
                copiedJavaSE = true;
                if (verbose) System.out.println("Copying "+javaSE+" to "+testJavaSE);
                if (testJavaSE.exists()) FileUtils.moveFile(testJavaSE, javaSEBak);
                FileUtils.copyFile(javaSE, testJavaSE);
            }
            
            if (cn1Jar.exists()) {
                copiedCn1Jar = true;
                if (verbose) System.out.println("Copying "+cn1Jar+" to "+testCn1Jar);
                if (testCn1Jar.exists()) FileUtils.moveFile(testCn1Jar, cn1JarBak);
                FileUtils.copyFile(cn1Jar, testCn1Jar);
            }
            
            if (cldcJar.exists()) {
                copiedCldcJar = true;
                if (verbose) System.out.println("Copying "+cldcJar+" to "+testCldcJar);
                if (testCldcJar.exists()) FileUtils.moveFile(testCldcJar, cldcJarBak);
                FileUtils.copyFile(cldcJar, testCldcJar);
            }
            File tmpErrorLog = File.createTempFile("cn1_test_errors", ".log");
            tmpErrorLog.deleteOnExit();
            
            
            
            //ant -f appium.xml test-ios-appium-simulator -Dcn1.iphone.target=debug_iphone_steve -Dcn1user=${CN1USER} -Dcn1password=${CN1PASS}
            String cn1user = System.getenv("CN1USER");
            String cn1pass = System.getenv("CN1PASS");
            cn1user = System.getProperty("CN1USER", cn1user);
            cn1pass = System.getProperty("CN1PASS", cn1pass);
            System.out.print("Sending to build server with account "+cn1user+" ... this may take a few minutes ...");
            List<String> commands = new ArrayList<String>();
            commands.add("ant");
            commands.add("-f");
            commands.add("appium.xml");
            
            
            if (cn1Sources != null) {
                commands.add("test-ios-appium-simulator-with-sources");
            } else {
                commands.add("test-ios-appium-simulator");
            }
            commands.add("-Dcn1.iphone.target="+(System.getProperty("debug")!=null?"debug_iphone_steve":"iphone"));
            if (cn1user != null && cn1pass != null) {
                commands.add("-Dcn1user="+cn1user);
                commands.add("-Dcn1password="+cn1pass);
            }
            if (cn1Sources != null) {
                commands.add("-Dcn1.sources="+cn1Sources.getAbsolutePath());
            }
            Process p = new ProcessBuilder(commands).inheritIO().directory(testDir).start();
            
            
            if (p.waitFor() != 0) {
                failedTests++;
                System.err.println("Errors occurred.  See log above for details");
                throw new RuntimeException("Test "+testDir+" failed");
            }
            
            passedTests++;
            System.out.println("Test "+testDir+" PASSED");
            
        } finally {
            if (copiedJavaSE) {
                testJavaSE.delete();
                if (javaSEBak.exists()) FileUtils.moveFile(javaSEBak, testJavaSE);
            }
            if (copiedCn1Jar) {
                testCn1Jar.delete();
                if (cn1JarBak.exists()) FileUtils.moveFile(cn1JarBak, testCn1Jar);
            }
            if (copiedCldcJar) {
                testCldcJar.delete();
                if (cldcJarBak.exists()) FileUtils.moveFile(cldcJarBak, testCldcJar);
            }
        }
    }
    
    
    private void runTest(Element test) throws IOException, InterruptedException {
        boolean testPassed = true;
        boolean testCompleted = false;
        StringBuilder sb = new StringBuilder();
        String path = test.getAttribute("path");
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Test missing 'path' attribute");
        }
        System.out.print("Running test "+path+"... ");
        File testDir = new File(path);
        if (!testDir.exists()) {
            throw new IllegalArgumentException("Testy "+testDir+" does not exist");
        }
        
        File javaSE = new File("JavaSE.jar");
        File testJavaSE = new File(testDir, "JavaSE.jar");
        File javaSEBak = new File(testJavaSE.getPath()+".bak."+System.currentTimeMillis());
        javaSEBak.deleteOnExit();
        boolean copiedJavaSE = false;
        
        File cn1Jar = new File("CodenameOne.jar");
        File testCn1Jar = new File(testDir, "lib/CodenameOne.jar");
        File cn1JarBak = new File(testCn1Jar.getPath()+".bak."+System.currentTimeMillis());
        cn1JarBak.deleteOnExit();
        boolean copiedCn1Jar = false;
        
        File cldcJar = new File("CLDC11.jar");
        File testCldcJar = new File(testDir, "lib/CLDC11.jar");
        File cldcJarBak = new File(testCldcJar.getPath()+".bak."+System.currentTimeMillis());
        cldcJarBak.deleteOnExit();
        boolean copiedCldcJar = false;
        
        File buildClientJar =  new File(testDir, "CodeNameOneBuildClient.jar");
        if (!buildClientJar.exists()) {
            FileUtils.copyInputStreamToFile(
                    CodenameOneCLI.class.getResourceAsStream("CodeNameOneBuildClient.jar"),
                    buildClientJar
            );
        }
        
        try {
            if (javaSE.exists()) {
                copiedJavaSE = true;
                if (verbose) System.out.println("Copying "+javaSE+" to "+testJavaSE);
                if (testJavaSE.exists()) FileUtils.moveFile(testJavaSE, javaSEBak);
                FileUtils.copyFile(javaSE, testJavaSE);
            }
            
            if (cn1Jar.exists()) {
                copiedCn1Jar = true;
                if (verbose) System.out.println("Copying "+cn1Jar+" to "+testCn1Jar);
                if (testCn1Jar.exists()) FileUtils.moveFile(testCn1Jar, cn1JarBak);
                FileUtils.copyFile(cn1Jar, testCn1Jar);
            }
            
            if (cldcJar.exists()) {
                copiedCldcJar = true;
                if (verbose) System.out.println("Copying "+cldcJar+" to "+testCldcJar);
                if (testCldcJar.exists()) FileUtils.moveFile(testCldcJar, cldcJarBak);
                FileUtils.copyFile(cldcJar, testCldcJar);
            }
            File tmpErrorLog = File.createTempFile("cn1_test_errors", ".log");
            tmpErrorLog.deleteOnExit();
                    
            Process p = new ProcessBuilder(ANT, "test").directory(testDir).redirectError(tmpErrorLog).start();
            
            String sep = System.getProperty("line.separator");
            try (InputStream is = p.getInputStream()) {
                Scanner scanner = new Scanner(is, "UTF-8");
                Pattern failedPattern = Pattern.compile(".*Passed: (\\d+) tests\\. Failed: (\\d+) tests\\..*");
                                                            //Total 1 tests passed
                Pattern allPassedPattern = Pattern.compile(".*Total (\\d+) tests passed.*");
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (!verbose) {
                        sb.append(line).append(sep);
                    } else {
                        System.out.println(line);
                    }
                    line = line.trim();
                    Matcher m = failedPattern.matcher(line);
                    Matcher m2 = allPassedPattern.matcher(line);
                    if (m.find()) {
                        testCompleted = true;
                        String numFailedStr = m.group(2);
                        if (Integer.parseInt(numFailedStr) > 0) {
                            testPassed = false;
                        }
                        passedTests += Integer.parseInt(m.group(1));
                        failedTests += Integer.parseInt(m.group(2));
                        System.out.println(line);
                    } else if (m2.find()) {
                        testCompleted = true;
                        passedTests += Integer.parseInt(m2.group(1));
                    }
                }
                if (p.waitFor() != 0) {
                    System.err.println("Errors occured.  Log:");
                    System.out.println(FileUtils.readFileToString(tmpErrorLog));
                    throw new RuntimeException("Test "+testDir+" failed");
                }
            }
            
            
            
        } finally {
            if (copiedJavaSE) {
                testJavaSE.delete();
                if (javaSEBak.exists()) FileUtils.moveFile(javaSEBak, testJavaSE);
            }
            if (copiedCn1Jar) {
                testCn1Jar.delete();
                if (cn1JarBak.exists()) FileUtils.moveFile(cn1JarBak, testCn1Jar);
            }
            if (copiedCldcJar) {
                testCldcJar.delete();
                if (cldcJarBak.exists()) FileUtils.moveFile(cldcJarBak, testCldcJar);
            }
        }
        if (!testCompleted) {
            System.err.println("\nTest did not complete.  Check the project structure of "+testDir+" to ensure that the 'ant test' target works.");
            if (sb.length() > 0) {
                System.err.println(sb.toString());
                throw new RuntimeException("Test "+testDir+" did not complete");
            }
        }
        if (!testPassed) {
            
            if (stopOnFail) {
                if (sb.length() > 0) {
                    System.err.println("Test "+testDir+" FAILED.  Error log:");
                    System.err.println(sb.toString());
                }
                throw new RuntimeException("Test "+testDir+" FAILED");
            } else {
                if (sb.length() > 0 && errors) {
                    System.err.println("Test "+testDir+" FAILED.  Error log:");
                    System.err.println(sb.toString());
                } else {
                    System.out.println("Test "+testDir+" FAILED.");
                }
            }
            
        } else {
            System.out.println("Test "+testDir+" PASSED.");
        }
        
        
    }
    
    private boolean verbose, errors, stopOnFail;
    private int passedTests;
    private int failedTests;
    private boolean skipCompileCn1Sources;
    
    private void test(String[] args) {
        Options opts = new Options();
        File seJarBak = new File("JavaSE.jar."+System.currentTimeMillis());
        seJarBak.deleteOnExit();
        File cn1JarBak = new File("CodenameOne.jar."+System.currentTimeMillis());
        cn1JarBak.deleteOnExit();
        File cldcJarBak = new File("CLDC11.jar."+System.currentTimeMillis());
        cldcJarBak.deleteOnExit();
        
        File seJarOrig = new File("JavaSE.jar");
        File cn1JarOrig = new File("CodenameOne.jar");
        File cldcJarOrig = new File("CLDC11.jar");
        
        
        try {
            if (seJarOrig.exists()) {
                FileUtils.copyFile(seJarOrig, seJarBak);
            }
            if (cn1JarOrig.exists()) {
                FileUtils.copyFile(cn1JarOrig, cn1JarBak);
            }
            if (cldcJarOrig.exists()) {
                FileUtils.copyFile(cldcJarOrig, cldcJarBak);
            }
            opts = new Options();
            opts.addOption("u", "update", false, "Update the tests to the latest.");
            opts.addOption("v", "verbose", false, "Verbose output");
            opts.addOption("version", true, "Codename One version to run against.  E.g. 3.8");
            opts.addOption("e", "errors", false, "Show more information about failures");
            opts.addOption("s", "stopOnFail", false, "Stop on failure");
            opts.addOption("h", "help", false, "Help");
            opts.addOption("seJar", true, "Path to version of JavaSE.jar to be used for tests.");
            opts.addOption("cn1Jar", true, "Path to version of CodenameOne.jar to be used for tests.");
            opts.addOption("cldcJar", true, "Path to version of CLDC11.jar to be used for tests.");
            opts.addOption("cn1Sources", true, "Path to codename one sources to use for tests");
            opts.addOption("t", "target", true, "Run tests on device. Only android supported");
            opts.addOption("d", "device", true, "The device ID to run tests on.  Accompanies -t android flag");
            opts.addOption("lv", "logcatVerbose", false, "Verbose logcat output.");
            opts.addOption("skipBuild", false, "Skip build step for device tests.  Assumes that you did build in previous test.");
            opts.addOption("skipCompileCn1Sources", false, "Skip the compilation of Codename One sources if it is already built.");
            opts.addOption("p", "pulse", true, "Add pulse to stdout every X seconds to stop CI from timing out");
            opts.addOption("appium", false, "Run tests with appium");
            DefaultParser parser = new DefaultParser();
            
            CommandLine line = parser.parse(opts, args);
            args = line.getArgs();
            
            boolean update = line.hasOption("u");
            verbose = line.hasOption("v");
            logcatVerbose = line.hasOption("lv");
            errors = line.hasOption("e");
            stopOnFail = line.hasOption("s");
            skipBuild = line.hasOption("skipBuild");
            skipCompileCn1Sources = line.hasOption("skipCompileCn1Sources");
            
            if (line.hasOption("p")) {
                int pulseInterval = Integer.parseInt(line.getOptionValue("p"));
                if (pulseInterval < 30) {
                    System.err.println("pulse must be at least 30 seconds");
                } else {
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                            public void run() {
                                System.out.println("<Pulse>");
                            }
                        }, 
                            pulseInterval * 1000L, pulseInterval * 1000L
                    );
                }
                
                
            }
            
            if (line.hasOption("h")) {
                printTestHelp(opts);
                return;
            }
            
            passedTests = 0;
            failedTests = 0;
            
            List<String> argsList = Arrays.asList(args);
            File testConf = new File("tests.xml");
            XMLParser xparser = new XMLParser();
            Element el = xparser.parse(new FileReader(testConf));
            boolean useDefaultCn1Jar = true;
            boolean useDefaultSeJar = true;
            boolean useDefaultCldcJar = true;
            
            String codenameOneJarPath = el.getAttribute("cn1Jar");
            if (line.hasOption("cn1Jar")) {
                codenameOneJarPath = line.getOptionValue("cn1Jar");
                useDefaultCn1Jar = false;
            }
            String javaSEJarPath = el.getAttribute("seJar");
            if (line.hasOption("seJar")) {
                javaSEJarPath = line.getOptionValue("seJar");
                useDefaultSeJar = false;
            }
            String cldcJarPath = el.getAttribute("clcdJar");
            if (line.hasOption("cldcJar")) {
                cldcJarPath = line.getOptionValue("cldcJar");
                useDefaultCldcJar = false;
            }
            String cn1Sources = el.getAttribute("cn1Sources");
            if (line.hasOption("cn1Sources")) {
                cn1Sources = line.getOptionValue("cn1Sources");
                useDefaultCn1Jar = false;
                useDefaultSeJar = false;
                useDefaultCldcJar = false;
            }
            if (cn1Sources != null && !cn1Sources.isEmpty()) {
                useDefaultCn1Jar = false;
                useDefaultSeJar = false;
                useDefaultCldcJar = false;
                File cn1SourcesDir = new File(cn1Sources);
                if (!cn1SourcesDir.exists()) {
                    throw new FileNotFoundException("Specified cn1Sources attribute with value "+cn1Sources+" but this location was not found.");
                }
                File cn1ProjectDir = new File(cn1SourcesDir, "CodenameOne");
                File javaSEProjectDir = new File(cn1SourcesDir, "Ports/JavaSE");
                File cldcProjectDir = new File(cn1SourcesDir, "Ports/CLDC11");
                Process p = null;
                if (!new File(cldcProjectDir, "dist/CLDC11.jar").exists() || !skipCompileCn1Sources) {
                    p = new ProcessBuilder(ANT, "jar").directory(cldcProjectDir).inheritIO().start();
                    if (p.waitFor() != 0) {
                        throw new RuntimeException("Failed to build CLDC11 project at "+cldcProjectDir);
                    }
                }
                if (!new File(cn1ProjectDir, "dist/CodenameOne.jar").exists() || !skipCompileCn1Sources) {
                    p = new ProcessBuilder(ANT, "jar").directory(cn1ProjectDir).inheritIO().start();
                    if (p.waitFor() != 0) {
                        throw new RuntimeException("Failed to build CodenameOne project at "+cn1ProjectDir);
                    }
                }
                if (!new File(javaSEProjectDir, "dist/JavaSE.jar").exists() || !skipCompileCn1Sources) {
                    p = new ProcessBuilder(ANT, "jar").directory(javaSEProjectDir).inheritIO().start();
                    if (p.waitFor() != 0) {
                        throw new RuntimeException("Failed to build JavaSE project at "+cn1ProjectDir);
                    }
                }
                
                FileUtils.copyFile(new File(cn1ProjectDir, "dist/CodenameOne.jar"), new File("CodenameOne.jar"));
                FileUtils.copyFile(new File(javaSEProjectDir, "dist/JavaSE.jar"), new File("JavaSE.jar"));
                FileUtils.copyFile(new File(cldcProjectDir, "dist/CLDC11.jar"), new File("CLDC11.jar"));
                        
            }
            
            if (codenameOneJarPath != null && !codenameOneJarPath.isEmpty()) {
                useDefaultCn1Jar = false;
                FileUtils.copyFile(new File(codenameOneJarPath), new File("CodenameOne.jar"));
            }
            if (javaSEJarPath != null && !javaSEJarPath.isEmpty()) {
                useDefaultSeJar = false;
                FileUtils.copyFile(new File(javaSEJarPath), new File("JavaSE.jar"));
            }
            if (cldcJarPath != null && !cldcJarPath.isEmpty()) {
                useDefaultCldcJar = false;
                FileUtils.copyFile(new File(cldcJarPath), new File("CLDC11.jar"));
            }
            
            File lib = new File("lib");
            if (line.hasOption("version")) {
                lib = new File("lib"+line.getOptionValue("version"));
            }
            if (!lib.exists()) {
                lib.mkdir();
            }
            
            File libCn1Jar = new File(lib, "CodenameOne.jar");
            File libCldcJar = new File(lib, "CLDC11.jar");
            File libSeJar = new File(lib, "JavaSE.jar");
            if (update || !libCn1Jar.exists() || !libCldcJar.exists() || !libSeJar.exists()) {
                File libsZip = downloadFiles(line.getOptionValue("version"));
                libsZip.deleteOnExit();
                if (update || !libCn1Jar.exists()) {
                    extractCodenameOneJarTo(libsZip, libCn1Jar);
                }
                if (update || !libCldcJar.exists()) {
                    extractCLDCJarTo(libsZip, libCldcJar);
                }
                if (update || !libSeJar.exists()) {
                    extractJavaSEJarTo(libsZip, libSeJar);
                }
            }
            
            if (line.hasOption("version") || !cn1JarOrig.exists() || useDefaultCn1Jar) {
                System.out.println("Using "+libCn1Jar);
                FileUtils.copyFile(libCn1Jar, cn1JarOrig);
            }
            if (line.hasOption("version") || !seJarOrig.exists() || useDefaultSeJar) {
                System.out.println("Using "+libSeJar);
                FileUtils.copyFile(libSeJar, seJarOrig);
            }
            if (line.hasOption("version") || !cldcJarOrig.exists() || useDefaultCldcJar) {
                System.out.println("Using "+libCldcJar);
                FileUtils.copyFile(libCldcJar, cldcJarOrig);
            }
            
            ArrayList<Element> tests = new ArrayList<Element>(el.getChildrenByTagName("test"));
            for (Element test : tests) {
                String path = test.getAttribute("path");
                String name = new File(path).getName();
                boolean matched = argsList.isEmpty();
                if (!matched) {
                    for (String arg : argsList) {
                        if (arg.endsWith("*")) {
                            if (name.startsWith(arg.substring(0, arg.length()-1))) {
                                matched = true;
                                break;
                            }
                        } else if (arg.startsWith("*")) {
                            if (name.endsWith(arg.substring(1))) {
                                matched = true;
                                break;
                            }
                        } else {
                            if (arg.equalsIgnoreCase(name)) {
                                matched = true;
                            }
                        }
                    }
                }
                if (matched && test.getAttribute("since") != null) {
                    double since = Double.parseDouble(test.getAttribute("since"));
                    if (line.hasOption("version")) {
                        double ver = Double.parseDouble(line.getOptionValue("version"));
                        if (ver < since) {
                            System.out.println("Skipping test "+name+" because it requires a higher version than "+ver);
                            matched = false;
                        }
                    }
                }
                if (!matched) {
                    continue;
                }
                if (update || !new File(path).exists()) prepareTest(test);
                if (!update) {
                    if (line.hasOption("t")) {
                        if ("android".equals(line.getOptionValue("t"))) {
                            String deviceName = null;
                            if (!line.hasOption("d") || "".equals(line.getOptionValue("d"))) {
                                Process p = new ProcessBuilder("adb", "devices", "-l").start();
                                try (InputStream is = p.getInputStream()) {
                                    Scanner scanner = new Scanner(is, "UTF-8");
                                    scanner.nextLine(); // skip first line.. it says "List of devices"
                                    List<String> devices = new ArrayList<String>();
                                    while (scanner.hasNextLine()) {
                                        String l = scanner.nextLine().trim();
                                        if (l.length() > 0) {
                                            devices.add(l.substring(0, l.indexOf(" ")));
                                        }
                                    }
                                    if (p.waitFor() != 0) {
                                        throw new RuntimeException("Failed to run adb devices -l.  Please ensure that adb is installed and in your environment PATH.");
                                    }
                                    if (devices.size() != 1) {
                                        System.err.println("More than or less than one device was listed.  Please specify the device to run tests on using the -d flag");
                                        System.err.println("Found devices: "+devices);
                                        throw new RuntimeException("Try again");
                                    }
                                    deviceName = devices.get(0);
                                    
                                }
                            } else {
                                deviceName = line.getOptionValue("d");
                            }
                            
                            runTestOnAndroid(test, 
                                    cn1Sources == null ? null : new File(cn1Sources), 
                                    "adb", 
                                    deviceName
                            );
                        } else if ("ios".equals(line.getOptionValue("t"))) {
                            
                            String deviceName = null;
                            if (!line.hasOption("d")) {
                                deviceName = "boot";
                            } else {
                                deviceName = line.getOptionValue("d");
                            }

                            runTestOnIOS(test, 
                                    cn1Sources == null ? null : new File(cn1Sources), 
                                    "xcrun", 
                                    deviceName
                            );
                        } else {
                            throw new IllegalArgumentException("Invalid option for -target.  Expected android or ios, received "+line.getOptionValue("t"));
                            
                        }
                    } else {
                        runTest(test);
                    }
                }
                
            }
            System.out.println("PASSED tests: "+passedTests+". FAILED tests: "+failedTests);
            if (!errors && !verbose) {
                System.out.println("Use -e option to show stack trace for failures");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            printTestHelp(opts);
            System.exit(1);
        } finally {
            
            if (seJarBak.exists()) {
                try {
                    if (seJarOrig.exists()) seJarOrig.delete();
                    FileUtils.moveFile(seJarBak, seJarOrig);
                    if (seJarBak.exists()) seJarBak.delete();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            if (cn1JarBak.exists()) {
                try {
                    if (cn1JarOrig.exists()) {
                        cn1JarOrig.delete();
                    }
                    FileUtils.moveFile(cn1JarBak, cn1JarOrig);
                    if (cn1JarBak.exists()) {
                        cn1JarBak.delete();
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            if (cldcJarBak.exists()) {
                try {
                    if (cldcJarOrig.exists()) {
                        cldcJarOrig.delete();
                    }
                    FileUtils.moveFile(cldcJarBak, cldcJarOrig);
                    if (cldcJarBak.exists()) {
                        cldcJarBak.delete();
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }
    
    
    private void unzipExt(java.util.zip.ZipFile z, String entryExt, File destinationFile) throws IOException {
        
        ZipEntry se = null;
        Enumeration<? extends ZipEntry> entries = z.entries();
        while (entries.hasMoreElements()) {
            ZipEntry next = entries.nextElement();
            if (next.getName().endsWith(entryExt)) {
                se = next;
                break;
            }
        }
        if (se == null) {
            throw new FileNotFoundException("No matching entries found in "+z+" with extension "+entryExt);
        }
        InputStream is = z.getInputStream(se);
        byte[] buffer = new byte[65536];
        FileOutputStream os = new FileOutputStream(destinationFile);
        int size = is.read(buffer);
        while(size > -1) {
            os.write(buffer, 0, size);
            size = is.read(buffer);
        }
        is.close();
        os.close();        
    }
    
    private void unzip(java.util.zip.ZipFile z, String entryName, File destinationFile) throws IOException {
        ZipEntry se = z.getEntry(entryName);
        InputStream is = z.getInputStream(se);
        byte[] buffer = new byte[65536];
        FileOutputStream os = new FileOutputStream(destinationFile);
        int size = is.read(buffer);
        while(size > -1) {
            os.write(buffer, 0, size);
            size = is.read(buffer);
        }
        is.close();
        os.close();        
    }
    
    private File extractFileWithExtTo(File zipLibs, String ext,  File dest) throws IOException {
        try {
            File tmp = zipLibs;
            java.util.zip.ZipFile z = new java.util.zip.ZipFile(tmp);
            unzipExt(z, ext, dest);
            return dest;
                        
        } catch (IOException ioe) {
            throw ioe;
        } catch (Throwable err) {
            System.out.println("An error occured downloading lib");
            err.printStackTrace();
            throw new RuntimeException(err);
        }
    }
    
    private File extractFileTo(File zipLibs, String name,  File dest) throws IOException {
        try {
            File tmp = zipLibs;
            java.util.zip.ZipFile z = new java.util.zip.ZipFile(tmp);
            unzip(z, name, dest);
            return dest;
                        
        } catch (IOException ioe) {
            throw ioe;
        } catch (Throwable err) {
            System.out.println("An error occured downloading lib");
            err.printStackTrace();
            throw new RuntimeException(err);
        }
    }
    
    private File extractJavaSEJarTo(File zipLibs, File dest) throws IOException  {
        return extractFileTo(zipLibs, "JavaSE.jar", dest);
    }
    
    private File extractCodenameOneJarTo(File zipLibs, File dest) throws IOException {
        return extractFileTo(zipLibs, "CodenameOne.jar", dest);
    }
    
    private File extractCLDCJarTo(File zipLibs, File dest) throws IOException {
        return extractFileTo(zipLibs, "CLDC11.jar", dest);
    }
    
    private File downloadFiles(String v) throws IOException {
        File tmp = File.createTempFile("DownloadLibs", "zip");
        tmp.deleteOnExit();
        HttpURLConnection.setFollowRedirects(true);
        URL u = null;
        if(v == null){
            u = new URL("http://www.codenameone.com/files/updatedLibs.zip");
        }else{
            u = new URL("http://www.codenameone.com/files/" + v + "/updatedLibs.zip");            
        }
        FileOutputStream os = new FileOutputStream(tmp);
        URLConnection uc = u.openConnection();
        InputStream is = uc.getInputStream();
        int length = uc.getContentLength();
        byte[] buffer = new byte[65536];
        int size = is.read(buffer);
        int offset = 0;
        int percent = 0;
        if(length > 0) {
            System.out.println("Downloading " + length + " bytes");
        }
        while(size > -1) {
            offset += size;
            if(length > 0) {
                float f = ((float)offset) / ((float)length) * 100;
                if(percent != ((int)f)) {
                    percent = (int)f;
                    System.out.println("Downloaded " + percent + "%");
                }
            } else {
                if(percent < offset / 102400) {
                    percent = offset / 102400;
                    System.out.println("Downloaded " + percent + "00Kb");
                }
            }
            os.write(buffer, 0, size);
            size = is.read(buffer);
        }
        is.close();
        os.close();
        System.out.println("Download completed!");
        return tmp;
    }
    
    private void css(String[] args) throws IOException, InterruptedException {
        try {
            Options opts = new Options();
            
            
            
            
            
            DefaultParser parser = new DefaultParser();
            CommandLine line = parser.parse(opts, args);
            args = line.getArgs();
            
            if (args.length < 1) {
                System.err.println("Invalid option for css");
                printCSSHelp();
                System.exit(1);
            }
            
            switch (args[0]) {
                case "install" :
                    installCSS(false);
                    break;
                    
                case "update":
                    installCSS(true);
                    
                default :
                    System.err.println("Invalid option for css");
                    printCSSHelp();
            }
            
        } catch (ParseException ex) {
            Logger.getLogger(CodenameOneCLI.class.getName()).log(Level.SEVERE, null, ex);
            printCSSHelp();
        }
    }
    
    private void installCSS(boolean update) throws IOException, InterruptedException {
        File settings = new File(dir, "codenameone_settings.properties");
        if (!settings.exists()) {
            System.err.println("This is not a codename one project directory.  Navigate to a project directory then run this command again.");
            System.exit(1);
        }
        File buildXml = new File(dir, "build.xml");
        String contents = FileUtils.readFileToString(buildXml, "UTF-8");
        if (contents.contains("compileCSS")) {
            System.out.println("build.xml file already includes compileCSS task.  Leaving build.xml file untouched");
        } else {
            System.out.println("Updating build.xml file to run compileCSS ANT task before pre-compile step...");
            String codenameOneSettingsPropertiesStr = "<property file=\"codenameone_settings.properties\"/>";
            if (contents.indexOf(codenameOneSettingsPropertiesStr) == -1) {
                codenameOneSettingsPropertiesStr = "<property file=\"codenameone_settings.properties\" />";
            }
            contents = contents.replace(codenameOneSettingsPropertiesStr, "<property file=\"codenameone_settings.properties\"/>\n    <taskdef name=\"compileCSS\"\n" +
                "        classname=\"com.codename1.ui.css.CN1CSSCompileTask\"\n" +
                "        classpath=\"lib/cn1css-ant-task.jar\"/>\n" +
                "    \n" +
                "    <target name=\"compile-css\">\n" +
                "        <compileCSS/>\n" +
                "    </target>");
            boolean isEclipse = new File(dir, "build.props").exists();
            if (isEclipse) {
                contents = contents.replace("<compileCSS/>", "<compileCSS/> <eclipse.refreshLocal resource=\"${basedir}/src\" depth=\"infinite\"/>");
            }
            if (contents.indexOf("<target name=\"-pre-compile\"") != -1) {
            
                contents = contents.replace("<target name=\"-pre-compile\">", "<target name=\"-pre-compile\" depends=\"compile-css\">");
            } else {
                contents = contents.replace("<target name=\"jar\"", "<target name=\"jar\" depends=\"compile-css\"");
            }
            
            contents = contents.replace("<target name=\"setupJavac\" depends=\"", "<target name=\"setupJavac\" depends=\"compile-css, ");
            File backupBuildXml = new File(buildXml.getParentFile(), "build.xml.bak."+System.currentTimeMillis());
            System.out.println("Backing up existing build.xml file at "+ backupBuildXml.getName());
            FileUtils.copyFile(buildXml, backupBuildXml);
            FileUtils.write(buildXml, contents);
            
        }
        
        File libDir = new File(dir, "lib");
        File cssDir = new File(dir, "css");
        libDir.mkdirs();
        cssDir.mkdirs();
        
        File themeCssFile = new File(cssDir, "theme.css");
        if (!themeCssFile.exists()) {
            System.out.println("Creating CSS file for your app at "+themeCssFile.getPath());
            FileUtils.write(themeCssFile, "");
        } else {
            System.out.println("Theme CSS file already exists at "+themeCssFile.getPath()+".  Leaving it untouched");
        }
        
        URL antTaskUrl = new URL("https://github.com/shannah/cn1-css/raw/master/bin/cn1css-ant-task.jar");
        HttpURLConnection conn = (HttpURLConnection)antTaskUrl.openConnection();
        conn.setInstanceFollowRedirects(true);
        
        File antJarFile = new File(libDir, "cn1css-ant-task.jar");
        if (!antJarFile.exists() || update) {
            System.out.println("Downloading latest cn1css-ant-task.jar from "+antTaskUrl);
            System.out.println("Installing to "+antJarFile.getPath());
            FileUtils.copyInputStreamToFile(conn.getInputStream(), antJarFile);
        } else {
            System.out.println(antJarFile.getPath()+" exists.  Leaving untouched.  Use cn1 css update to download latest");
        }
        
        /*
         Java javaTask = (Java)getProject().createTask("java");
                Path cp = javaTask.createClasspath();
                cp.add(new Path(getProject(), javaSEJar.getAbsolutePath()));
                cp.add(new Path(getProject(), designerJar.getAbsolutePath()));
                cp.add(new Path(getProject(), cssJar.getAbsolutePath()));
                
                javaTask.setClasspath(cp);
                javaTask.setFork(true);
                javaTask.setClassname("com.codename1.ui.css.CN1CSSCLI");
                javaTask.setFailonerror(true);
                String maxMemory = getProject().getProperty("cn1css.max.memory");
                if (maxMemory != null) {
                    javaTask.setMaxmemory("4096m");
                }
                
                Argument arg = javaTask.createArg();
                arg.setValue(f.getAbsolutePath());
                
                Argument destArg = javaTask.createArg();
                destArg.setValue(destFile.getAbsolutePath());
                
                javaTask.execute();
        */
        
        File javaSEJar = new File(dir, "JavaSE.jar");
        String codenameOneTempPath = System.getProperty("user.home") + File.separator + ".codenameone";
        String designerJarPath = cssDir.getAbsolutePath() + File.separator + "designer_1.jar";
        File designerJar = new File(designerJarPath);
        if (!designerJar.exists()) {
            designerJarPath = codenameOneTempPath + File.separator + "designer_1.jar";

            designerJar = new File(designerJarPath);
            if (!designerJar.exists()) {
                System.err.println("Failed to find the designer_1.jar file so we couldn't update the default resource file to enable CSS styles.\n"
                        + "To enable CSS styles in your app, you should add the following theme constant to your theme.res file: @OverlayThemes=theme.css");
                System.exit(1);
            }
        }
        
        //File cssJar = antJarFile;
        File resFile = new File(dir, "src" + File.separator + "theme.res");
        if (!resFile.exists()) {
            System.err.println("Could not find theme.res file in src directory. \n"
                    + "To enable CSS styles in your app, you should add the following theme constant to your theme.res file: @OverlayThemes=theme.css");
            System.exit(1);
        }
        
        JarFile jarFile = new JarFile(antJarFile);
        JarEntry cn1cssEntry = jarFile.getJarEntry("com/codename1/ui/css/cn1css.jar");
        File cssJar = new File(libDir, "cn1css.jar");
        FileUtils.copyInputStreamToFile(jarFile.getInputStream(cn1cssEntry), cssJar);
        
        jarFile.getInputStream(cn1cssEntry);
        
        ProcessBuilder pb = new ProcessBuilder();
        pb.inheritIO();
        pb.command("java", 
                "-cp", 
                javaSEJar.getAbsolutePath()+File.pathSeparator
                        +designerJar.getAbsolutePath()+File.pathSeparator
                        +cssJar.getAbsolutePath(),
                "com.codename1.ui.css.CN1CSSInstallerCLI",
                "install",
                themeCssFile.getAbsolutePath(),
                resFile.getAbsolutePath()
                );
        Process p = pb.start();
        if (p.waitFor() == 0) {
            System.out.println("Successfully install CSS file.  You may open the CSS file at "+themeCssFile.getPath()+ " and start customizing your theme.");
        } else {
            System.err.println("Failed to install theme constant to your resource file to activate the CSS theme.\n"
                    + "To enable CSS styles in your app, you should add the following theme constant to your theme.res file: @OverlayThemes=theme.css");
            System.exit(1);
        }
        
        
        
        
        
    }
    
    private void runApplet(String[] args) {
        if (args.length < 2) {
            System.err.println("cn1 run must include at least 2 arguments");
            System.exit(1);
        }
        CN1AppletLauncher launcher = new CN1AppletLauncher(args[0], args[1]);
        launcher.setBlockAndWait(true);
        launcher.start();
    }
    
    
    public static void main(String[] args) throws ParseException, IOException, InterruptedException {
        File f = new File(".").getAbsoluteFile();
        if (f.getName().equals(".")) {
            f = f.getParentFile();
        }
        CodenameOneCLI cli = new CodenameOneCLI(f);
        
        if (args.length == 0) {
            cli.printHelp(null, new Options());
            System.exit(1);
        }
        String command = args[0];
        
        switch (command) {
            case "create" : {
                ArrayDeque<String> l = new ArrayDeque<String>(Arrays.asList(args));
                l.removeFirst();
                cli.create(l.toArray(new String[l.size()]));
                break;
            }
            case "settings" :
                cli.settings();
                break;
            case "cn1test" :
                cli.cn1test();
                break;
            case "run": {
                ArrayDeque<String> l = new ArrayDeque<String>(Arrays.asList(args));
                l.removeFirst();
                cli.runApplet(l.toArray(new String[l.size()]));
                break;
            }
            case "css" : {
                ArrayDeque<String> l = new ArrayDeque<String>(Arrays.asList(args));
                l.removeFirst();
                cli.css(l.toArray(new String[l.size()]));
                break;
            }
            
            case "test" : {
                ArrayDeque<String> l = new ArrayDeque<String>(Arrays.asList(args));
                l.removeFirst();
                cli.test(l.toArray(new String[l.size()]));
                break;
            }
            
            case "install-jars" : {
                cli.copyJarsToProjectWithoutRes(new File("."));
                break;
            }
            
            case "install-tests" : {
                cli.copyTestXml(new File("."));
                break;
            }
            
            case "install-appium-tests" : {
                cli.copyAppiumXml(new File("."));
                break;
            }
            
            case "set-lib-version" : {
                if (args.length < 2) {
                    System.err.println("set-lib-version requires parameter");
                    
                    System.exit(1);
                }
                cli.setLibVersion(new File("."), args[1]);
                System.out.println("Version now "+args[1]);
                break;
            }
                
            default:
                System.err.println("Unknown command: "+command);
                cli.printHelp(null, new Options());
                System.exit(1);
        }
        
        System.exit(0);
        
    }
    
    private void createAndroidSrcJar(File cn1Sources, File dest) throws IOException, InterruptedException {
        File androidDir = new File(cn1Sources, "Ports" + File.separator + "Android");
        File cn1Dir = new File(cn1Sources, "CodenameOne");
        StringBuilder sb = new StringBuilder();
        String sep = System.getProperty("line.separator");
        sb.append("<zip zipfile=\"").append(dest.getAbsolutePath()).append("\">").append(sep);
        sb.append("        <fileset dir=\"").append(androidDir.getAbsolutePath()).append("/src\"/>").append(sep);
        
        sb.append("        <fileset dir=\"")
                .append(cn1Dir.getAbsolutePath())
                .append("/src\" excludes=\"**/*.html,**/CodenameOneThread.java\" />")
                .append(sep)
                .append("</zip>");
        File errorOutput = File.createTempFile("errorOutput", ".txt");
        errorOutput.deleteOnExit();
        File output = File.createTempFile("output", ".txt");
        output.deleteOnExit();
        try {
            int res = runAntScript(sb.toString(), errorOutput, output);
            if (res != 0) {
                System.err.println("Failed to create "+dest);
                System.err.println("Log: ");
                System.err.println(FileUtils.readFileToString(output));
                System.err.println(FileUtils.readFileToString(errorOutput));
                throw new IOException("Failed to create android_src jar using CodenameOne sources at "+cn1Sources+" with dest="+dest);
            }
            
        } finally {
            errorOutput.delete();
            output.delete();
        }

    }
    
    private int runAntScript(String script, File errorOutput, File output) throws IOException, InterruptedException {
        File dir = File.createTempFile("antScript", "dir");
        dir.delete();
        dir.mkdir();
        dir.deleteOnExit();
        try {
            File buildXml = new File(dir, "build.xml");
            StringBuilder sb = new StringBuilder();
            String sep = System.getProperty("line.separator");
            sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(sep);
            sb.append("<project name=\"inline\" default=\"default\" basedir=\".\">").append(sep);
            sb.append("<target name=\"default\">").append(sep).append(script).append(sep).append("</target></project>");
            
            FileUtils.writeStringToFile(buildXml, sb.toString());
            ProcessBuilder pb = new ProcessBuilder(ANT).directory(dir);
            if (errorOutput != null) {
                pb.redirectError(errorOutput);
            }
            if (output != null) {
                pb.redirectOutput(output);
            }
            Process p = pb.start();
            return p.waitFor();
        } finally {
            FileUtils.deleteDirectory(dir);
        }
        
        
    }
}
