package com.example.padsolver;


public class Deque {
	int maxLen = 64;
	int data[] = new int[maxLen];
	int head = 0, tail = 0, size = 0;
	
	public void push_back(int x) {
		data[tail++] = x;
		if (tail == maxLen) tail = 0;
		size++;
	}
	
	public void push_front(int x) {
		head --;
		if (head == -1) head = maxLen - 1;
		data[head] = x;
		size++;
	}
	
	public void pop_back() {
		tail --;
		if (tail == -1) tail = maxLen - 1;
		size --;
	}
	
	public void pop_front() {
		head ++;
		if (head == maxLen) head = 0;
		size --;
	}
	public int size() {
		return size;
	}
	
	public int get(int i) {
		i += head;
		if (i >= maxLen) i -= maxLen;
		return data[i];
	}
	
	public void set(int i, int x) {
		i += head;
		if (i >= maxLen) i -= maxLen;
		data[i] = x;
	}
	
	public int back() {
		return data[tail-1!=-1?tail-1:maxLen-1];
	}
	
	public int front() {
		return data[head];
	}
	
	public void resize(int len) {
		while (size > len) {
			pop_back();
		}
		while (size < len) {
			push_back(0);
		}
	}
	
//	public String exportToString() {
//		String ans = "";
//		for (int i = 0; i < size; i ++) {
//			ans += (char)(get(i));
//		}
//		return ans;
//	}
//	
//	public void importFromString(String str) {
//		head = 0;
//		tail = size = str.length();
//		for (int i = 0; i < size; i ++) {
//			data[i] = str.charAt(i);
//		}
//	}
}
