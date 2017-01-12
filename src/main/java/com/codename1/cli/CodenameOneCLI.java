/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codename1.cli;

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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
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
            
            
            
            clean(new File(dest, "build"));
            clean(new File(dest, "bin"));
            System.out.println("Eclipse project has been created at "+dest);
            System.out.println("Open the project in Eclipse, and perform a clean build to begin.");
            
            
        } catch (ZipException ex) {
            Logger.getLogger(CodenameOneCLI.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }
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
            case "create" :
                ArrayDeque<String> l = new ArrayDeque<String>(Arrays.asList(args));
                l.removeFirst();
                cli.create(l.toArray(new String[l.size()]));
                break;
            case "settings" :
                cli.settings();
                break;
            case "cn1test" :
                cli.cn1test();
                break;
            default:
                System.err.println("Unknown command: "+command);
                cli.printHelp(null, new Options());
                System.exit(1);
        }
        
        System.exit(0);
        
    }
}
