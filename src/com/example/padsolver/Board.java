package com.example.padsolver;

import android.util.Pair;

import java.util.LinkedList;
import java.util.Queue;

public class Board {
    int orbs[][];
    float value = -1;
    int combo = -1;
    Global global = Global.Instance;
    static Board startBoard, oprBoard;
    
    public Board() {
        orbs = new int[global.cols][global.rows];
        for (int i = 0; i < global.cols; i ++) {
        	for (int j = 0; j < global.rows; j ++) {
        		orbs[i][j] = 0;
        	}
        }
    }
    
    public Board clone() {
    	Board ans = new Board();
        orbs = new int[global.cols][global.rows];
        for (int i = 0; i < global.cols; i ++) {
        	for (int j = 0; j < global.rows; j ++) {
        		ans.orbs[i][j] = orbs[i][j];
        	}
        }
        return ans;
    }
    
    public void setOrbs(int orbsSrc[]) {
        for (int i = 0; i < global.cols; i ++) {
            for (int j = 0; j < global.rows; j ++) {
                orbs[i][j] = orbsSrc[i + j*global.cols];
            }
        }
        value = combo = -1;
    }
    
    public void setOrbs(int orbsSrc[][]) {
        for (int i = 0; i < global.cols; i ++) {
            for (int j = 0; j < global.rows; j ++) {
                orbs[i][j] = orbsSrc[i][j];
            }
        }
        value = combo = -1;
    }
    
    public void setOrbs(char c[], int mode) {
        for (int i = 0; i < global.cols; i ++) {
            for (int j = 0; j < global.rows; j ++) {
                int o = 0;
                for (int k = 0; k <= 7; k ++) {
                    if (global.orbName[mode][k] == c[i+j*global.cols] || global.orbName[mode][k] == c[i+j*global.cols]-32) {
                        o = k;
                        break;
                    }
                }
                orbs[i][j] = o;
            }
        }
        value = combo = -1;
    }
    
    public Pair<Float,Integer> getValue() {
    	float tmpValue = 0;
    	int tmpCombo = 0;
    	Pair<Float,Integer> ans;
        if (value < -1e-6) {
            while (true) {
            	Pair<Float,Integer> tp = findAndRemoveMatch();
            	tmpCombo += tp.second;
            	tmpValue += tp.first;
            	if (tp.second == 0) break;
            }
            tmpValue *= tmpCombo;
            value = tmpValue;
        } else {
            tmpValue = value;
            tmpCombo = combo;
        }
        ans = new Pair<Float,Integer>(tmpValue, tmpCombo);
        return ans;
    }
    
