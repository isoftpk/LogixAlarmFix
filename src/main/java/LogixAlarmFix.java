import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.swing.DocumentTreeModel;
import org.dom4j.swing.XMLTableModel;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;


import javax.swing.*;

/**
 * Set the format of the xml file content: compact and beautiful
 *
 * Set the encoding format of the xml file: encoding, keep the storage format and the declaration format the same
 *
 * @author mzy
 *
 */
public class LogixAlarmFix extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    // launch the application
    public void start(Stage primaryStage) {

        // set title for the stage
        primaryStage.setTitle("AlarmUtils");
        TreeView tree = null;
        
        try {

            SAXReader reader = new SAXReader();
            Document doc = reader.read(new File("CA100.L5X"));

            // Create output stream
            OutputStream os = new FileOutputStream("CA100_out.L5X");

            OutputFormat compactFormat = OutputFormat.createCompactFormat(); // compact format
            OutputFormat prettyFormat = OutputFormat.createPrettyPrint(); // pretty format

    //        compactFormat.setEncoding("utf-8");
    //        prettyFormat.setEncoding("gbk");

//            XMLWriter writer = new XMLWriter(os, prettyFormat);
            XMLWriter writer = new XMLWriter(os);

                List<Node> nPrograms = doc.selectNodes("/RSLogix5000Content/Controller/Programs/Program");

                for (Node nPrg : nPrograms) {
                    System.out.println(nPrg.getName() + ": " + nPrg.valueOf("@Name"));

                    Integer iAlarmCnt = 10;
                    Integer iAla = 12;

                    List<Node> nRoutines = nPrg.selectNodes("./Routines/Routine");

                    for (Node nRtn : nRoutines) {
                        if (nRtn.valueOf("@Type").equals("RLL")) {
                            System.out.println(nRtn.getName() + ": " + nRtn.valueOf("@Name"));

                            List<Node> nRungs = nRtn.selectNodes("./RLLContent/Rung");

                            for (Node nRung : nRungs) {
//                                System.out.println(nRung.getName() + ": " + nRung.valueOf("@Number"));
                                if (nRung.selectSingleNode("Comment") != null && nRung.selectSingleNode("Text") != null) {
                                    if (nRung.selectSingleNode("Comment").getText().contains("<@DIAG>")) {

                                        String sComment = nRung.selectSingleNode("Comment").getText();
                                        String sCommentDiag = sComment.substring(sComment.lastIndexOf("<@DIAG>"));

                                        ArrayList<String> lstCommentPrompt = new ArrayList<String>();
                                        ArrayList<String> lstCommentAlarms = new ArrayList<>(Arrays.asList(sComment.split("\\r?\\n")));

                                        String sUpdatedCommentAlarms = "";

                                        int i = 0;
                                        for (String sCommentAlarm : lstCommentAlarms) {
                                            i++;
                                            Pattern patAlarm = Pattern.compile("<Alarm\\[(.*?)>");
                                            Matcher matAlarm = patAlarm.matcher(sCommentAlarm);

                                            if (matAlarm.find()) {
                                                sUpdatedCommentAlarms += sCommentAlarm.replaceFirst("(?<=<Alarm\\[).*?(?=\\])", iAlarmCnt.toString()) + "\n";
                                                iAlarmCnt++;
                                            }
                                            else    {
                                                sUpdatedCommentAlarms += sCommentAlarm;
                                                if(sCommentAlarm.length() > 0 && i < lstCommentAlarms.size())
                                                    sUpdatedCommentAlarms += "\n";
                                            }

                                        }
                                        nRung.selectSingleNode("Comment").setText("\n<![CDATA[" + sUpdatedCommentAlarms + "]]>\n");
                                        System.out.println(sUpdatedCommentAlarms);


                                        Pattern patPrompt = Pattern.compile("<Prompt\\[(.*?)>");
                                        Matcher matPrompt = patPrompt.matcher(sCommentDiag);

                                        while (matPrompt.find()) {
                                            lstCommentPrompt.add(matPrompt.group());
                                        }
                                        for(String sComPrompt : lstCommentPrompt)  {
                                            System.out.println(sComPrompt);
                                        }

                                        String sTextAlarm = nRung.selectSingleNode("Text").getText();
                                        ArrayList<String> lstTextAlarm = new ArrayList<String>();

                                        Pattern patTextAlarm = Pattern.compile("JSR\\(zZ999_Diagnostics(.*?)\\)");
                                        Matcher matTextAlarm = patTextAlarm.matcher(sTextAlarm);

                                        while (matTextAlarm.find()) {
                                            lstTextAlarm.add(matTextAlarm.group());
//                                            System.out.println(sTextAlarm.substring(matTextAlarm.end()));


                                        }
                                        for(String sTxtAlarm : lstTextAlarm)  {
                                            System.out.println(sTxtAlarm);
                                        }

//                                        System.out.println(nRung.selectSingleNode("Text").getText());

                                    }
                                }

                            }
                        }
                    }
               }

            writer.setEscapeText(false);
            writer.write(doc);
            writer.close();

                //Iterator collection
//                Iterator elementIterator = rootElement.elementIterator();
                //Traverse Iterator Collection



//                while (elementIterator.hasNext()) {
//                    Element brandElement = (Element) elementIterator.next();
//                    //Get an element of the element
//                    String brand = brandElement.attributeValue("Name");
//                    //Continue from the previous node to get the next node iterator collection
//                    Iterator typeElements = brandElement.elementIterator();
//                    while (typeElements.hasNext()) {
//                        Element typeElement = (Element) typeElements.next();
//                        String type = typeElement.attributeValue("Name");
//                        System.out.println("Element:" + type);
//                    }
//                }


        } catch (DocumentException | FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }



        // build table model
//        XMLTableModel model = new XMLTableModel(reader.read(doc.getName()),doc);


//        VBox vbox = new VBox();
//
//        Scene scene = new Scene(vbox);
//
//        primaryStage.setScene(scene);

//        primaryStage.show();
            primaryStage.close();
    }

//    private String replaceOccurance(String text, String replaceFrom, String replaceTo, int occuranceIndex)
//    {
//        StringBuffer sb = new StringBuffer();
//        Pattern p = Pattern.compile(replaceFrom);
//        Matcher m = p.matcher(text);
//        int count = 0;
//        while (m.find())
//        {
//            if (count++ == occuranceIndex - 1)
//            {
//                m.appendReplacement(sb, replaceTo);
//            }
//        }
//        m.appendTail(sb);
//        return sb.toString();
//    }


}