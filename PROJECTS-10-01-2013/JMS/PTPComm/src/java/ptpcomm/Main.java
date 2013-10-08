/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ptpcomm;

import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.jms.*;
import message.NewInterface;

public class Main implements MessageListener {
String user;

    @Override
    public void onMessage(Message message) {

        try {

            TextMessage textMessage = (TextMessage) message;
            if(!read.get())
                System.out.println("\n" + textMessage.getText());
            else
                 System.out.println(textMessage.getText());
            read.set(false);
            System.out.print(user + ": ");
         //   sendMessage();
        } catch (JMSException jmse) {
            jmse.printStackTrace();
        }
    }
    @Resource(mappedName = "jms/MyQueue")
    private static Queue myQueue;
    @Resource(mappedName = "jms/QueueConnectionFactory")
    private static QueueConnectionFactory myQueueFactory;
    private QueueConnection qConnect = null;
    private QueueSession sendSession = null;
    private static QueueSession receiveSession = null;
    private AtomicBoolean read = new AtomicBoolean(false);
    BufferedReader br = null;
 static   QueueReceiver queueReceiver = null;
  QueueSender qSender = null;
    public Main() {
        try {
            // Connect to the provider and get the JMS connection
            br = new BufferedReader(new InputStreamReader(System.in));
            qConnect = myQueueFactory.createQueueConnection();

            // Create the JMS Session
            sendSession = qConnect.createQueueSession(
                    false, Session.AUTO_ACKNOWLEDGE);
           receiveSession = qConnect.createQueueSession(
                    false, Session.AUTO_ACKNOWLEDGE);


            // Now that setup is complete, start the Connection
           
            qConnect.start();
             
             qSender = sendSession.createSender(myQueue);
             

// Create the message listener
          

        } catch (JMSException jmse) {
            jmse.printStackTrace();
            System.exit(1);
        }
    }

    private void sendMessage(String user) {
        try {
            // Create JMS message

           System.out.print(user + ": ");
           System.out.flush();
            String s = null;
            read.set(false);
            try {
                s = br.readLine();
                read.set(true);
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }

            TextMessage msg = sendSession.createTextMessage();
           msg.setStringProperty("user", user);
            msg.setText(user + ": " + s);


            // Create the sender and send the message
            
            qSender.send(msg);



        } catch (JMSException jmse) {
            jmse.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String argv[]) {
Main mainObj = new Main();
mainObj.user = argv[0];
String messageSelector = "user <> '" + argv[0] + "'";
        try {
            queueReceiver = receiveSession.createReceiver(myQueue, messageSelector);
        } catch (JMSException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            queueReceiver.setMessageListener(mainObj);
        } catch (JMSException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
       // mainObj.sendMessage();//.sendMessage();
        while(true){
            mainObj.sendMessage(argv[0]); 
        }
//        try {
//            Thread.sleep(1000000000);
//        } catch (InterruptedException ex) {argv
//            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
}
