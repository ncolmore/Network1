package Drivers;

import Components.Transmitter;

import java.io.IOException;
import java.util.ArrayList;

public class CSMAwithCollisionAvoidance {
    public static void main(String[] args)  {
        ArrayList <Double> xValues=new ArrayList<>();
        ArrayList <Double> xValues2=new ArrayList<>();
        Transmitter t1=new Transmitter();
        t1.setLambda(Transmitter.getLambda1());
        t1.generateUValues();
        t1.generateXValues();
        xValues=t1.getxValuesMicroSeconds();
        xValues2=t1.getxValuesSeconds();
        for(int i=0;i<xValues.size();i++){
            System.out.println(xValues.get(i));
            System.out.println(xValues2.get(i));
        }
    }
}
