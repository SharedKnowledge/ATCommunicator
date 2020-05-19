package htw_berlin.ba_timsitte.communication;


public class TerminalText {

    private static TerminalText instance = null;

    private String text = "";

    /**
     * Singleton constructor
     */
    private TerminalText(){

    }

    /**
     *
     * @return
     */
    public static TerminalText getInstance(){
        if (instance == null){
            instance = new TerminalText();
        }
        return instance;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void appendText(String text){
        StringBuilder stringBuilder = new StringBuilder(this.text);
        stringBuilder.append("\n");
        this.text = stringBuilder.toString();
    }
}
