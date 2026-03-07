package mx.ipn.sima.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class WhatsappServiceTest {

    @Autowired
    private WhatsappService whatsappService;

    @Test
    void probarEnvioImagenYTexto() {
        // 1. Datos reales para la prueba
        String miTelefono = "525514799033"; // <--- PON TU CELULAR AQUÍ
        String urlImagen = "https://raw.githubusercontent.com/pokeapi/sprites/master/sprites/pokemon/25.png"; 
        
        // 2. IMPORTANTE: Deben ser exactamente la cantidad de variables {{n}} de tu plantilla
        List<String> variablesCuerpo = Arrays.asList("Descuento 2x1");

        // 3. Ejecutar envío
        boolean resultado = whatsappService.sendTemplateWithImage(
            miTelefono, 
            urlImagen, 
            variablesCuerpo
        );

        // 4. Verificar resultado
        assertTrue(resultado, "El envío falló. Revisa la consola para ver el error de Meta.");
    }
}