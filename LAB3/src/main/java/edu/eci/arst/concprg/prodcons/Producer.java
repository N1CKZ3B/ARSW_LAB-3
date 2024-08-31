/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arst.concprg.prodcons;

import java.util.Queue;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hcadavid
 */
public class Producer extends Thread {

    private Queue<Integer> queue = null;

    private int dataSeed = 0;
    private Random rand=null;
    private final long stockLimit;
    private Object syn;

    public Producer(Queue<Integer> queue,long stockLimit, Object syn) {
        this.queue = queue;
        rand = new Random(System.currentTimeMillis());
        this.stockLimit=stockLimit;
        this.syn = syn;
    }

    @Override
    public void run() {
        while (true) {
            synchronized(syn){
                if (queue.size() < stockLimit){
                    dataSeed = dataSeed + rand.nextInt(100);
                    System.out.println("Producer added " + dataSeed);
                    queue.add(dataSeed);
                    syn.notify();
                }else{
                    try {
                        syn.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                Logger.getLogger(Producer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
