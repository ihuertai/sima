package mx.ipn.sima.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class WhatsappRequest {
    @JsonProperty("messaging_product")
    private String messagingProduct = "whatsapp";
    private String to;
    private String type = "template";
    private Template template;

    /**
     * Constructor corregido para soportar múltiples variables en el cuerpo
     * @param to Teléfono del cliente (521...)
     * @param templateName Nombre de la plantilla en Meta
     * @param lang Código de idioma (es_MX)
     * @param imageUrl URL de la imagen para el Header
     * @param bodyValues Lista de textos para {{1}}, {{2}}, {{3}}, etc.
     */
    public WhatsappRequest(String to, String templateName, String lang, String imageUrl, List<String> bodyValues) {
        this.to = to;
        this.template = new Template(templateName, lang, imageUrl, bodyValues);
    }

    // Getters necesarios para que Jackson serialice a JSON
    public String getMessagingProduct() { return messagingProduct; }
    public String getTo() { return to; }
    public String getType() { return type; }
    public Template getTemplate() { return template; }

    public static class Template {
        private String name;
        private Language language;
        private List<Component> components = new ArrayList<>();

        public Template(String name, String lang, String imageUrl, List<String> bodyValues) {
            this.name = name;
            this.language = new Language(lang);
            
            // 1. Componente HEADER (Imagen)
            Component header = new Component("header");
            header.getParameters().add(new Parameter("image", imageUrl));
            this.components.add(header);

            // 2. Componente BODY (Variables dinámicas {{1}}, {{2}}, {{3}}...)
            Component body = new Component("body");
            if (bodyValues != null) {
                for (String valor : bodyValues) {
                    body.getParameters().add(new Parameter("text", valor));
                }
            }
            this.components.add(body);
        }

        public String getName() { return name; }
        public Language getLanguage() { return language; }
        public List<Component> getComponents() { return components; }
    }

    public static class Language {
        private String code;
        public Language(String code) { this.code = code; }
        public String getCode() { return code; }
    }

    public static class Component {
        private String type;
        private List<Parameter> parameters = new ArrayList<>();
        public Component(String type) { this.type = type; }
        public String getType() { return type; }
        public List<Parameter> getParameters() { return parameters; }
    }

    public static class Parameter {
        private String type;
        private String text;
        private Image image;

        // Constructor para texto
        public Parameter(String type, String value) {
            this.type = type;
            if ("text".equals(type)) {
                this.text = value;
            } else if ("image".equals(type)) {
                this.image = new Image(value);
            }
        }

        public String getType() { return type; }
        public String getText() { return text; }
        public Image getImage() { return image; }
    }

    public static class Image {
        private String link;
        public Image(String link) { this.link = link; }
        public String getLink() { return link; }
    }
}