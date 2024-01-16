package br.ufs.dcomp.ExemploRabbitMQ;

import com.rabbitmq.client.*;

import java.io.IOException;

import java.util.Scanner;

import java.time.LocalDate;

import java.time.LocalTime;

import java.util.HashMap;

import java.util.Map;

import java.time.format.DateTimeFormatter;

public class Receptor {

  //private static String user = "";

  public static void main(String[] argv) throws Exception {
    Scanner scanner = new Scanner(System.in);
    
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("ec2-3-90-146-31.compute-1.amazonaws.com"); 
    factory.setUsername("admin");
    factory.setPassword("password");
    factory.setVirtualHost("/");   
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();
    
    System.out.print("User: ");
    String user = scanner.nextLine();

                      //(queue-name, durable, exclusive, auto-delete, params); 
    channel.queueDeclare(user, false,   false,     false,       null);
    
    
    Consumer consumer = new DefaultConsumer(channel) {
      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)           throws IOException {
        
        Object sender = properties.getHeaders().get("sender");
        Object date = properties.getHeaders().get("date");
        Object hour = properties.getHeaders().get("hour");
        
        String message = new String(body, "UTF-8");
        System.out.println("");
        System.out.println("(" + date + " às " + hour + ") " + sender + " diz: "+ message);
      }
    };
                      //(queue-name, autoAck, consumer);    
    channel.basicConsume(user, true,    consumer);
    

    System.out.print(">> ");
    String routing_key = "";
    String message = scanner.nextLine();
    while(true){
      if(message.charAt(0) == '@'){
        routing_key = new String(message);
        
        System.out.print(routing_key + ">> ");
        message = scanner.nextLine();
        
        while(message.charAt(0) != '@'){
            LocalDate hoje = LocalDate.now();
            DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String dataFormatada = hoje.format(formatador); 
            
            Map<String, Object> headers = new HashMap<>();
            headers.put("sender", user);
            headers.put("date", dataFormatada);
            headers.put("hour", LocalTime.now().toString().substring(0, 5));
        
            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
            .headers(headers)
            .build();

            
            channel.basicPublish("", routing_key.replace("@", ""), props, message.getBytes("UTF-8"));  
            System.out.print(routing_key + ">> ");
            message = scanner.nextLine();
        }
      }
    }
  }
}
