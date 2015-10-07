package com.example.padsolver;

import android.util.Log;
import android.util.Pair;

public class State{
    Deque dirs = new Deque();
    int startX, startY;
    float value = -1;
    int combo = 0;
    int hash = 0;
    Global global = Global.Instance;
    
    public float getValue() {
        if (value < -1e-6) {
        	global.strResult = "";
            Board.oprBoard.setOrbs(Board.startBoard.orbs);       
            Board.oprBoard.simulate(startX, startY, dirs);          
            Pair<Float, Integer> pair = Board.oprBoard.getValue();
            value = pair.first;
            combo = pair.second;
        }
        return value;
    }
    
    public void setDirs(Deque dirs) {
        this.dirs.head = dirs.head;
        this.dirs.tail = dirs.tail;
        this.dirs.size = dirs.size;
        for (int i = 0; i < dirs.maxLen; i ++) {
        	this.dirs.data[i] = dirs.data[i];
        }
    }
    
    public void setStartXY(int x, int y) {
        startX = x;
        startY = y;
    }
    
    public void randomize(int length) {
        do {
            startX = (int) (Math.random() * global.cols);
            startY = (int) (Math.random() * global.rows);
            int curX = startX, curY = startY;
            dirs.resize(length);
            for (int i = 0; i < length; i ++) {
                do {
                    dirs.set(i, (int)(Math.random() * 4));
                } while ((i > 0 && !global.isValidDirPair(dirs.get(i), dirs.get(i-1))) ||
                		!global.isValidPosition(curX+global.dx[dirs.get(i)], curY+global.dy[dirs.get(i)]));
                curX += global.dx[dirs.get(i)];
                curY += global.dy[dirs.get(i)];
            }
        } while (!isValidDirs());
    }
    
    public int getHash() {
        if (hash == 0) {
            hash = global.getHash(startX, startY, dirs);
        }
        return hash;
    }

    public boolean isValidDirs() {
        return global.isValidDirs(startX, startY, dirs);
    }
    
    public State variate() {
        State ans = new State();
        int t;
        int r = (int)(Math.random() * 25);
        int cnt = 0;
        do {
        	if (cnt ++ == 1000) {
        		Log.v("debug", "" + r);
        	}
            ans.setDirs(dirs);
            ans.setStartXY(startX, startY);
            if (r <= 1 && ans.dirs.size() > 1) {
                ans.dirs.pop_back();
            } else if (r <= 3 && ans.dirs.size() < global.maxLength) {
                while(true) {
                    t = (int) (Math.random() * 4);
                    if (!global.isValidDirPair(t, ans.dirs.back())) continue;
                    break;
                }
                ans.dirs.push_back(t);
            } else if (r <= 5 && ans.dirs.size() > 1) {
                ans.startX += global.dx[ans.dirs.get(0)];
                ans.startY += global.dy[ans.dirs.get(0)];
                ans.dirs.pop_front();
            } else if (r <= 7 && ans.dirs.size() < global.maxLength) {
                while(true) {
                	t = (int) (Math.random() * 4);
                    if (!global.isValidDirPair(t, ans.dirs.front())) continue;
                    if (!global.isValidPosition(ans.startX-global.dx[t], ans.startY-global.dy[t])) continue;
                    break;
                }
                ans.startX -= global.dx[t];
                ans.startY -= global.dy[t];
                ans.dirs.push_front(t);
            } else if (r <= 9) {
                t = (int) (Math.random() * global.maxLength);
                for (int i = 0; i < t; i ++) {
                    int t1, t2;
                    while(true) {
                        t1 = (int) (Math.random() * dirs.size());
                        t2 = (int) (Math.random() * 4);
                        if (t1 > 0 && !global.isValidDirPair(ans.dirs.get(t1-1), t2)) continue;
                        if (t1 < ans.dirs.size() - 1 && !global.isValidDirPair(ans.dirs.get(t1+1), t2)) continue;
                        break;
                    };
                    ans.dirs.set(t1, t2);
                }
            } else if (r <= 17) {
                t = (int) (Math.random() * global.maxLength);
                for (int i = t; i < dirs.size(); i ++) {
                	int t1;
                	while (true) {
                		t1 =  (int) (Math.random() * 4);
                		if (i > 0 && !global.isValidDirPair(ans.dirs.get(i-1), t1)) continue;
                		break;
                	}
                    ans.dirs.set(i, t1);
                }
            } else {
                t = (int) (Math.random() * global.maxLength);
                for (int i = 0; i < t; i ++) {
                    int t1, t2;
                    while(true) {
                        t1 = (int) (Math.random() * dirs.size());
                        t2 = (int) (Math.random() * dirs.size());
                        if (t1 > 0 && !global.isValidDirPair(ans.dirs.get(t1-1), ans.dirs.get(t2))) continue;
                        if (t1 < ans.dirs.size() - 1 && !global.isValidDirPair(ans.dirs.get(t1+1), ans.dirs.get(t2))) continue;
                        if (t2 > 0 && !global.isValidDirPair(ans.dirs.get(t2-1), ans.dirs.get(t1))) continue;
                        if (t2 < ans.dirs.size() - 1 && !global.isValidDirPair(ans.dirs.get(t2+1), ans.dirs.get(t1))) continue;
                        break;
                    }
                    int tmp = ans.dirs.get(t1);
                    ans.dirs.set(t1, ans.dirs.get(t2));
                    ans.dirs.set(t2, tmp);
                }
            }
        } while (!ans.isValidDirs());
        return ans;
    }
    
    public State cross(State s) {
        State ans = new State();
        int x = startX, y = startY;
        for (int i = 0; i < global.cols; i ++) {
            for (int j = 0; j < global.rows; j ++) {
            	global.footprint[i][j] = 0;
            }
        }
        for (int i = 0; i < dirs.size(); i ++) {
            x += global.dx[dirs.get(i)];
            y += global.dy[dirs.get(i)];
            global.footprint[x][y] = i;
            global.footprintOwner[x][y] = getHash();
        }
        x = s.startX; y = s.startY;
        boolean found = false;
        for (int i = 0; i < s.dirs.size(); i ++) {
            x += global.dx[s.dirs.get(i)];
            y += global.dy[s.dirs.get(i)];
            if (global.footprintOwner[x][y] == getHash()) {
                Deque newDir = new Deque();
                if (i < global.footprint[x][y]) {
                    ans.setStartXY(s.startX, s.startY);
                    for (int j = 0; j <= i; j ++) {
                        newDir.push_back(s.dirs.get(j));
                    }
                    for (int j = global.footprint[x][y]+1; j < dirs.size(); j ++) {
                        newDir.push_back(dirs.get(j));
                    }
                } else {
                    ans.setStartXY(startX, startY);
                    for (int j = 0; j <= global.footprint[x][y]; j ++) {
                        newDir.push_back(dirs.get(j));
                    }
                    for (int j = i+1; j < s.dirs.size(); j ++) {
                        newDir.push_back(s.dirs.get(j));
                    }
                }
                ans.setDirs(newDir);
                found = true;
                break;
            }
        }
        if (!found || !ans.isValidDirs()) {
            ans.setStartXY(startX, startY);
            ans.setDirs(dirs);
        }
        return ans;
    }
    
    public String print() {
    	String ans = "";
        ans += startX;
    	ans += " " + startY + "\n";
        for (int i = 0; i < dirs.size(); i ++) {
            ans += global.dirName[dirs.get(i)] + " ";
        }
        ans += "\n";
        return ans;
    }
}
