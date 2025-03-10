/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package distribuidos.documentconverterclient;
import javax.swing.JFrame;

/**
 *
 * @author johan
 */
public class DocumentConverterClient {

    public static void main(String[] args) {
        try {
            // Crear e iniciar la ventana de la aplicación
            DocumentsFrame frame = new DocumentsFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        } catch (Exception e) {
            System.err.println("Error en el cliente: " + e.getMessage());
            e.printStackTrace(); // Muestra el stack trace para depuración
        }
    }
}
