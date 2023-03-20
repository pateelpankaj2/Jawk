package com.mpay.initialization;

import com.mpay.util.Constants;

public class PrivilegeIDs {
	public static final int SUPER_ADMIN = 1;
	public static final int MERCHANT_ADMIN = 2;
	public static final int MEMBER = 3;
	public static final int USER = 4;


	public static String toString(int privilege) {
		switch (privilege) {
		case SUPER_ADMIN:
			return Constants.SUPER_ADMIN;
		case MERCHANT_ADMIN:
			return Constants.MERCHANT_ADMIN;
		case MEMBER:
			return Constants.MEMBER;
		case USER:
			return Constants.USER;
		default:
			return String.valueOf(privilege);
		}
	}
}
