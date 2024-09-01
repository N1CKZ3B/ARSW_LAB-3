package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback=null;
    
    // Se toma la decision de cambiarlo a un AtomicInteger
    private AtomicInteger health = new AtomicInteger() ;
    
    private int defaultDamageValue;

    private final List<Immortal> immortalsPopulation;

    private final String name;

    private final Random r = new Random(System.currentTimeMillis());

    //Punto 4
    private Object syn;
    private boolean run = true;

    //Punto 5

    private int supposedTotalHealth;
    public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue, 
                    ImmortalUpdateReportCallback ucb, Object syn, int supposedTotalHealth) {
        super(name);
        this.updateCallback=ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health.set(health);

        this.defaultDamageValue=defaultDamageValue;

        this.syn = syn;

        this.supposedTotalHealth = supposedTotalHealth;
    }

    public void run() {

        while (this.getHealth().get() > 0) {
            if (run){
                Immortal im;

                int myIndex = immortalsPopulation.indexOf(this);

                int nextFighterIndex = r.nextInt(immortalsPopulation.size());

                if (this.getHealth().get() == supposedTotalHealth){
                    declareWinner(this);
                    break;
                }
                if (nextFighterIndex == myIndex) {
                    nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
                }

                im = immortalsPopulation.get(nextFighterIndex);

                this.fight(im);

                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }  
            }else{
                synchronized(syn){
                    try {
                        syn.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            

        }

    }

    public void fight(Immortal i2) {
        AtomicInteger i2Health = i2.getHealth();

        if(i2Health.get() > 0 && this.health.get() > 0){
            synchronized ((immortalsPopulation.indexOf(i2) < immortalsPopulation.indexOf(this))?i2:this) {
                synchronized ((immortalsPopulation.indexOf(i2) > immortalsPopulation.indexOf(this))?i2:this) {
                    if (i2Health.get() > 0 && this.health.get() > 0) {
                        i2.changeHealth(i2.getHealth().get() - defaultDamageValue);
                        this.health.addAndGet(defaultDamageValue);
                        updateCallback.processReport("Fight: " + this + " vs " + i2 + "\n");
                    } else {
                        if(i2Health.get() == 0){
                            killOpponent(i2);
                            updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");
                        } else {
                            killOpponent(this);
                            updateCallback.processReport(this + " says:" + this + " is already dead!\n");
                        }
                        
                    }
                }
            }
        }
    }

    public void declareWinner(Immortal i){
        updateCallback.processReport( this + " is the winner >w<"+"\n");
    }
    
    public void killOpponent(Immortal i){
        i.health.set(0);
    }

    public void changeHealth(int v) {
        health.set(v);
    }

    public AtomicInteger getHealth() {
        return health;
    }

    public void stopRun(){
        run = false;
    }
    
    public void resumeRun() {
        run = true;
    }

    @Override
    public String toString() {

        return name + "[" + health + "]";
    }
}
