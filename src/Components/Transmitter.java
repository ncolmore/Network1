package Components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static java.lang.Math.log;

public class Transmitter {
    public static final int dataFrameSize=100;//slots
    public static final int SIFSDuration=1;//slot
    public static final int ACK=2; //dlots
    public static final int RTS=2;//slots
    public static final int CTS=2;//slots
    public static final int CWMax=1024;//slots
    public static final int CW0=4;
    private static final int DIFS=2;//slots
    public static int handshake=RTS+DIFS+SIFSDuration+CTS;
    public static final int transmissionTotal=ACK+SIFSDuration+dataFrameSize;//slots  total amount of slots used during successful transmission
    private int CWCurrent;
    public int backoffPlusDIFS;
    private static final int lambda1=50000000;//frames/microsecond
    private static final int lambda2=100000000;//frames/microsecond
    private static final int lambda3=200000000;//frames/microsecond
    private static final int lambda4=300000000;//frames/microsecond
    private int backoffTime=0;//slots

    private int collisions;
    private int transmissions;
    private int lambda;
    private boolean ack;
    private Random generator;
    private ArrayList<Double> uValues;
    private ArrayList<Double> xValuesMicroSeconds;
    private ArrayList<Double> xValuesSeconds;
    private ArrayList<Integer> trafficSlots;
    public Transmitter(){
    setBackoffTime(0);
    setCollisions(0);
    uValues=new ArrayList<>();
    xValuesMicroSeconds=new ArrayList<>();
    xValuesSeconds=new ArrayList<>();
    trafficSlots=new ArrayList<>();
    generator=new Random();
    setTransmissions(0);
    setCollisions(0);
    CWCurrent=CW0;
    }
    public int getBackoffTime() {
        return backoffTime;
    }

    public void setBackoffTime(int backoffTime) {
        this.backoffTime = backoffTime;
    }

    public int getDIFS() {
        return DIFS;
    }



    public int getCollisions() {
        return collisions;
    }

    public void setCollisions(int collisions) {
        this.collisions = collisions;
    }

    public int getLambda() {
        return lambda;
    }

    public void setLambda(int lambda) {
        this.lambda = lambda;
    }

    public ArrayList<Double> getuValues() {
        return uValues;
    }

    public ArrayList<Double> getxValuesMicroSeconds() {
        return xValuesMicroSeconds;
    }

    public ArrayList<Double> getxValuesSeconds() {
        return xValuesSeconds;
    }

    public void generateUValues(){
        for(int i=0;i<50000;i++){
            uValues.add(generator.nextDouble());
            }



    }
    public void generateXValues(){ //the x values this returns are in microseconds
        double tempDouble;
        float modifier;
        for(int i=0;i<50000;i++){
            tempDouble=(1-(getuValues().get(i)));
            modifier=(float)-1/(float)lambda;
            tempDouble=modifier*log(tempDouble);
            xValuesMicroSeconds.add(tempDouble);
            xValuesSeconds.add(tempDouble*1000000);
        }



    }

    public void generateTrafficSlots(){
        for(int i=0;i<xValuesSeconds.size();i++){
            trafficSlots.add((int) Math.ceil(xValuesSeconds.get(i)/(2e-5)));


        }
        for(int i=0;i<xValuesSeconds.size();i++){
            if(i==0){
                //does nothing for the first portion
            }
            else{
                trafficSlots.set(i,trafficSlots.get(i)+trafficSlots.get(i-1));
            }

        }
    }

    public static int getLambda1() {
        return lambda1;
    }

    public static int getLambda2() {
        return lambda2;
    }

    public static int getLambda3() {
        return lambda3;
    }

    public static int getLambda4() {
        return lambda4;
    }

    public ArrayList<Integer> getTrafficSlots() {
        return trafficSlots;
    }

    public int getTransmissions() {
        return transmissions;
    }

    public void setTransmissions(int transmissions) {
        this.transmissions = transmissions;
    }

    public int getCWCurrent() {
        return CWCurrent;
    }

    public void setCWCurrent(int CWCurrent) {
        this.CWCurrent = CWCurrent;
    }

    public void doubleCW(){
        CWCurrent=CWCurrent*2;
    }
    public void resetCW(){
        CWCurrent=CW0;
    }

    public void generateBackoffTime(){
        Random r = new Random();
        int Low = 0;
        int High = CWCurrent-1;
        backoffTime = r.nextInt(High-Low) + Low;
        backoffPlusDIFS=backoffTime+DIFS;
    }

    public boolean isAck() {
        return ack;
    }

    public void setAck(boolean ack) {
        this.ack = ack;
    }

    public static int getTransmissionTotal() {
        return transmissionTotal;
    }

    public static int getCWMax() {
        return CWMax;
    }

}
