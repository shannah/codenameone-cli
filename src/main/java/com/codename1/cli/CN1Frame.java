/*
 * Copyright (c) 2012, Codename One and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Codename One designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *  
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Please contact Codename One through http://www.codenameone.com/ if you 
 * need additional information or have any questions.
 */

package com.codename1.cli;

import com.codename1.impl.javase.JavaSEPort;
import com.codename1.ui.Display;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;


/**
 * A wrapper class around a Codename One app, allows building desktop Java
 * applications.
 *
 * @author Shai Almog
 */
public class CN1Frame extends JFrame implements Runnable, WindowListener {
    private String APP_TITLE = "iOS Certificate Wizard";
    private static final String APP_NAME = "iOSCertificateWizard";
    private static final String APP_VERSION = "1.0";
    private static final int APP_WIDTH = 320;
    private static final int APP_HEIGHT = 480;
    private static final boolean APP_ADAPT_TO_RETINA = false;
    private static final boolean APP_RESIZEABLE = true;
    public static final String BUILD_KEY = "";
    public static final String PACKAGE_NAME = "com.codename1.services.certwizard";
    public static final String BUILT_BY_USER = "Codename One";

    //private static JFrame frm;
    private Object mainApp;
    private Class mainClass;
    
    //private static CertificateWizard certificateWizardCallback;
    
    
    /**
     * @param args the command line arguments
     */
    public CN1Frame(String title, Object lifecycle) {
        super(title);
        APP_TITLE = title;
        mainClass = lifecycle.getClass();
        mainApp = lifecycle;
        JavaSEPort.setNativeTheme("/NativeTheme.res");
        JavaSEPort.blockMonitors();
        JavaSEPort.setExposeFilesystem(true);
        JavaSEPort.setTablet(true);
        JavaSEPort.setUseNativeInput(true);
        JavaSEPort.setShowEDTViolationStacks(false);
        JavaSEPort.setShowEDTWarnings(false);
        CN1Frame frm = this;
        Display.init(frm.getContentPane());
        Display.getInstance().setProperty("build_key", BUILD_KEY);
        Display.getInstance().setProperty("package_name", PACKAGE_NAME);
        Display.getInstance().setProperty("built_by_user", BUILT_BY_USER);
        //Display.getInstance().callSerially(this);
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            Display.getInstance().callSerially(this);
            
            
            
        } else {
            super.setVisible(b);
        }
    }
    
    
    boolean hasRun = false;
    public void run() {
        try {
            if (hasRun) {
                return;
            }
            hasRun = true;
            mainApp = mainApp == null ? mainClass.newInstance() : mainApp;
            mainClass.getMethod("init", Object.class).invoke(mainApp, this);
            //mainApp.init(this);
            //mainApp.start();
            System.out.println("Calling start()");
            mainClass.getMethod("start").invoke(mainApp);
            System.out.println("Finished calling start()");
            CN1Frame frm = this;
            frm.setLocationByPlatform(true);
            frm.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frm.addWindowListener(this);
            frm.setResizable(APP_RESIZEABLE);
            //int w = APP_WIDTH;
            //int h = APP_HEIGHT;
            //if(APP_ADAPT_TO_RETINA && Toolkit.getDefaultToolkit().getScreenSize().width > 2000) {
            //    w *= 2;
            //    h *= 2;
            //}
            //frm.getContentPane().setPreferredSize(new java.awt.Dimension(w, h));
            //frm.getContentPane().setMinimumSize(new java.awt.Dimension(w, h));
            //frm.getContentPane().setMaximumSize(new java.awt.Dimension(w, h));
            frm.pack();
            super.setVisible(true);
        } catch (InstantiationException ex) {
            Logger.getLogger(CN1Frame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(CN1Frame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(CN1Frame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(CN1Frame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(CN1Frame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(CN1Frame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        
        Display.getInstance().callSerially(new Runnable() {
            @Override
            public void run() {
                try {
                    mainClass.getMethod("stop").invoke(mainApp);
                    //mainApp.stop();
                    //mainApp.destroy();
                    mainClass.getMethod("destroy").invoke(mainApp);
                    Display.getInstance().exitApplication();
                } catch (NoSuchMethodException ex) {
                    Logger.getLogger(CN1Frame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SecurityException ex) {
                    Logger.getLogger(CN1Frame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(CN1Frame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(CN1Frame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(CN1Frame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    @Override
    public void windowClosed(WindowEvent e) {
        
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }
    
    //public static void close() {
    //    CN1Frame frm = this;
    //    frm.dispatchEvent(new WindowEvent(frm, WindowEvent.WINDOW_CLOSING));
    //    frm.dispose();
    //}
}
