package mx.ipn.sima.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WhatsappResponse {
    @JsonProperty("entry")
    public List<Entry> entry;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Entry {
        @JsonProperty("changes")
        public List<Change> changes;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Change {
        @JsonProperty("value")
        public Value value;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Value {
        @JsonProperty("messaging_product")
        public String messagingProduct;
        @JsonProperty("messages")
        public List<Message> messages;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        public String from;
        public Text text;
        public String type;
        public Button button;
        public Interactive interactive;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Text {
        public String body;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Button {
        public String payload;
        public String text;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Interactive {
        public String type;
        @JsonProperty("button_reply")
        public ButtonReply buttonReply;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ButtonReply {
        public String id;
        public String title;
    }

}