    public Pair<Float,Integer> findAndRemoveMatch() {
        Board newBoard = new Board();
        int cnt, lastOrb;
        float tmpValue = 0;
        int tmpCombo = 0;
        for (int i = 0; i < global.cols; i ++) {
            cnt = 1;
            lastOrb = orbs[i][0];
            for (int j = 1; j < global.rows; j ++) {
                if (orbs[i][j] == lastOrb) {
                    cnt ++;
                    if (cnt == 3) {
                        for (int k = 0; k < 3; k ++) {
                            newBoard.orbs[i][j-k] = lastOrb;
                        }
                    } else if (cnt > 3) {
                        newBoard.orbs[i][j] = lastOrb;
                    }
                } else {
                    cnt = 1;
                    lastOrb = orbs[i][j];
                }
            }
        }
        for (int j = 0; j < global.rows; j ++) {
            cnt = 1;
            lastOrb = orbs[0][j];
            for (int i = 1; i < global.cols; i ++) {
                if (orbs[i][j] == lastOrb) {
                    cnt ++;
                    if (cnt == 3) {
                        for (int k = 0; k < 3; k ++) {
                            newBoard.orbs[i-k][j] = lastOrb;
                        }
                    } else if (cnt > 3) {
                        newBoard.orbs[i][j] = lastOrb;
                    }
                } else {
                    cnt = 1;
                    lastOrb = orbs[i][j];
                }
            }
        }
        
        int rowCnt[] = new int[global.rows];
        int x, y, tx, ty;
        for (int i = 0; i < global.cols; i ++) {
            for (int j = 0; j < global.rows; j ++) {
                if (newBoard.orbs[i][j] != 0) {
                    for (int k = 0; k < global.rows; k ++) {
                    	rowCnt[k] = 0;
                    }
                    boolean hasRow = false;
                    int matchSize = 0;
                    int orbColor = newBoard.orbs[i][j];
                    Queue<Pair<Integer, Integer>> q = new LinkedList<Pair<Integer, Integer>>();
                    q.add(new Pair<Integer, Integer>(i, j));
                    newBoard.orbs[i][j] = 0;
                    orbs[i][j] = 0;
                    while (!q.isEmpty()) {
                        Pair<Integer, Integer> head = q.element();
                        x = head.first;
                        y = head.second;
                        if ( ++rowCnt[y] == global.cols) {
                            hasRow = true;
                        };
                        matchSize ++;
                        for (int dir = 0; dir < 4; dir ++) {
                            tx = x + global.dx[dir];
                            ty = y + global.dy[dir];
                            if (global.isValidPosition(tx, ty) && orbColor == newBoard.orbs[tx][ty]) {
                                newBoard.orbs[tx][ty] = 0;
                                orbs[tx][ty] = 0;
                                q.add(new Pair<Integer, Integer>(tx, ty));
                            }
                        }
                        q.remove();
                    }
                    if (matchSize >= 3) {       //TODO
                        tmpCombo ++;
                        //tmpValue += weight[orbColor].normal;
                        tmpValue += matchSize;//= matchSize * matchSize;
                        global.strResult += (""+(global.orbName[0][orbColor])) + matchSize + " ";
                    }
                    if (hasRow) {
                    	tmpValue += 0;
                    }
                    if (global.mode == 1) { // kaarii
                    	if (matchSize == 4 && orbColor == 4) {
                    		tmpValue += 20;
                    	}
                    }
                }
            }
        }
        for (int i = 0; i < global.cols; i ++) {
            ty = global.rows-1;
            for (int j = global.rows-1; j >= 0; j --) {
                if (orbs[i][j] != 0) {
                    orbs[i][ty--] = orbs[i][j];
                }
            }
            for (; ty >= 0; ty --) {
                orbs[i][ty] = 0;
            }
        }
        Pair<Float, Integer> ans = new Pair<Float, Integer>(tmpValue, tmpCombo);
        return ans;
    }

    public void simulate(int startX, int startY, Deque dirs) {
        int tx, ty;
        for (int i = 0; i < dirs.size(); i ++) {
            tx = startX + global.dx[dirs.get(i)];
            ty = startY + global.dy[dirs.get(i)];
            int tmp = orbs[startX][startY];
            orbs[startX][startY] = orbs[tx][ty];
            orbs[tx][ty] = tmp;
            startX = tx;
            startY = ty;
        }
    }
    
    public void randomize(int colors) {
        Board a = new Board();
        int orbs[] = new int[global.cols * global.rows];
        int cnt;
        do {
            a.findAndRemoveMatch();
            cnt = 0;
            for (int i = 0; i < global.cols; i ++) {
                for (int j = 0; j < global.rows; j ++) {
                    if (a.orbs[i][j] == 0) {
                        cnt ++;
                        a.orbs[i][j] = (int) (Math.random() * colors + 1);
                    }
                }
            }
        } while (cnt != 0);
        
        for (int i = 0; i < global.cols; i ++) {
            for (int j = 0; j < global.rows; j ++) {
                orbs[i+j*global.cols] = a.orbs[i][j];
            }
        }
        setOrbs(orbs);
    }
    
    public String print() {
    	String ans = "";
        for (int k = 0; k < 2; ++k){
            for (int j = 0; j < global.rows; ++j)
                for (int i = 0; i < global.cols; ++i)
                    ans += global.orbName[k][orbs[i][j]];
            ans += "\n";
        }
        return ans;
    }
}

