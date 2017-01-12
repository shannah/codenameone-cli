/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codename1.cli;

import com.google.gson.Gson;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

/**
 *
 * @author shannah
 */
public class CreateProjectForm {
    private ExecutorService executor = Executors.newFixedThreadPool(4);
    private static final String templatesUrl = "https://raw.githubusercontent.com/shannah/codenameone-templates/master/templates.json";
    private final Properties props = new Properties();
    private TemplateInfo[] templates;
    
    public Properties getProperties() {
        return props;
    }
    
    
    public JFrame createCreateProjectFrame(ActionListener onCreate) {
        
        
        JFrame frame = new JFrame("Create Codename One Project");
        JPanel root = createCreateProjectForm(e->{
            frame.dispose();
            onCreate.actionPerformed(e);
        });
        frame.setLayout(new BorderLayout());
        frame.getContentPane().add(BorderLayout.CENTER, root);
        frame.pack();
        return frame;
    }
    
    public JPanel createCreateProjectForm(ActionListener onCreate) {
        JPanel root = new JPanel(new BorderLayout());
        
        JTextField mainClassField = new JTextField(props.getProperty("mainClass", ""));
        mainClassField.addActionListener(e->{
            props.setProperty("mainClass", mainClassField.getText());
        });
        
        JComboBox ide = new JComboBox(new String[]{"Netbeans", "Eclipse", "IntelliJ"});
        ide.addActionListener(e->{
            props.setProperty("ide", (String)ide.getSelectedItem());
        });
        
        
        
        JTextField templateSearch = new JTextField();
        JPanel templatesPanelWrapper = new JPanel(new BorderLayout());
        JTextField templateUrl = new JTextField(props.getProperty("templateUrl", ""));
        templateUrl.addActionListener(e->{
            props.setProperty("templateUrl", templateUrl.getText());
        });
        getTemplatesAsync().thenRun(()->{
            EventQueue.invokeLater(()->{
                System.out.println("Finished loading templates "+Arrays.toString(getTemplates()));
                JPanel templatesPanel = createTemplatesPanel(getTemplates(), templateUrl);
                //System.out.println("Created templates panel "+templatesPanel);
                templatesPanelWrapper.removeAll();
                templatesPanelWrapper.add(BorderLayout.CENTER, new JScrollPane(templatesPanel));
                root.revalidate();
            });
            
        });
        
        templateSearch.addInputMethodListener(new InputMethodListener() {

            @Override
            public void inputMethodTextChanged(InputMethodEvent event) {
                System.out.println("in inputMethodTextChanged...");
                getTemplatesAsync().thenRun(()->{
                    EventQueue.invokeLater(()->{
                        TemplateInfo[] allTemplates = getTemplates();
                        List<TemplateInfo> matches = new ArrayList<TemplateInfo>();
                        String keyword = templateSearch.getText().toLowerCase();
                        for (TemplateInfo ti : allTemplates) {
                            if (ti.name.toLowerCase().contains(keyword)) {
                                matches.add(ti);
                            }
                        }
                        JPanel templatesPanel = createTemplatesPanel(matches.toArray(new TemplateInfo[matches.size()]), templateUrl);
                        templatesPanelWrapper.removeAll();
                        templatesPanelWrapper.add(templatesPanel);
                    });
                    
                });
            }

            @Override
            public void caretPositionChanged(InputMethodEvent event) {
                
            }
            
        });
        
        JPanel north = new JPanel();
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.add(new JLabel("Main Class"));
        north.add(mainClassField);
        north.add(new JLabel("IDE"));
        north.add(ide);
        
        north.add(new JLabel("Template"));
        north.add(templateSearch);
        root.add(BorderLayout.NORTH, north);
        
        root.add(BorderLayout.CENTER, templatesPanelWrapper);
        
        JPanel south = new JPanel();
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
        south.add(templateUrl);
        
        JButton createBtn = new JButton("Create Project");
        createBtn.addActionListener(onCreate);
        
        south.add(createBtn);
        
        root.add(BorderLayout.SOUTH, south);
        
        return root;
                
    }
    
    private JPanel createTemplatesPanel(TemplateInfo[] templates, JTextField templateUrlField) {
        
        JPanel root = new JPanel(new GridLayout(0, 3));
        for (TemplateInfo tpl : templates) {
            System.out.println("Creating template button for "+tpl);
            root.add(createTemplateButton(tpl, templateUrlField));
        }
        
        return root;
    }
    
    private JButton createTemplateButton(TemplateInfo template, JTextField templateUrlField) {
        System.out.println("is dispatch thread: "+EventQueue.isDispatchThread());
        ImageIcon icn = null;
        try {
            
            icn = new ImageIcon(new URL(template.imageUrl));
            Image img = icn.getImage().getScaledInstance(320, 480, Image.SCALE_FAST);
            icn = new ImageIcon(img);
            
            
        } catch (Exception ex){
            ex.printStackTrace();
        }
        if (icn == null) {
            try {
                icn = new ImageIcon(CreateProjectForm.class.getResource("CodenameOneLogo.png"));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        JButton btn = new JButton(icn);
        
        //btn.setText(template.name);
        btn.addActionListener(e->{
            templateUrlField.setText(template.url);
            props.setProperty("templateUrl", template.url);
        });
        
        return btn;
        
    }
    
    private class TemplateInfo {
        String name;
        String url;
        String description;
        String imageUrl;
        
        public String toString() {
            return "TemplateInfo{ name="+name+", url="+url+", description="+description+", imageUrl="+imageUrl+"}";
        }
    }
    
    private class Templates {
        Map<String,TemplateInfo> templates;
    }
    
    
    private TemplateInfo[] extractTemplatesFromMap(Map<String,Map> m) {
        TemplateInfo[] out = new TemplateInfo[m.size()];
        int index=0;
        for (String k : m.keySet()) {
            TemplateInfo ti = new TemplateInfo();
            Map curr = (Map)m.get(k);
            ti.name = (String)curr.get("name");
            ti.imageUrl = (String)curr.get("imageUrl");
            ti.url = (String)curr.get("url");
            ti.description = (String)curr.get("description");
            out[index++] = ti;
        }
        return out;
    }
    
    private TemplateInfo[] loadTemplates() {
        try {
            HttpURLConnection conn = (HttpURLConnection)new URL(templatesUrl).openConnection();
            Gson g = new Gson();
            
            Map mTemplates = g.fromJson(new InputStreamReader(conn.getInputStream(), "UTF-8"), Map.class);
            System.out.println(mTemplates);
            templates = extractTemplatesFromMap(mTemplates);
            return templates;
        } catch (IOException ex) {
            Logger.getLogger(CreateProjectForm.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new TemplateInfo[0];
    }
    
    private CompletableFuture<TemplateInfo[]> loadTemplatesAsync() {
        return CompletableFuture.supplyAsync(()->loadTemplates(), executor);
    }
    
    private TemplateInfo[] getTemplates() {
        if (templates == null) {
            try {
                return loadTemplates();
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }
        return templates;
    }
    
    private CompletableFuture<TemplateInfo[]> getTemplatesAsync() {
        if (templates == null) {
            return loadTemplatesAsync();
        }
        return CompletableFuture.supplyAsync(()->templates, executor);
    }
}
