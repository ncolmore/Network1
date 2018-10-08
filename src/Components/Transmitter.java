package Components;

import java.util.ArrayList;
import java.util.Random;

import static java.lang.Math.log;

public class Transmitter {
    public static final int dataFrameSize=1500;//expressed in bytes
    public static final int slotDuration=20;//expressed in microseconds
    public static final int SIFSDuration=10;//microseconds
    public static final int ACK=30; //bytes
    public static final int RTS=30;//bytes
    public static final int CTS=30;//bytes
    public static final int  transmitRate=750000; //bytes/sec
    public static final int CWMax=1024;//slots
    public static final int CW0=4;
    public static final int simulationTime=10000000;//microseconds
    private static final int lambda1=50000000;//frames/microsecond
    private static final int lambda2=100000000;//frames/microsecond
    private static final int lambda3=200000000;//frames/microsecond
    private static final int lambda4=300000000;//frames/microsecond
    private int backoffTime;//expressed in microseconds
    private static final int DIFS=40;//expressed in microseconds
    private boolean transmitFrame;
    private int collisions;
    private int lambda;
    private Random generator;
    private ArrayList<Double> uValues;
    private ArrayList<Double> xValues;

    public Transmitter(){
    setBackoffTime(0);
    setCollisions(0);
    setTransmitFrame(false);
    uValues=new ArrayList<>();
    xValues=new ArrayList<>();
    generator=new Random();
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

    public boolean isTransmitFrame() {
        return transmitFrame;
    }

    public void setTransmitFrame(boolean transmitFrame) {
        this.transmitFrame = transmitFrame;
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

    public ArrayList<Double> getxValues() {
        return xValues;
    }

    public void generateUValues(){
        for(int i=0;i<500;i++){
            uValues.add(generator.nextDouble());
            }



    }
    public void generateXValues(){ //the x values this returns are in microseconds
        double tempDouble;
        float modifier;
        for(int i=0;i<500;i++){
            tempDouble=(1-(getuValues().get(i)));
            modifier=(float)1/(float)lambda;
            tempDouble=modifier*log(tempDouble);
            xValues.add(tempDouble);
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
}
