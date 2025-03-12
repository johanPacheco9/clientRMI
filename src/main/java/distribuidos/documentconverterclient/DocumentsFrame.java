package distribuidos.documentconverterclient;

import distribuidos.documentconverter.interfaces.Document;
import distribuidos.documentconverter.interfaces.IdocumentService;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

public class DocumentsFrame extends javax.swing.JFrame {
    private IdocumentService service;
    
   
    private String connecetedIp = "";
    
    
    
    /**
     * Creates new form DocumentsFrame
     */
    public DocumentsFrame() {
        initComponents();
        connectToService();
        
    }
    
   

     private void connectToService() {
        try {
           
            Registry registry = LocateRegistry.getRegistry("192.168.1.6", 8086);  // Assuming the main server is running at this IP and port
            service = (IdocumentService) registry.lookup("documentServer");
            connecetedIp = "192.168.1.6"; 
            System.out.println("Conectado al servidor RMI en " + connecetedIp);
        } catch (Exception e) {
            System.out.println("No se pudo conectar al servidor.");
            JOptionPane.showMessageDialog(this, "No se pudo conectar al servidor RMI.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFileChooserDocuments = new javax.swing.JFileChooser();
        ChooserButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        jFileChooserDocuments.setCurrentDirectory(new java.io.File("D:\\"));
            jFileChooserDocuments.setName("FileChooser"); // NOI18N

            setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

            ChooserButton.setActionCommand("Elegir archivo");
            ChooserButton.setLabel("Elegir archivo");
            ChooserButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    ChooserButtonActionPerformed(evt);
                }
            });

            jLabel1.setText("Seleccionar archivo a convertir en pdf:");

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(40, 40, 40)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(ChooserButton)
                    .addContainerGap(236, Short.MAX_VALUE))
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(81, 81, 81)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(ChooserButton))
                    .addContainerGap(255, Short.MAX_VALUE))
            );

            pack();
        }// </editor-fold>//GEN-END:initComponents

    private void ChooserButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChooserButtonActionPerformed
                                              
    jFileChooserDocuments.setMultiSelectionEnabled(true);
    int returnValue = jFileChooserDocuments.showOpenDialog(null);

    if (returnValue == JFileChooser.APPROVE_OPTION) {
        File[] selectedFiles = jFileChooserDocuments.getSelectedFiles();

        if (selectedFiles.length == 0) {
            JOptionPane.showMessageDialog(this, "No se seleccionó ningún archivo.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Document> documentList = new ArrayList<>();

        try {
            for (File file : selectedFiles) {
                if (!file.exists()) {
                    JOptionPane.showMessageDialog(this, "El archivo no existe: " + file.getAbsolutePath(), "Error", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                Document document = new Document(file.getName(), file.getAbsolutePath(), Files.readAllBytes(file.toPath()));
                documentList.add(document);
                System.out.println("📤 Preparando para enviar: " + file.getName() + " (" + document.getContent().length + " bytes)");
            }

            // Enviar los documentos al servidor
            List<byte[]> pdfFiles = new ArrayList<>();
            try {
                pdfFiles = service.distributeConversion(documentList); 

                if (!pdfFiles.isEmpty()) {
                    System.out.println("✅ Conversión recibida, cantidad de PDFs: " + pdfFiles.size());
                } else {
                    System.err.println("⚠️ No se recibió ningún archivo convertido.");
                }

            } catch (RemoteException ex) {
                Logger.getLogger(DocumentsFrame.class.getName()).log(Level.SEVERE, "Error al enviar documentos", ex);
            }

            // Guardar los PDFs convertidos en la carpeta de descargas del usuario
            File outputDir = new File(System.getProperty("user.home") + "/Downloads/converted_pdfs/");
            if (!outputDir.exists()) {
                outputDir.mkdirs(); // Crear la carpeta si no existe
            }

            for (int i = 0; i < pdfFiles.size(); i++) {
                String originalFileName = selectedFiles[i].getName().replace(".docx", ".pdf");
                File outputFile = new File(outputDir, originalFileName);
                Files.write(outputFile.toPath(), pdfFiles.get(i));
                System.out.println("📥 Guardado: " + outputFile.getAbsolutePath());
            }

            JOptionPane.showMessageDialog(DocumentsFrame.this, "Conversión completada. Los archivos PDF han sido guardados en:\n" + outputDir.getAbsolutePath(), "Éxito", JOptionPane.INFORMATION_MESSAGE);

            
            Desktop.getDesktop().open(outputDir);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error en la conversión: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    }//GEN-LAST:event_ChooserButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(DocumentsFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DocumentsFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DocumentsFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DocumentsFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new DocumentsFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton ChooserButton;
    private javax.swing.JFileChooser jFileChooserDocuments;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}
