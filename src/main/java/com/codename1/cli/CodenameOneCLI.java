/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codename1.cli;

import com.codename1.io.Util;
import com.codename1.templatebrowser.TemplateBrowser;
import com.codename1.templatebrowser.TemplateBrowser.TemplateBrowserConnector;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JFrame;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
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
            props.store(new FileOutputStream(cn1Settings), "Written by CodenameOneCLI");
            
            replaceRecursive(dest, "NetbeansProjectTemplate", dest.getName());
            
            copyJarsToProject(dest);
            
            System.out.println("Netbeans project created at "+dest);
            System.out.println("You can open this project in Netbeans");
            
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
            props.store(new FileOutputStream(cn1Settings), "Written by CodenameOneCLI");
            
            replaceRecursive(dest, "IntelliJProjectTemplate", dest.getName());
            replaceRecursive(dest, "com.mycompany.myapp.MyApplication", packageName+"."+mainClass);
            
            new File(dest, "IntelliJProjectTemplate.iml").renameTo(new File(dest, dest.getName()+".iml"));
            
            copyJarsToProject(dest);
            
            System.out.println("IntelliJ project created at "+dest);
            System.out.println("You can open this project in IntelliJ IDEA");
            
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
            
            
        } catch (ZipException ex) {
            Logger.getLogger(CodenameOneCLI.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }
    }
    
    private void copyJarsToProject(File projectDir) throws IOException {
        FileUtils.copyInputStreamToFile(
                CodenameOneCLI.class.getResourceAsStream("JavaSE.jar"), 
                new File(projectDir, "JavaSE.jar"));
        
        FileUtils.copyInputStreamToFile(
                CodenameOneCLI.class.getResourceAsStream("CLDC11.jar"), 
                new File(projectDir, "lib" + File.separator + "CLDC11.jar"));
        
        FileUtils.copyInputStreamToFile(
                CodenameOneCLI.class.getResourceAsStream("CodenameOne.jar"),
                new File(projectDir, "lib" + File.separator + "CodenameOne.jar")
        );
        
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
    
    private void copyJarsToProjectWithoutRes(File projectDir) throws IOException {
        FileUtils.copyInputStreamToFile(
                CodenameOneCLI.class.getResourceAsStream("JavaSE.jar"), 
                new File(projectDir, "JavaSE.jar"));
        
        FileUtils.copyInputStreamToFile(
                CodenameOneCLI.class.getResourceAsStream("CLDC11.jar"), 
                new File(projectDir, "lib" + File.separator + "CLDC11.jar"));
        
        FileUtils.copyInputStreamToFile(
                CodenameOneCLI.class.getResourceAsStream("CodenameOne.jar"),
                new File(projectDir, "lib" + File.separator + "CodenameOne.jar")
        );
        
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
                    + "  settings - Open project settings for project in current directory.");
            
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
        System.out.println("Usage: codenameone-cli css <command>\n"
                + "\n"
                + "Commands: \n"
                + " install - Installs the CSS library in this project.\n"
                + " update - Downloads latest CSS library and replaces existing one.\n"
                + "\n"
                + "Examples:\n"
                + "  $ codenameone-cli css install\n"
                + "");
        
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
            
            case "install-jars" : {
                cli.copyJarsToProjectWithoutRes(new File("."));
                break;
            }
                
            default:
                System.err.println("Unknown command: "+command);
                cli.printHelp(null, new Options());
                System.exit(1);
        }
        
        System.exit(0);
        
    }
}
