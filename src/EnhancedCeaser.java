import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.lang.reflect.Array;
import java.util.HashMap;

public class EnhancedCeaser implements EncryptionAlgorithm {
    private static String alphabet = null;
    private static Integer encryption_key = 0;
    private static Integer increment_factor = 0;
    private static String plugboard = null;
    private static HashMap<Character, Character> hashMap;


    /**
     * Method to change characters in the plugboard according to the plugboard passed in the XML file
     * Accepts numbers and letters
     *
     * @param word to be plug
     * @return word pluged
     */
    private static String plugboard(String word) {
        char[] charArray = word.toCharArray();

        for (int i = 0; i < word.length(); i++) {
            for (Character c : hashMap.keySet()) {
                if (c.charValue() == word.charAt(i)) {
                    Array.setChar(charArray, i, hashMap.get(c));
                    break;
                }
            }
        }
        String string = String.valueOf(charArray);

        return string;
    }

    /**
     * Decryption method used on the client
     *
     * @param cleartext message to encrypt
     * @return message encrypted
     */
    @Override
    public String encrypt(String cleartext) {

            String afterPlugString = null;

            String wordPluged = plugboard(cleartext);

            String stringChanged = "";

            for (int ch = 0; ch < wordPluged.length(); ch++) {

                int resultInc = ch * increment_factor;
                int change = encryption_key + resultInc;

                int indexOfAlphabet = alphabet.indexOf(wordPluged.charAt(ch));

                int index = (indexOfAlphabet + change) % alphabet.length();

                char charToChangeOnString;

                if(indexOfAlphabet == -1){
                    charToChangeOnString = wordPluged.charAt(ch);
                } else {
                    charToChangeOnString = alphabet.charAt(index);
                }
                stringChanged += charToChangeOnString;
            }
            afterPlugString = plugboard(stringChanged);

            return afterPlugString;
    }

    /**
     * Decryption method used on the server that does the reverse of encryption
     *
     * @param ciphertext encrypt message
     * @return unencrypted message
     */
    @Override
    public String decrypt(String ciphertext) {

                String afterPlugString = null;

                String wordPluged = plugboard(ciphertext);

                String stringChanged = "";

                for (int ch = 0; ch < wordPluged.length(); ch++) {

                    int resultInc = ch * (alphabet.length() - increment_factor);
                    int change = (alphabet.length() - encryption_key) + resultInc;

                    int indexOfAlphabet = alphabet.indexOf(wordPluged.charAt(ch));

                    int index = (indexOfAlphabet + change) % alphabet.length();

                    char charToChangeOnString;

                    if(indexOfAlphabet == -1){
                        charToChangeOnString = wordPluged.charAt(ch);
                    } else {
                        charToChangeOnString = alphabet.charAt(index);
                    }
                    stringChanged += charToChangeOnString;
                }
                afterPlugString = plugboard(stringChanged);

                return afterPlugString;
    }

    /**
     * Method for setting up the XML file and assigning variables
     *
     * @param configurationFilePath - the absolute path where the configuration file is stored
     * @throws Exception error in the file if it does not comply with the requirements and with the indicated tags
     * @throws NullPointerException error in the file if it does not comply with the requirements and with the indicated tags
     */
    @Override
    public void configure(String configurationFilePath) throws Exception {

        File xmlFile = new File(configurationFilePath);
        try {
            if (xmlFile.exists()) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(xmlFile);
                document.getDocumentElement().normalize();

                NodeList nodeList = document.getElementsByTagName("XML");
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);

                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) node;
                        alphabet = element.getElementsByTagName("alphabet").item(0).getTextContent().trim().toUpperCase();
                        encryption_key = Integer.valueOf(element.getElementsByTagName("encryption-key").item(0).getTextContent().trim());
                        increment_factor = Integer.valueOf(element.getElementsByTagName("increment-factor").item(0).getTextContent().trim());
                        plugboard = element.getElementsByTagName("plugboard").item(0).getTextContent().trim().toUpperCase();
                    }
                }
            }

            //value for alphabet -> default: ABCDEFGHIJKLMNOPQRSTUVWXYZ
            if (alphabet.equals("UPPER".toUpperCase().trim())) {
                alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            } else if (alphabet.equals("UPPER+DIGITS".toUpperCase().trim())) {
                alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            } else if (alphabet.equals("UPPER+DIGITS+PUNCTUATION".toUpperCase().trim())) {
                alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!?.,_:;'ºª#@%$€&/\\(){}[]^|=+*-<>";
            } else if (alphabet.equals("DIGITS+PUNCTUATION".toUpperCase().trim())) {
                alphabet = "0123456789!?.,_:;'ºª#@%$€&/\\(){}[]^|=+*-<>";
            } else if (alphabet.equals("DIGITS".toUpperCase().trim())) {
                alphabet = "0123456789";
            } else if (alphabet.equals("PUNCTUATION".toUpperCase().trim())) {
                alphabet = "!?.,_:;'ºª#@%$€&/\\(){}[]^|=+*-<>";
            } else {
                alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            }

            //validate if R and F is greater than 1
            if(increment_factor < 1 || encryption_key < 1){
                System.out.println("ERROR: the increment_factor = " + increment_factor + " or encryption_key = " + encryption_key + " is less than 1. " +
                        "\nchange the value(s) for equal or greater than 1. ");
                System.exit(0);

                //validate if R and F is less than size of alphabet
            } else if (alphabet.length() - increment_factor < 0 || alphabet.length() - encryption_key < 0){
                System.out.println("ERROR: the increment_factor = " + increment_factor + " or encryption_key = " + encryption_key + " is greater than alphabet = " + alphabet.length() + "." +
                        "\nchange the value(s) according to size of alphabet. ");
                System.exit(0);
            }


            String newPlugboard = plugboard.replaceAll("[^A-Z\\d]", "");

            if (newPlugboard.length() % 2 != 0) {
                System.out.println("ERROR: check is plugboard is in this format {'A':'B','B':'A'}.");
                System.exit(0);
            }

            hashMap = new HashMap<>();

            for (int k = 0; k < newPlugboard.length(); k++) {
                int module = k % 2;

                if (module == 0) {
                    hashMap.put(newPlugboard.charAt(k), newPlugboard.charAt(k + 1));
                }
            }
         } catch (NullPointerException e) {
            System.out.println("ERROR: error on file");
            System.exit(0);
        }
    }
}
