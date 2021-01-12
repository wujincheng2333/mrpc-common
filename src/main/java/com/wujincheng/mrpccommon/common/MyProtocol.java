package com.wujincheng.mrpccommon.common;

public class MyProtocol {

    public static final int BASE_LENGTH = 2 + 4;
    //public static final int HEAD_DATA = 0x88;
    //原码：就是二进制码，最高位为符号位，0表示正数，1表示负数，剩余部分表示真值。
    //反码：在原码的基础上，正数反码就是他本身，负数除符号位之外全部按位取反。
    //补码：正数的补码就是自己本身， 负数的补码是在自身反码的基础上加1
    //java中变量都是以补码的形式保存的
    // 0x90 ---> 00000000 00000000 00000000 10010000 ---> 1001 0000
    //最高位为1，代表负数，取反 ----> 1110 1111 ---> 补码 ---> 1111 0000 ---> -(64+32+16) = -112
    public static final byte HIGH_HEAD_DATA = (byte)0x90;
    public static final byte LOW_HEAD_DATA = (byte)0x90;

    public static final byte[] HIGH_LOW_HEAD_DATA={MyProtocol.HIGH_HEAD_DATA,MyProtocol.LOW_HEAD_DATA};

    public static void main(String[] args) {
        //byte 转 int
        System.out.println(HIGH_HEAD_DATA);
    }
}
