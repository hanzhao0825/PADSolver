package com.example.padsolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.util.Log;


public class GAOptimizer {
    int groupSize;
    List<State> group = new ArrayList<State>();
    Global global = Global.Instance;
    
    public void init(int groupSize) {
        this.groupSize = groupSize;
        for (int i = 0; i < groupSize; i ++) {
            State s = new State();
            s.randomize((int) (Math.random() * (global.maxLength / 2) + global.maxLength / 2 + 1));
            group.add(s);
        }
    }
    
    public void iterate(int times) {
        int rem = 0;
        for (int time = 1; time <= times; time ++) {
            Log.v("debug", "ITERATION NUM : " + time);
            
            //Add Random New Units
            for (int i = rem / 5; i > 0; i --) {
                State s = new State();
                s.randomize((int) (Math.random() * (global.maxLength / 2) + global.maxLength / 2 + 1));
                group.add(s);
            }
            
            //Reproduce
            int s = group.size();
            for (int i = 0; i < s; i ++) {
                for (int j = (int) (((float)rem / groupSize) * 3 + 1); j > 0 ; j --) {
                    group.add(group.get(i).variate());
                    group.add(group.get(i).cross(group.get((int) (Math.random() * group.size()))));
                }
            }
            
            
            //Kill Some
            Collections.sort(group, new Comparator<State>() {
                public int compare(State s1, State s2) {
                    if (s1.getValue() != s2.getValue())
                        return s1.getValue() > s2.getValue() ? -1 : 1;
                    if (s1.dirs.size() != s2.dirs.size())
                        return s1.dirs.size() < s2.dirs.size() ? -1 : 1;
                    if (s1.getHash() != s2.getHash())
                    	return s1.getHash() < s2.getHash() ? -1 : 1;
                    return 0;
                }
            });
            


            
            List<State> newGroup = new ArrayList<State>();
            newGroup.add(group.get(0));
            int cnt = 1, sum = 0;
            for (int i = 1; i < group.size(); ++i) {
                if (group.get(i).getHash() != group.get(i-1).getHash()) {
                    cnt = 1;
                    newGroup.add(group.get(i));
                    if (++sum == groupSize) break;
                } else {
                    ++ cnt;
                    if (cnt < (group.get(i).combo - global.rows + 1) / 2 + 1) {
                        newGroup.add(group.get(i));
                        if (++sum == groupSize) break;
                    }
                }
            }
            
            group = newGroup;
            
            //Find The Number Of Units That Appear More Than One Generation
            rem = 0;
            for (int i = 0; i < group.size(); i ++) {
                if (global.hashCnt.get(group.get(i).getHash()) != null) {
                    rem ++;
                }
            }
            Log.v("debug", "rem = " + rem);
            global.hashCnt.clear();
            for (int i = 0; i < group.size(); i ++) {
            	global.hashCnt.put(group.get(i).getHash(), 1);
            }
            
            for (int i = 0; i < Math.min(5, group.size()); i ++) {
                Log.v("debug", ""+ group.get(i).combo + " " + group.get(i).getValue() + " " + group.get(i).getHash());
            }
        }
        Log.v("debug", group.get(0).print());
    }
}
