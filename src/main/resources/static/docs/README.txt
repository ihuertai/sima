Coloca aqui el PDF que quieres enviar por WhatsApp.

Ruta sugerida:
- src/main/resources/static/docs/informacion-promocion.pdf

Importante:
- Meta necesita descargar el archivo desde una URL publica.
- En local (localhost) no es suficiente para WhatsApp Cloud API.
- Publica la app (o usa un tunel como ngrok) y configura en application.properties:
  whatsapp.flow.mas-info-pdf-url=https://TU_DOMINIO/docs/informacion-promocion.pdf
