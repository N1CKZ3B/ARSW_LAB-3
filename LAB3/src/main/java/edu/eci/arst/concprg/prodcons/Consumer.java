/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arst.concprg.prodcons;

import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hcadavid
 */
public class Consumer extends Thread{
    
    private Queue<Integer> queue;
    private Object syn;
    
    public Consumer(Queue<Integer> queue, Object syn){
        this.queue=queue;  
        this.syn = syn;      
    }
    
    @Override
    public void run() {
        while (true) {
            synchronized(syn){
                if (queue.size() > 0) {
                    int elem=queue.poll();
                    System.out.println("Consumer consumes "+elem);                         
                }else{
                    syn.notify();
                }
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(Producer.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
        }
    }
}
